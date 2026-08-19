package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetActionBarTextPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public abstract class EntityOwnedConstruct extends PathfinderMob implements OwnableEntity {
    private static final EntityDataAccessor<Byte> OWNED_FLAGS = SynchedEntityData.defineId(EntityOwnedConstruct.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> OWNER = SynchedEntityData.defineId(EntityOwnedConstruct.class,
            EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    private static final int OWNED_BIT = 4;

    private boolean validSpawn;

    protected EntityOwnedConstruct(EntityType<? extends EntityOwnedConstruct> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(OWNED_FLAGS, (byte) 0);
        entityData.define(OWNER, Optional.empty());
    }

    public boolean isOwned() {
        return (entityData.get(OWNED_FLAGS) & OWNED_BIT) != 0;
    }

    public void setOwned(boolean owned) {
        byte flags = entityData.get(OWNED_FLAGS);
        if (owned) {
            entityData.set(OWNED_FLAGS, (byte) (flags | OWNED_BIT));
        } else {
            entityData.set(OWNED_FLAGS, (byte) (flags & ~OWNED_BIT));
        }
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getOwnerReference() {
        return entityData.get(OWNER).orElse(null);
    }

    public void setOwner(LivingEntity owner) {
        entityData.set(OWNER, Optional.of(EntityReference.of(owner)));
        setOwned(true);
    }

    public boolean isOwner(LivingEntity entity) {
        return entity == getOwner();
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TCSounds.CLACK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TCSounds.CLACK.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TCSounds.TOOL.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 240;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (getTarget() != null && isAlliedTo(getTarget())) {
            setTarget(null);
        }
        if (!level().isClientSide() && !validSpawn) {
            discard();
        }
    }

    public void setValidSpawn() {
        validSpawn = true;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putBoolean("v", validSpawn);
        EntityReference.store(getOwnerReference(), output, "Owner");
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        validSpawn = input.getBooleanOr("v", false);
        EntityReference<LivingEntity> owner = EntityReference.readWithOldOwnerConversion(input, "Owner", level());
        if (owner != null) {
            entityData.set(OWNER, Optional.of(owner));
            setOwned(true);
        } else {
            entityData.set(OWNER, Optional.empty());
            setOwned(false);
        }
    }

    @Override
    public @Nullable PlayerTeam getTeam() {
        if (isOwned()) {
            LivingEntity owner = getOwner();
            if (owner != null) {
                return owner.getTeam();
            }
        }
        return super.getTeam();
    }

    @Override
    protected boolean considersEntityAsAlly(Entity other) {
        if (isOwned()) {
            LivingEntity owner = getOwner();
            if (other == owner) {
                return true;
            }
            if (owner != null) {
                return owner.isAlliedTo(other);
            }
        }
        return super.considersEntityAsAlly(other);
    }

    @Override
    public void die(DamageSource cause) {
        if (level() instanceof ServerLevel serverLevel && serverLevel.getGameRules().get(GameRules.SHOW_DEATH_MESSAGES) && hasCustomName() && getOwner() instanceof ServerPlayer player) {
            player.sendSystemMessage(getCombatTracker().getDeathMessage());
        }
        super.die(cause);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isRemoved()) {
            return InteractionResult.PASS;
        }
        if (player.isShiftKeyDown() || player.getMainHandItem().is(Items.NAME_TAG)) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide() && !isOwner(player)) {
            if (player instanceof ServerPlayer serverPlayer) {
                serverPlayer.connection.send(new ClientboundSetActionBarTextPacket(Component.translatable("tc.notowned").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC)));
            }
            return InteractionResult.SUCCESS;
        }
        return super.mobInteract(player, hand);
    }
}
