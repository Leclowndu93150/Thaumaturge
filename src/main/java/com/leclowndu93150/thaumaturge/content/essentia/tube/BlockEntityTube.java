package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.casters.IInteractWithCaster;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.cadence.CadencePhase;
import com.leclowndu93150.thaumaturge.content.essentia.cadence.TickCadence;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.essentia.tube.behaviour.PlainTubeBehaviour;
import com.leclowndu93150.thaumaturge.content.essentia.tube.behaviour.TubeBehaviour;
import com.leclowndu93150.thaumaturge.content.essentia.tube.facing.SideRanking;
import com.leclowndu93150.thaumaturge.content.essentia.tube.facing.SideRotation;
import com.leclowndu93150.thaumaturge.content.essentia.tube.state.TubeCell;
import com.leclowndu93150.thaumaturge.content.essentia.tube.state.TubeSuction;
import com.leclowndu93150.thaumaturge.content.essentia.tube.vent.PressureClash;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.content.particle.VentParticleOptions;
import com.leclowndu93150.thaumaturge.network.ClientboundTubeCreakPayload;
import com.leclowndu93150.thaumaturge.network.ClientboundTubeVentPayload;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.mojang.serialization.Codec;
import java.nio.ByteBuffer;
import java.util.Arrays;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public class BlockEntityTube extends AbstractSyncedBlockEntity implements IEssentiaTransport, IInteractWithCaster {
    protected static final Codec<ResourceKey<IAspect>> ASPECT_KEY_CODEC = LegacyIds.ASPECT_KEY_CODEC;

    private static final String ESSENTIA_TYPE_KEY = "EssentiaType";
    private static final String ESSENTIA_AMOUNT_KEY = "EssentiaAmount";
    private static final String SUCTION_TYPE_KEY = "SuctionType";
    private static final String SUCTION_KEY = "Suction";
    private static final String FACING_KEY = "Facing";
    private static final String OPEN_SIDES_KEY = "OpenSides";

    private static final Direction[] DIRECTIONS = Direction.values();
    private static final CadencePhase SUCTION_REFRESH = CadencePhase.every(2);
    private static final CadencePhase VENT_CHECK = CadencePhase.every(2);
    private static final CadencePhase EQUALISE = CadencePhase.every(5);
    private static final double BROADCAST_RADIUS = 32.0;
    private static final double BLOCK_CENTER = 0.5;
    private static final int MIN_TRANSFER = 1;
    private static final int PREFERRED_FACING = 0;
    private static final int FALLBACK_FACING = 1;
    private static final byte OPEN_BYTE = 1;
    private static final byte CLOSED_BYTE = 0;
    private static final int VENT_PAUSE_TICKS = 40;
    private static final int VENT_JET_TICKS = 50;
    private static final int DEFAULT_VENT_COLOR = 0xCDD2D6;
    private static final float JET_PUFF_SCALE = 1.5F;
    private static final int HEADING_BITS = 16;
    private static final long HEADING_MASK = (1L << HEADING_BITS) - 1L;
    private static final double HEADING_STEPS = HEADING_MASK + 1.0;
    private static final float TOOL_VOLUME = 0.5F;
    private static final float TOOL_PITCH_BASE = 0.95F;
    private static final float TOOL_PITCH_SPREAD = 0.15F;

    protected Direction flowSide = Direction.NORTH;
    protected final boolean[] sideOpen = new boolean[DIRECTIONS.length];

    private final TubeBehaviour behaviour;
    private final TickCadence cadence;
    private TubeCell cell = TubeCell.EMPTY;
    private TubeSuction pull = TubeSuction.NONE;
    private int ventPauseLeft;
    private int jetTicksLeft;
    private int ventTint = DEFAULT_VENT_COLOR;
    private @Nullable Vec3 jetHeading;

    public BlockEntityTube(BlockPos pos, BlockState state) {
        this(TTBlockEntities.TUBE.get(), pos, state);
    }

    protected BlockEntityTube(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, PlainTubeBehaviour.INSTANCE);
    }

    protected BlockEntityTube(BlockEntityType<?> type, BlockPos pos, BlockState state, TubeBehaviour behaviour) {
        super(type, pos, state);
        this.behaviour = behaviour;
        this.cadence = TickCadence.staggered(pos);
        Arrays.fill(sideOpen, true);
    }

    public void tickServer(Level level, BlockPos pos, BlockState state) {
        cadence.advance();
        if (ventPauseLeft > 0) {
            ventPauseLeft--;
            return;
        }
        if (cadence.isDue(SUCTION_REFRESH)) {
            refreshSuction(level, pos);
        }
        if (cadence.isDue(VENT_CHECK) && inspectPressure(level, pos)) {
            return;
        }
        if (cadence.isDue(EQUALISE)) {
            EssentiaFlowHandler.equalizeWithNeighbours(level, pos, this, directionalEqualize());
        }
    }

    public void tickClient(Level level, BlockPos pos, BlockState state) {
        if (jetTicksLeft <= 0) {
            return;
        }
        jetTicksLeft--;
        Vec3 heading = jetHeading(pos);
        VentParticleOptions puff = new VentParticleOptions(heading.x, heading.y, heading.z, ventTint, JET_PUFF_SCALE, false);
        level.addParticle(puff, pos.getX() + BLOCK_CENTER, pos.getY() + BLOCK_CENTER, pos.getZ() + BLOCK_CENTER, 0.0, 0.0, 0.0);
    }

    private Vec3 jetHeading(BlockPos pos) {
        if (jetHeading == null) {
            long seed = Mth.getSeed(pos);
            double yaw = (seed & HEADING_MASK) / HEADING_STEPS * Mth.TWO_PI;
            double lift = ((seed >>> HEADING_BITS) & HEADING_MASK) / HEADING_STEPS * 2.0 - 1.0;
            double spread = Math.sqrt(1.0 - lift * lift);
            jetHeading = new Vec3(Math.cos(yaw) * spread, lift, Math.sin(yaw) * spread);
        }
        return jetHeading;
    }

    private boolean inspectPressure(Level level, BlockPos pos) {
        Holder<IAspect> wanted = getSuctionType(null);
        if (!PressureClash.assess(level, pos, this, getSuctionAmount(null), wanted).isClash()) {
            return false;
        }
        ventPauseLeft = VENT_PAUSE_TICKS;
        ventTint = wanted != null ? wanted.value().color() : DEFAULT_VENT_COLOR;
        broadcastVent(level, pos, ventTint);
        return true;
    }

    public void playToolSound(Level level, BlockPos pos) {
        level.playSound(null, pos, TTSounds.TOOL.get(), SoundSource.BLOCKS, TOOL_VOLUME, TOOL_PITCH_BASE + level.getRandom().nextFloat() * TOOL_PITCH_SPREAD);
    }

    private void refreshSuction(Level level, BlockPos pos) {
        EssentiaFlowHandler.recalculateSuction(level, pos, this, suctionFilter(), restrictiveSuction(), directionalSuction());
        TubeCell tidied = cell.dropDanglingAspect();
        if (tidied != cell) {
            cell = tidied;
            setChanged();
        }
    }

    public void triggerVent(int color) {
        jetTicksLeft = VENT_JET_TICKS;
        ventTint = color;
    }

    public void broadcastVent(Level level, BlockPos pos, int color) {
        if (level instanceof ServerLevel server) {
            PacketDistributor.sendToPlayersNear(server, null, pos.getX() + BLOCK_CENTER, pos.getY() + BLOCK_CENTER, pos.getZ() + BLOCK_CENTER, BROADCAST_RADIUS,
                    new ClientboundTubeVentPayload(pos, color));
        }
    }

    public void broadcastCreak(Level level, BlockPos pos) {
        if (level instanceof ServerLevel server) {
            PacketDistributor.sendToPlayersNear(server, null, pos.getX() + BLOCK_CENTER, pos.getY() + BLOCK_CENTER, pos.getZ() + BLOCK_CENTER, BROADCAST_RADIUS, new ClientboundTubeCreakPayload(pos));
        }
    }

    public int ventingTicks() {
        return Math.max(ventPauseLeft, jetTicksLeft);
    }

    public int ventColor() {
        return ventTint;
    }

    protected @Nullable ResourceKey<IAspect> suctionFilter() {
        return behaviour.suctionFilter();
    }

    protected boolean restrictiveSuction() {
        return behaviour.restrictiveSuction();
    }

    protected boolean directionalSuction() {
        return behaviour.directionalSuction();
    }

    protected boolean directionalEqualize() {
        return behaviour.directionalEqualize();
    }

    public Direction flowSide() {
        return flowSide;
    }

    public void setFacingForPlacement(@Nullable LivingEntity placer) {
        flowSide = placer == null ? Direction.NORTH : Direction.orderedByNearest(placer)[0].getOpposite();
        pushUpdate(this);
    }

    public boolean[] sideOpenFlags() {
        return sideOpen;
    }

    public boolean isSideOpen(Direction side) {
        return sideOpen[side.ordinal()];
    }

    public void setOpenSide(Direction side, boolean open) {
        sideOpen[side.ordinal()] = open;
        setChanged();
    }

    public boolean toggleSide(Direction side) {
        boolean open = !isSideOpen(side);
        sideOpen[side.ordinal()] = open;
        pushUpdate(this);
        return open;
    }

    public boolean rotateFacing() {
        Direction target = SideRotation.next(flowSide(), this::rankFacingCandidate);
        if (target == null) {
            return false;
        }
        assignFlowSide(target);
        pushUpdate(this);
        return true;
    }

    private int rankFacingCandidate(Direction side) {
        if (!isSideOpen(side)) {
            return SideRanking.UNACCEPTABLE;
        }
        return hasTransportNeighbour(side) ? PREFERRED_FACING : FALLBACK_FACING;
    }

    protected void assignFlowSide(Direction direction) {
        flowSide = direction;
    }

    protected boolean hasTransportNeighbour(Direction side) {
        return level != null && EssentiaFlowHandler.transport(level, worldPosition.relative(side), side.getOpposite()) != null;
    }

    @Override
    public boolean onCasterRightClick(Level level, ItemStack casterStack, Player player, BlockPos pos, Direction side, InteractionHand hand) {
        if (level.isClientSide()) {
            return true;
        }
        BlockHitResult hit = BlockEssentiaTransport.traceLook(level, player, pos);
        if (hit == null) {
            return false;
        }
        int part = BlockEssentiaTransport.resolveSubHit(level.getBlockState(pos), hit, pos);
        if (part == TubeGeometry.CORE_HIT && !isSideOpen(hit.getDirection())) {
            part = hit.getDirection().ordinal();
        }
        if (!handleCasterClick(part)) {
            return false;
        }
        playToolSound(level, pos);
        player.swing(hand);
        return true;
    }

    public boolean handleCasterClick(int part) {
        if (level == null) {
            return false;
        }
        if (part >= 0 && part < DIRECTIONS.length) {
            Direction side = DIRECTIONS[part];
            boolean open = toggleSide(side);
            BlockEssentiaTransport.syncNeighbourSide(level, worldPosition, side, open);
            return true;
        }
        if (part == TubeGeometry.CORE_HIT) {
            rotateFacing();
            return true;
        }
        return false;
    }

    protected static void pushUpdate(BlockEntity blockEntity) {
        blockEntity.setChanged();
        Level level = blockEntity.getLevel();
        if (level != null && !level.isClientSide()) {
            BlockState state = blockEntity.getBlockState();
            level.sendBlockUpdated(blockEntity.getBlockPos(), state, state, Block.UPDATE_ALL);
        }
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != null && isSideOpen(face);
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face != null && isSideOpen(face);
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return face != null && isSideOpen(face);
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {
        pull = TubeSuction.of(aspect, amount);
    }

    protected void clearSuction() {
        pull = TubeSuction.NONE;
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction face) {
        return resolve(pull.aspect());
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        return pull.strength();
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction face) {
        return resolve(cell.aspect());
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return cell.amount();
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (amount < MIN_TRANSFER || face == null || !canOutputTo(face) || !cell.hasUnit() || !cell.holds(aspect)) {
            return 0;
        }
        cell = TubeCell.EMPTY;
        setChanged();
        return TubeCell.CAPACITY;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (amount < MIN_TRANSFER || face == null || !canInputFrom(face) || cell.isOccupied()) {
            return 0;
        }
        ResourceKey<IAspect> key = aspect.unwrapKey().orElse(null);
        if (key == null) {
            return 0;
        }
        cell = TubeCell.holding(key);
        setChanged();
        return TubeCell.CAPACITY;
    }

    @Override
    public int spaceFor(Holder<IAspect> aspect, Direction face) {
        return face != null && canInputFrom(face) && !cell.isOccupied() ? TubeCell.CAPACITY : 0;
    }

    public @Nullable ResourceKey<IAspect> essentiaKey() {
        return cell.aspect();
    }

    public int essentiaAmountRaw() {
        return cell.amount();
    }

    public @Nullable ResourceKey<IAspect> suctionKey() {
        return pull.aspect();
    }

    public int suctionRaw() {
        return pull.strength();
    }

    protected @Nullable Holder<IAspect> resolve(@Nullable ResourceKey<IAspect> key) {
        return key == null ? null : Aspects.resolve(level, key);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.storeNullable(ESSENTIA_TYPE_KEY, ASPECT_KEY_CODEC, cell.aspect());
        output.putInt(ESSENTIA_AMOUNT_KEY, cell.amount());
        output.storeNullable(SUCTION_TYPE_KEY, ASPECT_KEY_CODEC, pull.aspect());
        output.putInt(SUCTION_KEY, pull.strength());
        output.putInt(FACING_KEY, flowSide.ordinal());
        writeOpenSides(output);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        cell = new TubeCell(input.read(ESSENTIA_TYPE_KEY, ASPECT_KEY_CODEC).orElse(null), input.getIntOr(ESSENTIA_AMOUNT_KEY, 0));
        pull = new TubeSuction(input.read(SUCTION_TYPE_KEY, ASPECT_KEY_CODEC).orElse(null), input.getIntOr(SUCTION_KEY, 0));
        flowSide = BlockEssentiaTransport.directionOrNorth(input.getIntOr(FACING_KEY, Direction.NORTH.ordinal()));
        readOpenSides(input);
    }

    protected void readOpenSides(ValueInput input) {
        ByteBuffer stored = input.read(OPEN_SIDES_KEY, Codec.BYTE_BUFFER).orElse(null);
        boolean valid = stored != null && stored.remaining() == sideOpen.length;
        for (int i = 0; i < sideOpen.length; i++) {
            sideOpen[i] = !valid || stored.get(stored.position() + i) == OPEN_BYTE;
        }
    }

    protected void writeOpenSides(ValueOutput output) {
        byte[] bytes = new byte[sideOpen.length];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = sideOpen[i] ? OPEN_BYTE : CLOSED_BYTE;
        }
        output.store(OPEN_SIDES_KEY, Codec.BYTE_BUFFER, ByteBuffer.wrap(bytes));
    }
}
