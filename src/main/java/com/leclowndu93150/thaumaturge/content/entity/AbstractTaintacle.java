package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class AbstractTaintacle extends Monster {
    private static final double SPAWN_SPACING_HORIZONTAL = 24.0;
    private static final double SPAWN_SPACING_VERTICAL = 8.0;
    private static final int SUBSTRATE_CHECK_INTERVAL = 20;
    private static final float STARVE_DAMAGE = 1.0F;
    private static final byte EVENT_FLAIL = 16;
    private static final float FLAIL_MAX = 3.0F;
    private static final float FLAIL_DECAY = 0.01F;
    private static final int STRIKE_TICKS = 20;

    public float flailIntensity = 1.0F;
    private int strikeTicks;

    protected AbstractTaintacle(EntityType<? extends AbstractTaintacle> type, Level level) {
        super(type, level);
    }

    public static boolean checkTaintacleSpawnRules(EntityType<? extends AbstractTaintacle> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        if (!level.getBiome(pos).is(TTBiomeTags.IS_TAINTED) || !onTaint(level.getBlockState(pos)) && !onTaint(level.getBlockState(pos.below()))) {
            return false;
        }
        if (!level.getEntitiesOfClass(EntityTaintacle.class, new AABB(pos).inflate(SPAWN_SPACING_HORIZONTAL, SPAWN_SPACING_VERTICAL, SPAWN_SPACING_HORIZONTAL)).isEmpty()) {
            return false;
        }
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    private static boolean onTaint(BlockState state) {
        return state.is(TTBlocks.TAINT_FIBRE) || state.is(TTBlocks.TAINT_SOIL);
    }

    public static AttributeSupplier.Builder createTaintacleAttributes(double maxHealth, double attackDamage) {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, maxHealth).add(Attributes.ATTACK_DAMAGE, attackDamage).add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.FOLLOW_RANGE, 12.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        level.broadcastEntityEvent(this, EVENT_FLAIL);
        return super.doHurtTarget(level, target);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_FLAIL) {
            this.flailIntensity = FLAIL_MAX;
            this.strikeTicks = STRIKE_TICKS;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) {
            if (this.strikeTicks > 0) {
                this.strikeTicks--;
            }
            if (this.flailIntensity > 1.0F) {
                this.flailIntensity -= FLAIL_DECAY;
            }
            return;
        }
        if (this.tickCount % SUBSTRATE_CHECK_INTERVAL == 0 && !server.getBiome(this.blockPosition()).is(TTBiomeTags.IS_TAINTED)) {
            this.hurtServer(server, server.damageSources().starve(), STARVE_DAMAGE);
        }
    }

    public int strikeTicks() {
        return this.strikeTicks;
    }

    public float enrage() {
        return 0.0F;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(double x, double y, double z) {}

    @Override
    public void push(Entity entity) {}

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.GORE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TTSounds.TENTACLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.TENTACLE.get();
    }
}
