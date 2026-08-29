package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingStore;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.taint.item.ItemEssentiaCrystal;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

public final class SlotArcaneResult extends Slot {
    private final InventoryArcaneWorkbench craftMatrix;
    private final @Nullable BlockEntityArcaneWorkbench tile;
    private int amountCrafted;
    private boolean committedArcaneCraft;

    public SlotArcaneResult(
            ResultContainer result,
            InventoryArcaneWorkbench craftMatrix,
            @Nullable BlockEntityArcaneWorkbench tile,
            int x,
            int y) {
        super(result, 0, x, y);
        this.craftMatrix = craftMatrix;
        this.tile = tile;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        if (committedArcaneCraft) return true;
        if (!(player instanceof ServerPlayer serverPlayer) || tile == null) return super.mayPickup(player);
        ResultContainer resultContainer = (ResultContainer) container;
        if (resultContainer.getRecipeUsed() != null) return super.mayPickup(player);

        ArcaneCraftingInput.Positioned positioned = craftMatrix.asPositionedArcaneCraftInput();
        ArcaneCraftingInput input = positioned.input().withPlayer(player);
        ArcaneCraftingTransaction.Result result = ArcaneCraftingTransaction.commit(
                context(serverPlayer), serverPlayer, input, new NativeStore(player, positioned));
        if (!result.successful()) return false;
        committedArcaneCraft = true;
        resultContainer.setItem(0, result.output());
        return true;
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        amountCrafted += amount;
    }

    @Override
    protected void onSwapCraft(int numItemsCrafted) {
        amountCrafted += numItemsCrafted;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        amountCrafted = 0;

        if (committedArcaneCraft) {
            committedArcaneCraft = false;
            craftMatrix.setChanged();
            return;
        }

        if (!(player.level() instanceof ServerLevel)) return;

        List<ItemStack> remaining;
        ResultContainer result = (ResultContainer) this.container;
        RecipeHolder<?> stored = result.getRecipeUsed();
        if (stored != null && stored.value() instanceof CraftingRecipe cr) {
            remaining = cr.getRemainingItems(craftMatrix.asCraftInput());
        } else {
            return;
        }

        for (int i = 0; i < InventoryArcaneWorkbench.CRAFTING_SLOTS; i++) {
            ItemStack existing = craftMatrix.getItem(i);
            ItemStack leftover = i < remaining.size() ? remaining.get(i) : ItemStack.EMPTY;
            if (!existing.isEmpty()) {
                craftMatrix.removeItem(i, 1);
                existing = craftMatrix.getItem(i);
            }
            if (!leftover.isEmpty()) {
                if (existing.isEmpty()) {
                    craftMatrix.setItem(i, leftover);
                } else if (ItemStack.isSameItemSameComponents(existing, leftover)) {
                    leftover.grow(existing.getCount());
                    craftMatrix.setItem(i, leftover);
                } else if (!player.getInventory().add(leftover)) {
                    player.drop(leftover, false);
                }
            }
        }
    }

    private ArcaneWorkbenchContext context(ServerPlayer player) {
        String hostKey = player.serverLevel().dimension().location() + ":"
                + tile.getBlockPos().asLong();
        return ArcaneWorkbenchContext.placed(
                player, tile.getBlockPos(), UUID.nameUUIDFromBytes(hostKey.getBytes(StandardCharsets.UTF_8)), null);
    }

    private final class NativeStore implements IArcaneCraftingStore {
        private final Player player;
        private final ArcaneCraftingInput.Positioned positioned;

        private NativeStore(Player player, ArcaneCraftingInput.Positioned positioned) {
            this.player = player;
            this.positioned = positioned;
        }

        @Override
        public Reservation reserve(List<ItemStack> snapshot) {
            return matches(snapshot) ? new NativeReservation(snapshot) : null;
        }

        private boolean matches(List<ItemStack> snapshot) {
            ArcaneCraftingInput input = positioned.input();
            if (snapshot.size() != input.width() * input.height()) return false;
            for (int y = 0; y < input.height(); y++) {
                for (int x = 0; x < input.width(); x++) {
                    ItemStack actual = craftMatrix.getItem(x + positioned.left() + (y + positioned.top()) * 3);
                    if (!ItemStack.matches(actual, snapshot.get(x + y * input.width()))) return false;
                }
            }
            return true;
        }

        private final class NativeReservation implements Reservation {
            private final List<ItemStack> snapshot;
            private boolean finished;

            private NativeReservation(List<ItemStack> snapshot) {
                this.snapshot = snapshot;
            }

            @Override
            public boolean isValid() {
                return !finished && matches(snapshot);
            }

            @Override
            public void commit(ItemStack output, List<ItemStack> remainders, AspectList crystals) {
                if (!isValid()) throw new IllegalStateException("Arcane workbench reservation changed");
                ArcaneCraftingInput input = positioned.input();
                for (int y = 0; y < input.height(); y++) {
                    for (int x = 0; x < input.width(); x++) {
                        int compact = x + y * input.width();
                        int slot = x + positioned.left() + (y + positioned.top()) * 3;
                        craftMatrix.removeItem(slot, 1);
                        ItemStack remainder = compact < remainders.size()
                                ? remainders.get(compact).copy()
                                : ItemStack.EMPTY;
                        placeRemainder(slot, remainder);
                    }
                }
                consumeCrystals(crystals);
                finished = true;
            }

            private void placeRemainder(int slot, ItemStack remainder) {
                if (remainder.isEmpty()) return;
                ItemStack existing = craftMatrix.getItem(slot);
                if (existing.isEmpty()) craftMatrix.setItem(slot, remainder);
                else if (!player.getInventory().add(remainder)) player.drop(remainder, false);
            }

            private void consumeCrystals(AspectList crystals) {
                for (AspectInstance entry : crystals.entries()) {
                    int needed = entry.amount();
                    for (int slot = InventoryArcaneWorkbench.CRAFTING_SLOTS;
                            slot < InventoryArcaneWorkbench.WAND_SLOT && needed > 0;
                            slot++) {
                        ItemStack crystal = craftMatrix.getItem(slot);
                        Holder<IAspect> aspect = ItemEssentiaCrystal.aspectOf(crystal);
                        if (aspect != null
                                && aspect.is(entry.aspect().unwrapKey().orElseThrow())) {
                            int removed = Math.min(needed, crystal.getCount());
                            craftMatrix.removeItem(slot, removed);
                            needed -= removed;
                        }
                    }
                }
            }

            @Override
            public void close() {
                finished = true;
            }
        }
    }
}
