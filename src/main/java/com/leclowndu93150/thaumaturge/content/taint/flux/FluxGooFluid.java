package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.ThaumicSlime;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;

public abstract class FluxGooFluid extends BaseFlowingFluid {
    private static final int SLIME_ONE_IN = 50;
    private static final int SMALL_SLIME_MIN_AMOUNT = 3;
    private static final int LARGE_SLIME_MIN_AMOUNT = 7;
    private static final int SMALL_SLIME_SIZE = 1;
    private static final int LARGE_SLIME_SIZE = 2;
    private static final float SLIME_SOUND_VOLUME = 1.0F;
    private static final float SLIME_SOUND_PITCH = 1.0F;
    private static final float FULL_TURN_DEGREES = 360.0F;
    private static final double CELL_CENTRE = 0.5;
    private static final int FESTER_ONE_IN = 50;
    private static final int FESTER_MIN_AMOUNT = 7;
    private static final float FESTER_PRESSURE = 0.16F;
    private static final int FESTER_SPREAD_ATTEMPTS = 6;
    private static final int DECAY_ONE_IN = 4;
    private static final float POLLUTION_AMOUNT = 1.0F;
    private static final int LAST_QUANTUM = 1;
    private static final int GAS_AMOUNT = 1;
    private static final int REMOVE_WHEN_AT_MOST = 2;
    private static final double DRAG_AT_FULL = 0.5;
    private static final int VIS_EXHAUST_DURATION = 600;
    private static final int LEVELS_PER_AMPLIFIER = 3;
    private static final int FLOWING_DEFAULT_LEVEL = 7;

    private static final int MIN_SPLIT_AMOUNT = 2;
    private static final int MIN_DIFFERENCE = 2;
    private static final int GOO_DENSITY = 8;
    private static final int MAX_PARTIES = 5;

