package com.leclowndu93150.thaumaturge.content.workbench;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
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
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public class BlockEntityArcaneWorkbench extends BlockEntity implements MenuProvider {
    private static final int CHARGER_RADIUS = 1;

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
            int total = 0;
            for (BlockPos anchor : auraAnchors()) {
                total += (int) AuraHelper.getVis(level, anchor);
            }
            auraVis = total;
        }
    }

    public boolean spendAura(int vis, TransactionContext transaction) {
        if (vis <= 0) {
            return true;
        }
        if (level == null || level.isClientSide()) {
            return false;
        }
        List<BlockPos> anchors = auraAnchors();
        float remaining = vis;
        int share = Math.max(1, vis / anchors.size());
        while (remaining > 0.0F) {
            float drainedThisPass = 0.0F;
            for (BlockPos anchor : anchors) {
                float drained = AuraHelper.drainVis(level, anchor, Math.min(share, remaining), transaction);
                drainedThisPass += drained;
                remaining -= drained;
                if (remaining <= 0.0F) {
                    return true;
                }
            }
            if (drainedThisPass <= 0.0F) {
                return false;
            }
        }
        return true;
    }

    public UUID hostIdentity() {
        String key = level.dimension().identifier() + ":" + getBlockPos().asLong();
        return UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
    }

    private List<BlockPos> auraAnchors() {
        if (!(level.getBlockState(getBlockPos().above()).getBlock() instanceof BlockArcaneWorkbenchCharger)) {
            return List.of(getBlockPos());
        }
        ChunkPos center = ChunkPos.containing(getBlockPos());
        List<BlockPos> anchors = new ArrayList<>();
        for (int x = -CHARGER_RADIUS; x <= CHARGER_RADIUS; x++) {
            for (int z = -CHARGER_RADIUS; z <= CHARGER_RADIUS; z++) {
                anchors.add(new ChunkPos(center.x() + x, center.z() + z).getMiddleBlockPosition(getBlockPos().getY()));
            }
        }
        return anchors;
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
