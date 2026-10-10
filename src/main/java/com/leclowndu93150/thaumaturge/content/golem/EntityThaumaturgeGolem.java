package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.IGolemHands;
import com.leclowndu93150.thaumaturge.api.golems.IGolemProperties;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessories;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessoryBehavior;
import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemPartAbility;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityOwnedConstruct;
import com.leclowndu93150.thaumaturge.content.golem.accessory.GolemAccessoryStateHolder;
import com.leclowndu93150.thaumaturge.content.golem.accessory.GolemAccessoryStates;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyGolemSave;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTEntityDataSerializers;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.UnaryOperator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

public class EntityThaumaturgeGolem extends EntityOwnedConstruct implements IGolemAPI, RangedAttackMob {
    public static final int XP_PER_RANK_UNIT = 1000;

    private static final EntityDataAccessor<GolemProperties> DATA_PROPERTIES = syncedField(TTEntityDataSerializers.GOLEM_PROPERTIES.get());
    private static final EntityDataAccessor<GolemAccessoryStates> DATA_ACCESSORY_STATES = syncedField(TTEntityDataSerializers.GOLEM_ACCESSORY_STATES.get());
    private static final EntityDataAccessor<String> DATA_ACCESSORIES = syncedField(EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Byte> DATA_COLOR = syncedField(EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_FLAGS = syncedField(EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> DATA_CLIMBING = syncedField(EntityDataSerializers.BYTE);

    public static final int MAX_RANK = 10;

    private static final int KILL_EXPERIENCE = 5;
    private static final int FLAG_FOLLOWING = 1 << 1;
    private static final int FLAG_COMBAT = 1 << 3;
    private static final byte CLIMBING_BIT = 1;
    private static final char ACCESSORY_SEPARATOR = ',';
    private static final int ACCESSORY_SYNC_LIMIT = 1024;
    private static final int MELEE_KILL_XP = 8;
    private static final int OWNER_CREDIT_TICKS = 100;
    private static final int REPAIR_HEAL_INTERVAL = 40;
    private static final int HEAL_INTERVAL = 100;
    private static final double RANGED_TARGET_LIMIT_SQR = 1024.0D;
    private static final int REDRAW_BURST_TICKS = 20;
    private static final int REDRAW_INTERVAL = 20;
    private static final float RANK_SPEED = 0.025F;
    private static final float LIGHT_SPEED = 0.2F;
    private static final float HEAVY_SPEED = -0.175F;
    private static final float FLYER_SPEED = -0.33F;
    private static final float WHEELED_SPEED = 0.25F;
    private static final int NEXT_RANK_OFFSET = 1;
    private static final float CARRIED_DROP_OFFSET = 0.25F;
    private static final float ACCESSORY_DROP_OFFSET = 0.5F;
    private static final float BILL_DROP_OFFSET = 0.25F;
    private static final float RANK_UP_VOLUME = 0.25F;
    private static final float SOUND_VOLUME = 1.0F;
    private static final float SOUND_PITCH = 1.0F;
    private static final float BLAST_HEALTH_SHARE = 0.5F;
    private static final float BLAST_DAMAGE_FACTOR = 0.3F;
    private static final double CENTER = 0.5D;
    private static final String KEY_BUILD = "golem_build";
    private static final String KEY_HOME = "golem_home";
    private static final String KEY_FLAGS = "golem_flags";
    private static final String KEY_RANK_XP = "golem_rank_xp";
    private static final String KEY_DYE = "golem_dye";
    private static final String KEY_ACCESSORIES = "accessories";
    private static final String KEY_ACCESSORY_STATES = "accessory_states";

    public boolean partsDirty;
    public float wheelRotation;
    public float grinderRot;
    public float grinderSpeed;

    int rankXp;

    private final GolemHands hands = new GolemHands(this);
    private final GolemAccessoryStateHolder accessoryHolder = new GolemAccessoryStateHolder(this);
    private List<GolemAccessory> accessories = List.of();
    private @Nullable Task task;
    private boolean homeChecked;
    private boolean statesOverBudget;

    public EntityThaumaturgeGolem(EntityType<? extends EntityThaumaturgeGolem> type, Level level) {
        super(type, level);
        this.xpReward = KILL_EXPERIENCE;
    }

    private static <T> EntityDataAccessor<T> syncedField(EntityDataSerializer<T> serializer) {
        return SynchedEntityData.defineId(EntityThaumaturgeGolem.class, serializer);
    }

    public static int xpForNextRank(int rank) {
        return XP_PER_RANK_UNIT * Mth.square(rank + NEXT_RANK_OFFSET);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return GolemStatSheet.baseAttributes();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_ACCESSORY_STATES, GolemAccessoryStates.EMPTY);
        builder.define(DATA_ACCESSORIES, "");
        builder.define(DATA_CLIMBING, (byte) 0);
        builder.define(DATA_FLAGS, (byte) 0);
        builder.define(DATA_COLOR, (byte) 0);
        builder.define(DATA_PROPERTIES, GolemProperties.createDefault());
    }

    public List<GolemAccessory> getAccessories() {
        return accessories;
    }

    public GolemAccessoryStates syncedAccessoryStates() {
        return entityData.get(DATA_ACCESSORY_STATES);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (accessor.equals(DATA_ACCESSORIES)) {
            accessories = parseAccessories(entityData.get(DATA_ACCESSORIES));
        }
        if (level().isClientSide() && (accessor.equals(DATA_ACCESSORIES) || accessor.equals(DATA_PROPERTIES) || accessor.equals(DATA_COLOR))) {
            partsDirty = true;
        }
    }

    @Override
    public Optional<UUID> ownerIdentity() {
        return Optional.ofNullable(getOwnerReference()).map(reference -> reference.getUUID());
    }

    @Override
    public <S> Optional<S> accessoryState(GolemAccessoryBehavior<S> behavior) {
        return level().isClientSide() ? syncedAccessoryStates().state(behavior) : accessoryHolder.state(behavior);
    }

    @Override
    public <S> boolean updateAccessoryState(GolemAccessoryBehavior<S> behavior, UnaryOperator<S> update) {
        if (level().isClientSide()) {
            throw new IllegalStateException("Golem accessory state can only change on the server");
        }
        Optional<S> previous = accessoryHolder.state(behavior);
        accessoryHolder.update(behavior, update);
        if (previous.equals(accessoryHolder.state(behavior))) {
            return false;
        }
        syncAccessoryStates();
        return true;
    }

    @Override
    public GolemProperties properties() {
        return entityData.get(DATA_PROPERTIES);
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
        return entityData.get(DATA_COLOR).byteValue();
    }

    private void storeProperties(GolemProperties value) {
        entityData.set(DATA_PROPERTIES, value);
    }

    @Override
    public void swingArm() {
        swing(InteractionHand.MAIN_HAND, false);
    }

    public void paint(byte color) {
        entityData.set(DATA_COLOR, color);
    }

    public boolean isTrailingOwner() {
        return hasFlag(FLAG_FOLLOWING);
    }

    public void setTrailingOwner(boolean following) {
        assignFlag(FLAG_FOLLOWING, following);
    }

    public void recomputeStats() {
        if (level() instanceof ServerLevel) {
            refreshStats(true);
        }
    }

    public float travelSpeed() {
        float speed = 1.0F + RANK_SPEED * properties().rank();
        if (hasTrait(TTGolemTraits.LIGHT)) {
            speed += LIGHT_SPEED;
        }
        if (hasTrait(TTGolemTraits.HEAVY)) {
            speed += HEAVY_SPEED;
        }
        if (hasTrait(TTGolemTraits.FLYER)) {
            speed += FLYER_SPEED;
        }
        if (hasTrait(TTGolemTraits.WHEELED)) {
            speed += WHEELED_SPEED;
        }
        return speed;
    }

    @Override
    public boolean onClimbable() {
        return isScalingWall() || super.onClimbable();
    }

    public boolean isScalingWall() {
        return (entityData.get(DATA_CLIMBING) & CLIMBING_BIT) != 0;
    }

    public void setScalingWall(boolean beside) {
        entityData.set(DATA_CLIMBING, beside ? CLIMBING_BIT : (byte) 0);
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason, @Nullable SpawnGroupData groupData) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, reason, groupData);
        setHomeTo(blockPosition(), GolemStatSheet.HOME_RADIUS);
        refreshStats(true);
        setHealth(getMaxHealth());
        return result;
    }

