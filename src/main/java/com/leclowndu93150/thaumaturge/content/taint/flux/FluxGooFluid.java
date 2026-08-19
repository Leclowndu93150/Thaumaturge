package com.leclowndu93150.thaumaturge.content.taint.flux;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.entity.ThaumicSlime;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.InsideBlockEffectApplier;
import net.minecraft.world.entity.LivingEntity;
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
    private static final int GOO_DENSITY = 8;
    private static final int SLIME_SPAWN_CHANCE = 50;
    private static final int DECAY_ROLL_CHANCE = 4;
    private static final int SMALL_SLIME_META_MIN = 2;
    private static final int SMALL_SLIME_META_MAX = 6;
    private static final int VIS_EXHAUST_DURATION = 600;
    private static final float POLLUTE_AMOUNT = 1.0F;
    private static final Direction[] HORIZONTAL = {Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST};

    protected FluxGooFluid(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isRandomlyTicking() {
        return true;
    }

    @Override
    public void tick(ServerLevel level, BlockPos pos, BlockState blockState, FluidState fluidState) {
        updateTick(level, pos, fluidState, level.getRandom());
    }

    @Override
    protected void randomTick(ServerLevel level, BlockPos pos, FluidState state, RandomSource random) {
        updateTick(level, pos, state, random);
    }

    private void updateTick(ServerLevel level, BlockPos pos, FluidState state, RandomSource rand) {
        if (!level.getFluidState(pos).getType().isSame(this)) {
            return;
        }
        int meta = state.getAmount() - 1;
        boolean airAbove = level.getBlockState(pos.above()).isAir();
        if (meta >= SMALL_SLIME_META_MIN && meta < SMALL_SLIME_META_MAX && airAbove && rand.nextInt(SLIME_SPAWN_CHANCE) == 0) {
            spawnSlime(level, pos, 1);
        } else if (meta >= SMALL_SLIME_META_MAX && airAbove && rand.nextInt(SLIME_SPAWN_CHANCE) == 0) {
            spawnSlime(level, pos, 2);
        } else if (rand.nextInt(DECAY_ROLL_CHANCE) == 0) {
            if (meta == 0) {
                if (rand.nextBoolean()) {
                    AuraHelper.polluteAura(level, pos, POLLUTE_AMOUNT, true);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                } else {
                    level.setBlock(pos, TCBlocks.TAINT_FIBRE.get().defaultBlockState(), Block.UPDATE_ALL);
                }
            } else {
                setGoo(level, pos, meta, Block.UPDATE_CLIENTS);
                AuraHelper.polluteAura(level, pos, POLLUTE_AMOUNT, true);
            }
        } else {
            spreadTick(level, pos, state, rand);
        }
    }

    private void spreadTick(ServerLevel level, BlockPos pos, FluidState state, RandomSource rand) {
        boolean changed = false;
        int quantaRemaining = state.getAmount();
        int prevRemaining = quantaRemaining;
        quantaRemaining = tryToFlowVerticallyInto(level, pos, quantaRemaining);
        if (quantaRemaining < 1) {
            return;
        }
        if (quantaRemaining != prevRemaining) {
            changed = true;
            if (quantaRemaining == 1) {
                setGoo(level, pos, quantaRemaining, Block.UPDATE_CLIENTS);
                return;
            }
        } else if (quantaRemaining == 1) {
            return;
        }

        int lowerThan = quantaRemaining - 1;
        int total = quantaRemaining;
        int count = 1;
        for (Direction side : HORIZONTAL) {
            BlockPos off = pos.relative(side);
            if (displaceIfPossible(level, off)) {
                level.setBlock(off, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
            int quanta = getQuantaValueBelow(level, off, lowerThan);
            if (quanta >= 0) {
                count++;
                total += quanta;
            }
        }

        if (count == 1) {
            if (changed) {
                setGoo(level, pos, quantaRemaining, Block.UPDATE_CLIENTS);
            }
            return;
        }

        int each = total / count;
        int rem = total % count;
        for (Direction side : HORIZONTAL) {
            BlockPos off = pos.relative(side);
            int quanta = getQuantaValueBelow(level, off, lowerThan);
            if (quanta >= 0) {
                int newQuanta = each;
                if (rem == count || rem > 1 && rand.nextInt(count - rem) != 0) {
                    ++newQuanta;
                    --rem;
                }
                if (newQuanta != quanta) {
                    if (newQuanta == 0) {
                        level.setBlock(off, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    } else {
                        setGoo(level, off, newQuanta, Block.UPDATE_CLIENTS);
                    }
                    scheduleGooTick(level, off);
                }
                --count;
            }
        }
        if (rem > 0) {
            ++each;
        }
        setGoo(level, pos, each, Block.UPDATE_CLIENTS);
    }

    private int tryToFlowVerticallyInto(ServerLevel level, BlockPos pos, int amtToInput) {
        BlockPos other = pos.below();
        if (other.getY() < level.getMinY()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return 0;
        }

        int amt = getQuantaValueBelow(level, other, QUANTA_PER_BLOCK);
        if (amt >= 0) {
            amt += amtToInput;
            if (amt > QUANTA_PER_BLOCK) {
                setGoo(level, other, QUANTA_PER_BLOCK, Block.UPDATE_ALL);
                scheduleGooTick(level, other);
                return amt - QUANTA_PER_BLOCK;
            }
            if (amt > 0) {
                setGoo(level, other, amt, Block.UPDATE_ALL);
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

        if (densityOther < GOO_DENSITY) {
            BlockState displaced = level.getBlockState(other);
            setGoo(level, other, amtToInput, Block.UPDATE_ALL);
            level.setBlock(pos, displaced, Block.UPDATE_ALL);
            scheduleGooTick(level, other);
            FluidState displacedFluid = displaced.getFluidState();
            if (!displacedFluid.isEmpty()) {
                level.scheduleTick(pos, displacedFluid.getType(), displacedFluid.getType().getTickDelay(level));
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
            return GOO_DENSITY > fluidState.getFluidType().getDensity();
        }
        if (state.is(TCBlocks.TAINT_FIBRE.get())) {
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

    private void setGoo(Level level, BlockPos pos, int quanta, int flags) {
        level.setBlock(pos, gooBlockState(quanta), flags);
    }

    public static BlockState gooBlockState(int quanta) {
        int clamped = Math.max(1, Math.min(quanta, QUANTA_PER_BLOCK));
        return TCBlocks.FLUX_GOO.get().defaultBlockState().setValue(LiquidBlock.LEVEL, clamped >= QUANTA_PER_BLOCK ? 0 : QUANTA_PER_BLOCK - clamped);
    }

    private void scheduleGooTick(ServerLevel level, BlockPos pos) {
        level.scheduleTick(pos, level.getFluidState(pos).getType(), getTickDelay(level));
    }

    @Override
    protected void entityInside(Level level, BlockPos pos, Entity entity, InsideBlockEffectApplier effectApplier) {
        FluidState fs = level.getFluidState(pos);
        int amount = fs.getAmount();
        int meta = amount - 1;
        if (entity instanceof ThaumicSlime slime) {
            if (!level.isClientSide() && slime.getSize() < meta && level.getRandom().nextBoolean()) {
                slime.setSize(slime.getSize() + 1, true);
                if (meta > 1) {
                    level.setBlock(pos, gooBlockState(meta), Block.UPDATE_CLIENTS);
                } else {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                }
            }
            return;
        }
        float quanta = amount / (float) QUANTA_PER_BLOCK;
        Vec3 motion = entity.getDeltaMovement();
        double damp = 1.0 - quanta;
        entity.setDeltaMovement(motion.x * damp, motion.y, motion.z * damp);
        if (entity instanceof LivingEntity living) {
            int amp = meta / 3;
            living.addEffect(new MobEffectInstance(TCMobEffects.VIS_EXHAUST, VIS_EXHAUST_DURATION, amp, true, true, false));
        }
    }

    private static void spawnSlime(ServerLevel level, BlockPos pos, int size) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        ThaumicSlime slime = TCEntities.THAUMIC_SLIME.get().create(level, EntitySpawnReason.NATURAL);
        if (slime == null) {
            return;
        }
        slime.setSize(size, true);
        slime.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        level.addFreshEntity(slime);
        level.playSound(null, pos, TCSounds.GORE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
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
