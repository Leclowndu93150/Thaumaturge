package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneRecipe;
import com.leclowndu93150.thaumaturge.content.misc.TCActionBar;
import com.leclowndu93150.thaumaturge.content.recipe.ThaumaturgeCraftingManager;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCMenus;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.jspecify.annotations.Nullable;

public final class MenuArcaneWorkbench extends AbstractContainerMenu {
    private static final int RESULT_SLOT = 0;
    private static final int CRYSTAL_START = 10;
    private static final int WAND_SLOT = 16;
    private static final int PLAYER_INV_START = 17;
    private static final int PLAYER_INV_END = 44;
    private static final int HOTBAR_START = 44;
    private static final int HOTBAR_END = 53;

    private static final int RESULT_X = 160;
    private static final int RESULT_Y = 64;
    private static final int CRAFT_ORIGIN_X = 41;
    private static final int CRAFT_ORIGIN_Y = 41;
    private static final int CRAFT_SPACING = 23;
    public static final int[] CRYSTAL_X = {64, 16, 112, 16, 112, 64};
    public static final int[] CRYSTAL_Y = {13, 35, 35, 94, 94, 116};
    public static final int WAND_X = 160;
    public static final int WAND_Y = 100;
    private static final int PLAYER_INV_X = 16;
    private static final int PLAYER_INV_Y = 151;
    private static final int HOTBAR_Y = 209;
    private static final int SLOT_SPACING = 18;

    public static final List<ResourceKey<IAspect>> PRIMAL_ORDER = List.of(TCAspects.AER, TCAspects.IGNIS, TCAspects.AQUA, TCAspects.TERRA, TCAspects.ORDO, TCAspects.PERDITIO);

    private static final int AURA_DATA_INDEX = 0;
    private static final int AURA_REFRESH_INTERVAL = 10;

    private final InventoryArcaneWorkbench craftingInventory;
    private final ResultContainer resultContainer = new ResultContainer();
    private final SimpleContainerData containerData = new SimpleContainerData(1);
    private final ContainerLevelAccess access;
    private final Player player;
    private final @Nullable BlockEntityArcaneWorkbench tile;
    private final Runnable onChange;
    private long lastAuraRefresh = Long.MIN_VALUE;
    private int lastVis = -1;

