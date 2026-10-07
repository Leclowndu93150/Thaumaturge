package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.api.warp.WarpType;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchGuardian;
import com.leclowndu93150.thaumaturge.content.entity.EntityEldritchOrb;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.eldritch.CastingArms;
import com.leclowndu93150.thaumaturge.content.entity.eldritch.LeadingAim;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitNames;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityEldritchWarden extends EntityThaumaturgeBoss implements RangedAttackMob {
    private static final BossTitles TITLES = new BossTitles("entity.thaumaturge.eldritch_warden.name.custom", List.of("Aphoom-Zhah", "Basatan", "Chaugnar Faugn", "Mnomquah", "Nyogtha", "Oorn",
            "Shaikorth", "Rhan-Tegoth", "Rhogog", "Shudde M'ell", "Vulthoom", "Yag-Kosha", "Yibb-Tstll", "Zathog", "Zushakon"));
    private static final byte EMERGE_EVENT = 18;
    private static final double HEALTH = 400.0;
    private static final double SPEED = 0.33;
    private static final double STRIKE = 10.0;
    private static final double PLATING = 4.0;
    private static final double VOLLEY_MIN_DISTANCE = 3.0;
    private static final double VOLLEY_PACE = 1.0;
    private static final int VOLLEY_MIN_DELAY = 20;
    private static final int VOLLEY_MAX_DELAY = 40;
    private static final float VOLLEY_RANGE = 24.0F;
    private static final double CHARGE_PACE = 1.1;
    private static final double HOMING_PACE = 0.8;
    private static final double STROLL_PACE = 1.0;
    private static final float GAZE_RANGE = 8.0F;
    private static final int EMERGENCE = 150;
    private static final int SHROUD_INTERVAL = 4;
    private static final int SHROUD_SPIRALS = 33;
    private static final int SHROUD_COLOR = 2232623;
    private static final double SAP_FOOTPRINT = 0.25;
    private static final float ORB_CHANCE = 0.8F;
    private static final double HAND_DROP = 0.13;
    private static final float ORB_SPEED = 1.0F;
    private static final float ORB_SPREAD = 2.0F;
    private static final float ORB_VOLUME = 2.0F;
    private static final float SCREECH_FLING = 1.5F;
    private static final double FLING_RISE = 0.1;
    private static final int SCREECH_TICKS = 400;
    private static final int SCREECH_WARP = 3;
    private static final int SCREECH_WARP_SPREAD = 3;
    private static final float SCREECH_VOLUME = 4.0F;
    private static final int CALL_INTERVAL = 500;

    private final CastingArms arms = new CastingArms(this);
    private final WardenShield shield = new WardenShield(this);
    private final FieldFrenzy frenzy = new FieldFrenzy(this, arms);
    private int title;

    public EntityEldritchWarden(EntityType<? extends EntityEldritchWarden> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.MAX_ABSORPTION, WardenShield.capacityFor(HEALTH)).add(Attributes.MOVEMENT_SPEED, SPEED)
                .add(Attributes.ATTACK_DAMAGE, STRIKE).add(Attributes.ARMOR, PLATING);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new LongRangeAttackGoal(this, VOLLEY_MIN_DISTANCE, VOLLEY_PACE, VOLLEY_MIN_DELAY, VOLLEY_MAX_DELAY, VOLLEY_RANGE));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, CHARGE_PACE, false));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, HOMING_PACE));
        goalSelector.addGoal(7, new RandomStrollGoal(this, STROLL_PACE));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, GAZE_RANGE));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, EntityCultist.class, true));
    }

    public CastingArms arms() {
        return arms;
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        spawnTimer = EMERGENCE;
        title = TITLES.roll(random);
        shield.raise();
        ChampionHelper.makeChampion(this, true);
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public void generateName() {
        MobTraits.champion(this).ifPresent(trait -> setCustomName(TITLES.name(title, MobTraitNames.of(trait))));
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        shield.show(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        shield.hide(player);
    }

    @Override
    public void tick() {
        if (getSpawnTimer() == EMERGENCE && !level().isClientSide()) {
            level().broadcastEntityEvent(this, EMERGE_EVENT);
        }
        super.tick();
        if (level().isClientSide()) {
            arms.relax();
        }
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        if (frenzy.active()) {
            bossEvent.setProgress(getHealth() / getMaxHealth());
        } else {
            super.customServerAiStep(level);
        }
        shield.tick();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel server)) {
            return;
        }
        seepSap(server);
        if (getSpawnTimer() > 0 && tickCount % SHROUD_INTERVAL == 0) {
            shroud(server);
        }
        if (frenzy.active()) {
            frenzy.tick(server);
        }
    }

    private void seepSap(ServerLevel level) {
        BlockState sap = TTBlocks.EFFECT_SAP.get().defaultBlockState();
        for (int corner = 0; corner < 4; corner++) {
            double sideX = corner % 2 == 0 ? -SAP_FOOTPRINT : SAP_FOOTPRINT;
            double sideZ = corner / 2 == 0 ? -SAP_FOOTPRINT : SAP_FOOTPRINT;
            BlockPos spot = BlockPos.containing(getX() + sideX, getY(), getZ() + sideZ);
            if (level.isEmptyBlock(spot)) {
                level.setBlockAndUpdate(spot, sap);
            }
        }
    }

    private void shroud(ServerLevel level) {
        float height = Math.max(1.0F, getBbHeight() * ((EMERGENCE - getSpawnTimer()) / (float) EMERGENCE));
        Vec3 centre = position().add(0.0, height / 2.0F, 0.0);
        int floor = Mth.floor(getBoundingBox().minY) - 1;
        for (int spiral = 0; spiral < SHROUD_SPIRALS; spiral++) {
            Effects.smokeSpiral(level, centre).radius(height).start(random.nextInt(360)).minY(floor).color(SHROUD_COLOR).send();
        }
    }

    @Override
    public boolean isInvulnerableTo(ServerLevel level, DamageSource source) {
        return frenzy.active() || super.isInvulnerableTo(level, source);
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (source.is(DamageTypes.DROWN) || source.is(DamageTypes.WITHER) || source.is(DamageTypeTags.WITHER_IMMUNE_TO)) {
            return false;
        }
        boolean hurt = super.hurtServer(level, source, damage);
        if (hurt && shield.broken()) {
            frenzy.ignite();
        }
        return hurt;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (random.nextFloat() > 1.0F - ORB_CHANCE) {
            launchOrb(target);
        } else if (hasLineOfSight(target)) {
            screech(target);
        }
    }

    private void launchOrb(LivingEntity target) {
        EntityEldritchOrb orb = new EntityEldritchOrb(level(), this);
        Vec3 hand = arms.castFromNextHand();
        orb.setPos(orb.getX() + hand.x, orb.getY() - HAND_DROP, orb.getZ() + hand.z);
        Vec3 aim = LeadingAim.at(this, target);
        orb.shoot(aim.x, aim.y, aim.z, ORB_SPEED, ORB_SPREAD);
        playSound(TTSounds.EGATTACK.get(), ORB_VOLUME, 1.0F + random.nextFloat() * 0.1F);
        level().addFreshEntity(orb);
    }

    private void screech(LivingEntity target) {
        float bearing = getYRot() * Mth.DEG_TO_RAD;
        target.push(-Mth.sin(bearing) * SCREECH_FLING, FLING_RISE, Mth.cos(bearing) * SCREECH_FLING);
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, SCREECH_TICKS, 0));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SCREECH_TICKS, 0));
        if (target instanceof ServerPlayer player) {
            WarpHelper.addWarp(player, SCREECH_WARP + random.nextInt(SCREECH_WARP_SPREAD), WarpType.TEMPORARY);
        }
        playSound(TTSounds.EGSCREECH.get(), SCREECH_VOLUME, 1.0F + random.nextFloat() * 0.1F);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == EMERGE_EVENT) {
            spawnTimer = EMERGENCE;
        } else if (!arms.receive(event)) {
            super.handleEntityEvent(event);
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof EntityEldritchGuardian) && super.canAttack(target);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.EGIDLE.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.EGDEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return CALL_INTERVAL;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte(BossTitles.SAVE_KEY, (byte) title);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        title = TITLES.clamp(input.getByteOr(BossTitles.SAVE_KEY, (byte) 0));
    }
}
