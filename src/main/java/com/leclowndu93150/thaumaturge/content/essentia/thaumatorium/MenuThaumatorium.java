package com.leclowndu93150.thaumaturge.content.essentia.thaumatorium;

import com.leclowndu93150.thaumaturge.content.menu.AbstractTTMenu;
import com.leclowndu93150.thaumaturge.content.menu.BlockMenu;
import com.leclowndu93150.thaumaturge.network.ClientboundThaumatoriumRecipesPayload;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class MenuThaumatorium extends AbstractTTMenu implements BlockMenu<BlockEntityThaumatorium> {
    private static final int MACHINE_SLOTS = 1;
    private static final double REACH_BUFFER = 4.0;

    public static final int CATALYST_X = 56;
    public static final int CATALYST_Y = 24;
    private static final int PLAYER_GRID_Y = 135;
    private static final int HOTBAR_Y = 193;

    public final @Nullable BlockEntityThaumatorium blockEntity;
    private final Player player;
    public List<ClientboundThaumatoriumRecipesPayload.Entry> clientRecipes = List.of();
    private ItemStack lastCatalyst = ItemStack.EMPTY;
    private int lastQueueSize = -1;

    public MenuThaumatorium(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, clientBlockEntity(playerInventory, buf));
    }

    @Override
    public @Nullable BlockEntityThaumatorium blockEntity() {
        return blockEntity;
    }

    private static @Nullable BlockEntityThaumatorium clientBlockEntity(
            Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        return playerInventory.player.level().getBlockEntity(buf.readBlockPos())
                        instanceof BlockEntityThaumatorium machine
                ? machine
                : null;
    }

    public MenuThaumatorium(int containerId, Inventory playerInventory, @Nullable BlockEntityThaumatorium blockEntity) {
        super(TTMenus.THAUMATORIUM.get(), containerId);
        this.blockEntity = blockEntity;
        this.player = playerInventory.player;
        ItemStackHandler items = blockEntity != null ? blockEntity.catalyst() : new ItemStackHandler(1);

        addSlot(new SlotItemHandler(items, 0, CATALYST_X, CATALYST_Y));

        addInventoryExtendedSlots(playerInventory, 8, PLAYER_GRID_Y);
        addInventoryHotbarSlots(playerInventory, 8, HOTBAR_Y);
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        if (blockEntity == null
                || !(player instanceof ServerPlayer serverPlayer)
                || !(serverPlayer.level() instanceof ServerLevel server)) {
            return;
        }
        ItemStack catalyst = blockEntity.catalystStack();
        if (ItemStack.matches(catalyst, lastCatalyst) && blockEntity.queue().size() == lastQueueSize) {
            return;
        }
        lastCatalyst = catalyst.copy();
        lastQueueSize = blockEntity.queue().size();
        List<ResourceLocation> ids = new ArrayList<>();
        var recipes = blockEntity.candidateRecipes(server, serverPlayer, ids);
        List<ClientboundThaumatoriumRecipesPayload.Entry> entries = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++) {
            entries.add(new ClientboundThaumatoriumRecipesPayload.Entry(
                    ids.get(i),
                    recipes.get(i).rawResult().copy(),
                    blockEntity.queue().contains(ids.get(i)),
                    recipes.get(i).aspects()));
        }
        PacketDistributor.sendToPlayer(serverPlayer, new ClientboundThaumatoriumRecipesPayload(containerId, entries));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return quickMoveBetween(index, MACHINE_SLOTS, stack -> true);
    }

    @Override
    public boolean stillValid(Player player) {
        return blockEntity != null
                && !blockEntity.isRemoved()
                && player.canInteractWithBlock(blockEntity.getBlockPos(), REACH_BUFFER);
    }
}
