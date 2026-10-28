package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class EntityTaintCrawler extends Monster {
    private static final double MAX_HEALTH = 8.0;
    private static final double MOVEMENT_SPEED = 0.275;
    private static final double ATTACK_DAMAGE = 2.0;
    private static final double FOLLOW_RANGE = 16.0;
    private static final double MOVE_SPEED_MODIFIER = 1.0;
    private static final float LOOK_DISTANCE = 8.0F;
    private static final int FLOAT_PRIORITY = 1;
    private static final int MELEE_PRIORITY = 2;
    private static final int STROLL_PRIORITY = 3;
    private static final int LOOK_PRIORITY = 4;
    private static final int RETALIATE_PRIORITY = 1;
    private static final int HUNT_PRIORITY = 2;
    private static final int TRAIL_INTERVAL = 40;
    private static final float TAINT_CHANCE = 0.3F;
    private static final int FLUX_TAINT_BASE_TICKS = 60;
    private static final int TAINT_AMPLIFIER = 0;

    private @Nullable BlockPos lastFibre;

    public EntityTaintCrawler(EntityType<? extends EntityTaintCrawler> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(FLOAT_PRIORITY, new FloatGoal(this));
        this.goalSelector.addGoal(MELEE_PRIORITY, new MeleeAttackGoal(this, MOVE_SPEED_MODIFIER, false));
        this.goalSelector.addGoal(STROLL_PRIORITY, new WaterAvoidingRandomStrollGoal(this, MOVE_SPEED_MODIFIER));
        this.goalSelector.addGoal(LOOK_PRIORITY, new LookAtPlayerGoal(this, Player.class, LOOK_DISTANCE));
        this.goalSelector.addGoal(LOOK_PRIORITY, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(RETALIATE_PRIORITY, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(HUNT_PRIORITY, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.isAlive() || this.tickCount % TRAIL_INTERVAL != 0 || !(this.level() instanceof ServerLevel server)) {
            return;
        }
        layFibreAtFeet(server);
    }

    private void layFibreAtFeet(ServerLevel level) {
        BlockPos here = this.blockPosition();
        if (here.equals(this.lastFibre) || ThaumaturgeCommonConfig.WUSS_MODE.get() || !isFibreSpot(level, here)) {
            return;
        }
        level.setBlockAndUpdate(here, BlockTaintFibre.stateForWorld(level, here));
        this.lastFibre = here;
    }

    private static boolean isFibreSpot(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.getFluidState().isEmpty() || state.is(TTBlocks.TAINT_FIBRE.get())) {
            return false;
        }
        if (!state.isAir() && !state.canBeReplaced()) {
            return false;
        }
        return TaintHelper.hasSturdyNeighbour(level, pos) && !BlockTaintFibre.isOnlyAdjacentToTaint(level, pos);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        boolean attacked = super.doHurtTarget(target);
        if (attacked && target instanceof LivingEntity living && this.random.nextFloat() < TAINT_CHANCE) {
            living.addEffect(new MobEffectInstance(
                    TTMobEffects.FLUX_TAINT, FLUX_TAINT_BASE_TICKS, TAINT_AMPLIFIER, true, false, false));
        }
        return attacked;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.GORE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TTSounds.GORE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.GORE.get();
    }

    @Override
    public boolean isPushable() {
        return this.isAlive() && !this.isSpectator();
    }
}
