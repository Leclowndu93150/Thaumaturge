package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.essentia.cadence.CadencePhase;
import com.leclowndu93150.thaumaturge.content.essentia.cadence.TickCadence;
import com.leclowndu93150.thaumaturge.content.essentia.tube.facing.SideRanking;
import com.leclowndu93150.thaumaturge.content.essentia.tube.facing.SideRotation;
import com.leclowndu93150.thaumaturge.content.essentia.tube.valve.ValveSpinAnimator;
import com.leclowndu93150.thaumaturge.content.essentia.tube.valve.ValveState;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityTubeValve extends BlockEntityTube {
    private static final String ALLOW_FLOW_KEY = "AllowFlow";
    private static final String HAD_POWER_KEY = "HadPower";
    private static final CadencePhase POWER_SAMPLE = CadencePhase.every(5);
    private static final int ACCEPTABLE_HEAD_SIDE = 0;
    private static final float SQUEAK_VOLUME = 0.7F;
    private static final float SQUEAK_PITCH_BASE = 0.9F;
    private static final float SQUEAK_PITCH_SPREAD = 0.2F;

    private final TickCadence sampler;
    private final ValveSpinAnimator spin = new ValveSpinAnimator();
    private ValveState valve = ValveState.OPEN;

    public BlockEntityTubeValve(BlockPos pos, BlockState state) {
        super(TTBlockEntities.TUBE_VALVE.get(), pos, state);
        this.sampler = TickCadence.staggered(pos);
    }

    public boolean allowFlow() {
        return valve.allowsFlow();
    }

    public float rotation() {
        return spin.angle();
    }

    public float rotation(float partialTick) {
        return spin.angle(partialTick);
    }

    public void setAllowFlow(boolean allow) {
        transitionTo(valve.withFlow(allow), false);
    }

    @Override
    public void tickServer(Level level, BlockPos pos, BlockState state) {
        sampler.advance();
        if (sampler.isDue(POWER_SAMPLE)) {
            boolean powered = level.hasNeighborSignal(pos);
            if (valve.isEdge(powered)) {
                transitionTo(valve.afterSample(powered), true);
            }
        }
        super.tickServer(level, pos, state);
    }

    private void transitionTo(ValveState next, boolean audible) {
        valve = next;
        if (!next.allowsFlow()) {
            clearSuction();
        }
        if (level != null && audible) {
            level.playSound(null, worldPosition, TTSounds.SQUEEK.get(), SoundSource.BLOCKS, SQUEAK_VOLUME, SQUEAK_PITCH_BASE + level.getRandom().nextFloat() * SQUEAK_PITCH_SPREAD);
        }
        setChangedAndSync();
    }

    @Override
    public void tickClient(Level level, BlockPos pos, BlockState state) {
        spin.aim(allowFlow());
        spin.tick();
        super.tickClient(level, pos, state);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return super.isConnectable(face) && face != flowSide();
    }
    @Override
    public boolean rotateFacing() {
        Direction target = SideRotation.next(flowSide(), this::rankHeadCandidate);
        if (target != null) {
            assignFlowSide(target);
            pushUpdate(this);
            refreshAroundIfLoaded();
        }
        return target != null;
    }

    private void refreshAroundIfLoaded() {
        if (level != null) {
            BlockEssentiaTransport.refreshConnectionsAround(level, worldPosition);
        }
    }

    private int rankHeadCandidate(Direction side) {
        int rank = ACCEPTABLE_HEAD_SIDE;
        if (hasTransportNeighbour(side)) {
            rank = SideRanking.UNACCEPTABLE;
        }
        return rank;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return allowFlow() && super.canInputFrom(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return allowFlow() && super.canOutputTo(face);
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!allowFlow()) {
            return 0;
        }
        return super.addEssentia(aspect, amount, face);
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!allowFlow()) {
            return 0;
        }
        return super.takeEssentia(aspect, amount, face);
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> type, int strength) {
        boolean flowing = allowFlow();
        if (flowing) {
            super.setSuction(type, strength);
        }
        if (!flowing && strength < 1) {
            clearSuction();
        }
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction face) {
        return allowFlow() ? super.getSuctionType(face) : null;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        if (!allowFlow()) {
            return 0;
        }
        return super.getSuctionAmount(face);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean(ALLOW_FLOW_KEY, valve.allowsFlow());
        output.putBoolean(HAD_POWER_KEY, valve.poweredAtLastSample());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        valve = ValveState.of(input.getBooleanOr(ALLOW_FLOW_KEY, true), input.getBooleanOr(HAD_POWER_KEY, false));
    }
}
