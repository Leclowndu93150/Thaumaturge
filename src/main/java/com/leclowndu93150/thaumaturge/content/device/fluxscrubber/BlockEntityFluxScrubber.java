package com.leclowndu93150.thaumaturge.content.device.fluxscrubber;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.aura.relay.VisRelayNetwork;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityFluxScrubber extends BlockEntity implements IEssentiaTransport {
    public static final int WORK_POWER = 5;
    private static final int POWER_REQUEST = 10;
    private static final int DRAW_RETRY_TICKS = 20;
    private static final int RADIUS = 16;
    private static final int DIAMETER = RADIUS * 2 + 1;
    private static final int SCAN_VOLUME = DIAMETER * DIAMETER * DIAMETER;
    private static final int CHECKS_PER_TICK = 16;
    private static final float SPARKLE_RED = 0xDD / 255.0F;
    private static final float SPARKLE_SCALE = 0.8F;

    private int essentia;
    private int charges;
    private int power;
    private int drawCooldown;
    private int scanIndex;
    private int scanOffset;
    private int scanStep;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();

    public BlockEntityFluxScrubber(BlockPos pos, BlockState state) {
        super(TTBlockEntities.FLUX_SCRUBBER.get(), pos, state);
    }

    public static int essentiaCapacity() {
        return ThaumaturgeCommonConfig.FLUX_SCRUBBER_ESSENTIA_CAPACITY.get();
    }

    public int storedEssentia() {
        return essentia;
    }

    public int charges() {
        return charges;
    }

    public int power() {
        return power;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityFluxScrubber scrubber) {
        if (level instanceof ServerLevel server) {
            scrubber.tickServer(server, pos);
        }
    }

    private void tickServer(ServerLevel level, BlockPos pos) {
        int chargesPerRoll = ThaumaturgeCommonConfig.FLUX_SCRUBBER_CHARGES_PER_ROLL.get();
        if (charges >= chargesPerRoll) {
            charges -= chargesPerRoll;
            if (level.getRandom().nextDouble() < ThaumaturgeCommonConfig.FLUX_SCRUBBER_ESSENTIA_CHANCE.get() && essentia < essentiaCapacity()) {
                essentia = Math.min(essentiaCapacity(), essentia + ThaumaturgeCommonConfig.FLUX_SCRUBBER_ESSENTIA_PER_ROLL.get());
                changedAndSync();
            } else {
                setChanged();
            }
        }
        if (power < WORK_POWER) {
            drawPower(level, pos);
        }
        if (power >= WORK_POWER) {
            scanForFlux(level, pos);
        }
    }

    private void drawPower(ServerLevel level, BlockPos pos) {
        if (drawCooldown > 0) {
            drawCooldown--;
            return;
        }
        int drained = VisRelayNetwork.drainEverySourceNear(level, pos, TTAspects.AER, POWER_REQUEST);
        if (drained < POWER_REQUEST) {
            drained += VisRelayNetwork.drainNodesNear(level, pos, TTAspects.AER, POWER_REQUEST - drained);
        }
        if (drained > 0) {
            power += drained;
            setChanged();
        } else {
            drawCooldown = DRAW_RETRY_TICKS;
        }
    }

    private void scanForFlux(ServerLevel level, BlockPos origin) {
        ensureScanCycle(level.getRandom());
        for (int i = 0; i < CHECKS_PER_TICK && scanIndex < SCAN_VOLUME; i++) {
            int encoded = (int) ((scanOffset + (long) scanIndex++ * scanStep) % SCAN_VOLUME);
            int dx = encoded % DIAMETER - RADIUS;
            int yz = encoded / DIAMETER;
            int dz = yz % DIAMETER - RADIUS;
            int dy = yz / DIAMETER - RADIUS;
            if (dx * dx + dy * dy + dz * dz >= RADIUS * RADIUS) {
                continue;
            }
            cursor.setWithOffset(origin, dx, dy, dz);
            if (level.isLoaded(cursor) && PhysicalFlux.isScrubbable(level.getBlockState(cursor))) {
                BlockPos target = cursor.immutable();
                if (PhysicalFlux.reduce(level, target, 1) != 1) {
                    continue;
                }
                power -= WORK_POWER;
                charges++;
                setChanged();
                Effects.simpleSparkle(level, Vec3.atCenterOf(target)).color(SPARKLE_RED, 0.0F, 1.0F).scale(SPARKLE_SCALE).send();
                return;
            }
        }
    }

    private void ensureScanCycle(RandomSource random) {
        if (scanStep == 0 || scanIndex >= SCAN_VOLUME) {
            scanOffset = random.nextInt(SCAN_VOLUME);
            do {
                scanStep = 1 + random.nextInt(SCAN_VOLUME - 1);
            } while (gcd(scanStep, SCAN_VOLUME) != 1);
            scanIndex = 0;
        }
    }

    private static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    private Direction outputFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    private @Nullable Holder<IAspect> praecantatio() {
        return level == null ? null : level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(TTAspects.PRAECANTATIO);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face == outputFace();
    }

    @Override
    public boolean canInputFrom(Direction face) {
        return false;
    }

    @Override
    public boolean canOutputTo(Direction face) {
        return isConnectable(face);
    }

    @Override
    public void setSuction(@Nullable Holder<IAspect> aspect, int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(Direction face) {
        return null;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return 0;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(Direction face) {
        return canOutputTo(face) && essentia > 0 ? praecantatio() : null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return canOutputTo(face) ? essentia : 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canOutputTo(face) || aspect == null || amount <= 0 || essentia <= 0 || !aspect.is(TTAspects.PRAECANTATIO)) {
            return 0;
        }
        int taken = Math.min(amount, essentia);
        essentia -= taken;
        changedAndSync();
        return taken;
    }

    @Override
    public int addEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        return 0;
    }

    @Override
    public int getMinimumSuction() {
        return 0;
    }

    @Override
    public int spaceFor(Holder<IAspect> aspect, Direction face) {
        return 0;
    }

    private void changedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        essentia = Math.clamp(input.getIntOr("Essentia", 0), 0, essentiaCapacity());
        charges = Math.max(0, input.getIntOr("Charges", 0));
        power = Math.max(0, input.getIntOr("Power", 0));
        scanIndex = 0;
        scanStep = 0;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("Essentia", essentia);
        output.putInt("Charges", charges);
        output.putInt("Power", power);
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
