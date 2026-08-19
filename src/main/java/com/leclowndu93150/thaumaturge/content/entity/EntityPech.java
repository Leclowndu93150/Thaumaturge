package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.casters.CastStreams;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.content.entity.ai.PechItemGoal;
import com.leclowndu93150.thaumaturge.content.entity.ai.PechTradeGoal;
import com.leclowndu93150.thaumaturge.content.pech.MenuPech;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
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
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.arrow.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public class EntityPech extends Monster implements RangedAttackMob {
    public static final int TYPE_FORAGER = 0;
    public static final int TYPE_MAGE = 1;
    public static final int TYPE_STALKER = 2;
    public static final int LOOT_SLOTS = 9;

    private static final EntityDataAccessor<Byte> DATA_TYPE = SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_ANGER = SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_TAMED = SynchedEntityData.defineId(EntityPech.class, EntityDataSerializers.BOOLEAN);

    private static final byte MUMBLE_EVENT = 16;
    private static final byte TRADE_MUMBLE_EVENT = 17;
    private static final byte TAME_EVENT = 18;
    private static final byte ANGER_EVENT = 19;
    private static final int ANGER_BASE_TICKS = 400;
    private static final int CHARGE_SOUND_INTERVAL = 100;
    private static final int HEAL_INTERVAL_TICKS = 40;
    private static final int MIN_LOOT_TO_STAY = 5;
    private static final float LOOT_DROP_CHANCE = 0.33F;
    private static final float POISON_ARROW_CHANCE = 0.2F;
    private static final int PECH_ENDER_PEARL_VALUE = 15;
    private static final int MAX_ASPECT_VALUE = 32;
    private static final float MAGE_BLAST_OFFSET_DIVISOR = 6.0F;
    private static final int MAX_NEARBY_PECHS = 4;

    public NonNullList<ItemStack> loot = NonNullList.withSize(LOOT_SLOTS, ItemStack.EMPTY);
    public boolean trading;
    public float mumble;
    private int chargeCount;

    private final RangedAttackGoal arrowAttackGoal = new RangedAttackGoal(this, 0.6, 20, 50, 15.0F);
    private final RangedAttackGoal blastAttackGoal = new RangedAttackGoal(this, 0.6, 20, 50, 15.0F);
    private final MeleeAttackGoal meleeAttackGoal = new MeleeAttackGoal(this, 0.6, false);
    private final AvoidEntityGoal<Player> avoidPlayerGoal = new AvoidEntityGoal<>(this, Player.class, 8.0F, 0.5, 0.6);

    public EntityPech(EntityType<? extends EntityPech> type, Level level) {
        super(type, level);
        this.xpReward = 8;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 30.0).add(Attributes.ATTACK_DAMAGE, 6.0).add(Attributes.MOVEMENT_SPEED, 0.5).add(Attributes.ARMOR, 2.0);
    }

    public static boolean checkPechSpawnRules(EntityType<EntityPech> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        int count = level.getEntitiesOfClass(EntityPech.class, new AABB(pos).inflate(16.0, 16.0, 16.0)).size();
        return count < MAX_NEARBY_PECHS && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new PechTradeGoal(this));
        this.goalSelector.addGoal(3, new PechItemGoal(this));
        this.goalSelector.addGoal(5, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(6, new MoveTowardsRestrictionGoal(this, 0.5));
        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 0.6));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, LivingEntity.class, 8.0F));
        this.goalSelector.addGoal(11, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 10, true, false, (target, level) -> this.getAnger() > 0));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(DATA_TYPE, (byte) TYPE_FORAGER);
        entityData.define(DATA_ANGER, 0);
        entityData.define(DATA_TAMED, false);
    }

    public int getPechType() {
        return this.entityData.get(DATA_TYPE);
    }

    public void setPechType(int type) {
        this.entityData.set(DATA_TYPE, (byte) type);
    }

    public int getAnger() {
        return this.entityData.get(DATA_ANGER);
    }

    public void setAnger(int anger) {
        this.entityData.set(DATA_ANGER, anger);
    }

    public boolean isTamed() {
        return this.entityData.get(DATA_TAMED);
    }

    public void setTamed(boolean tamed) {
        this.entityData.set(DATA_TAMED, tamed);
    }

    @Override
    protected Component getTypeName() {
        return switch (this.getPechType()) {
            case TYPE_MAGE -> Component.translatable("entity.thaumaturge.pech.mage");
            case TYPE_STALKER -> Component.translatable("entity.thaumaturge.pech.stalker");
            default -> Component.translatable("entity.thaumaturge.pech");
        };
    }

    public void setCombatTask() {
        if (this.level().isClientSide()) {
            return;
        }
        this.goalSelector.removeGoal(this.meleeAttackGoal);
        this.goalSelector.removeGoal(this.arrowAttackGoal);
        this.goalSelector.removeGoal(this.blastAttackGoal);
        ItemStack held = this.getMainHandItem();
        if (held.is(Items.BOW)) {
            this.goalSelector.addGoal(2, this.arrowAttackGoal);
        } else if (held.is(TCItems.PECH_WAND.get())) {
            this.goalSelector.addGoal(2, this.blastAttackGoal);
        } else {
            this.goalSelector.addGoal(2, this.meleeAttackGoal);
        }
        if (this.isTamed()) {
            this.goalSelector.removeGoal(this.avoidPlayerGoal);
        } else {
            this.goalSelector.addGoal(4, this.avoidPlayerGoal);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float velocity) {
        if (this.getPechType() == TYPE_STALKER) {
            ItemStack arrowStack = new ItemStack(Items.ARROW);
            if (this.random.nextFloat() < POISON_ARROW_CHANCE) {
                arrowStack = new ItemStack(Items.TIPPED_ARROW);
                arrowStack.set(DataComponents.POTION_CONTENTS, new PotionContents(Optional.empty(), Optional.empty(), List.of(new MobEffectInstance(MobEffects.POISON, 40)), Optional.empty()));
            }
            Arrow arrow = new Arrow(this.level(), this, arrowStack, null);
            double dx = target.getX() - this.getX();
            double dy = target.getBoundingBox().minY + target.getBbHeight() / 3.0F - arrow.getY();
            double dz = target.getZ() - this.getZ();
            double horizontal = Math.sqrt(dx * dx + dz * dz);
            arrow.shoot(dx, dy + horizontal * 0.2F, dz, 1.6F, 14 - this.level().getDifficulty().getId() * 4);
            arrow.setBaseDamage(velocity * 2.0F + this.random.nextGaussian() * 0.25 + this.level().getDifficulty().getId() * 0.11F);
            this.playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (this.random.nextFloat() * 0.4F + 0.8F));
            this.level().addFreshEntity(arrow);
        } else if (this.getPechType() == TYPE_MAGE) {
            double offset = this.distanceTo(target) / MAGE_BLAST_OFFSET_DIVISOR;
            FocusPackage pack = FocusPackage.builder().caster(this).add(TCIds.rl("projectile"), Map.of("speed", 2)).add(randomMageEffect()).build();
            FocusEngine.cast(this, pack, CastStreams.fromCasterToTarget(this, target, offset));
            this.swing(this.getUsedItemHand());
        }
    }

    private Identifier randomMageEffect() {
        if (this.random.nextBoolean()) {
            return TCIds.rl("curse");
        }
        if (this.random.nextBoolean()) {
            return TCIds.rl("flux");
        }
        if (this.random.nextBoolean()) {
            return TCIds.rl("earth");
        }
        return this.random.nextBoolean() ? TCIds.rl("air") : TCIds.rl("fire");
    }

    @Override
    public void setItemSlot(EquipmentSlot slot, ItemStack stack) {
        super.setItemSlot(slot, stack);
        if (!this.level().isClientSide() && slot == EquipmentSlot.MAINHAND) {
            this.setCombatTask();
        }
    }

    private void rollHeldItem() {
        switch (this.random.nextInt(20)) {
            case 0, 12 -> this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(TCItems.PECH_WAND.get()));
            case 1 -> this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE_SWORD));
            case 2, 4, 10, 11, 13 -> this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
            case 3 -> this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE_AXE));
            case 5 -> this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_SWORD));
            case 6 -> this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_AXE));
            case 7 -> this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.FISHING_ROD));
            case 8 -> this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STONE_PICKAXE));
            case 9 -> this.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_PICKAXE));
            default -> {
            }
        }
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        this.setDropChance(EquipmentSlot.MAINHAND, 0.2F);
        this.setDropChance(EquipmentSlot.OFFHAND, 0.2F);
        this.rollHeldItem();
        ItemStack held = this.getMainHandItem();
        if (held.is(TCItems.PECH_WAND.get())) {
            this.setPechType(TYPE_MAGE);
            this.setDropChance(EquipmentSlot.MAINHAND, 0.1F);
        } else if (!held.isEmpty()) {
            if (held.is(Items.BOW)) {
                this.setPechType(TYPE_STALKER);
            }
            this.populateDefaultEquipmentEnchantments(level, this.random, difficulty);
        }
        float f = difficulty.getSpecialMultiplier();
        this.setCanPickUpLoot(this.random.nextFloat() < 0.75F * f);
        this.setCombatTask();
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Override
    public int getAmbientSoundInterval() {
        return 120;
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return TCSounds.PECH_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.PECH_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.PECH_DEATH.get();
    }

    @Override
    public void playAmbientSound() {
        if (!this.level().isClientSide()) {
            if (this.random.nextInt(3) == 0) {
                for (Entity entity : this.level().getEntities(this, this.getBoundingBox().inflate(4.0, 2.0, 4.0))) {
                    if (entity instanceof EntityPech) {
                        this.level().broadcastEntityEvent(this, TRADE_MUMBLE_EVENT);
                        this.playSound(TCSounds.PECH_TRADE.get(), this.getSoundVolume(), this.getVoicePitch());
                        return;
                    }
                }
            }
            this.level().broadcastEntityEvent(this, MUMBLE_EVENT);
        }
        super.playAmbientSound();
    }

    private void becomeAngryAt(Entity entity) {
        if (entity instanceof Player player && player.isCreative()) {
            return;
        }
        if (!(entity instanceof LivingEntity living)) {
            return;
        }
        if (this.getAnger() <= 0) {
            this.level().broadcastEntityEvent(this, ANGER_EVENT);
            this.playSound(TCSounds.PECH_CHARGE.get(), this.getSoundVolume(), this.getVoicePitch());
        }
        this.setTarget(living);
        this.setAnger(ANGER_BASE_TICKS + this.random.nextInt(ANGER_BASE_TICKS));
        this.setTamed(false);
        this.setCombatTask();
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        if (this.isInvulnerableTo(level, source)) {
            return false;
        }
        Entity attacker = source.getEntity();
        if (attacker instanceof Player) {
            for (EntityPech pech : level.getEntitiesOfClass(EntityPech.class, this.getBoundingBox().inflate(32.0, 16.0, 32.0))) {
                if (pech != this) {
                    pech.becomeAngryAt(attacker);
                }
            }
            this.becomeAngryAt(attacker);
        }
        return super.hurtServer(level, source, damage);
    }

    @Override
    public void tick() {
        if (this.mumble > 0.0F) {
            this.mumble *= 0.75F;
        }
        if (this.getAnger() > 0 && !this.level().isClientSide()) {
            this.setAnger(this.getAnger() - 1);
        }
        if (this.getAnger() > 0 && this.getTarget() != null && !this.level().isClientSide()) {
            if (this.chargeCount > 0) {
                this.chargeCount--;
            }
            if (this.chargeCount == 0) {
                this.chargeCount = CHARGE_SOUND_INTERVAL;
                this.playSound(TCSounds.PECH_CHARGE.get(), this.getSoundVolume(), this.getVoicePitch());
            }
            this.level().broadcastEntityEvent(this, TRADE_MUMBLE_EVENT);
        }
        if (this.level().isClientSide() && this.random.nextInt(15) == 0 && this.getAnger() > 0) {
            this.spawnMoodParticle(ParticleTypes.ANGRY_VILLAGER);
        }
        if (this.level().isClientSide() && this.random.nextInt(25) == 0 && this.isTamed()) {
            this.spawnMoodParticle(ParticleTypes.HAPPY_VILLAGER);
        }
        super.tick();
    }

    private void spawnMoodParticle(ParticleOptions particle) {
        this.level().addParticle(particle, this.getX() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(), this.getY() + 0.5 + this.random.nextFloat() * this.getBbHeight(),
                this.getZ() + this.random.nextFloat() * this.getBbWidth() * 2.0F - this.getBbWidth(), this.random.nextGaussian() * 0.02, this.random.nextGaussian() * 0.02,
                this.random.nextGaussian() * 0.02);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        if (this.tickCount % HEAL_INTERVAL_TICKS == 0) {
            this.heal(1.0F);
        }
    }

    @Override
    public void handleEntityEvent(byte event) {
        switch (event) {
            case MUMBLE_EVENT -> this.mumble = (float) Math.PI;
            case TRADE_MUMBLE_EVENT -> this.mumble = (float) (Math.PI * 2);
            case TAME_EVENT -> {
                for (int i = 0; i < 5; i++) {
                    this.spawnMoodParticle(ParticleTypes.HAPPY_VILLAGER);
                }
            }
            case ANGER_EVENT -> {
                for (int i = 0; i < 5; i++) {
                    this.spawnMoodParticle(ParticleTypes.ANGRY_VILLAGER);
                }
                this.mumble = (float) (Math.PI * 2);
            }
            default -> super.handleEntityEvent(event);
        }
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        int filled = 0;
        for (ItemStack stack : this.loot) {
            if (!stack.isEmpty()) {
                filled++;
            }
        }
        return filled < MIN_LOOT_TO_STAY;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean hitByPlayer) {
        super.dropCustomDeathLoot(level, source, hitByPlayer);
        for (ItemStack stack : this.loot) {
            if (!stack.isEmpty() && this.random.nextFloat() < LOOT_DROP_CHANCE) {
                this.spawnAtLocation(level, stack.copy(), 1.5F);
            }
        }
    }

    public boolean canPickup(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (!this.isTamed() && this.isValued(stack)) {
            return true;
        }
        for (ItemStack slot : this.loot) {
            if (slot.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameComponents(stack, slot) && stack.getCount() + slot.getCount() <= slot.getMaxStackSize()) {
                return true;
            }
        }
        return false;
    }

    public ItemStack pickupItem(ItemStack stack) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (!this.isTamed() && this.isValued(stack)) {
            if (this.random.nextInt(10) < this.getValue(stack)) {
                this.setTamed(true);
                this.setCombatTask();
                this.level().broadcastEntityEvent(this, TAME_EVENT);
            }
            stack.shrink(1);
            return stack.isEmpty() ? ItemStack.EMPTY : stack;
        }
        for (int a = 0; a < this.loot.size(); a++) {
            ItemStack slot = this.loot.get(a);
            if (!stack.isEmpty() && !slot.isEmpty() && slot.getCount() < slot.getMaxStackSize() && ItemStack.isSameItemSameComponents(stack, slot)) {
                int room = slot.getMaxStackSize() - slot.getCount();
                int moved = Math.min(stack.getCount(), room);
                slot.grow(moved);
                stack.shrink(moved);
            }
        }
        for (int a = 0; a < this.loot.size(); a++) {
            if (!stack.isEmpty() && this.loot.get(a).isEmpty()) {
                this.loot.set(a, stack.copy());
                return ItemStack.EMPTY;
            }
        }
        return stack.isEmpty() ? ItemStack.EMPTY : stack;
    }

    public boolean isValued(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(Items.ENDER_PEARL)) {
            return true;
        }
        return AspectIndexAccess.index().of(stack).amountOf(desiderium()) > 1;
    }

    public int getValue(ItemStack stack) {
        if (stack.isEmpty()) {
            return 0;
        }
        if (stack.is(Items.ENDER_PEARL)) {
            return PECH_ENDER_PEARL_VALUE;
        }
        return Math.min(MAX_ASPECT_VALUE, AspectIndexAccess.index().of(stack).amountOf(desiderium()) / 2);
    }

    private Holder<IAspect> desiderium() {
        return this.level().registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(TCAspects.DESIDERIUM);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (player.isShiftKeyDown() || held.is(Items.NAME_TAG)) {
            return super.mobInteract(player, hand);
        }
        if (this.isTamed()) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, p) -> new MenuPech(id, inventory, this), this.getDisplayName()), buf -> buf.writeVarInt(this.getId()));
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("PechType", (byte) this.getPechType());
        output.putShort("Anger", (short) this.getAnger());
        output.putBoolean("Tamed", this.isTamed());
        ContainerHelper.saveAllItems(output, this.loot);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.setPechType(input.getByteOr("PechType", (byte) 0));
        this.setAnger(input.getShortOr("Anger", (short) 0));
        this.setTamed(input.getBooleanOr("Tamed", false));
        this.loot = NonNullList.withSize(LOOT_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, this.loot);
        this.setCombatTask();
    }
}
