package com.leclowndu93150.thaumaturge.content.spell.carrier;

import java.util.UUID;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public abstract class AbstractSpellCarrier extends Entity implements TraceableEntity, IEntityWithComplexSpawn {
    private static final String OWNER_KEY = "owner";
    private static final String AGE_KEY = "age";
    private static final String LIFETIME_KEY = "lifetime";

    protected final CarrierCharge charge = new CarrierCharge();
    private @Nullable UUID owner;
    private int lifetime;

    protected AbstractSpellCarrier(EntityType<? extends AbstractSpellCarrier> type, Level level) {
        super(type, level);
    }

    protected final void bind(LivingEntity caster, CarrierPayload payload, int ticks) {
        charge.arm(payload);
        owner = caster.getUUID();
        lifetime = ticks;
    }

    protected final boolean expired() {
        return tickCount > lifetime || getOwner() == null || charge.isSpent();
    }

    protected void saveCarrierData(CompoundTag output) {}

    protected void loadCarrierData(CompoundTag input) {}

    @Override
    public @Nullable LivingEntity getOwner() {
        Entity resolved = owner == null
                ? null
                : level() instanceof ServerLevel server ? server.getEntity(owner) : level().getPlayerByUUID(owner);
        return resolved instanceof LivingEntity living ? living : null;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void writeSpawnData(RegistryFriendlyByteBuf buffer) {
        charge.writeLook(buffer);
    }

    @Override
    public void readSpawnData(RegistryFriendlyByteBuf buffer) {
        charge.readLook(buffer);
    }

    @Override
    protected final void addAdditionalSaveData(CompoundTag output) {
        HolderLookup.Provider registries = registryAccess();
        charge.save(output, registries);
        if (owner != null) output.putUUID(OWNER_KEY, owner);
        output.putInt(AGE_KEY, tickCount);
        output.putInt(LIFETIME_KEY, lifetime);
        saveCarrierData(output);
    }

    @Override
    protected final void readAdditionalSaveData(CompoundTag input) {
        HolderLookup.Provider registries = registryAccess();
        charge.load(input, registries);
        owner = input.hasUUID(OWNER_KEY) ? input.getUUID(OWNER_KEY) : null;
        tickCount = (input.contains(AGE_KEY) ? input.getInt(AGE_KEY) : 0);
        lifetime = (input.contains(LIFETIME_KEY) ? input.getInt(LIFETIME_KEY) : 0);
        loadCarrierData(input);
    }
}
