package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.capability.PlayerResearchAccess;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.IGolemHands;
import com.leclowndu93150.thaumaturge.api.golems.IGolemProperties;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessories;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryBehavior;
import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemArmAbility;
import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemPartAbility;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.entity.construct.ConstructFollowOwnerGoal;
import com.leclowndu93150.thaumaturge.content.entity.construct.ConstructOwnerHurtByTargetGoal;
import com.leclowndu93150.thaumaturge.content.entity.construct.ConstructOwnerHurtTargetGoal;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityOwnedConstruct;
import com.leclowndu93150.thaumaturge.content.golem.accessory.GolemAccessoryStateHolder;
import com.leclowndu93150.thaumaturge.content.golem.accessory.GolemAccessoryStates;
import com.leclowndu93150.thaumaturge.content.golem.ai.BlockTaskGoal;
import com.leclowndu93150.thaumaturge.content.golem.ai.EntityTaskGoal;
import com.leclowndu93150.thaumaturge.content.golem.ai.ReturnHomeGoal;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEntityDataSerializers;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityThaumaturgeGolem extends EntityOwnedConstruct implements IGolemAPI, RangedAttackMob {

    private static final EntityDataAccessor<GolemProperties> PROPS =
            SynchedEntityData.defineId(EntityThaumaturgeGolem.class, TTEntityDataSerializers.GOLEM_PROPERTIES.get());
    private static final EntityDataAccessor<Byte> COLOR =
            SynchedEntityData.defineId(EntityThaumaturgeGolem.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> FLAGS =
            SynchedEntityData.defineId(EntityThaumaturgeGolem.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> CLIMBING =
            SynchedEntityData.defineId(EntityThaumaturgeGolem.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<String> ACCESSORIES =
            SynchedEntityData.defineId(EntityThaumaturgeGolem.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<GolemAccessoryStates> ACCESSORY_STATES = SynchedEntityData.defineId(
            EntityThaumaturgeGolem.class, TTEntityDataSerializers.GOLEM_ACCESSORY_STATES.get());
    private static final String ACCESSORY_STATES_KEY = "accessory_states";
    private static final int ACCESSORY_SYNC_BUDGET_BYTES = 1024;

    private static final int FLAG_FOLLOWING = 1 << 1;
    private static final int FLAG_COMBAT = 1 << 3;
    public static final int XP_PER_RANK_UNIT = 1000;
    public static final int MAX_RANK = 10;
    private static final int HOME_RANGE = 32;
    private static final int HOME_RANGE_SCOUT = 48;
    private static final double FETTER_CONTACT_DEPTH = 0.2;
    // Walking speed is intentionally capped below a sprinting player. Trait and
    // accessory bonuses must not turn routine navigation into a dash between waypoints.
    private static final double BASE_MOVEMENT_SPEED = 0.3;
    private static final double MAX_GROUND_MOVEMENT_ATTRIBUTE = 0.27;
    private static final float MAX_GROUND_NAVIGATION_MULTIPLIER = 1.0F;
    private static final double WORK_SPEED_PER_RANK = 0.025;
    private static final double LIGHT_WORK_BONUS = 0.2;
    private static final double HEAVY_WORK_PENALTY = 0.175;
    private static final double FLYER_WORK_PENALTY = 0.33;
    private static final double WHEELED_WORK_BONUS = 0.25;
    private static final double WHEELED_STEP_HEIGHT = 0.5;
    private static final double STEP_HEIGHT = 0.6;
    private static final double FOLLOW_RANGE = 40.0;
    private static final double FOLLOW_RANGE_SCOUT = 56.0;
    private static final double MELEE_SPEED = 1.0;
    private static final double FOLLOW_SPEED = 1.0;
    private static final float FOLLOW_START_DISTANCE = 10.0F;
    private static final float FOLLOW_STOP_DISTANCE = 2.0F;
    private static final float WATCH_PLAYER_RANGE = 8.0F;
    private static final int RETALIATION_MEMORY_TICKS = 300;
    private static final int KILL_XP = 8;
    private static final float LOOT_CHANCE = 0.3F;
    private static final float LOOTING_BONUS_PER_LEVEL = 0.15F;
    private static final ResourceLocation GOLEM_MASTERY_RESEARCH = TTIds.rl("golem_direct");
    private static final float RANK_UP_VOLUME = 0.25F;
    private static final int RANGED_TARGET_FORGET_DIST_SQR = 1024;

    public boolean redrawParts;
    public float wheelRotation;
    int rankXp;
    private final GolemAccessoryStateHolder accessoryStates = new GolemAccessoryStateHolder(this);
    private List<GolemAccessory> accessories = List.of();
    private boolean accessorySyncOverBudget;
    private final GolemHands hands = new GolemHands(this);
    private Task task;
    private int lastTaskEndTick = -20;
    private @Nullable DeckFetter deckFetter;
    private @Nullable Vec3 lastTracedPosition;

    public EntityThaumaturgeGolem(EntityType<? extends EntityThaumaturgeGolem> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static int xpForNextRank(int rank) {
        return XP_PER_RANK_UNIT * (rank + 1) * (rank + 1);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MOVEMENT_SPEED, MAX_GROUND_MOVEMENT_ATTRIBUTE)
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0)
                .add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.STEP_HEIGHT, 0.6);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(PROPS, GolemProperties.createDefault());
        entityData.define(COLOR, (byte) 0);
        entityData.define(FLAGS, (byte) 0);
        entityData.define(CLIMBING, (byte) 0);
        entityData.define(ACCESSORIES, "");
        entityData.define(ACCESSORY_STATES, GolemAccessoryStates.EMPTY);
    }

    public List<GolemAccessory> getAccessories() {
        return accessories;
    }

    public GolemAccessoryStates syncedAccessoryStates() {
        return entityData.get(ACCESSORY_STATES);
    }

    private static List<GolemAccessory> parseAccessories(String joined) {
        if (joined.isEmpty()) {
            return List.of();
        }
        List<GolemAccessory> parsed = new ArrayList<>();
        for (String id : joined.split(",")) {
            GolemAccessory accessory = GolemAccessories.get(ResourceLocation.parse(id));
            if (accessory != null) {
                parsed.add(accessory);
            }
        }
        return List.copyOf(parsed);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (ACCESSORIES.equals(accessor)) {
            accessories = parseAccessories(entityData.get(ACCESSORIES));
        }
    }

    private boolean addAccessory(GolemAccessory accessory, ItemStack attachedStack) {
        List<GolemAccessory> current = getAccessories();
        for (GolemAccessory worn : current) {
            if (worn == accessory) {
                return false;
            }
            if (accessory.group().excludes(worn.group())) {
                return false;
            }
        }
        String joined = entityData.get(ACCESSORIES);
        entityData.set(ACCESSORIES, joined.isEmpty() ? accessory.id().toString() : joined + "," + accessory.id());
        accessoryStates.attach(accessory, attachedStack);
        syncAccessoryStates();
        updateEntityAttributes();
        return true;
    }

    private void dropAccessories() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (GolemAccessory accessory : getAccessories()) {
            ItemStack stack = accessoryStates.detach(accessory);
            if (!stack.isEmpty()) {
                stack.setCount(1);
                spawnAtLocation(stack, 0.5F);
            }
        }
        accessoryStates.clear();
        entityData.set(ACCESSORIES, "");
        syncAccessoryStates();
    }

    private void syncAccessoryStates() {
        GolemAccessoryStates synced = accessoryStates.synced();
        int size = synced.slots().isEmpty() ? 0 : synced.encodedSize(registryAccess());
        if (size > ACCESSORY_SYNC_BUDGET_BYTES) {
            if (!accessorySyncOverBudget) {
                accessorySyncOverBudget = true;
                Thaumaturge.LOGGER.error(
                        "Golem {} accessory states encode to {} bytes, over the {} byte sync budget; clients keep the last state that fit",
                        getUUID(),
                        size,
                        ACCESSORY_SYNC_BUDGET_BYTES);
            }
            return;
        }
        accessorySyncOverBudget = false;
        entityData.set(ACCESSORY_STATES, synced);
    }

    @Override
    public Optional<UUID> ownerIdentity() {
        return Optional.ofNullable(getOwnerUUID());
    }

    @Override
    public <S> Optional<S> accessoryState(GolemAccessoryBehavior<S> behavior) {
        return level().isClientSide()
                ? entityData.get(ACCESSORY_STATES).state(behavior)
                : accessoryStates.state(behavior);
    }

    @Override
    public <S> boolean updateAccessoryState(GolemAccessoryBehavior<S> behavior, UnaryOperator<S> update) {
        if (level().isClientSide()) {
            throw new IllegalStateException("Golem accessory state is server authoritative");
        }
        if (!accessoryStates.update(behavior, update)) {
            return false;
        }
        syncAccessoryStates();
        return true;
    }

    private float accessoryRegenFactor() {
        float factor = 1.0F;
        for (GolemAccessory accessory : getAccessories()) {
            factor *= accessory.regenFactor();
        }
        return factor;
    }

    private boolean hasKillCreditAccessory() {
        for (GolemAccessory accessory : getAccessories()) {
            if (accessory.killCredit()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public GolemProperties properties() {
        return entityData.get(PROPS);
    }

    @Override
    protected Component getTypeName() {
        return properties().name(super.getTypeName());
    }

    @Override
    public void applyBuild(IGolemProperties properties) {
        if (level().isClientSide()) {
            return;
        }
        entityData.set(PROPS, GolemProperties.of(properties));
        updateEntityAttributes();
    }

    @Override
    public IGolemHands hands() {
        return hands;
    }

    @Override
    public LivingEntity asEntity() {
        return this;
    }

    @Override
    public byte color() {
        return entityData.get(COLOR);
    }

    public void setGolemColor(byte color) {
        entityData.set(COLOR, color);
    }

    private byte getFlags() {
        return entityData.get(FLAGS);
    }

    private void setFlag(int mask, boolean value) {
        byte flags = getFlags();
        entityData.set(FLAGS, (byte) (value ? flags | mask : flags & ~mask));
    }

    public boolean isFollowingOwner() {
        return (getFlags() & FLAG_FOLLOWING) != 0;
    }

    public void setFollowingOwner(boolean following) {
        setFlag(FLAG_FOLLOWING, following);
    }

    @Override
    public boolean isFighting() {
        return (getFlags() & FLAG_COMBAT) != 0;
    }

    private void setInCombat(boolean inCombat) {
        setFlag(FLAG_COMBAT, inCombat);
    }

    public void updateEntityAttributes() {
        applyStats();
        if (isFollowingOwner()) {
            clearRestriction();
        } else {
            int radius = (int) ((properties().hasTrait(TTGolemTraits.SCOUT.get()) ? HOME_RANGE_SCOUT : HOME_RANGE)
                    * accessoryRangeFactor());
            restrictTo(hasRestriction() ? getRestrictCenter() : LevelSpaces.localBlock(this), radius);
        }
        rebuildBehaviours();
    }

    private float accessoryRangeFactor() {
        float factor = 1.0F;
        for (GolemAccessory accessory : getAccessories()) {
            factor *= accessory.rangeFactor();
        }
        return factor;
    }

    private void applyStats() {
        GolemProperties props = properties();
        int healthBonus = 0;
        int armorBonus = 0;
        float speedFactor = 1.0F;
        for (GolemAccessory accessory : getAccessories()) {
            healthBonus += accessory.healthBonus();
            armorBonus += accessory.armorBonus();
            speedFactor *= accessory.speedFactor();
        }
        setBaseAttribute(Attributes.MAX_HEALTH, GolemStats.health(props) + props.rank() + healthBonus);
        if (getHealth() > getMaxHealth()) {
            setHealth(getMaxHealth());
        }
        setBaseAttribute(Attributes.ARMOR, GolemStats.armor(props) + armorBonus);
        setBaseAttribute(Attributes.ATTACK_DAMAGE, GolemStats.meleeDamage(props));
        setBaseAttribute(
                Attributes.STEP_HEIGHT,
                props.hasTrait(TTGolemTraits.WHEELED.get()) ? WHEELED_STEP_HEIGHT : STEP_HEIGHT);
        setBaseAttribute(
                Attributes.MOVEMENT_SPEED,
                props.hasTrait(TTGolemTraits.FLYER.get())
                        ? BASE_MOVEMENT_SPEED * speedFactor
                        : Math.min(MAX_GROUND_MOVEMENT_ATTRIBUTE * speedFactor, MAX_GROUND_MOVEMENT_ATTRIBUTE));
        setBaseAttribute(
                Attributes.FOLLOW_RANGE,
                (props.hasTrait(TTGolemTraits.SCOUT.get()) ? FOLLOW_RANGE_SCOUT : FOLLOW_RANGE)
                        * accessoryRangeFactor());
    }

    private void setBaseAttribute(Holder<Attribute> attribute, double value) {
        AttributeInstance instance = getAttribute(attribute);
        if (instance != null) {
            instance.setBaseValue(value);
        }
    }

    private void rebuildBehaviours() {
        GolemProperties props = properties();
        boolean fighter = props.hasTrait(TTGolemTraits.FIGHTER.get());
        boolean following = isFollowingOwner();
        getNavigation().stop();
        goalSelector.removeAllGoals(goal -> true);
        targetSelector.removeAllGoals(goal -> true);
        navigation = createGolemNavigation();
        moveControl =
                props.hasTrait(TTGolemTraits.FLYER.get()) ? new GolemFlyingMoveControl(this) : new MoveControl(this);
        if (fighter && !props.hasTrait(TTGolemTraits.FLYER.get())) {
            goalSelector.addGoal(0, new FloatGoal(this));
        }
        if (fighter) {
            IGolemArmAbility arms = props.arms().ability();
            Goal ranged =
                    arms != null && props.hasTrait(TTGolemTraits.RANGED.get()) ? arms.createRangedGoal(this) : null;
            if (ranged != null) {
                goalSelector.addGoal(1, ranged);
            }
            goalSelector.addGoal(2, new MeleeAttackGoal(this, MELEE_SPEED, false));
        }
        if (following) {
            goalSelector.addGoal(
                    3, new ConstructFollowOwnerGoal(this, FOLLOW_SPEED, FOLLOW_START_DISTANCE, FOLLOW_STOP_DISTANCE));
        } else {
            goalSelector.addGoal(3, new EntityTaskGoal(this));
            goalSelector.addGoal(4, new BlockTaskGoal(this));
            goalSelector.addGoal(5, new ReturnHomeGoal(this));
        }
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, WATCH_PLAYER_RANGE));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        if (!fighter) {
            return;
        }
        int priority = 1;
        if (following) {
            targetSelector.addGoal(priority++, new ConstructOwnerHurtByTargetGoal(this));
            targetSelector.addGoal(priority++, new ConstructOwnerHurtTargetGoal(this));
        }
        targetSelector.addGoal(priority, new HurtByTargetGoal(this).setUnseenMemoryTicks(RETALIATION_MEMORY_TICKS));
    }

    public float getGolemMoveSpeed() {
        GolemProperties props = properties();
        double speed = 1.0 + WORK_SPEED_PER_RANK * props.rank();
        if (props.hasTrait(TTGolemTraits.LIGHT.get())) {
            speed += LIGHT_WORK_BONUS;
        }
        if (props.hasTrait(TTGolemTraits.HEAVY.get())) {
            speed -= HEAVY_WORK_PENALTY;
        }
        if (props.hasTrait(TTGolemTraits.FLYER.get())) {
            speed -= FLYER_WORK_PENALTY;
        }
        if (props.hasTrait(TTGolemTraits.WHEELED.get())) {
            speed += WHEELED_WORK_BONUS;
        }
        // Bonuses can affect other behavior, but ground golems should walk at
        // roughly a player's normal pace instead of sprinting between tasks.
        return props.hasTrait(TTGolemTraits.FLYER.get())
                ? (float) speed
                : Math.min((float) speed, MAX_GROUND_NAVIGATION_MULTIPLIER);
    }

    private PathNavigation createGolemNavigation() {
        PathNavigation nativeNavigation = createNativeGolemNavigation();
        return LevelSpaces.isWorldOnly()
                ? nativeNavigation
                : new com.leclowndu93150.thaumaturge.content.golem.ai.GolemSpaceNavigation(
                        this, level(), nativeNavigation, properties().hasTrait(TTGolemTraits.FLYER.get()));
    }

    private PathNavigation createNativeGolemNavigation() {
        if (properties().hasTrait(TTGolemTraits.FLYER.get())) {
            FlyingPathNavigation nav = new FlyingPathNavigation(this, level());
            nav.setCanFloat(true);
            return nav;
        }
        if (properties().hasTrait(TTGolemTraits.CLIMBER.get())) {
            return new WallClimberNavigation(this, level());
        }
        return new GroundPathNavigation(this, level());
    }

    @Override
    protected float getJumpPower() {
        var frame = LevelSpaces.trackedFrame(this);
        if (frame.isWorld()) return super.getJumpPower();
        var up = frame.directionToWorld(new Vec3(0, 1, 0));
        return up.y > 0.5
                ? (float) (super.getJumpPower() * (1.0 + 0.6 * up.horizontalDistance()))
                : super.getJumpPower();
    }

    @Override
    public boolean onClimbable() {
        return isBesideClimbableBlock();
    }

    public boolean isBesideClimbableBlock() {
        return (entityData.get(CLIMBING) & 1) != 0;
    }

    public void setBesideClimbableBlock(boolean climbing) {
        byte flags = entityData.get(CLIMBING);
        entityData.set(CLIMBING, (byte) (climbing ? flags | 1 : flags & ~1));
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(
            ServerLevelAccessor level,
            DifficultyInstance difficulty,
            MobSpawnType spawnReason,
            @Nullable SpawnGroupData spawnGroupData) {
        restrictTo(LevelSpaces.localBlock(this), HOME_RANGE);
        updateEntityAttributes();
        return spawnGroupData;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return properties().hasTrait(TTGolemTraits.HEAVY.get()) && !properties().hasTrait(TTGolemTraits.FLYER.get())
                ? Entity.MovementEmission.ALL
                : Entity.MovementEmission.NONE;
    }

    @Override
    public boolean causeFallDamage(float fallDistance, float damageMultiplier, DamageSource source) {
        if (properties().hasTrait(TTGolemTraits.FLYER.get()) || properties().hasTrait(TTGolemTraits.CLIMBER.get())) {
            return false;
        }
        return super.causeFallDamage(fallDistance, damageMultiplier, source);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        updateSwingTime();
    }

    @Override
    public void tick() {
        if (level() instanceof ServerLevel server && hasRestriction() && !LevelSpaces.isWorldOnly()) {
            BlockPos trackedHome = LevelSpaces.rememberedBlockPosition(server, getUUID());
            if (trackedHome == null) {
                LevelSpaces.rememberBlock(server, getUUID(), getRestrictCenter());
            } else if (!trackedHome.equals(getRestrictCenter())) {
                super.restrictTo(trackedHome, (int) getRestrictRadius());
                getNavigation().stop();
            }
        }
        super.tick();
        if (GolemTrace.DEBUG_TRACE && !level().isClientSide()) {
            traceLargeMovement();
        }
        if (!level().isClientSide() && isFettered()) {
            pauseForFetter();
            return;
        }
        deckFetter = null;
        GolemProperties props = properties();
        if (props.hasTrait(TTGolemTraits.FLYER.get())) {
            setNoGravity(true);
        }
        if (!level().isClientSide()) {
            // Never snap a loaded golem to its exact home block. Home is a work radius,
            // not a spawn coordinate; ReturnHomeGoal can navigate back when idle.
            // Keep goHome() reserved for genuine emergencies (in-wall/void damage).
            if (task != null && task.isEnded()) {
                task = null;
            }
            if (getTarget() != null && !getTarget().isAlive()) {
                setTarget(null);
            }
            if (getTarget() != null
                    && props.hasTrait(TTGolemTraits.RANGED.get())
                    && distanceToSqr(getTarget()) > RANGED_TARGET_FORGET_DIST_SQR) {
                setTarget(null);
            }
            if (level() instanceof ServerLevel serverLevel
                    && !serverLevel.getServer().isPvpAllowed()
                    && getTarget() instanceof Player) {
                setTarget(null);
            }
            if (accessoryStates.tick()) {
                syncAccessoryStates();
            }
            int healInterval = (int) ((props.hasTrait(TTGolemTraits.REPAIR.get()) ? 40 : 100) * accessoryRegenFactor());
            if (tickCount % Math.max(1, healInterval) == 0) {
                heal(1.0F);
            }
            if (props.hasTrait(TTGolemTraits.CLIMBER.get())) {
                setBesideClimbableBlock(horizontalCollision);
            }
        } else {
            if (tickCount < 20 || tickCount % 20 == 0) {
                redrawParts = true;
            }
            if (props.hasTrait(TTGolemTraits.WHEELED.get())) {
                updateWheelRotation();
            }
        }
        tickAbility(props.head().ability());
        tickAbility(props.arms().ability());
        tickAbility(props.legs().ability());
        tickAbility(props.addon().ability());
    }

    /** Catch abrupt position writes, even when they bypass our goHome() helper. */
    @Override
    public void setPos(double x, double y, double z) {
        // Entity construction may call setPos before its remaining state exists.
        if (!GolemTrace.DEBUG_TRACE || tickCount == 0) {
            super.setPos(x, y, z);
            return;
        }
        Vec3 before = position();
        super.setPos(x, y, z);
        Vec3 change = position().subtract(before);
        if (change.horizontalDistanceSqr() >= 1.0 || Math.abs(change.y) >= 0.85) {
            GolemTrace.log(
                    "[golem-position] side={} golem={} tick={} event=position_write from={} to={} delta={} "
                            + "velocity={} caller={}",
                    level().isClientSide() ? "client" : "server",
                    getUUID(),
                    tickCount,
                    before,
                    position(),
                    change,
                    getDeltaMovement(),
                    StackWalker.getInstance()
                            .walk(frames -> frames.skip(1)
                                    .limit(4)
                                    .map(f -> f.getClassName() + "#" + f.getMethodName())
                                    .toList()));
        }
    }

    /** Trace unexpected world-space interpolation rather than Sable's plot coordinates. */
    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps) {
        if (GolemTrace.DEBUG_TRACE && level().isClientSide()) {
            Vec3 before = position();
            Vec3 rawTarget = new Vec3(x, y, z);
            // Sable can send coordinates in its far-away plot-space frame.
            // Compare the actual world-space target, not raw (20-million-block) values.
            Vec3 worldTarget = LevelSpaces.frameOf(level(), rawTarget).toWorld(rawTarget);
            Vec3 delta = worldTarget.subtract(before);
            if (delta.horizontalDistanceSqr() >= 0.5 * 0.5 || Math.abs(delta.y) >= 0.35) {
                // If the frame cannot yet be resolved, do not flood logs with
                // the same raw Sable plot coordinate every tracking update.
                if (Math.abs(worldTarget.x) < 1.0E6 && Math.abs(worldTarget.z) < 1.0E6) {
                    GolemTrace.log(
                            "[golem-client] event=lerp_target golem={} tick={} from={} worldTarget={} "
                                    + "rawTarget={} worldDelta={} steps={} frame={}",
                            getUUID(),
                            tickCount,
                            before,
                            worldTarget,
                            rawTarget,
                            delta,
                            steps,
                            LevelSpaces.trackedFrame(this));
                }
            }
        }
        super.lerpTo(x, y, z, yaw, pitch, steps);
    }

    @Override
    public void restrictTo(BlockPos pos, int radius) {
        super.restrictTo(pos, radius);
        if (level() instanceof ServerLevel server) {
            LevelSpaces.rememberBlock(server, getUUID(), pos);
        }
    }

    @Override
    public void clearRestriction() {
        super.clearRestriction();
        if (level() instanceof ServerLevel server) {
            LevelSpaces.forgetBlock(server, getUUID());
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (reason.shouldDestroy() && level() instanceof ServerLevel server) {
            LevelSpaces.forgetBlock(server, getUUID());
        }
        super.remove(reason);
    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && !isFettered();
    }

    public boolean isFettered() {
        var frame = LevelSpaces.trackedFrame(this);
        // Sample below the feet in deck coordinates so contact survives small gaps in collision resolution.
        BlockPos support = frame.isWorld()
                ? blockPosition().below()
                : BlockPos.containing(fetterFeet(frame).add(0.0, -FETTER_CONTACT_DEPTH, 0.0));
        BlockState below = level().getBlockState(support);
        return below.is(TTBlocks.GOLEM_FETTER.get()) && below.getValue(BlockGolemFetter.POWERED);
    }

    private Vec3 fetterFeet(SpaceFrame frame) {
        return deckFetter != null && deckFetter.frame().equals(frame) ? deckFetter.feet() : frame.toLocal(position());
    }

    private void pauseForFetter() {
        var frame = LevelSpaces.trackedFrame(this);
        if (frame.isWorld()) {
            deckFetter = null;
        } else {
            // Hold the same point on the deck when ship motion moves the collision contact.
            Vec3 feet = fetterFeet(frame);
            if (deckFetter == null || !deckFetter.frame().equals(frame)) {
                deckFetter = new DeckFetter(frame, feet);
            }
            setPos(frame.toWorld(feet));
        }
        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
        setTarget(null);
        releaseTask();
        task = null;
    }

    private record DeckFetter(SpaceFrame frame, Vec3 feet) {}

    private void tickAbility(@Nullable IGolemPartAbility ability) {
        if (ability != null) {
            ability.tick(this);
        }
    }

    private void updateWheelRotation() {
        wheelRotation = GolemWheel.spun(wheelRotation, getX() - xo, getZ() - zo, yBodyRot);
    }

    /** Tracks large server-side position corrections, including movement applied outside the golem's own tick. */
    private void traceLargeMovement() {
        Vec3 current = position();
        Vec3 previous = lastTracedPosition;
        lastTracedPosition = current;
        if (previous == null) return;
        Vec3 displacement = current.subtract(previous);
        // Smaller than the original thresholds so sub-block upward snaps also
        // show up, without reporting ordinary walking or the start of a jump.
        if (Math.abs(displacement.y) < 0.5 && displacement.horizontalDistanceSqr() < 0.8 * 0.8) return;
        Task currentTask = getTask();
        GolemTrace.log(
                "[golem-move] tick={} golem={} event=large_displacement from={} to={} displacement={} velocity={} "
                        + "grounded={} horizontalCollision={} verticalCollision={} frame={} nav={} navTarget={} task={}",
                level().getGameTime(),
                getUUID(),
                previous,
                current,
                displacement,
                getDeltaMovement(),
                onGround(),
                horizontalCollision,
                verticalCollision,
                LevelSpaces.trackedFrame(this),
                getNavigation().getClass().getSimpleName(),
                getNavigation().getTargetPos(),
                currentTask == null ? null : currentTask.id());
    }

    private void goHome(String reason) {
        Vec3 origin = position();
        for (BlockPos spot = getRestrictCenter(); spot.getY() < level().getMaxBuildHeight(); spot = spot.above()) {
            Vec3 target = Vec3.atBottomCenterOf(spot);
            if (level().noCollision(this, getBoundingBox().move(target.subtract(origin)))) {
                Vec3 arrival = LevelSpaces.toWorld(level(), target);
                if (GolemTrace.DEBUG_TRACE) {
                    GolemTrace.log(
                            "[golem-move] tick={} golem={} event=teleport_home reason={} from={} to={} home={} "
                                    + "frame={} grounded={} navigationTarget={}",
                            level().getGameTime(),
                            getUUID(),
                            reason,
                            origin,
                            arrival,
                            getRestrictCenter(),
                            LevelSpaces.trackedFrame(this),
                            onGround(),
                            getNavigation().getTargetPos());
                }
                teleportTo(arrival.x, arrival.y, arrival.z);
                getNavigation().stop();
                return;
            }
        }
    }

    @Override
    public boolean isWithinRestriction(BlockPos pos) {
        if (!hasRestriction()) {
            return true;
        }
        double radius = getRestrictRadius();
        return LevelSpaces.distanceSqr(level(), getRestrictCenter(), pos) < radius * radius;
    }

    @Override
    protected void actuallyHurt(DamageSource source, float damage) {
        GolemProperties props = properties();
        if (source.is(DamageTypeTags.IS_FIRE) && props.hasTrait(TTGolemTraits.FIREPROOF.get())) {
            return;
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION) && props.hasTrait(TTGolemTraits.BLASTPROOF.get())) {
            damage = Math.min(getMaxHealth() / 2.0F, damage * 0.3F);
        }
        if (source.is(DamageTypes.CACTUS)) {
            return;
        }
        if (hasRestriction() && (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FELL_OUT_OF_WORLD))) {
            goHome(source.is(DamageTypes.IN_WALL) ? "in_wall_damage" : "fell_out_of_world");
        }
        super.actuallyHurt(source, damage);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isRemoved() || player.getItemInHand(hand).is(Items.NAME_TAG)) {
            return InteractionResult.PASS;
        }
        if (!isOwner(player)) {
            return super.mobInteract(player, hand);
        }
        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (player.isShiftKeyDown()) {
            pickUpGolem(player, hand);
            return InteractionResult.CONSUME;
        }
        if (player.getItemInHand(hand).is(TTItems.GOLEM_BELL.get())) {
            if (PlayerResearchAccess.of(player).hasCompleted(GOLEM_MASTERY_RESEARCH)) {
                toggleFollow(player, hand);
            }
            return InteractionResult.CONSUME;
        }
        Optional<GolemAccessory> accessory = GolemAccessories.forItem(player.getItemInHand(hand));
        if (accessory.isPresent()) {
            if (addAccessory(accessory.get(), player.getItemInHand(hand))) {
                playSound(TTSounds.CLACK.get(), 1.0F, 1.0F);
                player.getItemInHand(hand).shrink(1);
                player.swing(hand, true);
            }
            return InteractionResult.CONSUME;
        }
        DyeColor dyeColor = player.getItemInHand(hand).getItem() instanceof DyeItem dye ? dye.getDyeColor() : null;
        if (dyeColor != null) {
            playSound(TTSounds.ZAP.get(), 1.0F, 1.0F);
            setGolemColor((byte) (1 + dyeColor.getId()));
            player.getItemInHand(hand).shrink(1);
            player.swing(hand, true);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.CONSUME;
    }

    private void pickUpGolem(Player player, InteractionHand hand) {
        playSound(TTSounds.ZAP.get(), 1.0F, 1.0F);
        releaseTask();
        dropCarried();
        dropAccessories();
        ItemStack placer = new ItemStack(TTItems.GOLEM_PLACER.get());
        placer.set(TTDataComponents.GOLEM_PROPERTIES.get(), properties());
        placer.set(TTDataComponents.GOLEM_XP.get(), rankXp);
        spawnAtLocation(placer, 0.5F);
        discard();
        player.swing(hand, true);
    }

    private void toggleFollow(Player player, InteractionHand hand) {
        releaseTask();
        playSound(TTSounds.SCAN.get(), 1.0F, 1.0F);
        setFollowingOwner(!isFollowingOwner());
        if (isFollowingOwner()) {
            sendActionBar(player, "message.thaumaturge.golem.follow");
            GolemEvent.FOLLOW.broadcast(this);
        } else {
            sendActionBar(player, "message.thaumaturge.golem.stay");
            GolemEvent.STAY.broadcast(this);
            restrictTo(
                    LevelSpaces.localBlock(this),
                    properties().hasTrait(TTGolemTraits.SCOUT.get()) ? HOME_RANGE_SCOUT : HOME_RANGE);
        }
        updateEntityAttributes();
        player.swing(hand, true);
    }

    private static void sendActionBar(Player player, String key) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable(key)));
        }
    }

    @Override
    public void die(DamageSource cause) {
        releaseTask();
        super.die(cause);
        if (!level().isClientSide()) {
            dropCarried();
        }
    }

    protected void dropCarried() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (ItemStack stack : hands.contents()) {
            if (!stack.isEmpty()) {
                spawnAtLocation(stack, 0.25F);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean playerKill) {
        super.dropCustomDeathLoot(level, source, playerKill);
        dropAccessories();
        float chance = LOOT_CHANCE + LOOTING_BONUS_PER_LEVEL * lootingLevel(level, source);
        for (ItemStack stack : properties().components()) {
            ItemStack copy = stack.copy();
            if (random.nextFloat() < chance) {
                if (copy.getCount() > 0) {
                    copy.shrink(random.nextInt(copy.getCount()));
                }
                spawnAtLocation(copy, 0.25F);
            }
        }
    }

    private static int lootingLevel(ServerLevel level, DamageSource source) {
        ItemStack weapon = source.getWeaponItem();
        if (weapon == null || weapon.isEmpty()) {
            return 0;
        }
        return weapon.getEnchantmentLevel(
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING));
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        setInCombat(getTarget() != null);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(level() instanceof ServerLevel level)) return false;
        DamageSource source = damageSources().mobAttack(this);
        float damage = EnchantmentHelper.modifyDamage(
                level, getWeaponItem(), target, source, (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
        boolean hurt = target.hurt(source, damage);
        if (hurt) {
            float knockback = getKnockback(target, source);
            if (knockback > 0.0F && target instanceof LivingEntity living) {
                living.knockback(
                        knockback * 0.5F, Mth.sin(getYRot() * Mth.DEG_TO_RAD), -Mth.cos(getYRot() * Mth.DEG_TO_RAD));
                setDeltaMovement(getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }
            EnchantmentHelper.doPostAttackEffects(level, target, source);
            setLastHurtMob(target);
            if (target instanceof LivingEntity living
                    && (properties().hasTrait(TTGolemTraits.DEFT.get()) || hasKillCreditAccessory())
                    && getOwner() instanceof Player owner) {
                living.setLastHurtByPlayer(owner);
            }
            if (properties().arms().ability() != null) {
                properties().arms().ability().onMeleeHit(this, target);
            }
            if (target instanceof Mob mob && !mob.isAlive()) {
                grantRankExperience(KILL_XP);
            }
        }
        return hurt;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (properties().arms().ability() != null) {
            properties().arms().ability().onRangedAttack(this, target, power);
        }
    }

    public @Nullable Task getTask() {
        return task;
    }

    public void setTask(@Nullable Task task) {
        if (this.task != null && task == null) lastTaskEndTick = tickCount;
        this.task = task;
    }

    public boolean recentlyFinishedTask() {
        return tickCount - lastTaskEndTick < 20;
    }

    private void releaseTask() {
        if (task != null) {
            task.release();
        }
    }

    public int getRankXp() {
        return rankXp;
    }

    public void setRankXp(int rankXp) {
        this.rankXp = rankXp;
    }

    @Override
    public void grantRankExperience(int xp) {
        GolemProperties props = properties();
        if (xp <= 0
                || level().isClientSide()
                || !props.hasTrait(TTGolemTraits.SMART.get())
                || props.rank() >= MAX_RANK) {
            return;
        }
        rankXp = (int) Math.min((long) rankXp + xp, Integer.MAX_VALUE);
        int needed = xpForNextRank(props.rank());
        if (rankXp < needed) {
            return;
        }
        rankXp -= needed;
        entityData.set(PROPS, GolemProperties.of(props.withRank(props.rank() + 1)));
        applyStats();
        if (GolemEvent.emotesEnabled()) {
            GolemEvent.RANK_UP.broadcast(this);
            playSound(SoundEvents.PLAYER_LEVELUP, RANK_UP_VOLUME, 1.0F);
        }
    }

    @Override
    public void onRemovedFromLevel() {
        if (!level().isClientSide()) {
            releaseTask();
        }
        super.onRemovedFromLevel();
    }

    @Override
    public void animateSwing() {
        swing(InteractionHand.MAIN_HAND, true);
    }

    @Override
    public void handleEntityEvent(byte id) {
        GolemEvent event = GolemEvent.byId(id);
        if (event == null) {
            super.handleEntityEvent(id);
        } else {
            event.play(this);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        HolderLookup.Provider registries = registryAccess();
        super.addAdditionalSaveData(output);
        TTNbt.store(output, "props", GolemProperties.CODEC, registries, properties());
        TTNbt.store(output, "homepos", BlockPos.CODEC, registries, getRestrictCenter());
        output.putByte("gflags", getFlags());
        output.putInt("rankXP", rankXp);
        output.putByte("color", color());
        output.putString("accessories", entityData.get(ACCESSORIES));
        if (!accessoryStates.isEmpty()) {
            CompoundTag stored = new CompoundTag();
            accessoryStates.save(stored, registries);
            output.put(ACCESSORY_STATES_KEY, stored);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        HolderLookup.Provider registries = registryAccess();
        super.readAdditionalSaveData(input);
        TTNbt.read(input, "props", GolemProperties.CODEC, registries).ifPresent(this::applyBuild);
        // A Sable home can move while this entity is unloaded. Keep the saved position as a fallback.
        super.restrictTo(
                TTNbt.read(input, "homepos", BlockPos.CODEC, registries).orElse(blockPosition()), HOME_RANGE);
        entityData.set(FLAGS, (input.contains("gflags") ? input.getByte("gflags") : (byte) 0));
        rankXp = (input.contains("rankXP") ? input.getInt("rankXP") : 0);
        setGolemColor((input.contains("color") ? input.getByte("color") : (byte) 0));
        entityData.set(ACCESSORIES, (input.contains("accessories") ? input.getString("accessories") : ""));
        accessoryStates.load(input.getCompound(ACCESSORY_STATES_KEY), getAccessories(), registries);
        syncAccessoryStates();
        updateEntityAttributes();
    }

    static final class GolemFlyingMoveControl extends MoveControl {
        private static final double ARRIVAL_DAMPING = 0.2D;

        private final EntityThaumaturgeGolem golem;

        GolemFlyingMoveControl(EntityThaumaturgeGolem golem) {
            super(golem);
            this.golem = golem;
        }

        @Override
        public void tick() {
            if (operation != Operation.MOVE_TO) {
                return;
            }
            double dx = wantedX - golem.getX();
            double dy = wantedY - golem.getY();
            double dz = wantedZ - golem.getZ();
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist < golem.getBoundingBox().getSize()) {
                operation = Operation.WAIT;
                golem.setDeltaMovement(golem.getDeltaMovement().scale(ARRIVAL_DAMPING));
            } else {
                Vec3 motion = golem.getDeltaMovement();
                golem.setDeltaMovement(
                        motion.add(GolemFlightSteering.desiredDelta(new Vec3(dx, dy, dz), speedModifier)));
                if (golem.getTarget() == null) {
                    golem.setYRot(-((float) Mth.atan2(golem.getDeltaMovement().x, golem.getDeltaMovement().z))
                            * (180.0F / (float) Math.PI));
                } else {
                    double tx = golem.getTarget().getX() - golem.getX();
                    double tz = golem.getTarget().getZ() - golem.getZ();
                    golem.setYRot(-((float) Mth.atan2(tx, tz)) * (180.0F / (float) Math.PI));
                }
                golem.yBodyRot = golem.getYRot();
            }
        }
    }
}
