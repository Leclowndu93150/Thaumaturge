package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
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

public final class EntityTaintCrawler extends Monster implements ITaintedMob {
    private static final int TRAIL_INTERVAL = 40;
    private static final int FLUX_TAINT_BASE_TICKS = 60;

    private BlockPos lastPos;

    public EntityTaintCrawler(EntityType<? extends EntityTaintCrawler> type, Level level) {
        super(type, level);
        this.lastPos = BlockPos.ZERO;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 8.0).add(Attributes.MOVEMENT_SPEED, 0.275).add(Attributes.ATTACK_DAMAGE, 2.0).add(Attributes.FOLLOW_RANGE, 16.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new FloatGoal(this));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        if (this.tickCount % TRAIL_INTERVAL != 0) {
            return;
        }
        BlockPos pos = this.blockPosition();
        if (pos.equals(lastPos)) {
            return;
        }
        lastPos = pos;
        BlockState here = server.getBlockState(pos);
        if (!here.isAir() && !here.canBeReplaced()) {
            return;
        }
        if (here.liquid()) {
            return;
        }
        if (here.is(TCBlocks.TAINT_FIBRE.get())) {
            return;
        }
        if (!TaintHelper.isAdjacentToSolidBlock(server, pos)) {
            return;
        }
        if (BlockTaintFibre.isOnlyAdjacentToTaint(server, pos)) {
            return;
        }
        server.setBlockAndUpdate(pos, TCBlocks.TAINT_FIBRE.get().defaultBlockState());
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean attacked = super.doHurtTarget(level, target);
        if (attacked && target instanceof LivingEntity living) {
            if (level.getRandom().nextFloat() < 0.3F) {
                living.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, FLUX_TAINT_BASE_TICKS, 0, true, false, false));
            }
        }
        return attacked;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.GORE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.GORE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.GORE.get();
    }

    @Override
    public boolean isPushable() {
        return true;
    }
}