    protected FluxGooFluid(BaseFlowingFluid.Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isRandomlyTicking() {
        return true;
    }

    @Override
    protected boolean canBeReplacedWith(FluidState state, BlockGetter level, BlockPos pos, Fluid other, Direction direction) {
        return false;
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState blockState, FluidState fluidState) {
        if (isSame(level.getFluidState(pos).getType())) {
            spreadGoo(level, pos);
        }
    }

    @Override
    protected void randomTick(ServerLevel level, BlockPos pos, FluidState fluidState, RandomSource random) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(TTBlocks.FLUX_GOO)) {
            return;
        }
        PhysicalFluxAuraFloor.observe(level, pos);
        int amount = amountOf(state);
        boolean openAbove = level.getBlockState(pos.above()).isAir();
        if (openAbove && trySpawnSlime(level, pos, amount, random)) {
            return;
        }
        if (openAbove && tryFester(level, pos, amount, random)) {
            return;
        }
        if (random.nextInt(DECAY_ONE_IN) == 0) {
            evaporate(level, pos, amount, random);
            return;
        }
        spreadGoo(level, pos);
    }

    private static boolean trySpawnSlime(ServerLevel level, BlockPos pos, int amount, RandomSource random) {
        if (amount < SMALL_SLIME_MIN_AMOUNT || random.nextInt(SLIME_ONE_IN) != 0) {
            return false;
        }
        ThaumicSlime slime = TTEntities.THAUMIC_SLIME.get().create(level, EntitySpawnReason.NATURAL);
        if (slime == null) {
            return false;
        }
        slime.setSize(amount >= LARGE_SLIME_MIN_AMOUNT ? LARGE_SLIME_SIZE : SMALL_SLIME_SIZE, true);
        slime.snapTo(pos.getX() + CELL_CENTRE, pos.getY(), pos.getZ() + CELL_CENTRE, random.nextFloat() * FULL_TURN_DEGREES, 0.0F);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.addFreshEntity(slime);
        level.playSound(null, pos, TTSounds.GORE.get(), SoundSource.BLOCKS, SLIME_SOUND_VOLUME, SLIME_SOUND_PITCH);
        return true;
    }

    private static boolean tryFester(ServerLevel level, BlockPos pos, int amount, RandomSource random) {
        if (amount < FESTER_MIN_AMOUNT || !ThaumaturgeCommonConfig.TAINT_FROM_FLUX.get() || ThaumaturgeCommonConfig.WUSS_MODE.get() || random.nextInt(FESTER_ONE_IN) != 0) {
            return false;
        }
        if (TaintBlooms.isProtected(level, pos) || !(TaintBiomeManager.isTainted(level, pos) || TaintBiomeManager.taintColumn(level, pos))) {
            return false;
        }
        AuraHelper.polluteAura(level, pos, POLLUTION_AMOUNT, true);
        TaintHelper.establishFoothold(level, pos, FESTER_PRESSURE, FESTER_SPREAD_ATTEMPTS);
        return true;
    }

    private static void evaporate(ServerLevel level, BlockPos pos, int amount, RandomSource random) {
        if (amount <= LAST_QUANTUM) {
            if (random.nextBoolean()) {
                setAmount(level, pos, 0);
                pollutePastTheFloor(level, pos);
            } else if (!TaintHelper.placeFibreFromFlux(level, pos)) {
                setAmount(level, pos, 0);
            }
            return;
        }
        setAmount(level, pos, amount - 1);
        pollutePastTheFloor(level, pos);
        BlockPos above = pos.above();
        BlockState aboveState = level.getBlockState(above);
        if ((aboveState.isAir() || aboveState.is(TTBlocks.FLUX_GAS)) && random.nextBoolean()) {
            PhysicalFlux.placeGas(level, above, GAS_AMOUNT);
        }
    }

    @Override
    protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier) {
        if (!pos.equals(entity.blockPosition())) {
            return;
        }
        int amount = level.getFluidState(pos).getAmount();
        if (amount <= 0) {
            return;
        }
        if (entity instanceof ThaumicSlime slime) {
            if (level instanceof ServerLevel server) {
                feedSlime(server, pos, slime, amount);
            }
            return;
        }
        Vec3 motion = entity.getDeltaMovement();
        double keep = 1.0 - DRAG_AT_FULL * amount / PhysicalFlux.MAX_QUANTA;
        entity.setDeltaMovement(motion.x * keep, motion.y, motion.z * keep);
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            living.addEffect(new MobEffectInstance(TTMobEffects.VIS_EXHAUST, VIS_EXHAUST_DURATION, (amount - 1) / LEVELS_PER_AMPLIFIER, true, true, false));
        }
    }

    private static void feedSlime(ServerLevel level, BlockPos pos, ThaumicSlime slime, int amount) {
        if (slime.getSize() >= amount - 1 || !level.getRandom().nextBoolean()) {
            return;
        }
        slime.setSize(slime.getSize() + 1, true);
        setAmount(level, pos, amount <= REMOVE_WHEN_AT_MOST ? 0 : amount - 1);
    }

    public static BlockState gooBlockState(int amount) {
        return TTBlocks.FLUX_GOO.get().defaultBlockState().setValue(LiquidBlock.LEVEL, PhysicalFlux.MAX_QUANTA - Math.clamp(amount, 1, PhysicalFlux.MAX_QUANTA));
    }

    private static void pollutePastTheFloor(ServerLevel level, BlockPos pos) {
        if (!PhysicalFluxAuraFloor.isEnabled()) {
            AuraHelper.polluteAura(level, pos, POLLUTION_AMOUNT, true);
        }
    }

    private static void spreadGoo(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(TTBlocks.FLUX_GOO)) {
            return;
        }
        int amount = amountOf(state);
        BlockPos below = pos.below();
        if (below.getY() < level.getMinY()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        if (level.hasChunkAt(below) && flowDown(level, pos, amount, below)) {
            return;
        }
        if (amount >= MIN_SPLIT_AMOUNT) {
            levelSideways(level, pos, amount);
        }
    }

    private static boolean flowDown(ServerLevel level, BlockPos pos, int amount, BlockPos below) {
        BlockState target = level.getBlockState(below);
        switch (classify(level, below, target)) {
            case GOO -> {
                int existing = amountOf(target);
                int moved = Math.min(PhysicalFlux.MAX_QUANTA - existing, amount);
                if (moved <= 0) {
                    return false;
                }
                setAmount(level, below, existing + moved);
                setAmount(level, pos, amount - moved);
                return true;
            }
            case EMPTY -> {
                setAmount(level, below, amount);
                setAmount(level, pos, 0);
                return true;
            }
            case DISPLACEABLE_BLOCK -> {
                Block.dropResources(target, level, below);
                setAmount(level, below, amount);
                setAmount(level, pos, 0);
                return true;
            }
            case DISPLACEABLE_FLUID -> {
                setAmount(level, below, amount);
                level.setBlock(pos, target, Block.UPDATE_ALL);
                scheduleFluidTick(level, pos);
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private static void levelSideways(ServerLevel level, BlockPos pos, int amount) {
        List<BlockPos> parties = new ArrayList<>(MAX_PARTIES);
        parties.add(pos);
        int total = amount;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = pos.relative(direction);
            if (!level.hasChunkAt(neighbour)) {
                continue;
            }
            BlockState state = level.getBlockState(neighbour);
            Cell cell = classify(level, neighbour, state);
            int held = cell == Cell.GOO ? amountOf(state) : cell == Cell.EMPTY ? 0 : amount;
            if (amount - held >= MIN_DIFFERENCE) {
                parties.add(neighbour);
                total += held;
            }
        }
        if (parties.size() == 1) {
            return;
        }
        Util.shuffle(parties, level.getRandom());
        int share = total / parties.size();
        int leftover = total % parties.size();
        for (int index = 0; index < parties.size(); index++) {
            BlockPos party = parties.get(index);
            int wanted = share + (index < leftover ? 1 : 0);
            if (wanted != currentAmount(level, party)) {
                setAmount(level, party, wanted);
            }
        }
    }

    private static Cell classify(ServerLevel level, BlockPos pos, BlockState state) {
        if (state.is(TTBlocks.FLUX_GOO)) {
            return Cell.GOO;
        }
        if (state.isAir()) {
            return Cell.EMPTY;
        }
        if (state.getBlock() instanceof LiquidBlock) {
            return isLighterFluid(state.getFluidState()) ? Cell.DISPLACEABLE_FLUID : Cell.BLOCKED;
        }
        boolean looseBlock = state.canBeReplaced() && state.getFluidState().isEmpty() && state.getCollisionShape(level, pos).isEmpty();
        return state.is(TTBlocks.TAINT_FIBRE) || looseBlock ? Cell.DISPLACEABLE_BLOCK : Cell.BLOCKED;
    }

    private static boolean isLighterFluid(FluidState fluid) {
        return fluid.is(FluidTags.WATER) || fluid.is(FluidTags.LAVA) || fluid.getType().getFluidType().getDensity() < GOO_DENSITY;
    }

    private static int amountOf(BlockState state) {
        return state.getFluidState().getAmount();
    }

    private static int currentAmount(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.is(TTBlocks.FLUX_GOO) ? amountOf(state) : 0;
    }

    private static void setAmount(ServerLevel level, BlockPos pos, int amount) {
        if (amount <= 0) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        int flags = level.getBlockState(pos).is(TTBlocks.FLUX_GOO) ? Block.UPDATE_CLIENTS : Block.UPDATE_ALL;
        level.setBlock(pos, FluxGooFluid.gooBlockState(amount), flags);
        scheduleFluidTick(level, pos);
    }

    private static void scheduleFluidTick(ServerLevel level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            level.scheduleTick(pos, fluidState.getType(), fluidState.getType().getTickDelay(level));
        }
    }

    private enum Cell {
        GOO, EMPTY, DISPLACEABLE_FLUID, DISPLACEABLE_BLOCK, BLOCKED
    }

    public static final class Flowing extends FluxGooFluid {
        public Flowing(BaseFlowingFluid.Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, FLOWING_DEFAULT_LEVEL));
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) {
            return state.getValue(LEVEL);
        }

        @Override
        public boolean isSource(FluidState state) {
            return false;
        }
    }

    public static final class Source extends FluxGooFluid {
        public Source(BaseFlowingFluid.Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return PhysicalFlux.MAX_QUANTA;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }
}
