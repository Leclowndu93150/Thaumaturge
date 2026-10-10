package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.cadence.IntervalTimer;
import com.leclowndu93150.thaumaturge.content.essentia.tube.buffer.BufferSides;
import com.leclowndu93150.thaumaturge.content.essentia.tube.buffer.BufferSuctionPolicy;
import com.leclowndu93150.thaumaturge.content.essentia.tube.buffer.ChokeLevel;
import com.leclowndu93150.thaumaturge.content.essentia.tube.buffer.PullCandidate;
import com.leclowndu93150.thaumaturge.content.essentia.tube.buffer.PullSelector;
import com.leclowndu93150.thaumaturge.content.essentia.tube.buffer.StrongerPullCheck;
import com.leclowndu93150.thaumaturge.content.essentia.tube.buffer.TakeVerdict;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.Codec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

public final class BlockEntityTubeBuffer extends AbstractSyncedBlockEntity implements IEssentiaTransport, IInteractWithCaster {
    public static final int MAX_AMOUNT = 10;

    private static final String CONTENTS_KEY = "Contents";
    private static final String CHOKED_KEY = "Choked";
    private static final String OPEN_KEY = "Open";
    private static final String FACING_KEY = "Facing";
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final int BELLOWS_RECOUNT_TICKS = 20;
    private static final int PULL_TICKS = 5;
    private static final int SINGLE_UNIT = 1;
    private static final int FIRST_SIDE = 0;
    private static final int NO_ARM = -1;
    private static final int NO_SUCTION = 0;
    private static final @Nullable Holder<IAspect> NO_SUCTION_TYPE = null;
    private static final float SQUEAK_VOLUME = 0.5F;
    private static final float SQUEAK_PITCH_BASE = 1.6F;
    private static final float SQUEAK_PITCH_SPREAD = 0.2F;
    private static final float TOOL_VOLUME = 0.5F;
    private static final float TOOL_PITCH_BASE = 0.95F;
    private static final float TOOL_PITCH_SPREAD = 0.15F;

    private final BufferSides sides = new BufferSides();
    private final BufferSuctionPolicy suctionPolicy = new BufferSuctionPolicy();
    private final StrongerPullCheck strongerPull = new StrongerPullCheck(sides, suctionPolicy);
    private final PullSelector pullSelector = new PullSelector(sides, suctionPolicy);
    private final IntervalTimer bellowsTimer = IntervalTimer.firingAfterFullPeriod(BELLOWS_RECOUNT_TICKS);
    private final IntervalTimer pullTimer = IntervalTimer.firingAfterFullPeriod(PULL_TICKS);
    private AspectList contents = AspectList.EMPTY;
    private Direction facing = Direction.NORTH;

