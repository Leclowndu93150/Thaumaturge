package com.leclowndu93150.thaumaturge.content.spell.carrier;

import com.leclowndu93150.thaumaturge.api.spell.SpellRegistries;
import com.leclowndu93150.thaumaturge.api.spell.Spells;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellContinuation;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.fx.SpellFxStyle;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SpellLook;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public record CarrierPayload(SpellContinuation continuation, SpellLook look) {
    public static final Codec<CarrierPayload> CODEC = RecordCodecBuilder
            .create(i -> i.group(SpellContinuation.CODEC.fieldOf("continuation").forGetter(CarrierPayload::continuation), SpellLook.CODEC.fieldOf("look").forGetter(CarrierPayload::look)).apply(i,
                    CarrierPayload::new));

    private static final String KEY = "spell";

    public void resume(ServerLevel level, List<SpellTarget> targets) {
        Spells.resume(level, continuation, targets);
    }

    public static void save(ValueOutput output, @Nullable CarrierPayload payload) {
        output.storeNullable(KEY, CODEC, payload);
    }

    public static @Nullable CarrierPayload load(ValueInput input) {
        return input.read(KEY, CODEC).orElse(null);
    }

    public static void particle(Level level, SpellLook look, Vec3 at, Vec3 motion) {
        Optional<Holder.Reference<SpellFxStyle>> style = SpellRegistries.fxStyles().get(look.fx());
        if (style.isPresent()) {
            style.get().value().spawn(level, at, motion, look.color(), level.getRandom());
        }
    }
}
