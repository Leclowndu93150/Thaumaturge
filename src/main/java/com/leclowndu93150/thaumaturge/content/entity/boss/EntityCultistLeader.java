package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.content.entity.EntityCultist;
import com.leclowndu93150.thaumaturge.content.entity.EntityGolemOrb;
import com.leclowndu93150.thaumaturge.content.entity.ai.CultistHurtByTargetGoal;
import com.leclowndu93150.thaumaturge.content.entity.ai.LongRangeAttackGoal;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionModifier;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.providers.VanillaEnchantmentProviders;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public class EntityCultistLeader extends EntityThaumaturgeBoss implements RangedAttackMob {
    private static final EntityDataAccessor<Byte> DATA_TITLE = SynchedEntityData.defineId(EntityCultistLeader.class, EntityDataSerializers.BYTE);

    private static final String[] TITLES = {"Alberic", "Anselm", "Bastian", "Beturian", "Chabier", "Chorache", "Chuse", "Dodorol", "Ebardo", "Ferrando", "Fertus", "Guillen", "Larpe", "Obano",
            "Zelipe"};
    private static final int LEADER_XP = 40;
    private static final float ORB_SPEED = 0.66F;
    private static final float ORB_SPREAD = 3.0F;
    private static final double AURA_RANGE = 8.0;
    private static final int AURA_REGEN_TICKS = 60;
    private static final int AURA_REGEN_AMPLIFIER = 1;
    private static final float WEAPON_ENCHANT_CHANCE = 0.5F;

    public EntityCultistLeader(EntityType<? extends EntityCultistLeader> type, Level level) {
        super(type, level);
        this.xpReward = LEADER_XP;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes().add(Attributes.MOVEMENT_SPEED, 0.32).add(Attributes.MAX_HEALTH, 150.0).add(Attributes.ATTACK_DAMAGE, 5.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(2, new LongRangeAttackGoal(this, 16.0, 1.0, 30, 40, 24.0F));
        this.goalSelector.addGoal(3, new MeleeAttackGoal(this, 1.1, false));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(7, new RandomStrollGoal(this, 0.8));
        this.goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new CultistHurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_TITLE, (byte) 0);
    }

    @Override
    public void generateName() {
        int mod = ChampionHelper.championType(this);
        if (mod >= 0) {
            this.setCustomName(Component.translatable("entity.thaumaturge.cultist_leader.name.custom", getTitle(), Component.translatable("champion.mod." + ChampionModifier.MODS.get(mod).name())));
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

    private void equipPraetorGear() {
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(TCItems.CRIMSON_PRAETOR_HELM.get()));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(TCItems.CRIMSON_PRAETOR_CHEST.get()));
        this.setItemSlot(EquipmentSlot.LEGS, new ItemStack(TCItems.CRIMSON_PRAETOR_LEGS.get()));
        this.setItemSlot(EquipmentSlot.FEET, new ItemStack(TCItems.CRIMSON_BOOTS.get()));
        if (this.level().getDifficulty() == Difficulty.EASY) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TCItems.VOID_SWORD.get()));
        } else {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(TCItems.CRIMSON_BLADE.get()));
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData data) {
        this.equipPraetorGear();
        float clamped = difficulty.getSpecialMultiplier();
        ItemStack weapon = this.getMainHandItem();
        if (!weapon.isEmpty() && this.random.nextFloat() < WEAPON_ENCHANT_CHANCE * clamped) {
            EnchantmentHelper.enchantItemFromProvider(weapon, level.registryAccess(), VanillaEnchantmentProviders.MOB_SPAWN_EQUIPMENT, difficulty, this.random);
        }
        this.setTitle(this.random.nextInt(TITLES.length));
        ChampionHelper.makeChampion(this, true);
        return super.finalizeSpawn(level, difficulty, reason, data);
    }

    @Override
    public boolean considersEntityAsAlly(Entity other) {
        return other instanceof EntityCultist || other instanceof EntityCultistLeader || super.considersEntityAsAlly(other);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof EntityCultist) && !(target instanceof EntityCultistLeader) && super.canAttack(target);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        this.spawnAtLocation(level, new ItemStack(TCItems.LOOT_BAG_RARE.get()), 1.5F);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        for (EntityCultist cultist : level.getEntitiesOfClass(EntityCultist.class, this.getBoundingBox().inflate(AURA_RANGE))) {
            if (cultist.getEffect(MobEffects.REGENERATION) == null) {
                cultist.addEffect(new MobEffectInstance(MobEffects.REGENERATION, AURA_REGEN_TICKS, AURA_REGEN_AMPLIFIER));
            }
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (!this.hasLineOfSight(target)) {
            return;
        }
        this.swing(this.getUsedItemHand());
        this.getLookControl().setLookAt(target.getX(), target.getBoundingBox().minY + target.getBbHeight() / 2.0F, target.getZ(), 30.0F, 30.0F);
        EntityGolemOrb blast = new EntityGolemOrb(this.level(), this, target, true);
        blast.setPos(blast.getX() + blast.getDeltaMovement().x / 2.0, blast.getY(), blast.getZ() + blast.getDeltaMovement().z / 2.0);
        double dx = target.getX() - this.getX();
        double dy = target.getBoundingBox().minY + target.getBbHeight() / 2.0F - (this.getY() + this.getBbHeight() / 2.0F);
        double dz = target.getZ() - this.getZ();
        blast.shoot(dx, dy + 2.0, dz, ORB_SPEED, ORB_SPREAD);
        this.playSound(TCSounds.EGATTACK.get(), 1.0F, 1.0F + this.random.nextFloat() * 0.1F);
        this.level().addFreshEntity(blast);
    }
}
