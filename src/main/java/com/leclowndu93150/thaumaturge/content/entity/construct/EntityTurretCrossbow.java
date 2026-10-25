package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;

public class EntityTurretCrossbow extends EntityOwnedConstruct implements RangedAttackMob {
    private static final String RAIL_SWITCHED_KEY = "rail_switched";
    private static final int HEAL_INTERVAL = 80;
    private static final int SWING_TICKS = 6;
    private static final int LOAD_TICKS = 10;
    private static final byte EVENT_SHOOT = 16;
    private static final byte EVENT_LOAD = 17;
    private static final double BASE_ARROW_DAMAGE = 2.25;
    private static final float ARROW_VELOCITY = 2.0F;
    private static final float ARROW_INACCURACY = 2.0F;
    private static final double MOVE_DAMPING = 20.0;
    private static final double KNOCKBACK_SHARE = 0.2;
    private static final double MAX_KNOCKBACK_RISE = 0.1;

    private int swingProgressInt;
    private boolean swingInProgress;
    private int loadProgressInt;
    private boolean loadInProgress;
    private boolean railSwitched;
    private float loadProgress;
    private float prevLoadProgress;
    public float swingAnim;

    public EntityTurretCrossbow(EntityType<? extends EntityTurretCrossbow> type, Level level) {
        super(type, level);
        this.lookControl = new ConstructLookControl(this);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new StationaryNavigation(this, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 30.0)
                .add(Attributes.FOLLOW_RANGE, 24.0)
                .add(Attributes.ARMOR, 2.0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new RangedAttackGoal(this, 0.0, 20, 60, 24.0F));
        goalSelector.addGoal(2, new WatchTargetGoal(this));
        targetSelector.addGoal(1, retaliateGoal(this));
        targetSelector.addGoal(
                2,
                new NearestAttackableTargetGoal<>(
                        this, LivingEntity.class, 5, true, false, entity -> entity instanceof Enemy));
    }

    static Goal retaliateGoal(EntityTurretCrossbow turret) {
        return new RetaliateGoal(turret);
    }

    protected boolean mayRetaliateAgainst(LivingEntity attacker) {
        return true;
    }

    protected double knockbackShare() {
        return KNOCKBACK_SHARE;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distanceFactor) {
        ItemStack held = getMainHandItem();
        if (held.isEmpty()) {
            return;
        }
        ItemStack arrowStack = held.copyWithCount(1);
        Arrow arrow = new Arrow(level(), this, arrowStack, null);
        arrow.setBaseDamage(BASE_ARROW_DAMAGE + distanceFactor * 2.0F + random.nextGaussian() * 0.25);
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        Vec3 look = getViewVector(1.0F);
        if (!isPassenger()) {
            arrow.setPos(arrow.getX() - look.x * 0.9, arrow.getY() - look.y * 0.9, arrow.getZ() - look.z * 0.9);
        } else {
            arrow.setPos(arrow.getX() + look.x * 1.75, arrow.getY() + look.y * 1.75, arrow.getZ() + look.z * 1.75);
        }
        double dx = target.getX() - getX();
        double dy = target.getBoundingBox().minY
                + target.getEyeHeight()
                + distanceFactor * distanceFactor * 3.0F
                - arrow.getY();
        double dz = target.getZ() - getZ();
        arrow.shoot(dx, dy, dz, ARROW_VELOCITY, ARROW_INACCURACY);
        level().addFreshEntity(arrow);
        level().broadcastEntityEvent(this, EVENT_SHOOT);
        playSound(SoundEvents.ARROW_SHOOT, 1.0F, 1.0F / (random.nextFloat() * 0.4F + 0.8F));
        held.shrink(1);
        if (held.isEmpty()) {
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == EVENT_SHOOT) {
            if (!swingInProgress) {
                swingProgressInt = -1;
                swingInProgress = true;
            }
        } else if (event == EVENT_LOAD) {
            if (!loadInProgress) {
                loadProgressInt = -1;
                loadInProgress = true;
            }
        } else {
            super.handleEntityEvent(event);
        }
    }

    public float getLoadProgress(float partialTicks) {
        float delta = loadProgress - prevLoadProgress;
        if (delta < 0.0F) {
            delta++;
        }
        return prevLoadProgress + delta * partialTicks;
    }

    private void updateArmSwingProgress() {
        if (swingInProgress) {
            swingProgressInt++;
            if (swingProgressInt >= SWING_TICKS) {
                swingProgressInt = 0;
                swingInProgress = false;
            }
        } else {
            swingProgressInt = 0;
        }
        swingAnim = swingProgressInt / (float) SWING_TICKS;
        if (loadInProgress) {
            loadProgressInt++;
            if (loadProgressInt >= LOAD_TICKS) {
                loadProgressInt = 0;
                loadInProgress = false;
            }
        } else {
            loadProgressInt = 0;
        }
        loadProgress = loadProgressInt / (float) LOAD_TICKS;
    }

