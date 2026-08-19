package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.api.entity.IEldritchMob;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchGuardian;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchOrb;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionModifier;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityEldritchWarden extends EntityThaumaturgeBoss implements RangedAttackMob, IEldritchMob {
    private static final EntityDataAccessor<Byte> DATA_TITLE = SynchedEntityData.defineId(EntityEldritchWarden.class, EntityDataSerializers.BYTE);

    private static final String[] TITLES = {"Aphoom-Zhah", "Basatan", "Chaugnar Faugn", "Mnomquah", "Nyogtha", "Oorn", "Shaikorth", "Rhan-Tegoth", "Rhogog", "Shudde M'ell", "Vulthoom", "Yag-Kosha",
            "Yibb-Tstll", "Zathog", "Zushakon"};
    private static final byte ARM_LEFT_EVENT = 15;
    private static final byte ARM_RIGHT_EVENT = 16;
    private static final byte FRENZY_EVENT = 17;
    private static final byte SPAWN_EVENT = 18;
    private static final int SPAWN_TICKS = 150;
    private static final int BONUS_ARMOR = 4;
    private static final double BASE_HEALTH = 400.0;
    private static final double SHIELD_FRACTION = 0.66;
    private static final int SHIELD_REGEN_INTERVAL = 25;
    private static final int FRENZY_TICKS = 150;
    private static final int FRENZY_RING_END = 121;
    private static final int FRENZY_RING_INTERVAL = 10;
    private static final int SAP_TICK_MIN = 250;
    private static final int SAP_TICK_SPREAD = 150;
    private static final float ORB_CHANCE = 0.8F;
    private static final float SCREECH_FLING = 1.5F;
    private static final int SCREECH_EFFECT_TICKS = 400;
    private static final int SCREECH_WARP_BASE = 3;
    private static final int SMOKE_SPIRAL_COUNT = 33;
    private static final int SMOKE_COLOR = 2232623;
    private static final int ARC_COLOR = 0xBB44BB;
    private static final int TELEPORT_TRIES = 20;

    public float armLiftL;
    public float armLiftR;
    private boolean fieldFrenzy;
    private int fieldFrenzyCounter;
    private boolean lastBlast;

    private final ServerBossEvent shieldEvent = new ServerBossEvent(Mth.createInsecureUUID(this.random), Component.empty(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);

    public EntityEldritchWarden(EntityType<? extends EntityEldritchWarden> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, BASE_HEALTH).add(Attributes.MAX_ABSORPTION, BASE_HEALTH * SHIELD_FRACTION).add(Attributes.MOVEMENT_SPEED, 0.33)
                .add(Attributes.ATTACK_DAMAGE, 10.0).add(Attributes.ARMOR, BONUS_ARMOR);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LongRangeAttackGoal(this, 3.0, 1.0, 20, 40, 24.0F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 1.0));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityCultist.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_TITLE, (byte) 0);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.shieldEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.shieldEvent.removePlayer(player);
    }

    @Override
    public void generateName() {
        int mod = ChampionHelper.championType(this);
        if (mod >= 0) {
            this.setCustomName(Component.translatable("entity.thaumaturge.eldritch_warden.name.custom", getTitle(), Component.translatable("champion.mod." + ChampionModifier.MODS.get(mod).name())));
        }
    }

    private String getTitle() {
        return TITLES[Math.floorMod(this.entityData.get(DATA_TITLE), TITLES.length)];
    }

    private void setTitle(int title) {
        this.entityData.set(DATA_TITLE, (byte) title);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("title", this.entityData.get(DATA_TITLE));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        setTitle(input.getByteOr("title", (byte) 0));
    }

    private int shieldCap() {
        return (int) (this.getAttributeBaseValue(Attributes.MAX_HEALTH) * SHIELD_FRACTION);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (this.fieldFrenzyCounter == 0) {
            super.customServerAiStep(level);
        } else {
            this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());
        }
        int cap = shieldCap();
        if (this.invulnerableTime <= 0 && this.tickCount % SHIELD_REGEN_INTERVAL == 0 && this.getAbsorptionAmount() < cap) {
            this.setAbsorptionAmount(this.getAbsorptionAmount() + 1.0F);
        }
        this.shieldEvent.setProgress(Mth.clamp(this.getAbsorptionAmount() / cap, 0.0F, 1.0F));
    }

    @Override
    public void tick() {
        if (this.getSpawnTimer() == SPAWN_TICKS && !this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, SPAWN_EVENT);
        }
        super.tick();
        if (this.level().isClientSide()) {
            if (this.armLiftL > 0.0F) {
                this.armLiftL -= 0.05F;
            }
            if (this.armLiftR > 0.0F) {
                this.armLiftR -= 0.05F;
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide()) {
            return;
        }
        ServerLevel server = (ServerLevel) this.level();
        for (int corner = 0; corner < 4; corner++) {
            BlockPos pos = BlockPos.containing(this.getX() + (corner % 2 * 2 - 1) * 0.25F, this.getY(), this.getZ() + (corner / 2 % 2 * 2 - 1) * 0.25F);
            if (server.isEmptyBlock(pos)) {
                server.setBlockAndUpdate(pos, TCBlocks.EFFECT_SAP.get().defaultBlockState());
            }
        }
        if (this.getSpawnTimer() > 0 && this.tickCount % 4 == 0) {
            float height = Math.max(1.0F, this.getBbHeight() * ((SPAWN_TICKS - this.getSpawnTimer()) / (float) SPAWN_TICKS));
            for (int spiral = 0; spiral < SMOKE_SPIRAL_COUNT; spiral++) {
                Effects.smokeSpiral(server, this.position().add(0.0, height / 2.0F, 0.0)).radius(height).start(this.random.nextInt(360)).minY(Mth.floor(this.getBoundingBox().minY) - 1)
                        .color(SMOKE_COLOR).send();
            }
        }
        if (this.fieldFrenzyCounter > 0) {
            if (this.fieldFrenzyCounter == FRENZY_TICKS) {
                this.teleportHome(server);
            }
            this.performFieldFrenzy(server);
        }
    }

    private void performFieldFrenzy(ServerLevel server) {
        if (this.fieldFrenzyCounter < FRENZY_RING_END && this.fieldFrenzyCounter % FRENZY_RING_INTERVAL == 0) {
            this.level().broadcastEntityEvent(this, FRENZY_EVENT);
            double radius = (FRENZY_TICKS - this.fieldFrenzyCounter) / 8.0;
            int step = 1 + this.fieldFrenzyCounter / 8;
            for (int q = 0; q < 180 / step; q++) {
                double radians = Math.toRadians(q * 2 * step);
                BlockPos pos = BlockPos.containing(Mth.floor(this.getX()) + radius * Math.cos(radians), this.getY(), Mth.floor(this.getZ()) + radius * Math.sin(radians));
                if (server.isEmptyBlock(pos) && server.getBlockState(pos.below()).isSolidRender()) {
                    server.setBlockAndUpdate(pos, TCBlocks.EFFECT_SAP.get().defaultBlockState());
                    server.scheduleTick(pos, TCBlocks.EFFECT_SAP.get(), SAP_TICK_MIN + this.random.nextInt(SAP_TICK_SPREAD));
                    if (this.random.nextFloat() < 0.3F) {
                        Effects.arcBolt(server, this.position().add(0.0, this.getBbHeight() / 2.0, 0.0)).to(Vec3.atCenterOf(pos)).color(ARC_COLOR).send();
                    }
                }
            }
            this.playSound(TCSounds.ZAP.get(), 1.0F, 0.9F + this.random.nextFloat() * 0.1F);
        }
        this.fieldFrenzyCounter--;
    }

    private void teleportHome(ServerLevel server) {
        BlockPos home = this.hasHome() ? this.getHomePosition() : this.blockPosition();
        double oldX = this.getX();
        double oldY = this.getY();
        double oldZ = this.getZ();
        int x = home.getX();
        int y = home.getY();
        int z = home.getZ();
        boolean placed = false;
        for (int attempt = 0; attempt < TELEPORT_TRIES; attempt++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (server.getBlockState(pos.below()).blocksMotion() && !server.getBlockState(pos).blocksMotion()) {
                placed = true;
                break;
            }
            x = home.getX() + this.random.nextInt(8) - this.random.nextInt(8);
            z = home.getZ() + this.random.nextInt(8) - this.random.nextInt(8);
        }
        if (!placed) {
            return;
        }
        this.setPos(x + 0.5, y + 0.1, z + 0.5);
        if (!server.noCollision(this, this.getBoundingBox())) {
            this.setPos(oldX, oldY, oldZ);
            return;
        }
        for (int i = 0; i < 128; i++) {
            double t = i / 127.0;
            server.sendParticles(ParticleTypes.PORTAL, oldX + (this.getX() - oldX) * t + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2.0,
                    oldY + (this.getY() - oldY) * t + this.random.nextDouble() * this.getBbHeight(), oldZ + (this.getZ() - oldZ) * t + (this.random.nextDouble() - 0.5) * this.getBbWidth() * 2.0, 1,
                    0.0, 0.0, 0.0, 0.0);
        }
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.0F);
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return this.fieldFrenzyCounter > 0 || super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypes.DROWN) || source.is(DamageTypes.WITHER) || source.is(DamageTypeTags.WITHER_IMMUNE_TO)) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && !this.fieldFrenzy && this.getAbsorptionAmount() <= 0.0F) {
            this.fieldFrenzy = true;
            this.fieldFrenzyCounter = FRENZY_TICKS;
        }
        return hurt;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        this.spawnTimer = SPAWN_TICKS;
        this.setTitle(this.random.nextInt(TITLES.length));
        this.setAbsorptionAmount(this.getAbsorptionAmount() + shieldCap());
        ChampionHelper.makeChampion(this, true);
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (this.random.nextFloat() > 1.0F - ORB_CHANCE) {
            EntityEldritchOrb blast = new EntityEldritchOrb(this.level(), this);
            this.lastBlast = !this.lastBlast;
            this.level().broadcastEntityEvent(this, this.lastBlast ? ARM_RIGHT_EVENT : ARM_LEFT_EVENT);
            int rr = this.lastBlast ? 90 : 180;
            double xx = Mth.cos((this.getYRot() + rr) % 360.0F / 180.0F * Mth.PI) * 0.5F;
            double zz = Mth.sin((this.getYRot() + rr) % 360.0F / 180.0F * Mth.PI) * 0.5F;
            blast.setPos(blast.getX() - xx, blast.getY() - 0.13, blast.getZ() - zz);
            double dx = target.getX() + target.getDeltaMovement().x - this.getX();
            double dy = target.getY() - this.getY() - target.getBbHeight() / 2.0F;
            double dz = target.getZ() + target.getDeltaMovement().z - this.getZ();
            blast.shoot(dx, dy, dz, 1.0F, 2.0F);
            this.playSound(TCSounds.EGATTACK.get(), 2.0F, 1.0F + this.random.nextFloat() * 0.1F);
            this.level().addFreshEntity(blast);
        } else if (this.hasLineOfSight(target)) {
            target.push(-Mth.sin(this.getYRot() * Mth.PI / 180.0F) * SCREECH_FLING, 0.1, Mth.cos(this.getYRot() * Mth.PI / 180.0F) * SCREECH_FLING);
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, SCREECH_EFFECT_TICKS, 0));
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SCREECH_EFFECT_TICKS, 0));
            if (target instanceof ServerPlayer player) {
                WarpHelper.addWarp(player, SCREECH_WARP_BASE + this.random.nextInt(3), WarpType.TEMPORARY);
            }
            this.playSound(TCSounds.EGSCREECH.get(), 4.0F, 1.0F + this.random.nextFloat() * 0.1F);
        }
    }

    @Override
    public void handleEntityEvent(byte event) {
        switch (event) {
            case ARM_LEFT_EVENT -> this.armLiftL = 0.5F;
            case ARM_RIGHT_EVENT -> this.armLiftR = 0.5F;
            case FRENZY_EVENT -> {
                this.armLiftL = 0.9F;
                this.armLiftR = 0.9F;
            }
            case SPAWN_EVENT -> this.spawnTimer = SPAWN_TICKS;
            default -> super.handleEntityEvent(event);
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof EntityEldritchGuardian) && super.canAttack(target);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.EGIDLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.EGDEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 500;
    }
}
