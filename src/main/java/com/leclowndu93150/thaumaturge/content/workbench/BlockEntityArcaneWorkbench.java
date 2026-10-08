package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.recipe.ArcaneWorkbenchContext;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.nio.charset.StandardCharsets;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BlockEntityArcaneWorkbench extends BlockEntity implements MenuProvider {
    public int auraVis = 0;

    private final InventoryArcaneWorkbench inventory = new InventoryArcaneWorkbench() {
        @Override
        public void setChanged() {
            super.setChanged();
            BlockEntityArcaneWorkbench.this.setChanged();
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
                ChunkPos chunkPos = new ChunkPos(getBlockPos());

                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        ChunkPos current = new ChunkPos(chunkPos.x + x, chunkPos.z + z);
                        auraVis += (int) AuraHelper.getVis(
                                level,
                                current.getMiddleBlockPosition(getBlockPos().getY()));
                    }
                }
            }
        }
    }

    public void spendAura(int vis) {
        if (level != null && !level.isClientSide()) {
            if (!(level.getBlockState(getBlockPos().above()).getBlock() instanceof BlockArcaneWorkbenchCharger)) {
                AuraHelper.drainVis(level, getBlockPos(), vis, false);
            } else {
                ChunkPos chunkPos = new ChunkPos(getBlockPos());
                int remaining = vis;
                int max = Math.max(1, vis / 9);
                int attempts = 0;

                while (remaining > 0) {
                    attempts++;
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            ChunkPos current = new ChunkPos(chunkPos.x + x, chunkPos.z + z);
                            if (max > remaining) max = remaining;
                            remaining = (int) (remaining
                                    - AuraHelper.drainVis(
                                            level,
                                            current.getMiddleBlockPosition(
                                                    getBlockPos().getY()),
                                            max,
                                            false));
                            if (remaining <= 0 || attempts > 1000) {
                                return;
                            }
                        }
                    }
                }
            }
        }
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
