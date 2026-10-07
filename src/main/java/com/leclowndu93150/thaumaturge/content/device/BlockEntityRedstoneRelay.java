package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityRedstoneRelay extends BlockEntity {
    private static final int MAX_SIGNAL = 15;

    private int in = 1;
    private int out = 15;

    public BlockEntityRedstoneRelay(BlockPos pos, BlockState state) {
        super(TTBlockEntities.REDSTONE_RELAY.get(), pos, state);
    }

    public int getIn() {
        return in;
    }

    public int getOut() {
        return out;
    }

    public void increaseIn() {
        in++;
        if (in > MAX_SIGNAL) {
            in = 1;
        }
        setChanged();
        sync();
    }

    public void increaseOut() {
        out++;
        if (out > MAX_SIGNAL) {
            out = 1;
        }
        setChanged();
        sync();
    }

    private void sync() {
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, 3);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        in = (input.contains("in") ? input.getByte("in") : (byte) 1);
        out = (input.contains("out") ? input.getByte("out") : (byte) 15);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putByte("in", (byte) in);
        output.putByte("out", (byte) out);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        {
            CompoundTag tag = new CompoundTag();
            saveAdditional(tag, registries);
            nbt.merge(tag);
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
