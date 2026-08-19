package com.leclowndu93150.thaumaturge.content.taint.block;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AbstractTaintBlock extends Block implements ITaintBlock {
    private static final int DIE_CHANCE = 10;
    private static final int WALK_EFFECT_CHANCE = 250;
    private static final int WALK_EFFECT_DURATION = 200;

    protected AbstractTaintBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!TaintHelper.isNearTaintSeed(level, pos) && random.nextInt(DIE_CHANCE) == 0) {
            die(level, pos, state);
            return;
        }
        subRandomTick(state, level, pos, random);
    }

    protected void subRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {}

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (living instanceof ITaintedMob) {
            return;
        }
        if (living.is(EntityTypeTags.UNDEAD)) {
            return;
        }
        if (serverLevel.getRandom().nextInt(WALK_EFFECT_CHANCE) == 0) {
            living.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, WALK_EFFECT_DURATION, 0, true, false, false));
        }
    }
}
