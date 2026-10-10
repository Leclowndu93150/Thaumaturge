package com.leclowndu93150.thaumaturge.content.essentia.crystalizer;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCrystalAccess;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaFlowHandler;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityEssentiaCrystalizer extends BlockEntity implements IEssentiaTransport {
    public static final int TARGET_PROGRESS = 200;

    private static final String ASPECT_KEY = "Aspect";
    private static final String PROGRESS_KEY = "Progress";
    private static final int STEP_INTERVAL = 5;
    private static final int IDLE_SUCTION = 128;
    private static final int BUSY_SUCTION = 64;
    private static final int ONE_POINT = 1;
    private static final int MAX_VIS_REQUEST = 20;
    private static final int MIN_VIS_REQUEST = 1;
    private static final int REQUEST_DIVISOR = 2;
    private static final float HUNDREDTHS = 100.0F;
    private static final int VIS_PROGRESS_FACTOR = 2;
    private static final int BASE_PROGRESS_STEP = 1;
    private static final int RED_SHIFT = 16;
    private static final int GREEN_SHIFT = 8;
    private static final int CHANNEL_MASK = 0xFF;
    private static final int VENT_COLOR = 0xFFFFFF;
    private static final float VENT_SCALE = 4.0F;
    private static final float COLOR_SCALE = 255.0F;
    private static final float MAX_SPEED = 20.0F;
    private static final float FULL_TURN = 360.0F;
    private static final int WHITE_TINT = 0xFFFFFF;
    private static final int SUCTION_FLOOR = 0;
    private static final int NOTHING_MOVED = 0;
    private static final boolean OUTPUT_ALLOWED = false;
    private static final double DROP_DISTANCE = 0.7;
    private static final double DROP_SPEED = 0.04;
    private static final int VENT_TICKS = 6;
    private static final double VENT_DISTANCE = 0.55;
    private static final double VENT_JITTER = 0.15;
    private static final float COLOR_STEP = 0.05F;
    private static final float TINT_LIFT = 0.25F;
    private static final float SPEED_RISE = 0.1F;
    private static final float SPEED_FALL = 0.2F;
    private static final float FINISH_VOLUME = 0.2F;
    private static final float FINISH_BASE_PITCH = 1.75F;
    private static final float FINISH_PITCH_SPREAD = 0.2F;

    private @Nullable ResourceKey<IAspect> aspect;
    private int progress;
    private int ventTicks;
    private float rotation;
    private float rotationSpeed;
    private float crystalRed = 1.0F;
    private float crystalGreen = 1.0F;
    private float crystalBlue = 1.0F;

    public BlockEntityEssentiaCrystalizer(BlockPos pos, BlockState state) {
        super(TTBlockEntities.ESSENTIA_CRYSTALIZER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEssentiaCrystalizer crystalizer) {
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        Direction input = state.getValue(BlockStateProperties.FACING);
        Direction output = input.getOpposite();
        if (crystalizer.ventTicks > 0) {
            crystalizer.ventTicks--;
            crystalizer.emitPuff(server, output);
        }
        boolean stepTick = server.getGameTime() % STEP_INTERVAL == 0;
        if (!stepTick || server.hasNeighborSignal(pos)) {
            return;
        }
        if (crystalizer.aspect != null) {
            crystalizer.advance(server, output);
            return;
        }
        crystalizer.pullFromInput(server, input);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityEssentiaCrystalizer crystalizer) {
        crystalizer.fadeTint(level);
        crystalizer.spin(!level.hasNeighborSignal(pos));
    }

    private static float channel(int packed, int shift) {
        return ((packed >> shift) & CHANNEL_MASK) / COLOR_SCALE;
    }

    private static float approach(float current, float target, float step) {
        return current < target ? Math.min(current + step, target) : Math.max(current - step, target);
    }

    private void pullFromInput(ServerLevel server, Direction inputFace) {
        Direction touching = inputFace.getOpposite();
        IEssentiaTransport source = EssentiaFlowHandler.transport(server, worldPosition.relative(inputFace), touching);
        if (source == null || !source.canOutputTo(touching)) {
            return;
        }
        Holder<IAspect> offered = source.getEssentiaType(touching);
        if (offered == null || source.getEssentiaAmount(touching) < ONE_POINT) {
            return;
        }
        int ownPull = getSuctionAmount(inputFace);
        if (source.getSuctionAmount(touching) >= ownPull || ownPull < source.getMinimumSuction()) {
            return;
        }
        if (source.takeEssentia(offered, ONE_POINT, touching) == ONE_POINT) {
            hold(offered.unwrapKey().orElse(null));
        }
    }

    private void advance(ServerLevel server, Direction output) {
        int needed = TARGET_PROGRESS - progress;
        int request = Math.min(MAX_VIS_REQUEST, Math.max(MIN_VIS_REQUEST, needed / REQUEST_DIVISOR));
        float drained = AuraHelper.drainVis(server, worldPosition, request / HUNDREDTHS, false);
        int units = Math.min(request, Math.round(drained * HUNDREDTHS));
        progress += BASE_PROGRESS_STEP + VIS_PROGRESS_FACTOR * Math.max(units, 0);
        if (progress >= TARGET_PROGRESS) {
            finish(server, output);
            return;
        }
        setChanged();
    }

    private void finish(ServerLevel server, Direction output) {
        Holder<IAspect> held = heldHolder();
        aspect = null;
        progress = 0;
        if (held != null) {
            ItemStack crystal = EssentiaCrystalAccess.create(held, ONE_POINT);
            ItemStack leftover = InvHelper.insertStackAt(server, worldPosition.relative(output), output.getOpposite(), crystal, false);
            if (!leftover.isEmpty()) {
                dropOutward(server, output, leftover);
                ventTicks = VENT_TICKS;
            }
        }
        RandomSource random = server.getRandom();
        server.playSound(null, worldPosition, SoundEvents.LAVA_EXTINGUISH, SoundSource.BLOCKS, FINISH_VOLUME, FINISH_BASE_PITCH + random.nextFloat() * FINISH_PITCH_SPREAD);
        changedForClients();
    }

    private void dropOutward(ServerLevel server, Direction output, ItemStack stack) {
        Vec3 spot = Vec3.atCenterOf(worldPosition).add(output.getStepX() * DROP_DISTANCE, output.getStepY() * DROP_DISTANCE, output.getStepZ() * DROP_DISTANCE);
        ItemEntity item = new ItemEntity(server, spot.x, spot.y, spot.z, stack);
        item.setDeltaMovement(output.getStepX() * DROP_SPEED, output.getStepY() * DROP_SPEED, output.getStepZ() * DROP_SPEED);
        server.addFreshEntity(item);
    }

    private void emitPuff(ServerLevel server, Direction output) {
        RandomSource random = server.getRandom();
        Vec3 face = Vec3.atCenterOf(worldPosition).add(output.getStepX() * VENT_DISTANCE, output.getStepY() * VENT_DISTANCE, output.getStepZ() * VENT_DISTANCE);
        double mx = output.getStepX() + jitter(random);
        double my = output.getStepY() + jitter(random);
        double mz = output.getStepZ() + jitter(random);
        Effects.vent(server, face).motion(mx, my, mz).color(VENT_COLOR).scale(VENT_SCALE).send();
    }

    private void fadeTint(Level level) {
        Holder<IAspect> held = aspect == null ? null : Aspects.resolve(level, aspect);
        int packed = held == null ? WHITE_TINT : held.value().color();
        crystalRed = approach(crystalRed, brighten(channel(packed, RED_SHIFT)), COLOR_STEP);
        crystalGreen = approach(crystalGreen, brighten(channel(packed, GREEN_SHIFT)), COLOR_STEP);
        crystalBlue = approach(crystalBlue, brighten(channel(packed, 0)), COLOR_STEP);
    }

    private static float brighten(float channel) {
        return Math.min(1.0F, channel + (1.0F - channel) * TINT_LIFT);
    }

    private void spin(boolean unpowered) {
        boolean working = unpowered && aspect != null;
        rotationSpeed = working ? approach(rotationSpeed, MAX_SPEED, SPEED_RISE) : approach(rotationSpeed, 0.0F, SPEED_FALL);
        rotation = (rotation + rotationSpeed) % FULL_TURN;
    }

    private void hold(@Nullable ResourceKey<IAspect> incoming) {
        aspect = incoming;
        progress = 0;
        changedForClients();
    }

    @Override
    public int spaceFor(Holder<IAspect> candidate, Direction side) {
        return heldPoints() == NOTHING_MOVED && isInputSide(side) ? ONE_POINT : NOTHING_MOVED;
    }

    private static double jitter(RandomSource random) {
        return (random.nextDouble() * 2.0 - 1.0) * VENT_JITTER;
    }

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable Direction face) {
        return null;
    }

    private void changedForClients() {
        setChanged();
        if (level != null) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return OUTPUT_ALLOWED;
    }

    private Direction inputFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    private boolean isInputSide(@Nullable Direction face) {
        return face != null && face == inputFace();
    }

    public @Nullable ResourceKey<IAspect> aspectKey() {
        return aspect;
    }

    public int progress() {
        return progress;
    }

    public float rotation() {
        return rotation;
    }

    public float rotationSpeed() {
        return rotationSpeed;
    }

    public float crystalRed() {
        return crystalRed;
    }

    public float crystalGreen() {
        return crystalGreen;
    }

    public float crystalBlue() {
        return crystalBlue;
    }

    private int heldPoints() {
        if (aspect != null) {
            return ONE_POINT;
        }
        return NOTHING_MOVED;
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return face == inputFace();
    }

    @Override
    public int addEssentia(Holder<IAspect> offered, int count, Direction side) {
        return admit(offered, count, side) ? ONE_POINT : NOTHING_MOVED;
    }

    private boolean admit(Holder<IAspect> offered, int count, Direction side) {
        if (aspect != null || count <= 0 || !isInputSide(side)) {
            return false;
        }
        Optional<ResourceKey<IAspect>> key = offered.unwrapKey();
        key.ifPresent(this::hold);
        return key.isPresent();
    }

    @Override
    public int takeEssentia(Holder<IAspect> wanted, int count, Direction side) {
        return NOTHING_MOVED;
    }

    @Override
    public boolean isConnectable(Direction face) {
        return isInputSide(face);
    }

    private @Nullable Holder<IAspect> heldHolder() {
        return aspect == null ? null : Aspects.resolve(level, aspect);
    }

    @Override
    public int getSuctionAmount(@Nullable Direction face) {
        int base = aspect == null ? IDLE_SUCTION : BUSY_SUCTION;
        boolean listening = isInputSide(face) && level != null && !level.hasNeighborSignal(worldPosition);
        return listening ? base : SUCTION_FLOOR;
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> suctionAspect, int amount) {}

    @Override
    public int getMinimumSuction() {
        return SUCTION_FLOOR;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspect = input.read(ASPECT_KEY, LegacyIds.ASPECT_KEY_CODEC).orElse(null);
        progress = aspect == null ? 0 : Mth.clamp(input.getIntOr(PROGRESS_KEY, 0), 0, TARGET_PROGRESS);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (aspect == null) {
            return;
        }
        output.putInt(PROGRESS_KEY, progress);
        output.store(ASPECT_KEY, LegacyIds.ASPECT_KEY_CODEC, aspect);
    }

    @Override
    public int getEssentiaAmount(@Nullable Direction face) {
        return heldPoints();
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable Direction face) {
        return heldHolder();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
