package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fish.WaterAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.PlayerTeam;

public class EntityTurretCrossbowAdvanced extends EntityTurretCrossbow {
    private static final EntityDataAccessor<Byte> FLAGS = SynchedEntityData.defineId(EntityTurretCrossbowAdvanced.class, EntityDataSerializers.BYTE);
    private static final int BIT_ANIMAL = 1;
    private static final int BIT_MOB = 2;
    private static final int BIT_PLAYER = 4;
    private static final int BIT_FRIENDLY = 8;
    private static final double MOVE_DAMPING = 15.0;

    public EntityTurretCrossbowAdvanced(EntityType<? extends EntityTurretCrossbowAdvanced> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAdvancedAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40.0).add(Attributes.FOLLOW_RANGE, 24.0).add(Attributes.ARMOR, 8.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(FLAGS, (byte) 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new RangedAttackGoal(this, 0.0, 20, 40, 24.0F));
        goalSelector.addGoal(2, new WatchTargetGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 5, true, false, (entity, level) -> isValidTarget(entity)));
        setTargetMob(true);
    }

    public boolean isValidTarget(LivingEntity entity) {
        if (entity == this || !entity.isAlive() || !matchesTargetKind(entity)) {
            return false;
        }
        PlayerTeam team = getTeam();
        PlayerTeam targetTeam = entity.getTeam();
        if (team != null && targetTeam == team && !getTargetFriendly()) {
            return false;
        }
        if (team != null && targetTeam != team && getTargetFriendly()) {
            return false;
        }
        if (isOwned() && getOwnerReference() != null) {
            if (entity instanceof OwnableEntity ownable && sameOwner(ownable) && !getTargetFriendly()) {
                return false;
            }
            if (!(entity instanceof OwnableEntity) && !(entity instanceof Player) && getTargetFriendly()) {
                return false;
            }
            if (entity instanceof OwnableEntity ownable && !sameOwner(ownable) && getTargetFriendly()) {
                return false;
            }
            if (entity == getOwner() && !getTargetFriendly()) {
                return false;
            }
        } else if (entity instanceof Player player && player.getAbilities().invulnerable && !getTargetFriendly()) {
            return false;
        }
        return true;
    }

    private boolean matchesTargetKind(LivingEntity entity) {
        boolean isAnimal = entity instanceof Animal || entity instanceof WaterAnimal;
        if (isAnimal && !(entity instanceof Enemy) && getTargetAnimal()) {
            return true;
        }
        if (entity instanceof Enemy && getTargetMob()) {
            return true;
        }
        if (entity instanceof Player && getTargetPlayer()) {
            if (level() instanceof ServerLevel serverLevel && !serverLevel.isPvpAllowed() && !getTargetFriendly()) {
                setTargetPlayer(false);
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean getFlag(int bit) {
        return (entityData.get(FLAGS) & bit) != 0;
    }

    private void setFlag(int bit, boolean value) {
        byte flags = entityData.get(FLAGS);
        entityData.set(FLAGS, (byte) (value ? flags | bit : flags & ~bit));
        setTarget(null);
    }

    public boolean getTargetAnimal() {
        return getFlag(BIT_ANIMAL);
    }

    public void setTargetAnimal(boolean value) {
        setFlag(BIT_ANIMAL, value);
    }

    public boolean getTargetMob() {
        return getFlag(BIT_MOB);
    }

    public void setTargetMob(boolean value) {
        setFlag(BIT_MOB, value);
    }

    public boolean getTargetPlayer() {
        return getFlag(BIT_PLAYER);
    }

    public void setTargetPlayer(boolean value) {
        setFlag(BIT_PLAYER, value);
    }

    private boolean sameOwner(OwnableEntity ownable) {
        return getOwnerReference() != null && ownable.getOwnerReference() != null && getOwnerReference().getUUID().equals(ownable.getOwnerReference().getUUID());
    }

    public boolean getTargetFriendly() {
        return getFlag(BIT_FRIENDLY);
    }

    public void setTargetFriendly(boolean value) {
        setFlag(BIT_FRIENDLY, value);
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel serverLevel && !serverLevel.isPvpAllowed() && getTarget() instanceof Player && getTarget() != getOwner()) {
            setTarget(null);
        }
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        entityData.set(FLAGS, input.getByteOr("targets", (byte) 0));
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putByte("targets", entityData.get(FLAGS));
    }

    @Override
    protected ItemStack placerItem() {
        return new ItemStack(TCItems.TURRET_ADVANCED.get());
    }

    @Override
    protected void openTurretMenu(Player player) {
        MenuTurretAdvanced.open(player, this);
    }

    @Override
    protected double moveDamping() {
        return MOVE_DAMPING;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource source, boolean recentlyHit) {
        float bonus = 0.0F;
        if (random.nextFloat() < 0.2F + bonus) {
            spawnAtLocation(level, new ItemStack(TCItems.MIND_BIOTHAUMIC.get()), 0.5F);
        }
        if (random.nextFloat() < 0.5F + bonus) {
            spawnAtLocation(level, new ItemStack(TCItems.MECHANISM_SIMPLE.get()), 0.5F);
        }
        if (random.nextFloat() < 0.5F + bonus) {
            spawnAtLocation(level, new ItemStack(TCBlocks.PLANK_GREATWOOD.get()), 0.5F);
        }
        if (random.nextFloat() < 0.5F + bonus) {
            spawnAtLocation(level, new ItemStack(TCBlocks.PLANK_GREATWOOD.get()), 0.5F);
        }
        if (random.nextFloat() < 0.3F + bonus) {
            spawnAtLocation(level, new ItemStack(TCItems.PLATE_BRASS.get()), 0.5F);
        }
        if (random.nextFloat() < 0.4F + bonus) {
            spawnAtLocation(level, new ItemStack(TCItems.PLATE_IRON.get()), 0.5F);
        }
        if (random.nextFloat() < 0.4F + bonus) {
            spawnAtLocation(level, new ItemStack(TCItems.PLATE_IRON.get()), 0.5F);
        }
    }
}