    @Override
    public void handleEntityEvent(byte id) {
        GolemEvent event = GolemEvent.byId(id);
        if (event != null) {
            event.play(this);
            return;
        }
        super.handleEntityEvent(id);
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return hasTrait(TTGolemTraits.HEAVY) && !hasTrait(TTGolemTraits.FLYER) ? Entity.MovementEmission.ALL : Entity.MovementEmission.NONE;
    }

    @Override
    public void tick() {
        super.tick();
        if (!isRemoved()) {
            runUpkeep();
        }
    }

    private void runUpkeep() {
        if (level().isClientSide()) {
            GolemUpkeep.runClient(this);
            return;
        }
        serverTick();
    }

    private void serverTick() {
        if (isFettered()) {
            holdFettered();
            return;
        }
        GolemUpkeep.runServer(this);
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageModifier, DamageSource source) {
        return !hasTrait(TTGolemTraits.FLYER) && !hasTrait(TTGolemTraits.CLIMBER) && super.causeFallDamage(fallDistance, damageModifier, source);
    }

    @Override
    public void aiStep() {
        super.updateSwingTime();
        super.aiStep();
    }

    @Override
    public boolean isEffectiveAi() {
        return super.isEffectiveAi() && !isFettered();
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource source, float amount) {
        if (negatesDamage(source)) {
            return;
        }
        if (hasHome() && isOutOfBounds(source)) {
            teleportHome();
        }
        super.actuallyHurt(level, source, dampenBlast(source, amount));
    }

