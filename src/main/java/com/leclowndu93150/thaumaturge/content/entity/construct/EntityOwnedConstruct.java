package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.Nullable;

public abstract class EntityOwnedConstruct extends PathfinderMob implements OwnableEntity {
    private static final EntityDataAccessor<Optional<UUID>> MASTER =
            SynchedEntityData.defineId(EntityOwnedConstruct.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final String MASTER_KEY = "Owner";
    private static final String COMMISSIONED_KEY = "v";
    private static final int CHATTER_INTERVAL = 240;

    private boolean commissioned;

    protected EntityOwnedConstruct(EntityType<? extends EntityOwnedConstruct> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder entityData) {
        super.defineSynchedData(entityData);
        entityData.define(MASTER, Optional.empty());
    }

    public boolean isOwned() {
        return entityData.get(MASTER).isPresent();
    }

    @Override
    public @Nullable UUID getOwnerUUID() {
        return entityData.get(MASTER).orElse(null);
    }

    @Override
    public @Nullable LivingEntity getOwner() {
        UUID owner = getOwnerUUID();
        if (owner == null) return null;
        Entity entity =
                level() instanceof ServerLevel server ? server.getEntity(owner) : level().getPlayerByUUID(owner);
        return entity instanceof LivingEntity living ? living : null;
    }

    public void setOwner(LivingEntity owner) {
        entityData.set(MASTER, Optional.of(owner.getUUID()));
    }

    public boolean isOwner(LivingEntity entity) {
        return isOwned() && entity == getOwner();
    }

    public void setValidSpawn() {
        commissioned = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (getTarget() != null && isAlliedTo(getTarget())) {
            setTarget(null);
        }
        if (!commissioned && !level().isClientSide()) {
            discard();
        }
    }

    @Override
    public @Nullable PlayerTeam getTeam() {
        LivingEntity master = isOwned() ? getOwner() : null;
        return master != null ? master.getTeam() : super.getTeam();
    }

    @Override
    public boolean isAlliedTo(Entity other) {
        LivingEntity master = isOwned() ? getOwner() : null;
        if (master != null) {
            return other == master || master.isAlliedTo(other);
        }
        return super.isAlliedTo(other);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (isRemoved() || player.isShiftKeyDown() || player.getMainHandItem().is(Items.NAME_TAG)) {
            return InteractionResult.PASS;
        }
        if (level().isClientSide() || isOwner(player)) {
            return super.mobInteract(player, hand);
        }
        TTActionBar.sendPurple(player, "message.thaumaturge.construct.not_owned");
        return InteractionResult.SUCCESS;
    }

    @Override
    public void die(DamageSource cause) {
        boolean announce = level() instanceof ServerLevel server
                && server.getGameRules().getBoolean(GameRules.RULE_SHOWDEATHMESSAGES)
                && hasCustomName();
        if (announce && getOwner() instanceof ServerPlayer master) {
            master.sendSystemMessage(getCombatTracker().getDeathMessage());
        }
        super.die(cause);
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    @Override
    public int getAmbientSoundInterval() {
        return CHATTER_INTERVAL;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return TTSounds.CLACK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return TTSounds.CLACK.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return TTSounds.TOOL.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag output) {
        HolderLookup.Provider registries = registryAccess();
        super.addAdditionalSaveData(output);
        output.putBoolean(COMMISSIONED_KEY, commissioned);
        if (getOwnerUUID() != null) output.putUUID(MASTER_KEY, getOwnerUUID());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag input) {
        HolderLookup.Provider registries = registryAccess();
        super.readAdditionalSaveData(input);
        commissioned = (input.contains(COMMISSIONED_KEY) ? input.getBoolean(COMMISSIONED_KEY) : false);
        entityData.set(MASTER, input.hasUUID(MASTER_KEY) ? Optional.of(input.getUUID(MASTER_KEY)) : Optional.empty());
    }
}
