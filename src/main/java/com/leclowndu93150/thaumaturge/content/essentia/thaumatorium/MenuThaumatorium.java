package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import com.leclowndu93150.thaumaturge.network.ClientboundThaumatoriumRecipesPayload;
import com.leclowndu93150.thaumaturge.registry.TCMenus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import org.jspecify.annotations.Nullable;

public final class MenuThaumatorium extends AbstractContainerMenu {
    public static final int CATALYST_X = 56;
    public static final int CATALYST_Y = 24;
    private static final int PLAYER_GRID_Y = 135;
    private static final int HOTBAR_Y = 193;
    private static final int PLAYER_ROW_SLOTS = 9;
    private static final int PLAYER_ROWS = 3;

    public final @Nullable BlockEntityThaumatorium blockEntity;
    private final Player player;
    public List<ClientboundThaumatoriumRecipesPayload.Entry> clientRecipes = List.of();
    private ItemStack lastCatalyst = ItemStack.EMPTY;
    private int lastQueueSize = -1;

    public MenuThaumatorium(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, clientBlockEntity(playerInventory, buf));
    }

    private static @Nullable BlockEntityThaumatorium clientBlockEntity(Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        return playerInventory.player.level().getBlockEntity(buf.readBlockPos()) instanceof BlockEntityThaumatorium machine ? machine : null;
    }

    public MenuThaumatorium(int containerId, Inventory playerInventory, @Nullable BlockEntityThaumatorium blockEntity) {
        super(TCMenus.THAUMATORIUM.get(), containerId);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;
        ItemStacksResourceHandler items = blockEntity != null ? blockEntity.catalyst() : new ItemStacksResourceHandler(1);

        addSlot(new ResourceHandlerSlot(items, items::set, 0, CATALYST_X, CATALYST_Y));

        for (int row = 0; row < PLAYER_ROWS; row++) {
            for (int col = 0; col < PLAYER_ROW_SLOTS; col++) {
                addSlot(new Slot(playerInventory, col + row * PLAYER_ROW_SLOTS + PLAYER_ROW_SLOTS, 8 + col * 18, PLAYER_GRID_Y + row * 18));
            }
        }
        for (int col = 0; col < PLAYER_ROW_SLOTS; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, HOTBAR_Y));
        }
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (blockEntity == null || !(player instanceof ServerPlayer serverPlayer) || !(serverPlayer.level() instanceof ServerLevel server)) {
            return;
        }
        ItemStack catalyst = blockEntity.catalystStack();
        if (ItemStack.matches(catalyst, lastCatalyst) && blockEntity.queue().size() == lastQueueSize) {
            return;
        }
        lastCatalyst = catalyst.copy();
        lastQueueSize = blockEntity.queue().size();
        List<Identifier> ids = new ArrayList<>();
        var recipes = blockEntity.candidateRecipes(server, serverPlayer, ids);
        List<ClientboundThaumatoriumRecipesPayload.Entry> entries = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++) {
            entries.add(new ClientboundThaumatoriumRecipesPayload.Entry(ids.get(i), recipes.get(i).rawResult().create(), blockEntity.queue().contains(ids.get(i)), recipes.get(i).aspects()));
        }
        PacketDistributor.sendToPlayer(serverPlayer, new ClientboundThaumatoriumRecipesPayload(containerId, entries));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index == 0) {
            if (!moveItemStackTo(stack, 1, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, 1, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity == null || blockEntity.getBlockPos().distToCenterSqr(player.getX(), player.getY(), player.getZ()) <= 64.0;
    }
}
