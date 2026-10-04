package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemOrb;
import com.leclowndu93150.thaumaturge.content.entity.ai.CultistHurtByTargetGoal;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitNames;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityCultistLeader extends EntityThaumaturgeBoss implements RangedAttackMob {
    private static final BossTitles TITLES = new BossTitles("entity.thaumaturge.cultist_leader.name.custom",
            List.of("Alberic", "Anselm", "Bastian", "Beturian", "Chabier", "Chorache", "Chuse", "Dodorol", "Ebardo", "Ferrando", "Fertus", "Guillen", "Larpe", "Obano", "Zelipe"));
    private static final Map<EquipmentSlot, Supplier<? extends Item>> PRAETOR_KIT = Map.of(EquipmentSlot.HEAD, TCItems.CRIMSON_PRAETOR_HELM, EquipmentSlot.CHEST, TCItems.CRIMSON_PRAETOR_CHEST,
            EquipmentSlot.LEGS, TCItems.CRIMSON_PRAETOR_LEGS, EquipmentSlot.FEET, TCItems.CRIMSON_BOOTS);
    private static final int EXPERIENCE = 40;
    private static final double SPEED = 0.32;
    private static final double HEALTH = 150.0;
    private static final double STRIKE = 5.0;
    private static final double VOLLEY_PACE = 1.0;
    private static final int VOLLEY_MIN_DELAY = 30;
    private static final int VOLLEY_MAX_DELAY = 40;
    private static final double VOLLEY_MIN_DISTANCE = 16.0;
    private static final float VOLLEY_RANGE = 24.0F;
    private static final double CHARGE_PACE = 1.1;
    private static final double WANDER_PACE = 0.8;
    private static final float GAZE_RANGE = 8.0F;
    private static final float ORB_SPEED = 0.66F;
    private static final float ORB_SPREAD = 3.0F;
    private static final double ORB_LOFT = 2.0;
    private static final float AIM_TURN = 30.0F;
    private static final double RALLY_RADIUS = 8.0;
    private static final int RALLY_DURATION = 60;
    private static final int RALLY_AMPLIFIER = 1;
    private static final float BLADE_ENCHANT_CHANCE = 0.5F;

    private int title;

    public EntityCultistLeader(EntityType<? extends EntityCultistLeader> type, Level level) {
        super(type, level);
        this.xpReward = EXPERIENCE;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MOVEMENT_SPEED, SPEED).add(Attributes.MAX_HEALTH, HEALTH).add(Attributes.ATTACK_DAMAGE, STRIKE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new LongRangeAttackGoal(this, VOLLEY_MIN_DISTANCE, VOLLEY_PACE, VOLLEY_MIN_DELAY, VOLLEY_MAX_DELAY, VOLLEY_RANGE));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, CHARGE_PACE, false));
        goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, WANDER_PACE));
        goalSelector.addGoal(7, new RandomStrollGoal(this, WANDER_PACE));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, GAZE_RANGE));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new CultistHurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        PRAETOR_KIT.forEach((slot, item) -> setItemSlot(slot, new ItemStack(item.get())));
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(level.getDifficulty() == Difficulty.EASY ? TCItems.VOID_SWORD.get() : TCItems.CRIMSON_BLADE.get()));
        if (random.nextFloat() < BLADE_ENCHANT_CHANCE * difficulty.getSpecialMultiplier()) {
            EnchantmentHelper.enchantItemFromProvider(getMainHandItem(), level.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficulty, random);
        }
        title = TITLES.roll(random);
        ChampionHelper.makeChampion(this, true);
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public void generateName() {
        MobTraits.champion(this).ifPresent(trait -> setCustomName(TITLES.name(title, MobTraitNames.of(trait))));
    }

    @Override
    public boolean considersEntityAsAlly(Entity other) {
        return isBrother(other) || super.considersEntityAsAlly(other);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !isBrother(target) && super.canAttack(target);
    }

    private static boolean isBrother(Entity entity) {
        return entity instanceof EntityCultist || entity instanceof EntityCultistLeader;
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        for (EntityCultist follower : level.getEntitiesOfClass(EntityCultist.class, getBoundingBox().inflate(RALLY_RADIUS), follower -> !follower.hasEffect(MobEffects.REGENERATION))) {
            follower.addEffect(new MobEffectInstance(MobEffects.REGENERATION, RALLY_DURATION, RALLY_AMPLIFIER));
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (!hasLineOfSight(target)) {
            return;
        }
        Vec3 heart = target.getBoundingBox().getCenter();
        swing(getUsedItemHand());
        getLookControl().setLookAt(heart.x, heart.y, heart.z, AIM_TURN, AIM_TURN);
        EntityGolemOrb orb = new EntityGolemOrb(level(), this, target, true);
        orb.setPos(orb.getX() + orb.getDeltaMovement().x / 2.0, orb.getY(), orb.getZ() + orb.getDeltaMovement().z / 2.0);
        Vec3 aim = heart.subtract(position().add(0.0, getBbHeight() / 2.0F, 0.0));
        orb.shoot(aim.x, aim.y + ORB_LOFT, aim.z, ORB_SPEED, ORB_SPREAD);
        playSound(TCSounds.EGATTACK.get(), 1.0F, 1.0F + random.nextFloat() * 0.1F);
        level().addFreshEntity(orb);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        spawnAtLocation(level, new ItemStack(TCItems.LOOT_BAG_RARE.get()), 1.5F);
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
