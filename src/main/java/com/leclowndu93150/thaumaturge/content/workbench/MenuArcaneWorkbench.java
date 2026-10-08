package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneCraftingTransaction;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneCraftingStore;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.content.recipe.workbench.ArcaneCraftingInput;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import java.util.ArrayList;
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
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import org.jspecify.annotations.Nullable;

public final class MenuArcaneWorkbench extends AbstractContainerMenu {
    private static final int RESULT_SLOT = 0;
    private static final int CRAFT_START = 1;
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
    public static final int CRYSTAL_SLOT_START = 9;
    public static final int[] CRYSTAL_X = {64, 16, 112, 16, 112, 64};
    public static final int[] CRYSTAL_Y = {13, 35, 35, 94, 94, 116};
    public static final int WAND_X = 160;
    public static final int WAND_Y = 100;
    private static final int PLAYER_INV_X = 16;
    private static final int PLAYER_INV_Y = 151;
    private static final int HOTBAR_Y = 209;
    private static final int SLOT_SPACING = 18;

    public static final List<ResourceKey<IAspect>> PRIMAL_ORDER = List.of(
            TTAspects.AER, TTAspects.IGNIS, TTAspects.AQUA, TTAspects.TERRA, TTAspects.ORDO, TTAspects.PERDITIO);

    private static final int AURA_DATA_INDEX = 0;
    private static final int AURA_REFRESH_INTERVAL = 10;

    private final InventoryArcaneWorkbench craftingInventory;
    private final ResultContainer resultContainer = new ResultContainer();
    private final SimpleContainerData containerData = new SimpleContainerData(1);
    private final ContainerLevelAccess access;
    private final Player player;
    private final @Nullable BlockEntityArcaneWorkbench tile;
    private final Runnable onChange;
    private final SlotArcaneResult resultSlot;
    private long lastAuraRefresh = Long.MIN_VALUE;
    private int lastVis = -1;
    private boolean displayingArcane;

    public MenuArcaneWorkbench(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(
                containerId,
                playerInventory,
                new InventoryArcaneWorkbench(),
                ContainerLevelAccess.create(playerInventory.player.level(), buf.readBlockPos()),
                null);
    }

    public MenuArcaneWorkbench(int containerId, Inventory playerInventory, BlockEntityArcaneWorkbench tile) {
        this(
                containerId,
                playerInventory,
                tile.getInventory(),
                ContainerLevelAccess.create(tile.getLevel(), tile.getBlockPos()),
                tile);
    }

    private MenuArcaneWorkbench(
            int containerId,
            Inventory playerInventory,
            InventoryArcaneWorkbench craftingInventory,
            ContainerLevelAccess access,
            @Nullable BlockEntityArcaneWorkbench tile) {
        super(TTMenus.ARCANE_WORKBENCH.get(), containerId);
        this.craftingInventory = craftingInventory;
        this.access = access;
        this.player = playerInventory.player;
        this.tile = tile;
        this.onChange = () -> slotsChanged(craftingInventory);
        this.resultSlot = new SlotArcaneResult(resultContainer, this, RESULT_X, RESULT_Y);

        craftingInventory.addChangedListener(onChange);
        addSlots(playerInventory);
        addDataSlots(containerData);
        slotsChanged(craftingInventory);
    }

