package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.entity.ai.HoldsStill;
import com.leclowndu93150.thaumaturge.content.entity.ai.ItemCollector;
import com.leclowndu93150.thaumaturge.content.pech.MenuPech;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class EntityPech extends Monster implements RangedAttackMob, HoldsStill, ItemCollector {
    public static final String DROPPED_BY_PECH_TAG = "thaumaturge:released_by_pech";
    private static final String LEGACY_DROPPED_BY_PECH_TAG = "PechDrop";

    public static final int TYPE_FORAGER = 0;
    public static final int TYPE_MAGE = 1;
    public static final int TYPE_STALKER = 2;
    public static final int LOOT_SLOTS = 9;

    private static final EntityDataAccessor<Byte> DATA_VARIANT =
            SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_RAGE =
            SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_DOMESTICATED =
            SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.BOOLEAN);

    private static final int HEAL_INTERVAL_TICKS = 40;
    private static final int MIN_LOOT_TO_STAY = 5;
    private static final float LOOT_DROP_CHANCE = 0.33F;
    private static final int MAX_NEARBY_PECHS = 4;

    public NonNullList<ItemStack> loot = PechHoard.empty();
    public boolean atTradeTable;
    public float chatterLevel;
    private @Nullable Goal combatGoal;
    private int chargeCooldown;

    public EntityPech(EntityType<? extends EntityPech> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.ATTACK_DAMAGE, 6.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.ARMOR, 2.0);
    }

    public static boolean checkPechSpawnRules(
            EntityType<EntityPech> type,
            ServerLevelAccessor level,
            MobSpawnType reason,
            BlockPos pos,
            RandomSource random) {
        int count = level.getEntitiesOfClass(EntityPech.class, new AABB(pos).inflate(16.0, 16.0, 16.0))
                .size();
        return !level.getBiome(pos).is(TTBiomeTags.IS_TAINTED)
                && count < MAX_NEARBY_PECHS
                && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        PechGoals.register(this, this.goalSelector, this.targetSelector);
        this.refreshCombatGoal();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_DOMESTICATED, false);
        entityData.define(DATA_RAGE, 0);
        entityData.define(DATA_VARIANT, (byte) TYPE_FORAGER);
    }

    public boolean isDomesticated() {
        return this.entityData.get(DATA_DOMESTICATED);
    }

    public void setDomesticated(boolean domesticated) {
        this.entityData.set(DATA_DOMESTICATED, domesticated);
    }

    public int variant() {
        return this.entityData.get(DATA_VARIANT).intValue();
    }

    public void assignVariant(int type) {
        this.entityData.set(DATA_VARIANT, Byte.valueOf((byte) type));
    }

    public int rageTicks() {
        return this.entityData.get(DATA_RAGE).intValue();
    }

    public void setRageTicks(int rage) {
        this.entityData.set(DATA_RAGE, Integer.valueOf(rage));
    }

    @Override
    protected Component getTypeName() {
        return switch (this.variant()) {
            case TYPE_MAGE -> Component.translatable("entity.thaumaturge.pech.mage");
            case TYPE_STALKER -> Component.translatable("entity.thaumaturge.pech.stalker");
            default -> Component.translatable("entity.thaumaturge.pech");
        };
    }

    private static boolean isRangedWeapon(ItemStack held) {
        return held.is(Items.BOW) || held.is(TTItems.PECH_WAND.get());
    }

    private Goal createCombatGoal() {
        if (!isRangedWeapon(this.getMainHandItem())) {
            return new MeleeAttackGoal(this, 0.6, false);
        }
        return new RangedAttackGoal(this, 0.6, 20, 50, 15.0F);
    }

    public void refreshCombatGoal() {
        if (this.level().isClientSide()) {
            return;
        }
        Goal stale = this.combatGoal;
        if (stale != null) {
            this.goalSelector.removeGoal(stale);
        }
        Goal fresh = createCombatGoal();
        this.goalSelector.addGoal(PechGoals.COMBAT_PRIORITY, fresh);
        this.combatGoal = fresh;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (this.variant() == TYPE_STALKER) {
            PechAttacks.shootArrow(this, target, velocity);
        } else if (this.variant() == TYPE_MAGE) {
            PechAttacks.castSpell(this, target);
        }
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        if (!this.level().isClientSide() && slot == EquipmentSlot.MAINHAND) {
            this.refreshCombatGoal();
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType reason,
            @Nullable SpawnGroupData groupData) {
        RandomSource random = level.getRandom();
        ItemStack held = new ItemStack(PechLoadout.roll(random));
        boolean wand = held.is(TTItems.PECH_WAND.get());
        this.setItemSlot(EquipmentSlot.MAINHAND, held);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.2F);
        this.setDropChance(EquipmentSlot.MAINHAND, wand ? 0.1F : 0.2F);
        if (wand) {
            this.assignVariant(TYPE_MAGE);
        } else if (held.is(Items.BOW)) {
            this.assignVariant(TYPE_STALKER);
        }
        if (!wand && !held.isEmpty()) {
            this.populateDefaultEquipmentEnchantments(level, random, difficulty);
        }
        this.setCanPickUpLoot(random.nextFloat() < 0.75F * difficulty.getSpecialMultiplier());
        this.refreshCombatGoal();
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    public float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return TTSounds.PECH_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TTSounds.PECH_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.PECH_DEATH.get();
    }

    @Override
    public void playAmbientSound() {
        if (this.level() instanceof ServerLevel level) {
            if (this.getRandom().nextInt(3) == 0
                    && !level.getEntitiesOfClass(
                                    EntityPech.class,
                                    this.getBoundingBox().inflate(4.0, 2.0, 4.0),
                                    peer -> peer != this)
                            .isEmpty()) {
                level.broadcastEntityEvent(this, PechMoods.EVENT_TRADE);
                this.playSound(TTSounds.PECH_TRADE.get(), this.getSoundVolume(), this.getVoicePitch());
                return;
            }
            level.broadcastEntityEvent(this, PechMoods.EVENT_IDLE);
        }
        super.playAmbientSound();
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (this.level() instanceof ServerLevel level
                && !this.isInvulnerableTo(source)
                && source.getEntity() instanceof Player player) {
            PechTemper.raiseAlarm(this, level, player);
        }
        return super.hurt(source, damage);
    }

    @Override
    public void tick() {
        PechMoods.decay(this);
        super.tick();
        if (this.level().isClientSide()) {
            PechMoods.tickClient(this);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (this.tickCount % HEAL_INTERVAL_TICKS == 0) {
            this.heal(1.0F);
        }
        int rage = PechTemper.decayRage(this);
        if (rage > 0 && this.getTarget() != null) {
            PechTemper.tickCharge(this);
            this.level().broadcastEntityEvent(this, PechMoods.EVENT_TRADE);
        }
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (!PechMoods.handleEvent(this, event)) {
            super.handleEntityEvent(event);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return PechHoard.filledSlots(this.loot) < MIN_LOOT_TO_STAY;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean hitByPlayer) {
        super.dropCustomDeathLoot(level, source, hitByPlayer);
        for (ItemStack stack : this.loot) {
            if (!stack.isEmpty() && this.getRandom().nextFloat() < LOOT_DROP_CHANCE) {
                ItemEntity dropped = this.spawnAtLocation(stack.copy(), 1.5F);
                if (dropped != null) {
                    dropped.addTag(DROPPED_BY_PECH_TAG);
                }
            }
        }
    }

    @Override
    public boolean holdingStill() {
        return this.isDomesticated() && this.atTradeTable;
    }

    @Override
    public boolean wantsToCollect(ItemEntity item) {
        return !item.getTags().contains(DROPPED_BY_PECH_TAG)
                && !item.getTags().contains(LEGACY_DROPPED_BY_PECH_TAG)
                && this.wouldTake(item.getItem());
    }

    public boolean wouldTake(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        return (!this.isDomesticated() && this.isPrizedItem(stack)) || PechHoard.canStore(this.loot, stack);
    }

    @Override
    public ItemStack collect(ItemStack stack) {
        ItemStack rest = stack.copy();
        if (!this.isDomesticated() && this.isPrizedItem(rest)) {
            int value = this.getValue(rest);
            rest.shrink(1);
            if (this.getRandom().nextInt(10) < value) {
                this.level().broadcastEntityEvent(this, PechMoods.EVENT_HAPPY);
                this.setDomesticated(true);
                this.refreshCombatGoal();
            }
            return rest;
        }
        return PechHoard.store(this.loot, rest);
    }

    public boolean isPrizedItem(ItemStack stack) {
        return stack.is(Items.ENDER_PEARL)
                || AspectIndexAccess.of(stack)
                                .amountOf(TTAspects.DESIDERIUM, this.level().registryAccess())
                        > 1;
    }

    public int getValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.is(Items.ENDER_PEARL)) {
            return 15;
        }
        int desiderium = AspectIndexAccess.of(stack)
                .amountOf(TTAspects.DESIDERIUM, this.level().registryAccess());
        return Math.min(32, desiderium / 2);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() || held.is(Items.NAME_TAG)) {
            return super.mobInteract(player, hand);
        }
        if (this.isDomesticated()) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(
                        new SimpleMenuProvider(
                                (id, inventory, p) -> new MenuPech(id, inventory, this), this.getDisplayName()),
                        buf -> buf.writeVarInt(this.getId()));
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        HolderLookup.Provider registries = registryAccess();
        super.addAdditionalSaveData(output);
        PechSavedState.save(output, registries, this.loot, this.isDomesticated(), this.rageTicks(), this.variant());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        HolderLookup.Provider registries = registryAccess();
        super.readAdditionalSaveData(input);
        PechSavedState.State saved = PechSavedState.load(input, registries);
        this.loot = saved.loot();
        this.setDomesticated(saved.domesticated());
        this.setRageTicks(saved.rageTicks());
        this.assignVariant(saved.variant());
        this.refreshCombatGoal();
    }

    private static final class PechTemper {
        private static final int ANGER_MIN = 400;
        private static final int ANGER_SPREAD = 400;
        private static final int CHARGE_INTERVAL = 100;

        private PechTemper() {}

        static void raiseAlarm(EntityPech pech, ServerLevel level, Player culprit) {
            for (EntityPech other : level.getEntitiesOfClass(
                    EntityPech.class, pech.getBoundingBox().inflate(32.0, 16.0, 32.0), peer -> peer != pech)) {
                provoke(other, culprit);
            }
            provoke(pech, culprit);
        }

        private static void provoke(EntityPech pech, Entity culprit) {
            if (culprit instanceof Player player && player.isCreative()) {
                return;
            }
            if (!(culprit instanceof LivingEntity living)) {
                return;
            }
            boolean wasCalm = pech.rageTicks() <= 0;
            if (wasCalm && pech.level() instanceof ServerLevel server) {
                server.broadcastEntityEvent(pech, PechMoods.EVENT_ANGRY);
                pech.playSound(TTSounds.PECH_CHARGE.get(), pech.getSoundVolume(), pech.getVoicePitch());
            }
            pech.setTarget(living);
            pech.setRageTicks(ANGER_MIN + pech.getRandom().nextInt(ANGER_SPREAD));
            pech.setDomesticated(false);
            pech.refreshCombatGoal();
        }

        static void tickCharge(EntityPech pech) {
            pech.chargeCooldown = Math.max(pech.chargeCooldown - 1, 0);
            if (pech.chargeCooldown == 0) {
                pech.chargeCooldown = CHARGE_INTERVAL;
                pech.playSound(TTSounds.PECH_CHARGE.get(), pech.getSoundVolume(), pech.getVoicePitch());
            }
        }

        static int decayRage(EntityPech pech) {
            int rage = pech.rageTicks();
            if (rage > 0) {
                rage--;
                pech.setRageTicks(rage);
            }
            return rage;
        }
    }
}
