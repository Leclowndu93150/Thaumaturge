package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreCore;
import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreHost;
import com.leclowndu93150.thaumaturge.content.device.bore.ArcaneBoreTool;
import com.leclowndu93150.thaumaturge.content.device.bore.MenuArcaneBore;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import com.leclowndu93150.thaumaturge.server.TCFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.common.util.FakePlayer;

public class EntityArcaneBore extends EntityOwnedConstruct implements ArcaneBoreHost {
    private static final EntityDataAccessor<Direction> FACING =
            SynchedEntityData.defineId(EntityArcaneBore.class, EntityDataSerializers.DIRECTION);
    private static final EntityDataAccessor<Boolean> ACTIVE =
            SynchedEntityData.defineId(EntityArcaneBore.class, EntityDataSerializers.BOOLEAN);

    private static final int HEAL_INTERVAL = 50;
    private static final double MOVE_DAMPING = 5.0;
    private static final byte EVENT_DIG_START = 16;
    private static final byte EVENT_DIG_STOP = 17;
    private static final int DIG_VISUAL_GRACE_TICKS = 4;
    private static final double EJECT_DISTANCE = 0.75;
    private static final float DISMANTLE_DROP_HEIGHT = 0.5F;
    private static final float COMMON_LOOT_CHANCE = 0.5F;
    private static final float RARE_LOOT_CHANCE = 0.2F;
    private static final double HURT_YAW_SPREAD = 45.0;
    private static final double HURT_PITCH_SPREAD = 20.0;
    private static final double KNOCKBACK_CLAMP = 0.1;

    private final ArcaneBoreCore core = new ArcaneBoreCore();

    public boolean clientDigging;
    private long clientDigStopTime;
    private boolean serverDigging;