    private void addSlots(Inventory playerInventory) {
        addSlot(resultSlot);

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                addSlot(new Slot(
                        craftingInventory,
                        col + row * 3,
                        CRAFT_ORIGIN_X + col * CRAFT_SPACING,
                        CRAFT_ORIGIN_Y + row * CRAFT_SPACING));
            }
        }

        for (int i = 0; i < PRIMAL_ORDER.size(); i++) {
            addSlot(new SlotCrystalEssentia(
                    craftingInventory, CRYSTAL_SLOT_START + i, CRYSTAL_X[i], CRYSTAL_Y[i], PRIMAL_ORDER.get(i)));
        }

        addSlot(new SlotWorkbenchWand(craftingInventory, InventoryArcaneWorkbench.WAND_SLOT, WAND_X, WAND_Y));

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(
                        playerInventory,
                        col + row * 9 + 9,
                        PLAYER_INV_X + col * SLOT_SPACING,
                        PLAYER_INV_Y + row * SLOT_SPACING));
            }
        }

        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, PLAYER_INV_X + col * SLOT_SPACING, HOTBAR_Y));
        }
    }

    @Override
    public void slotsChanged(Container container) {
        if (container != craftingInventory) return;
        if (tile == null) return;
        if (!(tile.getLevel() instanceof ServerLevel serverLevel)) return;
        updateResult((ServerPlayer) player, serverLevel);
    }

    private void updateResult(ServerPlayer sp, ServerLevel level) {
        ItemStack result = ItemStack.EMPTY;
        RecipeHolder<?> recipeToStore = null;
        displayingArcane = false;

        ArcaneCraftingTransaction.Result arcane = ArcaneCraftingTransaction.preview(
                context(sp), sp, craftingInventory.asArcaneCraftInput().withPlayer(sp));
        if (arcane.successful()) {
            result = arcane.output();
            displayingArcane = true;
        }

        CraftingInput vanillaInput = craftingInventory.asCraftInput();
        if (result.isEmpty()) {
            Optional<RecipeHolder<CraftingRecipe>> vanilla = findVanillaRecipe(level, vanillaInput);
            if (vanilla.isPresent()) {
                result = vanilla.get().value().assemble(vanillaInput, level.registryAccess());
                recipeToStore = vanilla.get();
            }
        }

        resultContainer.setRecipeUsed(recipeToStore);
        resultContainer.setItem(0, result);
    }

    boolean craftsOnServer() {
        return tile != null && player instanceof ServerPlayer && tile.getLevel() instanceof ServerLevel;
    }

    ItemStack takeCraft() {
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel level = (ServerLevel) tile.getLevel();
        List<ItemStack> remainders = new ArrayList<>();
        ItemStack crafted = craft(serverPlayer, level, remainders);
        remainders.forEach(player.getInventory()::placeItemBackInInventory);
        if (crafted.isEmpty()) {
            updateResult(serverPlayer, level);
        }
        return crafted;
    }

    private ItemStack craft(ServerPlayer serverPlayer, ServerLevel level, List<ItemStack> remainders) {
        ItemStack displayed = resultContainer.getItem(0);
        if (displayed.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ItemStack crafted = displayingArcane
                ? craftArcane(serverPlayer, displayed.copy(), remainders)
                : craftVanilla(serverPlayer, level, displayed.copy(), remainders);
        return crafted;
    }

    private ItemStack craftArcane(ServerPlayer serverPlayer, ItemStack expected, List<ItemStack> remainders) {
        ArcaneCraftingInput.Positioned positioned = craftingInventory.asPositionedArcaneCraftInput();
        ArcaneCraftingInput input = positioned.input().withPlayer(serverPlayer);
        WorkbenchCraftingStore store = new WorkbenchCraftingStore(
                craftingInventory,
                serverPlayer,
                positioned.left(),
                positioned.top(),
                input.width(),
                input.height(),
                remainders::add);
        ArcaneCraftingTransaction.Result result =
                ArcaneCraftingTransactions.craftExpected(context(serverPlayer), serverPlayer, input, store, expected);
        return result.successful() ? result.output() : ItemStack.EMPTY;
    }

    private ItemStack craftVanilla(
            ServerPlayer serverPlayer, ServerLevel level, ItemStack expected, List<ItemStack> remainders) {
        RecipeHolder<?> used = resultContainer.getRecipeUsed();
        if (used == null || !(used.value() instanceof CraftingRecipe recipe)) {
            return ItemStack.EMPTY;
        }
        CraftingInput.Positioned positioned = craftingInventory.asPositionedCraftInput();
        CraftingInput input = positioned.input();
        if (!recipe.matches(input, level)) {
            return ItemStack.EMPTY;
        }
        WorkbenchCraftingStore store = new WorkbenchCraftingStore(
                craftingInventory,
                serverPlayer,
                positioned.left(),
                positioned.top(),
                input.width(),
                input.height(),
                remainders::add);
        IArcaneCraftingStore.Consumption consumption = new IArcaneCraftingStore.Consumption(
                input.items(), recipe.getRemainingItems(input), AspectList.EMPTY, craftingInventory.wandStack());
        ItemStack output = recipe.assemble(input, level.registryAccess());
        if (!ItemStack.matches(output, expected)) return ItemStack.EMPTY;
        if (!store.consume(consumption, false)) {
            return ItemStack.EMPTY;
        }
        ItemStack crafted = output.copy();
        ResearchProgressionEvents.recordCrafted(serverPlayer, crafted);
        return output;
    }

    private ArcaneWorkbenchContext context(ServerPlayer serverPlayer) {
        return tile.craftingContext(serverPlayer);
    }

    private Optional<RecipeHolder<CraftingRecipe>> findVanillaRecipe(ServerLevel level, CraftingInput input) {
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level);
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
        return access.evaluate(
                (level, pos) -> level.getBlockState(pos).is(TTBlocks.ARCANE_WORKBENCH.get())
                        && player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0,
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
    public void clicked(int slotId, int button, ClickType containerInput, Player player) {
        if (slotId == RESULT_SLOT
                && containerInput == ClickType.PICKUP
                && !getCarried().isEmpty()) {
            ItemStack output = resultContainer.getItem(0);
            if (!ItemStack.isSameItemSameComponents(output, getCarried())
                    || output.getCount() + getCarried().getCount()
                            > getCarried().getMaxStackSize()) return;
        }
        if (slotId == WAND_SLOT && getCarried().getItem() instanceof ItemWand wand && wand.isStaff(getCarried())) {
            TTActionBar.sendPurple(player, "tc.workbench.staff");
        }
        if (slotId == RESULT_SLOT && containerInput == ClickType.SWAP && craftsOnServer()) {
            swapCraft(button);
            return;
        }
        super.clicked(slotId, button, containerInput, player);
    }

    private void swapCraft(int button) {
        if (!Inventory.isHotbarSlot(button) && button != Inventory.SLOT_OFFHAND) {
            return;
        }
        Inventory inventory = player.getInventory();
        if (!inventory.getItem(button).isEmpty() || !resultSlot.hasItem()) {
            return;
        }
        ItemStack crafted = takeCraft();
        if (crafted.isEmpty()) {
            return;
        }
        inventory.setItem(button, crafted);
        resultSlot.onSwapCraft(crafted.getCount());
        resultSlot.onTake(player, crafted);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) return ItemStack.EMPTY;

        if (slotIndex == RESULT_SLOT && craftsOnServer()) {
            return quickMoveCraft();
        }

        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();

        if (slotIndex == RESULT_SLOT) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, copy);
        } else if (slotIndex >= PLAYER_INV_START && slotIndex < HOTBAR_END) {
            if (SlotWorkbenchWand.isUsableWand(stack)
                    && !this.slots.get(WAND_SLOT).hasItem()
                    && !this.moveItemStackTo(stack, WAND_SLOT, WAND_SLOT + 1, false)) {
                return ItemStack.EMPTY;
            }
            for (int i = 0; i < PRIMAL_ORDER.size(); i++) {
                if (SlotCrystalEssentia.isValidCrystal(stack, PRIMAL_ORDER.get(i))) {
                    if (!this.moveItemStackTo(stack, CRYSTAL_START + i, CRYSTAL_START + i + 1, false)) {
                        return ItemStack.EMPTY;
                    }
                    if (stack.isEmpty()) break;
                }
            }
            if (!stack.isEmpty() && !this.moveItemStackTo(stack, CRAFT_START, CRYSTAL_START, false)) {
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

    private ItemStack quickMoveCraft() {
        ServerPlayer serverPlayer = (ServerPlayer) player;
        ServerLevel level = (ServerLevel) tile.getLevel();
        ItemStack displayed = resultContainer.getItem(0);
        boolean hasRoom = false;
        for (int i = PLAYER_INV_START; i < HOTBAR_END; i++) {
            Slot slot = slots.get(i);
            ItemStack existing = slot.getItem();
            if (slot.mayPlace(displayed)
                    && (existing.isEmpty()
                            || ItemStack.isSameItemSameComponents(existing, displayed)
                                    && existing.getCount() < slot.getMaxStackSize(displayed))) {
                hasRoom = true;
                break;
            }
        }
        if (!hasRoom) return ItemStack.EMPTY;
        List<ItemStack> remainders = new ArrayList<>();
        ItemStack crafted = craft(serverPlayer, level, remainders);
        if (crafted.isEmpty()) {
            updateResult(serverPlayer, level);
            return ItemStack.EMPTY;
        }
        ItemStack overflow = crafted.copy();
        moveItemStackTo(overflow, PLAYER_INV_START, HOTBAR_END, true);
        resultSlot.onQuickCraft(overflow, crafted);
        if (!overflow.isEmpty()) player.drop(overflow, false);
        remainders.forEach(player.getInventory()::placeItemBackInInventory);
        resultSlot.onTake(player, crafted);
        return crafted;
    }

    public InventoryArcaneWorkbench getCraftingInventory() {
        return craftingInventory;
    }
}