    public MenuArcaneWorkbench(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, new InventoryArcaneWorkbench(), ContainerLevelAccess.create(playerInventory.player.level(), buf.readBlockPos()), null);
    }

    public MenuArcaneWorkbench(int containerId, Inventory playerInventory, BlockEntityArcaneWorkbench tile) {
        this(containerId, playerInventory, tile.getInventory(), ContainerLevelAccess.create(tile.getLevel(), tile.getBlockPos()), tile);
    }

    private MenuArcaneWorkbench(int containerId, Inventory playerInventory, InventoryArcaneWorkbench craftingInventory, ContainerLevelAccess access, @Nullable BlockEntityArcaneWorkbench tile) {
        super(TCMenus.ARCANE_WORKBENCH.get(), containerId);
        this.craftingInventory = craftingInventory;
        this.access = access;
        this.player = playerInventory.player;
        this.tile = tile;
        this.onChange = () -> slotsChanged(craftingInventory);

        craftingInventory.addChangedListener(onChange);
        addSlots(playerInventory);
        addDataSlots(containerData);
        slotsChanged(craftingInventory);
    }

    private void addSlots(Inventory playerInventory) {
        addSlot(new SlotArcaneResult(resultContainer, craftingInventory, tile, RESULT_X, RESULT_Y));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(craftingInventory, col + row * 3, CRAFT_ORIGIN_X + col * CRAFT_SPACING, CRAFT_ORIGIN_Y + row * CRAFT_SPACING));
            }
        }

        for (int i = 0; i < PRIMAL_ORDER.size(); i++) {
            addSlot(new SlotCrystalEssentia(craftingInventory, 9 + i, CRYSTAL_X[i], CRYSTAL_Y[i], PRIMAL_ORDER.get(i)));
        }

        addSlot(new SlotWorkbenchWand(craftingInventory, InventoryArcaneWorkbench.WAND_SLOT, WAND_X, WAND_Y));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, PLAYER_INV_X + col * SLOT_SPACING, PLAYER_INV_Y + row * SLOT_SPACING));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * SLOT_SPACING, HOTBAR_Y));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        if (container != craftingInventory)
            return;
        if (tile == null)
            return;
        if (!(tile.getLevel() instanceof ServerLevel serverLevel))
            return;
        updateResult((ServerPlayer) player, serverLevel);
    }

    private void updateResult(ServerPlayer sp, ServerLevel level) {
        ItemStack result = ItemStack.EMPTY;
        ArcaneCraftingInput input = craftingInventory.asArcaneCraftInput();
        RecipeHolder<?> recipeToStore = null;

        IArcaneRecipe arcane = ThaumaturgeCraftingManager.findMatchingArcaneRecipe(level, input, sp);
        if (arcane != null) {
            tile.refreshAura();
            WorkbenchPayment.Plan plan = WorkbenchPayment.plan(arcane, craftingInventory, sp);
            if (WorkbenchPayment.canCraft(plan, tile)) {
                result = arcane.assemble(input);
            }
        }

        CraftingInput vanillaInput = craftingInventory.asCraftInput();
        if (result.isEmpty()) {
            Optional<RecipeHolder<CraftingRecipe>> vanilla = findVanillaRecipe(level, vanillaInput);
            if (vanilla.isPresent()) {
                result = vanilla.get().value().assemble(vanillaInput);
                recipeToStore = vanilla.get();
            }
        }

        resultContainer.setRecipeUsed(recipeToStore);
        resultContainer.setItem(0, result);
    }

    @SuppressWarnings("unchecked")
    private Optional<RecipeHolder<CraftingRecipe>> findVanillaRecipe(ServerLevel level, CraftingInput input) {
        return level.recipeAccess().getRecipes().stream().filter(r -> r.value() instanceof CraftingRecipe && !(r.value() instanceof IArcaneRecipe)).map(r -> (RecipeHolder<CraftingRecipe>) r)
                .filter(r -> r.value().matches(input, level)).findFirst();
    }

    @Override
    public void broadcastChanges() {
        if (tile != null && tile.getLevel() != null) {
            long gameTime = tile.getLevel().getGameTime();
            if (gameTime >= lastAuraRefresh + AURA_REFRESH_INTERVAL) {
                lastAuraRefresh = gameTime;
                tile.refreshAura();
            }
            if (lastVis != tile.auraVis) {
                slotsChanged(craftingInventory);
                containerData.set(AURA_DATA_INDEX, tile.auraVis);
                lastVis = tile.auraVis;
            }
        }
        super.broadcastChanges();
    }

    public int getCachedVis() {
        return containerData.get(AURA_DATA_INDEX);
    }

    @Override
    public boolean stillValid(Player player) {
        return access.evaluate((level, pos) -> level.getBlockState(pos).is(TCBlocks.ARCANE_WORKBENCH.get()) && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0,
                true);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        craftingInventory.removeChangedListener(onChange);
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.container != resultContainer && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public void clicked(int slotId, int button, ContainerInput containerInput, Player player) {
        if (slotId == WAND_SLOT && getCarried().getItem() instanceof ItemWand wand && wand.isStaff(getCarried())) {
            TCActionBar.sendPurple(player, "tc.workbench.staff");
        }
        super.clicked(slotId, button, containerInput, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem())
            return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (slotIndex == RESULT_SLOT) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else if (slotIndex >= PLAYER_INV_START && slotIndex < HOTBAR_END) {
            if (SlotWorkbenchWand.isUsableWand(stack) && !this.slots.get(WAND_SLOT).hasItem() && !this.moveItemStackTo(stack, WAND_SLOT, WAND_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
            for (int i = 0; i < PRIMAL_ORDER.size(); i++) {
                if (SlotCrystalEssentia.isValidCrystal(stack, PRIMAL_ORDER.get(i))) {
                    if (!this.moveItemStackTo(stack, CRYSTAL_START + i, CRYSTAL_START + i + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                    if (stack.isEmpty())
                        break;
                }
            }
            if (!stack.isEmpty()) {
                if (slotIndex < PLAYER_INV_END) {
                    if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                        return ItemStack.EMPTY;
                    }
                } else {
                    if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                        return ItemStack.EMPTY;
                    }
                }
            }
        } else {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == copy.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);
        return copy;
    }

    public InventoryArcaneWorkbench getCraftingInventory() {
        return craftingInventory;
    }
}
