package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SpellLook;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

public final class CarrierCharge {
    private @Nullable CarrierPayload payload;
    private SpellLook look = SpellLook.DEFAULT;

    public void arm(CarrierPayload payload) {
        this.payload = payload;
        this.look = payload.look();
    }

    public boolean isSpent() {
        return payload == null;
    }

    public @Nullable CarrierPayload payload() {
        return payload;
    }

    public SpellLook look() {
        return look;
    }

    public void resume(ServerLevel level, List<SpellTarget> targets) {
        if (payload != null) {
            payload.resume(level, targets);
        }
    }

    public void save(CompoundTag output, HolderLookup.Provider registries) {
        CarrierPayload.save(output, payload, registries);
    }

    public void load(CompoundTag input, HolderLookup.Provider registries) {
        payload = CarrierPayload.load(input, registries);
        look = payload != null ? payload.look() : SpellLook.DEFAULT;
    }

    public void writeLook(RegistryFriendlyByteBuf buffer) {
        SpellLook.STREAM_CODEC.encode(buffer, look);
    }

    public void readLook(RegistryFriendlyByteBuf buffer) {
        look = SpellLook.STREAM_CODEC.decode(buffer);
    }
}
