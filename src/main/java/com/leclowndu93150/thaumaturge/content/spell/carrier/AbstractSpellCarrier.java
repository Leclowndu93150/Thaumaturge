package com.leclowndu93150.thaumaturge.content.spell.carrier;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.entity.IEntityWithComplexSpawn;
import org.jspecify.annotations.Nullable;

public abstract class AbstractSpellCarrier extends Entity implements TraceableEntity, IEntityWithComplexSpawn {
    private static final String OWNER_KEY = "owner";
    private static final String AGE_KEY = "age";
    private static final String LIFETIME_KEY = "lifetime";

    protected final CarrierCharge charge = new CarrierCharge();
    private @Nullable EntityReference<LivingEntity> owner;
    private int lifetime;

    protected AbstractSpellCarrier(EntityType<? extends AbstractSpellCarrier> type, Level level) {
        super(type, level);
    }

    protected final void bind(LivingEntity caster, CarrierPayload payload, int ticks) {
        charge.arm((ServerLevel) caster.level(), payload);
        owner = EntityReference.of(caster);
        lifetime = ticks;
    }

    protected final boolean expired() {
        return tickCount > lifetime || getOwner() == null || charge.isSpent() || level() instanceof ServerLevel server && !charge.valid(server);
    }

    protected void saveCarrierData(ValueOutput output) {}

    protected void loadCarrierData(ValueInput input) {}

    @Override
    public @Nullable LivingEntity getOwner() {
        return EntityReference.getLivingEntity(owner, level());
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
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
    protected final void addAdditionalSaveData(ValueOutput output) {
        charge.save(output);
        EntityReference.store(owner, output, OWNER_KEY);
        output.putInt(AGE_KEY, tickCount);
        output.putInt(LIFETIME_KEY, lifetime);
        saveCarrierData(output);
    }

    @Override
    protected final void readAdditionalSaveData(ValueInput input) {
        charge.load(input);
        owner = EntityReference.read(input, OWNER_KEY);
        tickCount = input.getIntOr(AGE_KEY, 0);
        lifetime = input.getIntOr(LIFETIME_KEY, 0);
        loadCarrierData(input);
    }
}
