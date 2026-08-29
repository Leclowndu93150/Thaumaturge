package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.registry.TCBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
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

public abstract class AbstractTaintacle extends Monster implements ITaintedMob {
    private static final int SUBSTRATE_CHECK_INTERVAL = 20;
    private static final float STARVE_DAMAGE = 1.0F;
    private static final byte EVENT_FLAIL = 16;
    private static final float FLAIL_MAX = 3.0F;
    private static final float FLAIL_DECAY = 0.01F;

    public float flailIntensity = 1.0F;

    protected AbstractTaintacle(EntityType<? extends AbstractTaintacle> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createTaintacleAttributes(double maxHealth, double attackDamage) {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, maxHealth)
                .add(Attributes.ATTACK_DAMAGE, attackDamage)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.FOLLOW_RANGE, 12.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        ServerLevel level = (ServerLevel) level();
        level.broadcastEntityEvent(this, EVENT_FLAIL);
        return super.doHurtTarget(target);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EVENT_FLAIL) {
            this.flailIntensity = FLAIL_MAX;
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel server)) {
            if (this.flailIntensity > 1.0F) {
                this.flailIntensity -= FLAIL_DECAY;
            }
            return;
        }
        if (this.tickCount % SUBSTRATE_CHECK_INTERVAL == 0
                && !server.getBiome(this.blockPosition()).is(TCBiomeTags.IS_TAINTED)) {
            this.hurt(server.damageSources().starve(), STARVE_DAMAGE);
        }
    }

    /**
     * TC4 natural taintacles only spawned in Tainted Lands and on fibrous taint or taint soil.
     * The biome spawn list alone is not sufficient because otherwise vanilla can choose ordinary
     * grass/dirt inside the biome and create a taintacle that immediately fails its habitat rules.
     */
    public static boolean checkTaintacleSpawnRules(
            EntityType<? extends AbstractTaintacle> type,
            ServerLevelAccessor level,
            MobSpawnType spawnType,
            BlockPos pos,
            RandomSource random) {
        if (!level.getBiome(pos).is(TCBiomeTags.IS_TAINTED)) {
            return false;
        }

        BlockState here = level.getBlockState(pos);
        BlockState below = level.getBlockState(pos.below());
        boolean onTaint = here.is(TCBlocks.TAINT_FIBRE.get())
                || below.is(TCBlocks.TAINT_FIBRE.get())
                || here.is(TCBlocks.TAINT_SOIL.get())
                || below.is(TCBlocks.TAINT_SOIL.get());
        if (!onTaint) {
            return false;
        }
        // TC4 rejected a natural spawn when another normal Taintacle was already nearby.
        if (!level.getEntitiesOfClass(EntityTaintacle.class, new AABB(pos).inflate(24.0, 8.0, 24.0))
                .isEmpty()) {
            return false;
        }
        return Monster.checkMonsterSpawnRules(type, level, spawnType, pos, random);
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
        return TCSounds.GORE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.TENTACLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.TENTACLE.get();
    }
}