    public BlockEntityTubeBuffer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.TUBE_BUFFER.get(), pos, state);
    }

    public AspectList heldAspects() {
        return contents;
    }

    public int chokedSide(Direction side) {
        return sides.choke(side).ordinal();
    }

    public void cycleChokedSide(Direction side) {
        sides.cycleChoke(side);
        setChangedAndSync();
    }

    public boolean[] sideOpenFlags() {
        return sides.openFlags();
    }

    public boolean isSideOpen(Direction side) {
        return sides.isOpen(side);
    }

    public void setOpenSide(Direction side, boolean open) {
        sides.setOpen(side, open);
        setChangedAndSync();
    }

    public boolean toggleOpenSide(Direction side) {
        boolean open = !isSideOpen(side);
        setOpenSide(side, open);
        return open;
    }

    public Direction placedFacing() {
        return facing;
    }

    public void setFacingForPlacement(@Nullable LivingEntity placer) {
        facing = facingFor(placer);
        setChangedAndSync();
    }

    private static Direction facingFor(@Nullable LivingEntity placer) {
        if (placer == null) {
            return Direction.NORTH;
        }
        return Direction.orderedByNearest(placer)[0].getOpposite();
    }

    @Override
    public boolean onCasterRightClick(Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (level.isClientSide()) {
            return true;
        }
        int arm = lookedAtArm(level, player, pos);
        boolean handled = arm != NO_ARM && handleCasterClick(arm, player.isShiftKeyDown());
        if (handled) {
            player.swing(hand);
        }
        return handled;
    }

    public boolean handleCasterClick(int part, boolean sneaking) {
        if (level == null || part < 0 || part >= DIRECTIONS.length) {
            return false;
        }
        Direction side = DIRECTIONS[part];
        if (sneaking) {
            cycleChokedSide(side);
            playClickSound(level, TTSounds.SQUEEK.get(), SQUEAK_VOLUME, SQUEAK_PITCH_BASE, SQUEAK_PITCH_SPREAD);
            return true;
        }
        boolean open = toggleOpenSide(side);
        BlockEssentiaTransport.syncNeighbourSide(level, worldPosition, side, open);
        playClickSound(level, TTSounds.TOOL.get(), TOOL_VOLUME, TOOL_PITCH_BASE, TOOL_PITCH_SPREAD);
        return true;
    }

    public void tickServer(Level level, BlockPos pos, BlockState state) {
        if (suctionPolicy.needsCount() || bellowsTimer.advance()) {
            suctionPolicy.recount(level, pos);
        }
        if (pullTimer.advance() && !isFull()) {
            pullFromNeighbour(level, pos);
        }
    }

    private void pullFromNeighbour(Level level, BlockPos pos) {
        int from = FIRST_SIDE;
        while (from < DIRECTIONS.length) {
            PullCandidate candidate = pullSelector.select(level, pos, from);
            if (candidate == null || transfer(candidate)) {
                return;
            }
            from = candidate.side().ordinal() + 1;
        }
    }

    private static int lookedAtArm(Level level, Player player, BlockPos pos) {
        BlockHitResult hit = BlockEssentiaTransport.traceLook(level, player, pos);
        return hit == null ? NO_ARM : BlockEssentiaTransport.resolveSubHit(level.getBlockState(pos), hit, pos);
    }

    private void playClickSound(Level world, SoundEvent sound, float volume, float pitchBase, float pitchSpread) {
        world.playSound(null, worldPosition, sound, SoundSource.BLOCKS, volume, pitchBase + world.getRandom().nextFloat() * pitchSpread);
    }

    public int heldTotal() {
        return contents.totalAmount();
    }

    public int fill(ResourceKey<IAspect> aspect, int amount) {
        Holder<IAspect> resolved = Aspects.resolve(level, aspect);
        return resolved == null ? 0 : insert(resolved, amount);
    }

    private int insert(Holder<IAspect> aspect, int amount) {
        if (amount != SINGLE_UNIT || isFull()) {
            return 0;
        }
        contents = contents.add(aspect, SINGLE_UNIT);
        setChangedAndSync();
        return SINGLE_UNIT;
    }

    public boolean drain(Holder<IAspect> aspect, int amount) {
        if (amount < SINGLE_UNIT || contents.amountOf(aspect) < amount) {
            return false;
        }
        contents = contents.remove(aspect, amount);
        setChangedAndSync();
        return true;
    }

    private boolean openOn(@Nullable Direction side) {
        return side != null && sides.isOpen(side);
    }

    public int bellowsCount() {
        return suctionPolicy.bellowsCount();
    }

    private boolean isFull() {
        return heldTotal() >= MAX_AMOUNT;
    }

    private boolean transfer(PullCandidate candidate) {
        Direction towardPeer = candidate.side().getOpposite();
        if (candidate.peer().takeEssentia(candidate.aspect(), SINGLE_UNIT, towardPeer) <= 0) {
            return false;
        }
        insert(candidate.aspect(), SINGLE_UNIT);
        return true;
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction side) {
        return NO_SUCTION_TYPE;
    }

    @Override
    public int getSuctionAmount(@Nullable Direction side) {
        ChokeLevel level = side == null ? ChokeLevel.NORMAL : sides.choke(side);
        return suctionPolicy.offered(level);
    }

    @Override
    public int getMinimumSuction() {
        return NO_SUCTION;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> type, int strength) {}

    @Override
    public boolean isConnectable(Direction side) {
        return openOn(side);
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction side) {
        return heldTotal();
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction side) {
        if (contents.isEmpty()) {
            return null;
        }
        List<AspectInstance> held = contents.entries();
        AspectInstance chosen = level == null ? held.get(0) : held.get(level.getRandom().nextInt(held.size()));
        return chosen.aspect();
    }

    @Override
    public int takeEssentia(Holder<IAspect> kind, int amount, Direction side) {
        if (side == null || !canOutputTo(side)) {
            return 0;
        }
        boolean outbid = level != null && strongerPull.verdict(level, worldPosition, kind, side) == TakeVerdict.OUTBID;
        int moved = Math.min(amount, contents.amountOf(kind));
        if (outbid || moved < SINGLE_UNIT) {
            return 0;
        }
        boolean removed = drain(kind, moved);
        return removed ? moved : 0;
    }

    @Override
    public boolean canOutputTo(Direction side) {
        return openOn(side);
    }

    @Override
    public int addEssentia(Holder<IAspect> kind, int amount, Direction side) {
        boolean accepted = side != null && canInputFrom(side) && kind.unwrapKey().isPresent();
        return accepted ? insert(kind, amount) : 0;
    }

    @Override
    public boolean canInputFrom(Direction side) {
        return side != null && sides.isOpen(side);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(CONTENTS_KEY, AspectList.CODEC, contents);
        output.store(CHOKED_KEY, Codec.INT.listOf(), sides.chokeOrdinals());
        output.store(OPEN_KEY, Codec.BOOL.listOf(), sides.openList());
        output.putInt(FACING_KEY, facing.ordinal());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        List<Integer> chokes = input.read(CHOKED_KEY, Codec.INT.listOf()).orElse(List.of());
        List<Boolean> opens = input.read(OPEN_KEY, Codec.BOOL.listOf()).orElse(List.of());
        contents = input.read(CONTENTS_KEY, AspectList.CODEC).orElse(AspectList.EMPTY);
        sides.restore(chokes, opens);
        facing = BlockEssentiaTransport.directionOrNorth(input.getIntOr(FACING_KEY, Direction.NORTH.ordinal()));
    }
}