    @Override
    public void tick() {
        prevLoadProgress = loadProgress;
        if (!level().isClientSide() && getMainHandItem().isEmpty()) {
            refillFromDispenser();
        }
        super.tick();
        if (getTarget() != null && (!getTarget().isAlive() || isAlliedTo(getTarget()))) {
            setTarget(null);
        }
        if (!level().isClientSide()) {
            swivelWithHead();
            regenerate(HEAL_INTERVAL, 1.0F);
            followActivatorRail();
        } else {
            updateArmSwingProgress();
        }
    }

    private void followActivatorRail() {
        BlockState rail = railStateBelow();
        if (rail == null || !rail.is(TTBlocks.ACTIVATOR_RAIL.get())) {
            if (railSwitched) {
                railSwitched = false;
                setNoAi(false);
            }
            return;
        }
        boolean powered = rail.getValue(BlockStateProperties.POWERED);
        setNoAi(powered);
        railSwitched = powered;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        super.addAdditionalSaveData(output);
        output.putBoolean(RAIL_SWITCHED_KEY, railSwitched);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        super.readAdditionalSaveData(input);
        railSwitched = input.getBoolean(RAIL_SWITCHED_KEY);
    }

    protected BlockState railStateBelow() {
        int x = Mth.floor(getX());
        int y = Mth.floor(getY());
        int z = Mth.floor(getZ());
        BlockPos pos = new BlockPos(x, y, z);
        BlockState state = level().getBlockState(pos);
        if (state.is(TTBlocks.ACTIVATOR_RAIL.get())) {
            return state;
        }
        BlockState below = level().getBlockState(pos.below());
        if (below.is(TTBlocks.ACTIVATOR_RAIL.get())) {
            return below;
        }
        return null;
    }

    private void refillFromDispenser() {
        BlockPos below = blockPosition().below();
        BlockState state = level().getBlockState(below);
        if (!(level().getBlockEntity(below) instanceof DispenserBlockEntity dispenser)
                || !state.hasProperty(DispenserBlock.FACING)
                || state.getValue(DispenserBlock.FACING) != Direction.UP) {
            return;
        }
        for (int slot = 0; slot < dispenser.getContainerSize(); slot++) {
            ItemStack stack = dispenser.getItem(slot);
            if (!stack.isEmpty() && isValidAmmo(stack)) {
                setItemSlot(EquipmentSlot.MAINHAND, dispenser.removeItem(slot, stack.getCount()));
                playSound(TTSounds.TICKS.get(), 1.0F, 1.0F);
                level().broadcastEntityEvent(this, EVENT_LOAD);
                break;
            }
        }
    }

    public boolean isValidAmmo(ItemStack stack) {
        return stack.is(Items.ARROW) || stack.is(Items.TIPPED_ARROW) || stack.is(Items.SPECTRAL_ARROW);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!(level() instanceof ServerLevel level)) return false;
        jolt();
        return super.hurt(source, amount);
    }

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(knockbackPower(strength, knockbackShare()), x, z);
        Vec3 movement = getDeltaMovement();
        if (movement.y > MAX_KNOCKBACK_RISE) {
            setDeltaMovement(movement.x, MAX_KNOCKBACK_RISE, movement.z);
        }
    }

    static double knockbackPower(double power, double share) {
        return power * share;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isOwner(player) && isAlive()) {
            if (level().isClientSide()) {
                return InteractionResult.SUCCESS;
            }
            if (player.isShiftKeyDown()) {
                playSound(TTSounds.ZAP.get(), 1.0F, 1.0F);
                dropAmmo();
                spawnAtLocation(placerItem(), 0.5F);
                discard();
                player.swing(hand);
            } else {
                openTurretMenu(player);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    protected ItemStack placerItem() {
        return new ItemStack(TTItems.TURRET_BASIC.get());
    }

    protected void openTurretMenu(Player player) {
        MenuTurretBasic.open(player, this);
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, dampHorizontalMovement(type, movement, moveDamping()));
    }

    protected double moveDamping() {
        return MOVE_DAMPING;
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (!level().isClientSide()) {
            dropAmmo();
        }
    }

    protected void dropAmmo() {
        if (!getMainHandItem().isEmpty() && level() instanceof ServerLevel serverLevel) {
            spawnAtLocation(getMainHandItem(), 0.5F);
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    public int getMaxHeadXRot() {
        return 90;
    }

    @Override
    public int getHeadRotSpeed() {
        return 20;
    }

    private static final class RetaliateGoal extends HurtByTargetGoal {
        private final EntityTurretCrossbow turret;

        private RetaliateGoal(EntityTurretCrossbow turret) {
            super(turret);
            this.turret = turret;
        }

        @Override
        public boolean canUse() {
            LivingEntity attacker = turret.getLastHurtByMob();
            return super.canUse() && attacker != null && turret.mayRetaliateAgainst(attacker);
        }
    }
}