    private static boolean isOutOfBounds(DamageSource source) {
        return source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FELL_OUT_OF_WORLD);
    }

    private boolean negatesDamage(DamageSource source) {
        if (source.is(DamageTypes.CACTUS)) {
            return true;
        }
        return source.is(DamageTypeTags.IS_FIRE) && hasTrait(TTGolemTraits.FIREPROOF);
    }

    private float dampenBlast(DamageSource source, float amount) {
        boolean blastResisted = source.is(DamageTypeTags.IS_EXPLOSION) && hasTrait(TTGolemTraits.BLASTPROOF);
        return blastResisted ? Math.min(getMaxHealth() * BLAST_HEALTH_SHARE, BLAST_DAMAGE_FACTOR * amount) : amount;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        boolean nameTagInHand = player.getItemInHand(hand).is(Items.NAME_TAG);
        if (isRemoved() || nameTagInHand) {
            return InteractionResult.PASS;
        }
        if (isOwner(player)) {
            return interactAsOwner(player, hand);
        }
        return super.mobInteract(player, hand);
    }

    private InteractionResult interactAsOwner(Player player, InteractionHand hand) {
        if (level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        GolemOwnerActions.apply(this, player, hand);
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    public void die(DamageSource source) {
        releaseTask();
        super.die(source);
        spillHeldItems();
    }

    protected void spillHeldItems() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (ItemStack carried : hands.contents()) {
            if (!carried.isEmpty()) {
                spawnAtLocation(serverLevel, carried.copy(), CARRIED_DROP_OFFSET);
            }
        }
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean killedByPlayer) {
        dropAccessories(level);
        dropBillComponents(level, GolemBillDrops.chance(lootingLevel(level, source)));
    }

    private void dropBillComponents(ServerLevel level, float chance) {
        properties().components().stream().map(line -> GolemBillDrops.roll(random, line, chance)).filter(surviving -> !surviving.isEmpty())
                .forEach(surviving -> spawnAtLocation(level, surviving, BILL_DROP_OFFSET));
    }

    private static int lootingLevel(ServerLevel level, DamageSource source) {
        ItemStack weapon = source.getWeaponItem();
        if (weapon == null) {
            return 0;
        }
        Holder<Enchantment> looting = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.LOOTING);
        return EnchantmentHelper.getItemEnchantmentLevel(looting, weapon);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        if (!level().isClientSide()) {
            assignFlag(FLAG_COMBAT, getTarget() != null);
        }
    }

    @Override
    public boolean isInCombat() {
        return hasFlag(FLAG_COMBAT);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        boolean connected = super.doHurtTarget(level, target);
        if (connected) {
            afterMeleeHit(target);
        }
        return connected;
    }

    private void afterMeleeHit(Entity target) {
        if (target instanceof LivingEntity living && grantsKillCredit() && getOwner() instanceof Player owner) {
            living.setLastHurtByPlayer(owner, OWNER_CREDIT_TICKS);
        }
        Optional.ofNullable(properties().arms().ability()).ifPresent(arm -> arm.onMeleeHit(this, target));
        if (target instanceof Mob mob && mob.isDeadOrDying()) {
            addRankXp(MELEE_KILL_XP);
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        Optional.ofNullable(properties().arms().ability()).ifPresent(arm -> arm.onRangedAttack(this, target, power));
    }

    public @Nullable Task activeJob() {
        return task;
    }

    public void assignJob(@Nullable Task job) {
        Task previous = task;
        task = job;
        if (previous != job) {
            relinquish(previous);
        }
    }

    private static void relinquish(@Nullable Task finished) {
        if (finished == null || !finished.isClaimed()) {
            return;
        }
        finished.release();
    }

    @Override
    public void addRankXp(int gained) {
        if (!canEarnRank()) {
            return;
        }
        int total = rankXp + gained;
        int cost = xpForNextRank(properties().rank());
        if (total < cost) {
            rankXp = total;
            return;
        }
        rankXp = total - cost;
        rankUp();
    }

    @Override
    public void setProperties(IGolemProperties properties) {
        storeProperties(GolemProperties.of(properties));
    }

    public int getRankXp() {
        return rankXp;
    }

    public void setRankXp(int xp) {
        this.rankXp = xp < 0 ? 0 : xp;
    }

    private boolean canEarnRank() {
        return !level().isClientSide() && hasTrait(TTGolemTraits.SMART) && properties().rank() < MAX_RANK;
    }

    @Override
    public void onRemovedFromLevel() {
        releaseTaskOnServer();
        super.onRemovedFromLevel();
    }

    private void releaseTaskOnServer() {
        if (level().isClientSide()) {
            return;
        }
        releaseTask();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        writeBuildAndHome(output);
        writeProgress(output);
        writeAccessories(output);
    }

    private void writeBuildAndHome(ValueOutput output) {
        output.store(KEY_BUILD, GolemProperties.CODEC, properties());
        output.store(KEY_HOME, BlockPos.CODEC, getHomePosition());
    }

    private void writeProgress(ValueOutput output) {
        output.putByte(KEY_FLAGS, entityData.get(DATA_FLAGS));
        output.putInt(KEY_RANK_XP, rankXp);
        output.putByte(KEY_DYE, color());
    }

    private void writeAccessories(ValueOutput output) {
        output.putString(KEY_ACCESSORIES, entityData.get(DATA_ACCESSORIES));
        if (accessoryHolder.isEmpty()) {
            return;
        }
        accessoryHolder.save(output.child(KEY_ACCESSORY_STATES));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        readBuildAndHome(input);
        byte legacyFlags = LegacyGolemSave.readFlags(input, (byte) 0);
        entityData.set(DATA_FLAGS, input.getByteOr(KEY_FLAGS, legacyFlags));
        Optional<Integer> storedXp = input.getInt(KEY_RANK_XP);
        rankXp = storedXp.isPresent() ? storedXp.get() : LegacyGolemSave.readRankXp(input, 0);
        byte legacyDye = LegacyGolemSave.readDye(input, (byte) 0);
        entityData.set(DATA_COLOR, input.getByteOr(KEY_DYE, legacyDye));
        entityData.set(DATA_ACCESSORIES, input.getStringOr(KEY_ACCESSORIES, ""));
        accessoryHolder.load(input.childOrEmpty(KEY_ACCESSORY_STATES), accessories);
        syncAccessoryStates();
        recomputeStats();
    }

    private void readBuildAndHome(ValueInput input) {
        Optional<GolemProperties> storedBuild = input.read(KEY_BUILD, GolemProperties.CODEC);
        if (storedBuild.isEmpty()) {
            storedBuild = LegacyGolemSave.readBuild(input);
        }
        storedBuild.ifPresent(this::storeProperties);
        Optional<BlockPos> storedHome = input.read(KEY_HOME, BlockPos.CODEC);
        if (storedHome.isEmpty()) {
            storedHome = LegacyGolemSave.readHome(input);
        }
        setHomeTo(storedHome.orElse(BlockPos.ZERO), GolemStatSheet.HOME_RADIUS);
    }

    boolean hasTrait(DeferredHolder<GolemTrait, GolemTrait> trait) {
        return properties().hasTrait(trait.get());
    }

    void releaseTask() {
        assignJob(null);
    }

    void playGolemSound(SoundEvent sound) {
        level().playSound(null, getX(), getY(), getZ(), sound, getSoundSource(), SOUND_VOLUME, SOUND_PITCH);
    }

    void fitAccessory(GolemAccessory accessory, ItemStack held) {
        accessoryHolder.attach(accessory, held);
        List<GolemAccessory> next = new ArrayList<>(accessories);
        next.add(accessory);
        storeAccessories(next);
        syncAccessoryStates();
        recomputeStats();
    }

    void dropAccessories(ServerLevel level) {
        for (GolemAccessory accessory : List.copyOf(accessories)) {
            dropReturnedAccessory(level, accessoryHolder.detach(accessory));
        }
        accessoryHolder.clear();
        entityData.set(DATA_ACCESSORIES, "");
        syncAccessoryStates();
    }

    private void dropReturnedAccessory(ServerLevel level, ItemStack returned) {
        if (returned.isEmpty()) {
            return;
        }
        spawnAtLocation(level, returned.copyWithCount(1), ACCESSORY_DROP_OFFSET);
    }

    private boolean hasFlag(int mask) {
        return (entityData.get(DATA_FLAGS) & mask) == mask;
    }

    private void assignFlag(int mask, boolean on) {
        int current = entityData.get(DATA_FLAGS);
        int updated = on ? current | mask : current & ~mask;
        entityData.set(DATA_FLAGS, (byte) updated);
    }

    private static List<GolemAccessory> parseAccessories(String stored) {
        if (stored.isEmpty()) {
            return List.of();
        }
        List<GolemAccessory> parsed = new ArrayList<>();
        for (String part : stored.split(String.valueOf(ACCESSORY_SEPARATOR))) {
            Identifier id = Identifier.tryParse(part);
            GolemAccessory accessory = id == null ? null : GolemAccessories.get(id);
            if (accessory != null) {
                parsed.add(accessory);
            }
        }
        return List.copyOf(parsed);
    }

    private void storeAccessories(List<GolemAccessory> worn) {
        StringBuilder joined = new StringBuilder();
        for (GolemAccessory accessory : worn) {
            if (!joined.isEmpty()) {
                joined.append(ACCESSORY_SEPARATOR);
            }
            joined.append(accessory.id());
        }
        entityData.set(DATA_ACCESSORIES, joined.toString());
    }

    private void syncAccessoryStates() {
        GolemAccessoryStates snapshot = accessoryHolder.synced();
        int size = snapshot.encodedSize(level().registryAccess());
        if (size > ACCESSORY_SYNC_LIMIT) {
            if (!statesOverBudget) {
                statesOverBudget = true;
                Thaumaturge.LOGGER.error("Golem {} accessory state needs {} bytes, over the sync limit of {}", getUUID(), size, ACCESSORY_SYNC_LIMIT);
            }
            return;
        }
        statesOverBudget = false;
        entityData.set(DATA_ACCESSORY_STATES, snapshot);
    }

    private void refreshStats(boolean rebuildBehaviour) {
        GolemStatSheet.apply(this);
        if (isTrailingOwner()) {
            clearHome();
        } else {
            setHomeTo(hasHome() ? getHomePosition() : blockPosition(), GolemStatSheet.homeRadius(this));
        }
        if (rebuildBehaviour) {
            rebuildBehaviour();
        }
    }

    private void rebuildBehaviour() {
        getNavigation().stop();
        moveControl.setWait();
        for (GoalSelector selector : List.of(targetSelector, goalSelector)) {
            GolemBehaviours.clear(selector);
        }
        moveControl = GolemBehaviours.moveControlFor(this);
        navigation = GolemBehaviours.navigationFor(this);
        GolemBehaviours.assemble(this, goalSelector, targetSelector);
    }

    private boolean grantsKillCredit() {
        return hasTrait(TTGolemTraits.DEFT) || accessories.stream().anyMatch(GolemAccessory::killCredit);
    }

    public boolean isFettered() {
        BlockState underfoot = level().getBlockState(blockPosition().below());
        if (!underfoot.is(TTBlocks.GOLEM_FETTER.get())) {
            return false;
        }
        return underfoot.getValue(BlockGolemFetter.POWERED);
    }

    private void holdFettered() {
        getNavigation().stop();
        setDeltaMovement(Vec3.ZERO);
        setTarget(null);
        releaseTask();
    }

    void holdFlyerAltitude() {
        if (hasTrait(TTGolemTraits.FLYER)) {
            setNoGravity(true);
        }
    }

    void correctHome() {
        if (homeChecked) {
            return;
        }
        homeChecked = true;
        if (hasHome() && !blockPosition().equals(getHomePosition())) {
            teleportHome();
        }
    }

    void forgetEndedTask() {
        if (task != null && task.isEnded()) {
            task = null;
        }
    }

    void tickAccessories() {
        if (accessoryHolder.tick()) {
            syncAccessoryStates();
        }
    }

    void followClimbable() {
        if (hasTrait(TTGolemTraits.CLIMBER)) {
            setScalingWall(horizontalCollision);
        }
    }

    void flagRedraw() {
        if (tickCount <= REDRAW_BURST_TICKS || tickCount % REDRAW_INTERVAL == 0) {
            partsDirty = true;
        }
    }

    void spinWheel() {
        if (hasTrait(TTGolemTraits.WHEELED)) {
            wheelRotation = GolemWheel.spun(wheelRotation, getX() - xo, getZ() - zo, yBodyRot);
        }
    }

    void tickAbilities() {
        GolemProperties props = properties();
        tickAbility(props.head().ability());
        tickAbility(props.arms().ability());
        tickAbility(props.legs().ability());
        tickAbility(props.addon().ability());
    }

    private void tickAbility(@Nullable IGolemPartAbility ability) {
        if (ability != null) {
            ability.tick(this);
        }
    }

    void validateTarget() {
        LivingEntity target = getTarget();
        if (target == null) {
            return;
        }
        boolean tooFar = hasTrait(TTGolemTraits.RANGED) && distanceToSqr(target) > RANGED_TARGET_LIMIT_SQR;
        boolean forbidden = target instanceof Player && !((ServerLevel) level()).isPvpAllowed();
        if (!target.isAlive() || tooFar || forbidden) {
            setTarget(null);
        }
    }

    void regenerate() {
        int interval = Math.max(1, (int) ((hasTrait(TTGolemTraits.REPAIR) ? REPAIR_HEAL_INTERVAL : HEAL_INTERVAL) * GolemStatSheet.regenFactor(accessories)));
        if (tickCount % interval == 0 && getHealth() < getMaxHealth()) {
            heal(1.0F);
        }
    }

    private void teleportHome() {
        BlockPos home = getHomePosition();
        for (int y = home.getY(); y <= level().getMaxY(); y++) {
            Vec3 spot = new Vec3(home.getX() + CENTER, y, home.getZ() + CENTER);
            AABB moved = getBoundingBox().move(spot.subtract(position()));
            if (level().noCollision(this, moved)) {
                teleportTo(spot.x, spot.y, spot.z);
                getNavigation().stop();
                return;
            }
        }
    }

    private void rankUp() {
        storeProperties(properties().withRank(properties().rank() + 1));
        refreshStats(false);
        if (GolemEvent.emotesEnabled()) {
            GolemEvent.RANK_UP.broadcast(this);
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.PLAYER_LEVELUP, getSoundSource(), RANK_UP_VOLUME, SOUND_PITCH);
        }
    }

    static final class GolemFlyingMoveControl extends MoveControl {
        private static final double ARRIVAL_DAMPING = 0.2D;

        private final EntityThaumaturgeGolem golem;

        GolemFlyingMoveControl(EntityThaumaturgeGolem golem) {
            super(golem);
            this.golem = golem;
        }

        private void flyTowardWanted() {
            Vec3 offset = new Vec3(wantedX, wantedY, wantedZ).subtract(golem.position());
            boolean arrived = offset.length() < golem.getBoundingBox().getSize();
            if (arrived) {
                settle();
            } else {
                GolemFlightSteering.steer(golem, offset, speedModifier);
                faceHeading();
            }
        }

        private void settle() {
            operation = Operation.WAIT;
            golem.setDeltaMovement(golem.getDeltaMovement().scale(ARRIVAL_DAMPING));
        }

        private void faceHeading() {
            LivingEntity target = golem.getTarget();
            if (target == null) {
                Vec3 velocity = golem.getDeltaMovement();
                GolemFacing.face(golem, velocity.x, velocity.z);
            } else {
                GolemFacing.face(golem, target.getX() - golem.getX(), target.getZ() - golem.getZ());
            }
        }

        @Override
        public void tick() {
            if (operation == Operation.MOVE_TO) {
                flyTowardWanted();
            }
        }
    }
}
