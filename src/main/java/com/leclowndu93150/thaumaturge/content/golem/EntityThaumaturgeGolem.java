package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.IGolemProperties;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessories;
import com.leclowndu93150.thaumaturge.api.golems.accessory.GolemAccessory;
import com.leclowndu93150.thaumaturge.api.golems.parts.IGolemFunction;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.construct.ConstructFollowOwnerGoal;
import com.leclowndu93150.thaumaturge.content.entity.construct.ConstructOwnerHurtByTargetGoal;
import com.leclowndu93150.thaumaturge.content.entity.construct.ConstructOwnerHurtTargetGoal;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityOwnedConstruct;
import com.leclowndu93150.thaumaturge.content.golem.ai.GotoBlockGoal;
import com.leclowndu93150.thaumaturge.content.golem.ai.GotoEntityGoal;
import com.leclowndu93150.thaumaturge.content.golem.ai.GotoHomeGoal;
import com.leclowndu93150.thaumaturge.content.particle.GolemEmoteParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCEntityDataSerializers;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.ARGB;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public class EntityThaumaturgeGolem extends EntityOwnedConstruct implements IGolemAPI, RangedAttackMob {
    public static final int XP_PER_RANK_UNIT = 1000;
    public static final int MAX_RANK = 10;

    private static final EntityDataAccessor<GolemProperties> PROPS = SynchedEntityData.defineId(EntityThaumaturgeGolem.class, TCEntityDataSerializers.GOLEM_PROPERTIES.get());
    private static final EntityDataAccessor<Byte> COLOR = SynchedEntityData.defineId(EntityThaumaturgeGolem.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(EntityThaumaturgeGolem.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Byte> CLIMBING = SynchedEntityData.defineId(EntityThaumaturgeGolem.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<String> ACCESSORIES = SynchedEntityData.defineId(EntityThaumaturgeGolem.class, EntityDataSerializers.STRING);

    private static final int FLAG_FOLLOWING = 1 << 1;
    private static final int FLAG_COMBAT = 1 << 3;
    private static final int HOME_RANGE = 32;
    private static final int HOME_RANGE_SCOUT = 48;
    private static final double BASE_MOVEMENT_SPEED = 0.3;
    private static final int RANGED_TARGET_FORGET_DIST_SQR = 1024;
    private static final int EVENT_EMOTE_TASK = 5;
    private static final int EVENT_EMOTE_FAIL = 6;
    private static final int EVENT_EMOTE_CONFUSED = 7;
    private static final int EVENT_EMOTE_STAY = 8;
    private static final int EVENT_EMOTE_RANKUP = 9;

    public boolean redrawParts;
    public float wheelRotation;
    public float grinderRot;
    public float grinderSpeed;
    int rankXp;
    private boolean firstRun = true;
    private Task task;

    public EntityThaumaturgeGolem(EntityType<? extends EntityThaumaturgeGolem> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.MAX_HEALTH, 10.0).add(Attributes.ATTACK_DAMAGE, 0.0).add(Attributes.FOLLOW_RANGE, 40.0)
                .add(Attributes.ARMOR, 2.0).add(Attributes.STEP_HEIGHT, 0.6);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(PROPS, GolemProperties.createDefault());
        entityData.define(COLOR, (byte) 0);
        entityData.define(FLAGS, (byte) 0);
        entityData.define(CLIMBING, (byte) 0);
        entityData.define(ACCESSORIES, "");
    }

    public String getAccessoryString() {
        return entityData.get(ACCESSORIES);
    }

    public List<GolemAccessory> getAccessories() {
        String joined = entityData.get(ACCESSORIES);
        if (joined.isEmpty()) {
            return List.of();
        }
        List<GolemAccessory> accessories = new ArrayList<>();
        for (String id : joined.split(",")) {
            GolemAccessory accessory = GolemAccessories.get(Identifier.parse(id));
            if (accessory != null) {
                accessories.add(accessory);
            }
        }
        return accessories;
    }

    private boolean addAccessory(GolemAccessory accessory) {
        List<GolemAccessory> current = getAccessories();
        for (GolemAccessory worn : current) {
            if (worn == accessory) {
                return false;
            }
            if (accessory.group() != GolemAccessory.Group.NONE && worn.group() == accessory.group()) {
                return false;
            }
        }
        String joined = entityData.get(ACCESSORIES);
        entityData.set(ACCESSORIES, joined.isEmpty() ? accessory.id().toString() : joined + "," + accessory.id());
        updateEntityAttributes();
        return true;
    }

    private void dropAccessories() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (GolemAccessory accessory : getAccessories()) {
            ItemStack stack = ItemGolemAccessory.stackFor(accessory);
            if (!stack.isEmpty()) {
                spawnAtLocation(serverLevel, stack, 0.5F);
            }
        }
        entityData.set(ACCESSORIES, "");
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
    protected void registerGoals() {
        goalSelector.addGoal(2, new GotoEntityGoal(this));
        goalSelector.addGoal(3, new GotoBlockGoal(this));
        goalSelector.addGoal(4, new GotoHomeGoal(this));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
    }

    @Override
    public IGolemProperties getProperties() {
        return entityData.get(PROPS);
    }

    @Override
    public void setProperties(IGolemProperties properties) {
        entityData.set(PROPS, ((GolemProperties) properties).copy());
    }

    private GolemProperties props() {
        return entityData.get(PROPS);
    }

    @Override
    public byte getGolemColor() {
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
    public boolean isInCombat() {
        return (getFlags() & FLAG_COMBAT) != 0;
    }

    private void setInCombat(boolean inCombat) {
        setFlag(FLAG_COMBAT, inCombat);
    }

    public void updateEntityAttributes() {
        GolemProperties props = props();
        List<GolemAccessory> accessories = getAccessories();
        int accessoryHealth = 0;
        int accessoryArmor = 0;
        float rangeFactor = 1.0F;
        float speedFactor = 1.0F;
        for (GolemAccessory accessory : accessories) {
            accessoryHealth += accessory.healthBonus();
            accessoryArmor += accessory.armorBonus();
            rangeFactor *= accessory.rangeFactor();
            speedFactor *= accessory.speedFactor();
        }
        int maxHealth = 10 + props.getMaterial().healthMod();
        if (props.hasTrait(TCGolemTraits.FRAGILE.get())) {
            maxHealth = (int) (maxHealth * 0.75);
        }
        maxHealth += props.getRank() + accessoryHealth;
        getAttribute(Attributes.MAX_HEALTH).setBaseValue(maxHealth);
        getAttribute(Attributes.STEP_HEIGHT).setBaseValue(props.hasTrait(TCGolemTraits.WHEELED.get()) ? 0.5 : 0.6);
        getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(BASE_MOVEMENT_SPEED * speedFactor);
        int homeRange = props.hasTrait(TCGolemTraits.SCOUT.get()) ? HOME_RANGE_SCOUT : HOME_RANGE;
        setHomeTo(getHomePosition().equals(BlockPos.ZERO) ? blockPosition() : getHomePosition(), (int) (homeRange * rangeFactor));
        getAttribute(Attributes.FOLLOW_RANGE).setBaseValue((props.hasTrait(TCGolemTraits.SCOUT.get()) ? 56.0 : 40.0) * rangeFactor);
        getAttribute(Attributes.ARMOR).setBaseValue(computeArmor(props) + accessoryArmor);
        this.navigation = createGolemNavigation();
        if (props.hasTrait(TCGolemTraits.FLYER.get())) {
            this.moveControl = new GolemFlyingMoveControl(this);
        }
        if (props.hasTrait(TCGolemTraits.FIGHTER.get())) {
            double damage = props.getMaterial().damage();
            if (props.hasTrait(TCGolemTraits.BRUTAL.get())) {
                damage = Math.max(damage * 1.5, damage + 1.0);
            }
            damage += props.getRank() * 0.25;
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(damage);
        } else {
            getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.0);
        }
        createAI();
    }

    private static int computeArmor(GolemProperties props) {
        int armor = props.getMaterial().armor();
        if (props.hasTrait(TCGolemTraits.ARMORED.get())) {
            armor = (int) Math.max(armor * 1.5, armor + 1);
        }
        if (props.hasTrait(TCGolemTraits.FRAGILE.get())) {
            armor = (int) (armor * 0.75);
        }
        return armor;
    }

    private void createAI() {
        goalSelector.removeAllGoals(goal -> true);
        targetSelector.removeAllGoals(goal -> true);
        if (isFollowingOwner()) {
            goalSelector.addGoal(4, new ConstructFollowOwnerGoal(this, 1.0, 10.0F, 2.0F));
        } else {
            goalSelector.addGoal(3, new GotoEntityGoal(this));
            goalSelector.addGoal(4, new GotoBlockGoal(this));
            goalSelector.addGoal(5, new GotoHomeGoal(this));
        }
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
        if (props().hasTrait(TCGolemTraits.FIGHTER.get())) {
            if (navigation instanceof GroundPathNavigation) {
                goalSelector.addGoal(0, new FloatGoal(this));
            }
            if (props().hasTrait(TCGolemTraits.RANGED.get()) && props().getArms().function() != null) {
                Goal rangedGoal = props().getArms().function().createRangedAttackGoal(this);
                if (rangedGoal != null) {
                    goalSelector.addGoal(1, rangedGoal);
                }
            }
            goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.15, false));
            if (isFollowingOwner()) {
                targetSelector.addGoal(1, new ConstructOwnerHurtByTargetGoal(this));
                targetSelector.addGoal(2, new ConstructOwnerHurtTargetGoal(this));
            }
            targetSelector.addGoal(3, new HurtByTargetGoal(this));
        }
    }

    private PathNavigation createGolemNavigation() {
        if (props().hasTrait(TCGolemTraits.FLYER.get())) {
            FlyingPathNavigation nav = new FlyingPathNavigation(this, level());
            nav.setCanFloat(true);
            return nav;
        }
        if (props().hasTrait(TCGolemTraits.CLIMBER.get())) {
            return new WallClimberNavigation(this, level());
        }
        return new GroundPathNavigation(this, level());
    }

    public float getGolemMoveSpeed() {
        GolemProperties props = props();
        return 1.0F + props.getRank() * 0.025F + (props.hasTrait(TCGolemTraits.LIGHT.get()) ? 0.2F : 0.0F) + (props.hasTrait(TCGolemTraits.HEAVY.get()) ? -0.175F : 0.0F)
                + (props.hasTrait(TCGolemTraits.FLYER.get()) ? -0.33F : 0.0F) + (props.hasTrait(TCGolemTraits.WHEELED.get()) ? 0.25F : 0.0F);
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
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason spawnReason, @Nullable SpawnGroupData spawnGroupData) {
        setHomeTo(blockPosition(), HOME_RANGE);
        updateEntityAttributes();
        return spawnGroupData;
    }

    @Override
    protected Entity.MovementEmission getMovementEmission() {
        return props().hasTrait(TCGolemTraits.HEAVY.get()) && !props().hasTrait(TCGolemTraits.FLYER.get()) ? Entity.MovementEmission.ALL : Entity.MovementEmission.NONE;
    }

    @Override
    public boolean causeFallDamage(double fallDistance, float damageMultiplier, DamageSource source) {
        if (props().hasTrait(TCGolemTraits.FLYER.get()) || props().hasTrait(TCGolemTraits.CLIMBER.get())) {
            return false;
        }
        return super.causeFallDamage(fallDistance, damageMultiplier, source);
    }

    @Override
    public void tick() {
        super.tick();
        GolemProperties props = props();
        if (props.hasTrait(TCGolemTraits.FLYER.get())) {
            setNoGravity(true);
        }
        if (!level().isClientSide()) {
            if (firstRun) {
                firstRun = false;
                if (hasHome() && !blockPosition().equals(getHomePosition())) {
                    goHome();
                }
            }
            if (task != null && task.isSuspended()) {
                task = null;
            }
            if (getTarget() != null && !getTarget().isAlive()) {
                setTarget(null);
            }
            if (getTarget() != null && props.hasTrait(TCGolemTraits.RANGED.get()) && distanceToSqr(getTarget()) > RANGED_TARGET_FORGET_DIST_SQR) {
                setTarget(null);
            }
            if (level() instanceof ServerLevel serverLevel && !serverLevel.isPvpAllowed() && getTarget() instanceof Player) {
                setTarget(null);
            }
            int healInterval = (int) ((props.hasTrait(TCGolemTraits.REPAIR.get()) ? 40 : 100) * accessoryRegenFactor());
            if (tickCount % Math.max(1, healInterval) == 0) {
                heal(1.0F);
            }
            if (props.hasTrait(TCGolemTraits.CLIMBER.get())) {
                setBesideClimbableBlock(horizontalCollision);
            }
        } else {
            if (tickCount < 20 || tickCount % 20 == 0) {
                redrawParts = true;
            }
            if (props.hasTrait(TCGolemTraits.WHEELED.get())) {
                updateWheelRotation();
            }
        }
        tickPartFunction(props.getHead().function());
        tickPartFunction(props.getArms().function());
        tickPartFunction(props.getLegs().function());
        tickPartFunction(props.getAddon().function());
    }

    private void tickPartFunction(@Nullable IGolemFunction function) {
        if (function != null) {
            function.onUpdateTick(this);
        }
    }

    private void updateWheelRotation() {
        double dist = Math.sqrt(distanceToSqr(xOld, yOld, zOld));
        double dx = getX() - xOld;
        double dz = getZ() - zOld;
        float travelDir = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0F;
        double dir = 360.0F - (getYRot() - travelDir);
        wheelRotation = (float) (wheelRotation + dist / 1.571 * dir);
        if (wheelRotation > 360.0F) {
            wheelRotation -= 360.0F;
        }
    }

    private void goHome() {
        double oldX = getX();
        double oldY = getY();
        double oldZ = getZ();
        double homeX = getHomePosition().getX() + 0.5;
        double homeY = getHomePosition().getY();
        double homeZ = getHomePosition().getZ() + 0.5;
        BlockPos probe = BlockPos.containing(homeX, homeY, homeZ);
        boolean foundCeiling = false;
        while (!foundCeiling && probe.getY() < level().getMaxY()) {
            BlockPos above = probe.above();
            if (!level().getBlockState(above).getCollisionShape(level(), above).isEmpty()) {
                foundCeiling = true;
            } else {
                homeY++;
                probe = above;
            }
        }
        boolean placed = false;
        if (foundCeiling) {
            teleportTo(homeX, homeY, homeZ);
            if (level().noCollision(this, getBoundingBox())) {
                placed = true;
            }
        }
        if (!placed) {
            teleportTo(oldX, oldY, oldZ);
        } else {
            getNavigation().stop();
        }
    }

    @Override
    protected void actuallyHurt(ServerLevel level, DamageSource source, float damage) {
        GolemProperties props = props();
        if (source.is(DamageTypeTags.IS_FIRE) && props.hasTrait(TCGolemTraits.FIREPROOF.get())) {
            return;
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION) && props.hasTrait(TCGolemTraits.BLASTPROOF.get())) {
            damage = Math.min(getMaxHealth() / 2.0F, damage * 0.3F);
        }
        if (source.is(DamageTypes.CACTUS)) {
            return;
        }
        if (hasHome() && (source.is(DamageTypes.IN_WALL) || source.is(DamageTypes.FELL_OUT_OF_WORLD))) {
            goHome();
        }
        super.actuallyHurt(level, source, damage);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isRemoved() || player.getItemInHand(hand).is(Items.NAME_TAG)) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide() || !isOwner(player)) {
            return super.mobInteract(player, hand);
        }
        if (player.isShiftKeyDown()) {
            pickUpGolem(player, hand);
            return InteractionResult.SUCCESS_SERVER;
        }
        if (player.getItemInHand(hand).is(TCItems.GOLEM_BELL.get())) {
            toggleFollow(player, hand);
            return InteractionResult.SUCCESS_SERVER;
        }
        if (player.getItemInHand(hand).getItem() instanceof ItemGolemAccessory accessoryItem) {
            if (addAccessory(accessoryItem.accessory())) {
                playSound(TCSounds.CLACK.get(), 1.0F, 1.0F);
                player.getItemInHand(hand).shrink(1);
                player.swing(hand, true);
            }
            return InteractionResult.SUCCESS_SERVER;
        }
        DyeColor dyeColor = player.getItemInHand(hand).get(DataComponents.DYE);
        if (dyeColor != null) {
            playSound(TCSounds.ZAP.get(), 1.0F, 1.0F);
            setGolemColor((byte) (1 + dyeColor.getId()));
            player.getItemInHand(hand).shrink(1);
            player.swing(hand, true);
            return InteractionResult.SUCCESS_SERVER;
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    private void pickUpGolem(Player player, InteractionHand hand) {
        playSound(TCSounds.ZAP.get(), 1.0F, 1.0F);
        if (task != null) {
            task.setReserved(false);
        }
        dropCarried();
        dropAccessories();
        ItemStack placer = new ItemStack(TCItems.GOLEM_PLACER.get());
        placer.set(TCDataComponents.GOLEM_PROPERTIES.get(), props().copy());
        placer.set(TCDataComponents.GOLEM_XP.get(), rankXp);
        spawnAtLocation((ServerLevel) level(), placer, 0.5F);
        discard();
        player.swing(hand, true);
    }

    private void toggleFollow(Player player, InteractionHand hand) {
        if (task != null) {
            task.setReserved(false);
        }
        playSound(TCSounds.SCAN.get(), 1.0F, 1.0F);
        setFollowingOwner(!isFollowingOwner());
        if (isFollowingOwner()) {
            sendActionBar(player, "golem.follow");
            if (ThaumaturgeCommonConfig.SHOW_GOLEM_EMOTES.get()) {
                level().broadcastEntityEvent(this, (byte) EVENT_EMOTE_TASK);
            }
            clearHome();
        } else {
            sendActionBar(player, "golem.stay");
            if (ThaumaturgeCommonConfig.SHOW_GOLEM_EMOTES.get()) {
                level().broadcastEntityEvent(this, (byte) EVENT_EMOTE_STAY);
            }
            setHomeTo(blockPosition(), props().hasTrait(TCGolemTraits.SCOUT.get()) ? HOME_RANGE_SCOUT : HOME_RANGE);
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
        if (task != null) {
            task.setReserved(false);
        }
        super.die(cause);
        if (!level().isClientSide()) {
            dropCarried();
        }
    }

    protected void dropCarried() {
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        for (ItemStack stack : getCarrying()) {
            if (!stack.isEmpty()) {
                spawnAtLocation(serverLevel, stack, 0.25F);
            }
        }
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean playerKill) {
        super.dropCustomDeathLoot(level, source, playerKill);
        dropAccessories();
        for (ItemStack stack : props().generateComponents()) {
            ItemStack copy = stack.copy();
            if (random.nextFloat() < 0.3F) {
                if (copy.getCount() > 0) {
                    copy.shrink(random.nextInt(copy.getCount()));
                }
                spawnAtLocation(level, copy, 0.25F);
            }
        }
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        super.setTarget(target);
        setInCombat(getTarget() != null);
    }

    @Override
    public boolean doHurtTarget(ServerLevel level, Entity target) {
        float damage = (float) getAttributeValue(Attributes.ATTACK_DAMAGE);
        boolean hurt = target.hurtServer(level, damageSources().mobAttack(this), damage);
        if (hurt) {
            if (target instanceof LivingEntity living && (props().hasTrait(TCGolemTraits.DEFT.get()) || hasKillCreditAccessory()) && getOwnerReference() != null) {
                living.setLastHurtByPlayer(getOwnerReference().getUUID(), 100);
            }
            if (props().getArms().function() != null) {
                props().getArms().function().onMeleeAttack(this, target);
            }
            if (target instanceof Mob mob && !mob.isAlive()) {
                addRankXp(8);
            }
        }
        return hurt;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (props().getArms().function() != null) {
            props().getArms().function().onRangedAttack(this, target, power);
        }
    }

    public @Nullable Task getTask() {
        return task;
    }

    public void setTask(@Nullable Task task) {
        this.task = task;
    }

    public int getRankXp() {
        return rankXp;
    }

    public void setRankXp(int rankXp) {
        this.rankXp = rankXp;
    }

    @Override
    public void addRankXp(int xp) {
        if (!props().hasTrait(TCGolemTraits.SMART.get()) || level().isClientSide()) {
            return;
        }
        int rank = props().getRank();
        if (rank >= MAX_RANK) {
            return;
        }
        rankXp += xp;
        int needed = (rank + 1) * (rank + 1) * XP_PER_RANK_UNIT;
        if (rankXp >= needed) {
            rankXp -= needed;
            GolemProperties props = props().copy();
            props.setRank(rank + 1);
            setProperties(props);
            if (ThaumaturgeCommonConfig.SHOW_GOLEM_EMOTES.get()) {
                level().broadcastEntityEvent(this, (byte) EVENT_EMOTE_RANKUP);
                playSound(SoundEvents.PLAYER_LEVELUP, 0.25F, 1.0F);
            }
        }
    }

    @Override
    public ItemStack holdItem(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return stack;
        }
        int slots = props().hasTrait(TCGolemTraits.HAULER.get()) ? 2 : 1;
        for (int i = 0; i < slots; i++) {
            EquipmentSlot slot = i == 0 ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            ItemStack held = getItemBySlot(slot);
            if (held.isEmpty()) {
                setItemSlot(slot, stack);
                return ItemStack.EMPTY;
            }
            if (held.getCount() < held.getMaxStackSize() && ItemStack.isSameItemSameComponents(held, stack)) {
                int transfer = Math.min(stack.getCount(), held.getMaxStackSize() - held.getCount());
                stack.shrink(transfer);
                held.grow(transfer);
                if (stack.getCount() <= 0) {
                    return ItemStack.EMPTY;
                }
            }
        }
        return stack;
    }

    @Override
    public ItemStack dropItem(ItemStack stack) {
        ItemStack out = ItemStack.EMPTY;
        int slots = props().hasTrait(TCGolemTraits.HAULER.get()) ? 2 : 1;
        for (int i = 0; i < slots; i++) {
            EquipmentSlot slot = i == 0 ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            ItemStack held = getItemBySlot(slot);
            if (held.isEmpty()) {
                continue;
            }
            if (stack != null && !stack.isEmpty()) {
                if (ItemStack.isSameItemSameComponents(held, stack)) {
                    out = held.copy();
                    out.setCount(Math.min(stack.getCount(), out.getCount()));
                    held.shrink(stack.getCount());
                    if (held.getCount() <= 0) {
                        setItemSlot(slot, ItemStack.EMPTY);
                    }
                }
            } else {
                out = held.copy();
                setItemSlot(slot, ItemStack.EMPTY);
            }
            if (!out.isEmpty()) {
                break;
            }
        }
        if (props().hasTrait(TCGolemTraits.HAULER.get()) && getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && !getItemBySlot(EquipmentSlot.OFFHAND).isEmpty()) {
            setItemSlot(EquipmentSlot.MAINHAND, getItemBySlot(EquipmentSlot.OFFHAND).copy());
            setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
        return out;
    }

    @Override
    public int canCarryAmount(ItemStack stack) {
        int space = 0;
        int slots = props().hasTrait(TCGolemTraits.HAULER.get()) ? 2 : 1;
        for (int i = 0; i < slots; i++) {
            EquipmentSlot slot = i == 0 ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            ItemStack held = getItemBySlot(slot);
            if (held.isEmpty()) {
                space += stack.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(held, stack)) {
                space += held.getMaxStackSize() - held.getCount();
            }
        }
        return space;
    }

    @Override
    public boolean canCarry(ItemStack stack, boolean partial) {
        int space = canCarryAmount(stack);
        return space > 0 && (partial || space >= stack.getCount());
    }

    @Override
    public boolean isCarrying(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return false;
        }
        int slots = props().hasTrait(TCGolemTraits.HAULER.get()) ? 2 : 1;
        for (int i = 0; i < slots; i++) {
            EquipmentSlot slot = i == 0 ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            ItemStack held = getItemBySlot(slot);
            if (!held.isEmpty() && ItemStack.isSameItemSameComponents(held, stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ItemStack> getCarrying() {
        if (props().hasTrait(TCGolemTraits.HAULER.get())) {
            return List.of(getItemBySlot(EquipmentSlot.MAINHAND), getItemBySlot(EquipmentSlot.OFFHAND));
        }
        return List.of(getItemBySlot(EquipmentSlot.MAINHAND));
    }

    @Override
    public LivingEntity getGolemEntity() {
        return this;
    }

    @Override
    public Level getGolemWorld() {
        return level();
    }

    @Override
    public void swingArm() {
        swing(InteractionHand.MAIN_HAND, true);
    }

    @Override
    public void handleEntityEvent(byte id) {
        switch (id) {
            case EVENT_EMOTE_TASK -> emote(0.0, 1.0F, 1.0F, 1.0F, GolemEmoteParticleOptions.ICON_TASK, 6, 2.0F);
            case EVENT_EMOTE_FAIL -> emote(0.025, 0.1F, 1.0F, 1.0F, GolemEmoteParticleOptions.ICON_FAIL, 10, 2.0F);
            case EVENT_EMOTE_CONFUSED -> emote(0.05, 1.0F, 1.0F, 1.0F, GolemEmoteParticleOptions.ICON_CONFUSED, 10, 2.0F);
            case EVENT_EMOTE_STAY -> emote(0.01, 1.0F, 1.0F, 0.1F, GolemEmoteParticleOptions.ICON_STAY, 20, 2.0F);
            case EVENT_EMOTE_RANKUP -> {
                for (int i = 0; i < 5; i++) {
                    GolemEmoteParticleOptions data = new GolemEmoteParticleOptions(0xFFFFFF, GolemEmoteParticleOptions.ICON_HEART, 20 + random.nextInt(20), 0.3F + random.nextFloat() * 0.4F);
                    level().addParticle(data, getX(), getY() + getBbHeight(), getZ(), random.nextGaussian() * 0.01F, random.nextFloat() * 0.02, random.nextGaussian() * 0.01F);
                }
            }
            default -> super.handleEntityEvent(id);
        }
    }

    private void emote(double vy, float r, float g, float b, int icon, int age, float scale) {
        GolemEmoteParticleOptions data = new GolemEmoteParticleOptions(ARGB.colorFromFloat(1.0F, r, g, b), icon, age, scale);
        level().addParticle(data, getX(), getY() + getBbHeight() + 0.1, getZ(), 0.0, vy, 0.0);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store("props", GolemProperties.CODEC, props());
        output.store("homepos", BlockPos.CODEC, getHomePosition());
        output.putByte("gflags", getFlags());
        output.putInt("rankXP", rankXp);
        output.putByte("color", getGolemColor());
        output.putString("accessories", entityData.get(ACCESSORIES));
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        input.read("props", GolemProperties.CODEC).ifPresent(this::setProperties);
        setHomeTo(input.read("homepos", BlockPos.CODEC).orElse(BlockPos.ZERO), HOME_RANGE);
        entityData.set(FLAGS, input.getByteOr("gflags", (byte) 0));
        rankXp = input.getIntOr("rankXP", 0);
        setGolemColor(input.getByteOr("color", (byte) 0));
        entityData.set(ACCESSORIES, input.getStringOr("accessories", ""));
        updateEntityAttributes();
    }

    static final class GolemFlyingMoveControl extends MoveControl {
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
                golem.setDeltaMovement(golem.getDeltaMovement().scale(0.5));
            } else {
                Vec3 motion = golem.getDeltaMovement();
                golem.setDeltaMovement(motion.add(dx / dist * 0.033 * speedModifier, dy / dist * 0.0125 * speedModifier, dz / dist * 0.033 * speedModifier));
                if (golem.getTarget() == null) {
                    golem.setYRot(-((float) Mth.atan2(golem.getDeltaMovement().x, golem.getDeltaMovement().z)) * (180.0F / (float) Math.PI));
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
