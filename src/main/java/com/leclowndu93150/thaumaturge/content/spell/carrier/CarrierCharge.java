package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellContinuation;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SpellLook;
import com.leclowndu93150.thaumaturge.content.spell.engine.CastAuthorities;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class CarrierCharge {
    private @Nullable CarrierPayload payload;
    private SpellLook look = SpellLook.DEFAULT;

    public void arm(CarrierPayload payload) {
        this.payload = payload.continuation().authority().isEmpty() ? payload : null;
        this.look = payload.look();
    }

    public void arm(ServerLevel level, CarrierPayload payload) {
        SpellContinuation continuation = payload.continuation();
        if (continuation.authority().isEmpty()) {
            arm(payload);
            return;
        }
        var token = CastAuthorities.issue(level, continuation.authority().orElseThrow());
        this.payload = token.map(value -> new CarrierPayload(
                new SpellContinuation(continuation.nodes(), continuation.state(), continuation.caster(), continuation.castId(), continuation.authority(), java.util.Optional.of(value), 0),
                payload.look())).orElse(null);
        this.look = payload.look();
    }

    public boolean valid(ServerLevel level) {
        return payload != null && payload.continuation().authority().map(authority -> CastAuthorities.valid(level, authority)).orElse(true);
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
        if (payload == null || targets.isEmpty()) {
            return;
        }
        if (!valid(level)) {
            payload = null;
            return;
        }
        CarrierPayload current = payload;
        SpellContinuation continuation = current.continuation();
        if (continuation.authority().isPresent()) {
            if (continuation.sequence() == Long.MAX_VALUE) {
                payload = null;
                return;
            }
            payload = new CarrierPayload(new SpellContinuation(continuation.nodes(), continuation.state(), continuation.caster(), continuation.castId(), continuation.authority(), continuation.token(),
                    continuation.sequence() + 1), current.look());
        }
        if (!current.resume(level, targets) && continuation.authority().isPresent()) {
            payload = null;
        }
    }

    public void save(ValueOutput output) {
        CarrierPayload.save(output, payload);
    }

    public void load(ValueInput input) {
        payload = CarrierPayload.load(input);
        look = payload != null ? payload.look() : SpellLook.DEFAULT;
    }

    public void writeLook(RegistryFriendlyByteBuf buffer) {
        SpellLook.STREAM_CODEC.encode(buffer, look);
    }

    public void readLook(RegistryFriendlyByteBuf buffer) {
        look = SpellLook.STREAM_CODEC.decode(buffer);
    }
}
