package com.leclowndu93150.thaumaturge.content.device.fluxscrubber;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
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

    private static final int NONE = 0;

    private static final String ESSENTIA_KEY = "Essentia";
    private static final String CHARGES_KEY = "Charges";
    private static final String POWER_KEY = "Power";
    private static final int POWER_REQUEST = 10;
    private static final int RETRY_DELAY = 20;
    private static final int SCAN_RADIUS = 16;
    private static final int SCAN_SIDE = 2 * SCAN_RADIUS + 1;
    private static final int SCAN_CELLS = SCAN_SIDE * SCAN_SIDE * SCAN_SIDE;
    private static final int SCAN_RADIUS_SQUARED = SCAN_RADIUS * SCAN_RADIUS;
    private static final int CELL_BUDGET = 16;
    private static final int REMOVAL_AMOUNT = 1;
    private static final float SPARKLE_RED = 0.62F;
    private static final float SPARKLE_GREEN = 0.3F;
    private static final float SPARKLE_BLUE = 0.88F;
    private static final float SPARKLE_SCALE = 0.7F;

    private int essentia;
    private int charges;
    private int power;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private int refillWait;
    private int scanOffset;
    private int scanStep = 1;
    private int scanIndex = SCAN_CELLS;

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
        if (level instanceof ServerLevel serverLevel) {
            scrubber.work(serverLevel, pos);
        }
    }

    @Override
    public boolean isConnectable(final Direction face) {
        return isOutputSide(face);
    }

    @Override
    public boolean canOutputTo(final Direction face) {
        return isOutputSide(face);
    }

    @Override
    public boolean canInputFrom(final Direction face) {
        return false;
    }

    @Override
    public @Nullable Holder<IAspect> getEssentiaType(@Nullable final Direction face) {
        if (essentia <= 0 || !isOutputSide(face)) {
            return null;
        }
        return Aspects.resolve(level, TTAspects.PRAECANTATIO);
    }

    @Override
    public int getEssentiaAmount(@Nullable final Direction face) {
        return isOutputSide(face) ? essentia : NONE;
    }

    @Override
    public int takeEssentia(final Holder<IAspect> aspect, final int amount, @Nullable final Direction face) {
        boolean allowed = isOutputSide(face) && aspect.is(TTAspects.PRAECANTATIO);
        if (!allowed || amount <= 0 || essentia <= 0) {
            return NONE;
        }
        int taken = Math.min(amount, essentia);
        essentia -= taken;
        setChangedAndSend();
        return taken;
    }

    @Override
    public int addEssentia(final Holder<IAspect> aspect, final int amount, @Nullable final Direction face) {
        return NONE;
    }

    @Override
    public void setSuction(@Nullable final Holder<IAspect> aspect, final int amount) {}

    @Override
    public @Nullable Holder<IAspect> getSuctionType(@Nullable final Direction face) {
        return passiveSuctionType();
    }

    @Override
    public int getSuctionAmount(@Nullable final Direction face) {
        return NONE;
    }

    @Override
    public int getMinimumSuction() {
        return NONE;
    }

    @Override
    public int spaceFor(final Holder<IAspect> aspect, @Nullable final Direction face) {
        return NONE;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        essentia = Math.min(storedOrZero(input, ESSENTIA_KEY), essentiaCapacity());
        charges = storedOrZero(input, CHARGES_KEY);
        power = storedOrZero(input, POWER_KEY);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        writeCounters(output);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private static int storedOrZero(ValueInput input, String key) {
        return Math.max(NONE, input.getIntOr(key, NONE));
    }

    private void writeCounters(ValueOutput output) {
        output.putInt(ESSENTIA_KEY, essentia);
        output.putInt(CHARGES_KEY, charges);
        output.putInt(POWER_KEY, power);
    }

    private static @Nullable Holder<IAspect> passiveSuctionType() {
        return null;
    }

    private boolean isOutputSide(@Nullable Direction face) {
        return face == getBlockState().getValue(BlockStateProperties.FACING);
    }

    private void setChangedAndSend() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    private void work(ServerLevel level, BlockPos pos) {
        if (power < WORK_POWER) {
            refill(level, pos);
        }
        if (power >= WORK_POWER) {
            scan(level, pos);
        }
    }

    private void scan(ServerLevel level, BlockPos pos) {
        RandomSource random = level.getRandom();
        for (int checked = 0; checked < CELL_BUDGET; checked++) {
            if (scanIndex >= SCAN_CELLS) {
                reseed(random);
            }
            int cell = (int) ((scanOffset + (long) scanIndex * scanStep) % SCAN_CELLS);
            scanIndex++;
            int dx = cell / (SCAN_SIDE * SCAN_SIDE) - SCAN_RADIUS;
            int dy = cell / SCAN_SIDE % SCAN_SIDE - SCAN_RADIUS;
            int dz = cell % SCAN_SIDE - SCAN_RADIUS;
            if (dx * dx + dy * dy + dz * dz >= SCAN_RADIUS_SQUARED) {
                continue;
            }
            cursor.setWithOffset(pos, dx, dy, dz);
            if (!level.hasChunkAt(cursor) || !PhysicalFlux.isScrubbable(level.getBlockState(cursor))) {
                continue;
            }
            if (PhysicalFlux.reduce(level, cursor, REMOVAL_AMOUNT) > 0) {
                power -= WORK_POWER;
                charges++;
                Effects.simpleSparkle(level, Vec3.atCenterOf(cursor)).color(SPARKLE_RED, SPARKLE_GREEN, SPARKLE_BLUE).scale(SPARKLE_SCALE).send();
                roll(random);
                setChangedAndSend();
                return;
            }
        }
    }

    private void roll(RandomSource random) {
        int perRoll = ThaumaturgeCommonConfig.FLUX_SCRUBBER_CHARGES_PER_ROLL.get();
        double chance = ThaumaturgeCommonConfig.FLUX_SCRUBBER_ESSENTIA_CHANCE.get();
        int yield = ThaumaturgeCommonConfig.FLUX_SCRUBBER_ESSENTIA_PER_ROLL.get();
        int capacity = essentiaCapacity();
        while (charges >= perRoll) {
            charges -= perRoll;
            if (random.nextDouble() < chance) {
                essentia = Math.min(capacity, essentia + yield);
            }
        }
    }

    private void refill(ServerLevel level, BlockPos pos) {
        if (refillWait > 0) {
            refillWait--;
            return;
        }
        int obtained = VisRelayNetwork.drainEverySourceNear(level, pos, TTAspects.AER, POWER_REQUEST);
        if (obtained < POWER_REQUEST) {
            obtained += VisRelayNetwork.drainNodesNear(level, pos, TTAspects.AER, POWER_REQUEST - obtained);
        }
        if (obtained > 0) {
            power += obtained;
            setChanged();
        } else {
            refillWait = RETRY_DELAY;
        }
    }

    private void reseed(RandomSource random) {
        scanOffset = random.nextInt(SCAN_CELLS);
        int step;
        do {
            step = 1 + random.nextInt(SCAN_CELLS - 1);
        } while (gcd(step, SCAN_CELLS) != 1);
        scanStep = step;
        scanIndex = 0;
    }

    private static int gcd(int a, int b) {
        int x = a;
        int y = b;
        while (y != 0) {
            int next = x % y;
            x = y;
            y = next;
        }
        return x;
    }
}