    public EntityArcaneBore(EntityType<? extends EntityArcaneBore> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 50.0).add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(FACING, Direction.NORTH);
        entityData.define(ACTIVE, false);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel serverLevel)) {
            return;
        }
        yBodyRot = yHeadRot;
        if (tickCount % HEAL_INTERVAL == 0) {
            heal(1.0F);
        }
        updateActiveFromRedstone();
        core.serverTick(this, serverLevel, tickCount);
    }

    private void updateActiveFromRedstone() {
        BlockPos pos = new BlockPos(Mth.floor(getX()), Mth.floor(getY()), Mth.floor(getZ()));
        BlockState state = level().getBlockState(pos);
        if (!state.is(TCBlocks.ACTIVATOR_RAIL.get())) {
            pos = pos.below();
            state = level().getBlockState(pos);
        }
        if (state.is(TCBlocks.ACTIVATOR_RAIL.get())) {
            setActive(!state.getValue(BlockStateProperties.POWERED));
        } else if (!isPassenger()) {
            setActive(level().hasNeighborSignal(blockPosition().below()));
        }
    }

    @Override
    public Level boreLevel() {
        return level();
    }

    @Override
    public BlockPos borePos() {
        return blockPosition();
    }

    @Override
    public Vec3 borePosition() {
        return position();
    }

    @Override
    public float boreEyeHeight() {
        return getEyeHeight();
    }

    @Override
    public Direction boreFacing() {
        return getFacing();
    }

    @Override
    public boolean boreActive() {
        return isActive();
    }

    @Override
    public RandomSource boreRandom() {
        return random;
    }

    @Override
    public CollisionContext boreCollisionContext() {
        return CollisionContext.of(this);
    }

    @Override
    public ItemStack boreTool() {
        return getMainHandItem();
    }

    @Override
    public void setBoreTool(ItemStack stack) {
        setItemSlot(EquipmentSlot.MAINHAND, stack);
    }

    @Override
    public void hurtBoreTool() {
        getMainHandItem().hurtAndBreak(1, this, EquipmentSlot.MAINHAND);
    }

    @Override
    public void aimBore(double x, double y, double z, float yawStep, float pitchStep) {
        getLookControl().setLookAt(x, y, z, yawStep, pitchStep);
    }

    @Override
    public void setBoreDigging(boolean digging) {
        if (serverDigging == digging) {
            return;
        }
        serverDigging = digging;
        level().broadcastEntityEvent(this, digging ? EVENT_DIG_START : EVENT_DIG_STOP);
    }

    @Override
    public void playBoreSound(SoundEvent sound, float volume, float pitch) {
        playSound(sound, volume, pitch);
    }

    @Override
    public FakePlayer boreDigger(ServerLevel level) {
        return TCFakePlayer.BORE.at(level, this);
    }

    @Override
    public void dropBoreOutput(ServerLevel level, ItemStack stack) {
        Direction back = getFacing().getOpposite();
        level.addFreshEntity(new ItemEntity(
                level,
                getX() + back.getStepX() * EJECT_DISTANCE,
                getY() + 0.5,
                getZ() + back.getStepZ() * EJECT_DISTANCE,
                stack));
    }

    @Override
    public void showBoreDig(ServerLevel level, BlockPos target, int delay) {
        Effects.boreDig(level, target, this, delay);
    }

    @Override
    public float boreHealth() {
        return getHealth();
    }

    @Override
    public float boreMaxHealth() {
        return getMaxHealth();
    }

    @Override
    public boolean boreValid() {
        return isAlive();
    }

    @Override
    public Component boreDisplayName() {
        return getDisplayName();
    }

    @Override
    public void writeBoreRef(RegistryFriendlyByteBuf buf) {
        buf.writeBoolean(false);
        buf.writeVarInt(getId());
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide()) {
            return super.hurt(source, amount);
        }
        if (source.getEntity() instanceof LivingEntity living && isOwner(living)) {
            Direction face =
                    Direction.getNearest(getX() - living.getX(), getY() - living.getY(), getZ() - living.getZ());
            if (face != Direction.DOWN) {
                setFacing(face);
            }
            return false;
        }
        setYRot((float) (getYRot() + random.nextGaussian() * HURT_YAW_SPREAD));
        setXRot((float) (getXRot() + random.nextGaussian() * HURT_PITCH_SPREAD));
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return true;
    }

    @Override
    public void die(DamageSource cause) {
        super.die(cause);
        if (!level().isClientSide()) {
            dropHeld();
        }
    }

    private void dropHeld() {
        if (!getMainHandItem().isEmpty()) {
            spawnAtLocation(getMainHandItem(), DISMANTLE_DROP_HEIGHT);
            setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!level().isClientSide() && isOwner(player) && isAlive()) {
            if (player.isShiftKeyDown()) {
                playSound(TCSounds.ZAP.get(), 1.0F, 1.0F);
                dropHeld();
                spawnAtLocation(new ItemStack(TCItems.ARCANE_BORE.get()), DISMANTLE_DROP_HEIGHT);
                discard();
                player.swing(hand);
            } else {
                MenuArcaneBore.open(player, this);
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public void knockback(double strength, double x, double z) {
        super.knockback(strength, x, z);
        Vec3 movement = getDeltaMovement();
        if (movement.y > KNOCKBACK_CLAMP) {
            setDeltaMovement(movement.x, KNOCKBACK_CLAMP, movement.z);
        }
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, new Vec3(movement.x / MOVE_DAMPING, movement.y, movement.z / MOVE_DAMPING));
    }

    public boolean isActive() {
        return entityData.get(ACTIVE);
    }

    public void setActive(boolean active) {
        entityData.set(ACTIVE, active);
    }

    public Direction getFacing() {
        return entityData.get(FACING);
    }

    public void setFacing(Direction facing) {
        entityData.set(FACING, facing);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        super.readAdditionalSaveData(input);
        core.setCharge(input.getFloat("charge"));
        setFacing(Direction.values()[input.getByte("facing")]);
        setActive(input.getBoolean("active"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        super.addAdditionalSaveData(output);
        output.putFloat("charge", core.charge());
        output.putByte("facing", (byte) getFacing().ordinal());
        output.putBoolean("active", isActive());
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, source, recentlyHit);
        if (random.nextFloat() < RARE_LOOT_CHANCE) {
            spawnAtLocation(new ItemStack(TCItems.MIND_CLOCKWORK.get()), DISMANTLE_DROP_HEIGHT);
        }
        if (random.nextFloat() < RARE_LOOT_CHANCE) {
            spawnAtLocation(new ItemStack(TCItems.MORPHIC_RESONATOR.get()), DISMANTLE_DROP_HEIGHT);
        }
        if (random.nextFloat() < RARE_LOOT_CHANCE) {
            spawnAtLocation(new ItemStack(TCBlocks.CRYSTAL_AER.get()), DISMANTLE_DROP_HEIGHT);
        }
        if (random.nextFloat() < RARE_LOOT_CHANCE) {
            spawnAtLocation(new ItemStack(TCBlocks.CRYSTAL_TERRA.get()), DISMANTLE_DROP_HEIGHT);
        }
        if (random.nextFloat() < COMMON_LOOT_CHANCE) {
            spawnAtLocation(new ItemStack(TCItems.MECHANISM_SIMPLE.get()), DISMANTLE_DROP_HEIGHT);
        }
        if (random.nextFloat() < COMMON_LOOT_CHANCE) {
            spawnAtLocation(new ItemStack(TCItems.PLATE_BRASS.get()), DISMANTLE_DROP_HEIGHT);
        }
        if (random.nextFloat() < COMMON_LOOT_CHANCE) {
            spawnAtLocation(new ItemStack(TCBlocks.PLANK_GREATWOOD.get()), DISMANTLE_DROP_HEIGHT);
        }
    }

    @Override
    public int getMaxHeadXRot() {
        return 90;
    }

    @Override
    public int getHeadRotSpeed() {
        return 10;
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == EVENT_DIG_START) {
            clientDigging = true;
        } else if (event == EVENT_DIG_STOP) {
            clientDigging = false;
            clientDigStopTime = level().getGameTime();
        } else {
            super.handleEntityEvent(event);
        }
    }

    public boolean clientDiggingSmoothed() {
        return clientDigging || level().getGameTime() - clientDigStopTime <= DIG_VISUAL_GRACE_TICKS;
    }

    public boolean validInventory() {
        return ArcaneBoreTool.valid(getMainHandItem());
    }
}
