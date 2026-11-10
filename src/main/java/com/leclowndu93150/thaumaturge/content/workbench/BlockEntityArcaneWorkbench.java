package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.api.transaction.Transaction;
import com.leclowndu93150.thaumaturge.api.transaction.TransactionContext;
import com.leclowndu93150.thaumaturge.content.aura.AuraVisDrain;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityArcaneWorkbench extends AbstractSyncedBlockEntity implements MenuProvider {
    private static final double AURA_PAYMENT_EPSILON = 0.0001D;

    public int auraVis = 0;

    private final InventoryArcaneWorkbench inventory = new InventoryArcaneWorkbench() {
        @Override
        public void setChanged() {
            super.setChanged();
            BlockEntityArcaneWorkbench.this.setChangedAndSync();
        }
    };

    public BlockEntityArcaneWorkbench(BlockPos worldPosition, BlockState blockState) {
        super(TTBlockEntities.ARCANE_WORKBENCH.get(), worldPosition, blockState);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        ContainerHelper.saveAllItems(output, inventory.getItems(), registries);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            inventory.getItems().set(slot, ItemStack.EMPTY);
        }
        ContainerHelper.loadAllItems(input, inventory.getItems(), registries);
        inventory.setChanged();
    }

    public InventoryArcaneWorkbench getInventory() {
        return inventory;
    }

    public ArcaneWorkbenchContext craftingContext(ServerPlayer player) {
        String hostKey = player.serverLevel().dimension().location() + ":" + worldPosition.asLong();
        return ArcaneWorkbenchContext.placed(
                player, worldPosition, UUID.nameUUIDFromBytes(hostKey.getBytes(StandardCharsets.UTF_8)), null);
    }

    public void refreshAura() {
        if (level != null && !level.isClientSide()) {
            auraVis = 0;
            if (!(level.getBlockState(getBlockPos().above()).getBlock() instanceof BlockArcaneWorkbenchCharger)) {
                auraVis = (int) AuraHelper.getVis(level, getBlockPos());
            } else {
                BlockPos home = LevelSpaces.worldBlock(level, getBlockPos());
                ChunkPos chunkPos = new ChunkPos(home);

                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        ChunkPos current = new ChunkPos(chunkPos.x + x, chunkPos.z + z);
                        auraVis += (int) AuraHelper.getVis(level, current.getMiddleBlockPosition(home.getY()));
                    }
                }
            }
        }
    }

    public void spendAura(int vis) {
        if (vis <= 0) {
            return;
        }
        try (Transaction transaction = Transaction.openRoot()) {
            if (spendAura(vis, transaction)) {
                transaction.commit();
            }
        }
    }

    public boolean spendAura(int vis, TransactionContext transaction) {
        if (vis <= 0) {
            return true;
        }
        if (level == null || level.isClientSide()) {
            return false;
        }
        if (!(level.getBlockState(getBlockPos().above()).getBlock() instanceof BlockArcaneWorkbenchCharger)) {
            return AuraHelper.drainVis(level, getBlockPos(), vis, transaction) >= vis;
        }

        BlockPos home = LevelSpaces.worldBlock(level, getBlockPos());
        ChunkPos chunkPos = new ChunkPos(home);
        List<BlockPos> anchors = new ArrayList<>(9);
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                ChunkPos current = new ChunkPos(chunkPos.x + x, chunkPos.z + z);
                BlockPos anchor = current.getMiddleBlockPosition(home.getY());
                if (LoadedChunks.contains(level, anchor)) {
                    anchors.add(anchor);
                }
            }
        }
        return drainAcrossAnchors(
                vis, anchors, (anchor, request) -> AuraHelper.drainVis(level, anchor, request, transaction));
    }

    static boolean drainAcrossAnchors(double owed, List<BlockPos> anchors, AuraDrainer drainer) {
        return AuraVisDrain.acrossAnchors(owed, anchors, drainer::drain) + AURA_PAYMENT_EPSILON >= owed;
    }

    @FunctionalInterface
    interface AuraDrainer {
        float drain(BlockPos anchor, float request);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.thaumaturge.arcane_workbench");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new MenuArcaneWorkbench(containerId, inventory, this);
    }
}
