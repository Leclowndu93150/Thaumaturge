package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IEntityAspectSource;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.content.effect.WispEffects;
import com.leclowndu93150.thaumaturge.content.entity.ai.FlyingWanderGoal;
import com.leclowndu93150.thaumaturge.content.entity.ai.WispZapGoal;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class WispEntity extends Monster implements IEntityAspectSource {
    private static final EntityDataAccessor<String> DATA_ASPECT = SynchedEntityData.defineId(WispEntity.class, EntityDataSerializers.STRING);

    private static final int XP_REWARD = 5;
    private static final int COMPOUND_TYPE_ONE_IN = 10;
    private static final int RANDOM_AGGRO_INTERVAL = 1000;
    private static final int MAX_NEARBY_WISPS = 8;
    private static final double NEARBY_WISP_RANGE = 16.0;
    private static final int MAX_SPAWN_CLUSTER = 2;
    private static final float WISP_SOUND_VOLUME = 0.25F;
    private static final float VERTICAL_DRAG = 0.6F;
    private static final float FLYING_FRICTION_IMPULSE = 0.02F;
    private static final int SELF_ASPECT_AMOUNT = 5;
    private static final float SPAWN_BURST_SIZE = 10.0F;
    private static final float DEATH_BURST_SIZE = 1.0F;
    private static final float DEATH_BURST_Y_OFFSET = 0.45F;
    private static final float MOTE_SPREAD = 0.7F;

    public WispEntity(EntityType<? extends WispEntity> type, Level level) {
        super(type, level);
        this.xpReward = XP_REWARD;
        this.moveControl = new Ghast.GhastMoveControl(this, false, () -> false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 22.0).add(Attributes.ATTACK_DAMAGE, 3.0).add(Attributes.FOLLOW_RANGE, 16.0).add(Attributes.FLYING_SPEED, 0.1);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(5, new FlyingWanderGoal(this, true, () -> true));
        this.goalSelector.addGoal(7, new Ghast.GhastLookGoal(this));
        this.goalSelector.addGoal(7, new WispZapGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, RANDOM_AGGRO_INTERVAL, true, false, null));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_ASPECT, "");
    }

    public @Nullable Holder<IAspect> aspect() {
        String id = this.entityData.get(DATA_ASPECT);
        if (id.isEmpty()) {
            return null;
        }
        Identifier parsed = Identifier.tryParse(id);
        if (parsed == null) {
            return null;
        }
        return this.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(ResourceKey.create(IAspect.REGISTRY_KEY, parsed)).map(holder -> (Holder<IAspect>) holder).orElse(null);
    }

    public void setAspect(Identifier aspect) {
        this.entityData.set(DATA_ASPECT, aspect.toString());
    }

    @Override
    public AspectList entityAspects() {
        Holder<IAspect> self = aspect();
        var registry = this.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        AspectList aspects = AspectList.EMPTY.add(registry.getOrThrow(TCAspects.AURAM), SELF_ASPECT_AMOUNT).add(registry.getOrThrow(TCAspects.VOLATUS), SELF_ASPECT_AMOUNT);
        if (self != null) {
            aspects = aspects.add(self, SELF_ASPECT_AMOUNT);
        }
        return aspects;
    }

    @Override
    public void travel(Vec3 input) {
        this.travelFlying(input, FLYING_FRICTION_IMPULSE);
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    protected void checkFallDamage(double ya, boolean onGround, BlockState onState, BlockPos pos) {}

    @Override
    public void tick() {
        super.tick();
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x, movement.y * VERTICAL_DRAG, movement.z);
        if (this.level().isClientSide()) {
            clientAmbientFx();
        }
    }

    private void clientAmbientFx() {
        if (this.tickCount <= 1) {
            this.level().addParticle(WispEffects.burst(SPAWN_BURST_SIZE), this.getX(), this.getY(), this.getZ(), 0.0, 0.0, 0.0);
        }
        if (this.isDeadOrDying() && this.deathTime == 1) {
            this.level().addParticle(WispEffects.burst(DEATH_BURST_SIZE), this.getX(), this.getY() + DEATH_BURST_Y_OFFSET, this.getZ(), 0.0, 0.0, 0.0);
        }
        Holder<IAspect> self = aspect();
        if (self != null && this.random.nextBoolean()) {
            this.level().addParticle(WispEffects.mote(this.random, self.value().color()), this.getX() + (this.random.nextFloat() - this.random.nextFloat()) * MOTE_SPREAD,
                    this.getY() + (this.random.nextFloat() - this.random.nextFloat()) * MOTE_SPREAD, this.getZ() + (this.random.nextFloat() - this.random.nextFloat()) * MOTE_SPREAD, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level() instanceof ServerLevel && aspect() == null) {
            pickRandomAspect();
        }
    }

    private void pickRandomAspect() {
        var registry = this.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY);
        boolean compound = this.random.nextInt(COMPOUND_TYPE_ONE_IN) == 0;
        List<Holder.Reference<IAspect>> pool = registry.listElements().filter(holder -> holder.value().isPrimal() != compound).toList();
        if (!pool.isEmpty()) {
            setAspect(pool.get(this.random.nextInt(pool.size())).key().identifier());
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.WISPLIVE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.LAVA_EXTINGUISH;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.WISPDEAD.get();
    }

    @Override
    protected float getSoundVolume() {
        return WISP_SOUND_VOLUME;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        Holder<IAspect> self = aspect();
        if (self != null) {
            this.spawnAtLocation(level, EssentiaCrystalFactory.of(self));
        }
    }

    @Override
    public int getMaxSpawnClusterSize() {
        return MAX_SPAWN_CLUSTER;
    }

    public static boolean checkWispSpawnRules(EntityType<WispEntity> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        int nearby = level.getEntitiesOfClass(WispEntity.class, new AABB(pos).inflate(NEARBY_WISP_RANGE)).size();
        return nearby < MAX_NEARBY_WISPS && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("Type", this.entityData.get(DATA_ASPECT));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.entityData.set(DATA_ASPECT, input.getStringOr("Type", ""));
    }
}
