package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.ThaumicSlime;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
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
    private static final int QUANTA_PER_BLOCK = 8;
    private static final double MAX_DRAG = 0.5;
    private static final int GOO_DENSITY = 8;
    private static final int SLIME_SPAWN_CHANCE = 50;
    private static final int DECAY_ROLL_CHANCE = 4;
    private static final int SMALL_SLIME_META_MIN = 2;
    private static final int SMALL_SLIME_META_MAX = 6;
    private static final int FESTER_CHANCE = 50;
    private static final float FESTER_PRESSURE = 0.16F;
    private static final int VIS_EXHAUST_DURATION = 600;
    private static final float POLLUTE_AMOUNT = 1.0F;
    private static final int MAX_PARTIES = 5;

    protected FluxGooFluid(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isRandomlyTicking() {
        return true;
    }

    @Override
    protected boolean canBeReplacedWith(
            FluidState state, BlockGetter level, BlockPos pos, Fluid fluid, Direction direction) {
        return false;
    }

    @Override
    public void tick(Level level, BlockPos pos, FluidState fluidState) {
        if (level instanceof ServerLevel server
                && level.getFluidState(pos).getType().isSame(this)) {
            spreadTick(server, pos, fluidState);
        }
    }

    @Override
    protected void randomTick(Level world, BlockPos pos, FluidState state, RandomSource random) {
        if (!(world instanceof ServerLevel level)) return;
        PhysicalFluxAuraFloor.observe(level, pos);
        lifecycleTick(level, pos, state, random);
    }

    private void lifecycleTick(ServerLevel level, BlockPos pos, FluidState state, RandomSource rand) {
        if (!level.getFluidState(pos).getType().isSame(this)) {
            return;
        }
        int meta = state.getAmount() - 1;
        boolean airAbove = level.getBlockState(pos.above()).isAir();
        if (meta >= SMALL_SLIME_META_MIN
                && meta < SMALL_SLIME_META_MAX
                && airAbove
                && rand.nextInt(SLIME_SPAWN_CHANCE) == 0
                && spawnSlime(level, pos, 1)) {
            return;
        } else if (meta >= SMALL_SLIME_META_MAX
                && airAbove
                && rand.nextInt(SLIME_SPAWN_CHANCE) == 0
                && spawnSlime(level, pos, 2)) {
            return;
        } else if (meta >= SMALL_SLIME_META_MAX && airAbove && tryFester(level, pos, rand)) {
            return;
        } else if (rand.nextInt(DECAY_ROLL_CHANCE) == 0) {
            boolean pollutes = !PhysicalFluxAuraFloor.isEnabled();
            if (meta == 0) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                if (pollutes && rand.nextBoolean()) {
                    AuraHelper.polluteAura(level, pos, POLLUTE_AMOUNT, true);
                }
            } else {
                setGoo(level, pos, meta, Block.UPDATE_CLIENTS);
                if (pollutes) {
                    AuraHelper.polluteAura(level, pos, POLLUTE_AMOUNT, true);
                }
                if (airAbove && rand.nextBoolean()) {
                    PhysicalFlux.placeGas(level, pos.above(), 1);
                }
            }
        } else {
            spreadTick(level, pos, state);
        }
    }

    private static boolean tryFester(ServerLevel level, BlockPos pos, RandomSource rand) {
        if (!ThaumaturgeCommonConfig.TAINT_FROM_FLUX.get()
                || ThaumaturgeCommonConfig.WUSS_MODE.get()
                || rand.nextInt(FESTER_CHANCE) != 0
                || TaintBlooms.isProtected(level, pos)
                || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) {
            return false;
        }
        if (!TaintHelper.plantSeed(level, pos, 0.0F)) {
            return false;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        AuraHelper.polluteAura(level, pos, POLLUTE_AMOUNT, true);
        TaintEcology.addPressure(level, pos, FESTER_PRESSURE);
        return true;
    }

    private void spreadTick(ServerLevel level, BlockPos pos, FluidState state) {
        int quantaRemaining = state.getAmount();
        int prevRemaining = quantaRemaining;
        quantaRemaining = tryToFlowVerticallyInto(level, pos, quantaRemaining);
        if (quantaRemaining < 1) {
            return;
        }
        if (quantaRemaining != prevRemaining) {
            setGoo(level, pos, quantaRemaining, Block.UPDATE_CLIENTS);
            scheduleGooTick(level, pos);
            if (quantaRemaining == 1) {
                return;
            }
        } else if (quantaRemaining == 1) {
            return;
        }

        int lowerThan = quantaRemaining - 1;
        List<BlockPos> parties = new ArrayList<>(MAX_PARTIES);
        parties.add(pos);
        int total = quantaRemaining;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighbour = pos.relative(direction);
            if (!level.hasChunkAt(neighbour)) {
                continue;
            }
            BlockState neighbourState = level.getBlockState(neighbour);
            if (!neighbourState.isAir() && displaceIfPossible(level, neighbour)) {
                level.setBlock(neighbour, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            int quanta = getQuantaValueBelow(level, neighbour, lowerThan);
            if (quanta >= 0) {
                parties.add(neighbour);
                total += quanta;
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
            BlockState currentState = level.getBlockState(party);
            int current = currentState.is(TTBlocks.FLUX_GOO.get())
                    ? currentState.getFluidState().getAmount()
                    : 0;
            if (wanted != current) {
                if (wanted == 0) {
                    level.setBlock(party, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                } else {
                    int flags = currentState.is(TTBlocks.FLUX_GOO.get()) ? Block.UPDATE_CLIENTS : Block.UPDATE_ALL;
                    setGoo(level, party, wanted, flags);
                    scheduleGooTick(level, party);
                }
            }
        }
    }

    private int tryToFlowVerticallyInto(ServerLevel level, BlockPos pos, int amtToInput) {
        BlockPos other = pos.below();
        if (other.getY() < level.getMinBuildHeight()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return 0;
        }

        if (!level.hasChunkAt(other)) {
            return amtToInput;
        }
        int amt = getQuantaValueBelow(level, other, QUANTA_PER_BLOCK);
        if (amt >= 0) {
            amt += amtToInput;
            if (amt > QUANTA_PER_BLOCK) {
                int flags = level.getBlockState(other).is(TTBlocks.FLUX_GOO.get())
                        ? Block.UPDATE_CLIENTS
                        : Block.UPDATE_ALL;
                setGoo(level, other, QUANTA_PER_BLOCK, flags);
                scheduleGooTick(level, other);
                return amt - QUANTA_PER_BLOCK;
            }
            if (amt > 0) {
                int flags = level.getBlockState(other).is(TTBlocks.FLUX_GOO.get())
                        ? Block.UPDATE_CLIENTS
                        : Block.UPDATE_ALL;
                setGoo(level, other, amt, flags);
                scheduleGooTick(level, other);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                return 0;
            }
            return amtToInput;
        }

        int densityOther = getDensity(level, other);
        if (densityOther == Integer.MAX_VALUE) {
            if (displaceIfPossible(level, other)) {
                level.setBlock(other, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                setGoo(level, other, amtToInput, Block.UPDATE_ALL);
                scheduleGooTick(level, other);
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                return 0;
            }
            return amtToInput;
        }

        if (densityOther < GOO_DENSITY || isDisplaceableVanillaFluid(level.getFluidState(other))) {
            BlockState displaced = level.getBlockState(other);
            setGoo(level, other, amtToInput, Block.UPDATE_ALL);
            level.setBlock(pos, displaced, Block.UPDATE_ALL);
            scheduleGooTick(level, other);
            FluidState displacedFluid = displaced.getFluidState();
            if (!displacedFluid.isEmpty()) {
                level.scheduleTick(
                        pos, displacedFluid.getType(), displacedFluid.getType().getTickDelay(level));
            }
            return 0;
        }
        return amtToInput;
    }

    private int getQuantaValue(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return 0;
        }
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty() && fluidState.getType().isSame(this)) {
            return fluidState.getAmount();
        }
        return -1;
    }

    private int getQuantaValueBelow(ServerLevel level, BlockPos pos, int belowThis) {
        int quanta = getQuantaValue(level, pos);
        return quanta >= belowThis ? -1 : quanta;
    }

    private static int getDensity(ServerLevel level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        if (fluidState.isEmpty()) {
            return Integer.MAX_VALUE;
        }
        return fluidState.getFluidType().getDensity();
    }

    private boolean canDisplace(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return true;
        }
        FluidState fluidState = state.getFluidState();
        if (!fluidState.isEmpty()) {
            if (fluidState.getType().isSame(this)) {
                return false;
            }
            return isDisplaceableVanillaFluid(fluidState)
                    || GOO_DENSITY > fluidState.getFluidType().getDensity();
        }
        if (state.is(TTBlocks.TAINT_FIBRE.get())) {
            return true;
        }
        if (state.blocksMotion()) {
            return false;
        }
        return state.canBeReplaced();
    }

    private boolean displaceIfPossible(ServerLevel level, BlockPos pos) {
        boolean canDisplace = canDisplace(level, pos);
        if (canDisplace) {
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.getFluidState().isEmpty()) {
                Block.dropResources(state, level, pos);
            }
        }
        return canDisplace;
    }

    private static boolean isDisplaceableVanillaFluid(FluidState state) {
        return state.is(FluidTags.WATER) || state.is(FluidTags.LAVA);
    }

    private void setGoo(Level level, BlockPos pos, int quanta, int flags) {
        level.setBlock(pos, gooBlockState(quanta), flags);
    }

    public static BlockState gooBlockState(int quanta) {
        int clamped = Math.max(1, Math.min(quanta, QUANTA_PER_BLOCK));
        return TTBlocks.FLUX_GOO
                .get()
                .defaultBlockState()
                .setValue(LiquidBlock.LEVEL, clamped >= QUANTA_PER_BLOCK ? 0 : QUANTA_PER_BLOCK - clamped);
    }

    private static void scheduleGooTick(ServerLevel level, BlockPos pos) {
        FluidState fluidState = level.getFluidState(pos);
        if (!fluidState.isEmpty()) {
            Fluid fluid = fluidState.getType();
            level.scheduleTick(pos, fluid, fluid.getTickDelay(level));
        }
    }

    public static void applyEntityInside(Level level, BlockPos pos, Entity entity) {
        FluidState fs = level.getFluidState(pos);
        int amount = fs.getAmount();
        if (amount <= 0) {
            return;
        }
        int meta = amount - 1;
        if (entity instanceof ThaumicSlime slime) {
            if (level instanceof ServerLevel serverLevel
                    && slime.getSize() < meta
                    && serverLevel.getRandom().nextBoolean()) {
                slime.setSize(slime.getSize() + 1, true);
                if (meta > 1) {
                    level.setBlock(pos, gooBlockState(meta), Block.UPDATE_CLIENTS);
                    scheduleGooTick(serverLevel, pos);
                } else {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            return;
        }
        if (pos.equals(entity.blockPosition())) {
            float quanta = amount / (float) QUANTA_PER_BLOCK;
            Vec3 motion = entity.getDeltaMovement();
            double damp = 1.0 - quanta * MAX_DRAG;
            entity.setDeltaMovement(motion.x * damp, motion.y, motion.z * damp);
        }
        if (!level.isClientSide() && entity instanceof LivingEntity living) {
            int amp = meta / 3;
            living.addEffect(
                    new MobEffectInstance(TTMobEffects.VIS_EXHAUST, VIS_EXHAUST_DURATION, amp, true, true, false));
        }
    }

    private static boolean spawnSlime(ServerLevel level, BlockPos pos, int size) {
        ThaumicSlime slime = TTEntities.THAUMIC_SLIME.get().create(level);
        return spawnSlime(level, pos, size, slime);
    }

    static boolean spawnSlime(ServerLevel level, BlockPos pos, int size, ThaumicSlime slime) {
        if (slime == null) {
            return false;
        }
        slime.setSize(size, true);
        slime.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.addFreshEntity(slime);
        level.playSound(null, pos, TTSounds.GORE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    public static final class Source extends FluxGooFluid {
        public Source(Properties properties) {
            super(properties);
        }

        @Override
        public int getAmount(FluidState state) {
            return 8;
        }

        @Override
        public boolean isSource(FluidState state) {
            return true;
        }
    }

    public static final class Flowing extends FluxGooFluid {
        public Flowing(Properties properties) {
            super(properties);
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
}
