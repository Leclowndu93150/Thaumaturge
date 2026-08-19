package com.leclowndu93150.thaumaturge.content.infusion;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.content.device.BlockInlay;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class BlockEntityPedestal extends BlockEntity {
    private ItemStack item = ItemStack.EMPTY;

    public BlockEntityPedestal(BlockPos pos, BlockState state) {
        super(TCBlockEntities.PEDESTAL.get(), pos, state);
    }

    protected BlockEntityPedestal(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public ItemStack getItem() {
        return item;
    }

    public void setItem(ItemStack stack) {
        this.item = stack;
        setChanged();
        syncToClient();
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!item.isEmpty()) {
            output.store("Item", ItemStack.CODEC, item);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        item = input.read("Item", ItemStack.CODEC).orElse(ItemStack.EMPTY);
    }

    protected final void syncToClient() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState current = getBlockState();
        level.sendBlockUpdated(getBlockPos(), current, current, 3);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public @Nullable BlockPos findInstabilityMitigator() {
        if (level == null) {
            return null;
        }
        int charge = getBlockState().hasProperty(BlockPedestal.CHARGE) ? getBlockState().getValue(BlockPedestal.CHARGE) : 0;
        if (charge <= 0) {
            return null;
        }
        return seekSourceRecursive(getBlockPos(), charge, 0);
    }

    private @Nullable BlockPos seekSourceRecursive(BlockPos pos, int lastCharge, int depth) {
        if (level == null || depth > 32) {
            return null;
        }
        for (Direction face : Direction.Plane.HORIZONTAL) {
            BlockPos next = pos.relative(face);
            int source = BlockInlay.sourceStrengthAt(level, next);
            if (source >= BlockInlay.MITIGATOR_MIN_ENERGY) {
                return next;
            }
            int charge = BlockInlay.chargeAt(level, next);
            if (charge > lastCharge) {
                BlockPos found = seekSourceRecursive(next, charge, depth + 1);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }
}
