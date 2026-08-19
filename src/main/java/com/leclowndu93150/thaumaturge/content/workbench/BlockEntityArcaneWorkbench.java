package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

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
        super(TCBlockEntities.ARCANE_WORKBENCH.get(), worldPosition, blockState);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, inventory.getItems());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        ContainerHelper.loadAllItems(input, inventory.getItems());
        inventory.setChanged();
    }

    public InventoryArcaneWorkbench getInventory() {
        return inventory;
    }

    public void refreshAura() {
        if (level != null && !level.isClientSide()) {
            auraVis = 0;
            if (!(level.getBlockState(getBlockPos().above()).getBlock() instanceof BlockArcaneWorkbenchCharger)) {
                auraVis = (int) AuraHelper.getVis(level, getBlockPos());
            } else {
                ChunkPos chunkPos = ChunkPos.containing(getBlockPos());

                for (int x = -1; x <= 1; x++) {
                    for (int z = -1; z <= 1; z++) {
                        ChunkPos current = new ChunkPos(chunkPos.x() + x, chunkPos.z() + z);
                        auraVis += (int) AuraHelper.getVis(level, current.getMiddleBlockPosition(getBlockPos().getY()));
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
                ChunkPos chunkPos = ChunkPos.containing(getBlockPos());
                int remaining = vis;
                int max = Math.max(1, vis / 9);
                int attempts = 0;

                while (remaining > 0) {
                    attempts++;
                    for (int x = -1; x <= 1; x++) {
                        for (int z = -1; z <= 1; z++) {
                            ChunkPos current = new ChunkPos(chunkPos.x() + x, chunkPos.z() + z);
                            if (max > remaining)
                                max = remaining;
                            remaining = (int) (remaining - AuraHelper.drainVis(level, current.getMiddleBlockPosition(getBlockPos().getY()), max, false));
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
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level == null || level.isClientSide())
            return;
        Containers.dropContents(level, getBlockPos(), getInventory().getItems());
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
