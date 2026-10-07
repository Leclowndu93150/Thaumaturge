package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;

public final class BlockEntityArcaneEar extends BlockEntity {
    private static final int NOTE_COUNT = 25;
    private static final int PULSE_TICKS = 10;
    private static final NoteBlockInstrument[] INSTRUMENTS = NoteBlockInstrument.values();

    private byte note;
    private byte instrument;
    private int redstoneSignal;

    public BlockEntityArcaneEar(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ARCANE_EAR.get(), pos, state);
    }

    public int note() {
        return note;
    }

    public NoteBlockInstrument instrument() {
        return INSTRUMENTS[Math.floorMod(instrument, INSTRUMENTS.length)];
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide()) {
            level.getData(TTAttachments.EAR_INDEX.get()).add(getBlockPos().immutable());
        }
    }

    @Override
    public void setRemoved() {
        if (level != null && !level.isClientSide()) {
            level.getData(TTAttachments.EAR_INDEX.get()).remove(getBlockPos());
        }
        super.setRemoved();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityArcaneEar ear) {
        if (ear.redstoneSignal <= 0) {
            return;
        }
        ear.redstoneSignal--;
        if (ear.redstoneSignal == 0 && state.getValue(BlockStateProperties.ENABLED)) {
            level.setBlock(pos, state.setValue(BlockStateProperties.ENABLED, false), Block.UPDATE_ALL);
            notifyPower(level, pos, state);
        }
    }

    public void updateTone() {
        if (level == null) {
            return;
        }
        Direction facing = getBlockState().getValue(BlockStateProperties.FACING);
        BlockState support = level.getBlockState(getBlockPos().relative(facing.getOpposite()));
        this.instrument = (byte) support.instrument().ordinal();
        setChanged();
    }

    public void changePitch() {
        note = (byte) ((note + 1) % NOTE_COUNT);
        setChanged();
    }

    public void playNote() {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        BlockPos pos = getBlockPos();
        float pitch = (float) Math.pow(2.0, (note - 12) / 12.0);
        server.playSound(null, pos, instrument().getSoundEvent().value(), SoundSource.BLOCKS, 3.0F, pitch);
        server.sendParticles(
                ParticleTypes.NOTE,
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5,
                0,
                note / 24.0,
                0.0,
                0.0,
                1.0);
    }

    public boolean matches(NoteBlockInstrument playedInstrument, int playedNote) {
        return instrument() == playedInstrument && note == playedNote;
    }

    public void trigger() {
        if (level == null || level.isClientSide()) {
            return;
        }
        playNote();
        BlockState state = getBlockState();
        if (getBlockState().getBlock() instanceof BlockArcaneEar ear && ear.isToggle()) {
            level.setBlock(
                    getBlockPos(),
                    state.setValue(BlockStateProperties.ENABLED, !state.getValue(BlockStateProperties.ENABLED)),
                    Block.UPDATE_ALL);
        } else {
            redstoneSignal = PULSE_TICKS;
            level.setBlock(getBlockPos(), state.setValue(BlockStateProperties.ENABLED, true), Block.UPDATE_ALL);
        }
        notifyPower(level, getBlockPos(), getBlockState());
    }

    private static void notifyPower(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.FACING).getOpposite();
        level.updateNeighborsAt(pos, state.getBlock());
        level.updateNeighborsAt(pos.relative(facing), state.getBlock());
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        note = input.getByte("note");
        instrument = input.getByte("tone");
        if (note < 0 || note > 24) {
            note = 0;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putByte("note", note);
        output.putByte("tone", instrument);
    }
}
