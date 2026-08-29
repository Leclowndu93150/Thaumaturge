package com.leclowndu93150.thaumaturge.content.device.fluxscrubber;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaTransport;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class BlockEntityFluxScrubber extends BlockEntity implements IEssentiaTransport {
    private static final int RADIUS = 16;
    private static final int DIAMETER = RADIUS * 2 + 1;
    private static final int SCAN_VOLUME = DIAMETER * DIAMETER * DIAMETER;
    private static final int CHECKS_PER_TICK = 16;
    // TC4 VisNetHandler stored vis in centivis; 5/10 legacy units are 0.05/0.10 modern aura vis.
    private static final float WORK_VIS = 0.05F;
    private static final float VIS_REFILL_REQUEST = 0.10F;

    private static int chargesPerRoll() {
        return ThaumaturgeCommonConfig.FLUX_SCRUBBER_CHARGES_PER_ROLL.get();
    }

    private static double essentiaChance() {
        return ThaumaturgeCommonConfig.FLUX_SCRUBBER_ESSENTIA_CHANCE.get();
    }

    private static int essentiaPerRoll() {
        return ThaumaturgeCommonConfig.FLUX_SCRUBBER_ESSENTIA_PER_ROLL.get();
    }

    public static int essentiaCapacity() {
        return ThaumaturgeCommonConfig.FLUX_SCRUBBER_ESSENTIA_CAPACITY.get();
    }

    private int essentia;
    private int charges;
    private float power;
    private int scanIndex;
    private int scanOffset;
    private int scanStep;

    private int animationOffset;

    public BlockEntityFluxScrubber(BlockPos pos, BlockState state) {
        super(TCBlockEntities.FLUX_SCRUBBER.get(), pos, state);
    }

    public int storedEssentia() {
        return essentia;
    }

    public int charges() {
        return charges;
    }

    public float power() {
        return power;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityFluxScrubber scrubber) {
        if (scrubber.charges >= chargesPerRoll()) {
            scrubber.charges -= chargesPerRoll();
            if (level.getRandom().nextDouble() < essentiaChance() && scrubber.essentia < essentiaCapacity()) {
                scrubber.essentia = Math.min(essentiaCapacity(), scrubber.essentia + essentiaPerRoll());
                scrubber.changedAndSync();
            } else {
                scrubber.setChanged();
            }
        }

        if (scrubber.power < WORK_VIS) {
            float drained = AuraHelper.drainVis(level, pos, VIS_REFILL_REQUEST, false);
            if (drained > 0.0F) {
                scrubber.power += drained;
                scrubber.setChanged();
            }
        }
        if (scrubber.power >= WORK_VIS) {
            scrubber.scanForFlux((ServerLevel) level, pos);
        }
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, BlockEntityFluxScrubber scrubber) {
        // TC4 assigned each tile a random phase offset so nearby scrubbers did not bob in lockstep.
        if (scrubber.animationOffset == 0)
            scrubber.animationOffset = level.getRandom().nextInt(1000);
    }

    public int animationOffset() {
        return animationOffset;
    }

    private void scanForFlux(ServerLevel level, BlockPos origin) {
        ensureScanCycle(level);
        for (int i = 0; i < CHECKS_PER_TICK && scanIndex < SCAN_VOLUME; i++) {
            int encoded = (scanOffset + scanIndex++ * scanStep) % SCAN_VOLUME;
            int dx = encoded % DIAMETER - RADIUS;
            int yz = encoded / DIAMETER;
            int dz = yz % DIAMETER - RADIUS;
            int dy = yz / DIAMETER - RADIUS;
            if (dx * dx + dy * dy + dz * dz >= RADIUS * RADIUS) continue;
            BlockPos target = origin.offset(dx, dy, dz);
            if (removeOneFluxQuanta(level, target)) {
                power -= WORK_VIS;
                charges++;
                setChanged();
                Effects.simpleSparkle(level, Vec3.atCenterOf(target))
                        .color(0xDD / 255.0F, 0.0F, 1.0F)
                        .scale(0.8F)
                        .send();
                return; // TC4 stopped the 16-position scan immediately after one successful cleanup.
            }
        }
    }

    private static boolean removeOneFluxQuanta(ServerLevel level, BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        if (level.getChunkSource().getChunkNow(chunkX, chunkZ) == null) return false;
        BlockState state = level.getBlockState(pos);
        if (!PhysicalFlux.isScrubbable(state)) return false;
        return PhysicalFlux.reduce(level, pos, 1) == 1;
    }

    private void ensureScanCycle(ServerLevel level) {
        if (scanStep == 0 || scanIndex >= SCAN_VOLUME) {
            scanOffset = level.getRandom().nextInt(SCAN_VOLUME);
            do {
                scanStep = 1 + level.getRandom().nextInt(SCAN_VOLUME - 1);
            } while (greatestCommonDivisor(scanStep, SCAN_VOLUME) != 1);
            scanIndex = 0;
        }
    }

    private static int greatestCommonDivisor(int first, int second) {
        while (second != 0) {
            int remainder = first % second;
            first = second;
            second = remainder;
        }
        return first;
    }

    private Direction outputFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    private Holder<IAspect> praecantatio() {
        return level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(TCAspects.PRAECANTATIO);
    }

    @Override
    public boolean isConnectable(Direction face) {
        return face != null && face == outputFace();
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
    public void setSuction(Holder<IAspect> aspect, int amount) {}

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
        return canOutputTo(face) && level != null ? praecantatio() : null;
    }

    @Override
    public int getEssentiaAmount(Direction face) {
        return canOutputTo(face) ? essentia : 0;
    }

    @Override
    public int takeEssentia(Holder<IAspect> aspect, int amount, Direction face) {
        if (!canOutputTo(face) || aspect == null || amount <= 0 || essentia <= 0 || level == null) return 0;
        if (!aspect.unwrapKey().map(TCAspects.PRAECANTATIO::equals).orElse(false)) return 0;
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

    private void changedAndSync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(getBlockPos(), state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        essentia = Math.max(0, Math.min(essentiaCapacity(), input.getInt("Essentia")));
        charges = Math.max(0, input.getInt("Charges"));
        power = Math.max(0.0F, input.getFloat("Power"));
        scanIndex = 0;
        scanStep = 0;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putInt("Essentia", essentia);
        output.putInt("Charges", charges);
        output.putFloat("Power", power);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Essentia", essentia);
        tag.putFloat("Power", power);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
