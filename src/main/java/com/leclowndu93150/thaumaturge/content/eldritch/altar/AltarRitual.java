package com.leclowndu93150.thaumaturge.content.eldritch.altar;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

record AltarRitual(UUID caster, long start, float drained) {
    public static final Codec<AltarRitual> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    UUIDUtil.CODEC.fieldOf("caster").forGetter(AltarRitual::caster),
                    Codec.LONG.fieldOf("start").forGetter(AltarRitual::start),
                    Codec.FLOAT.fieldOf("drained").forGetter(AltarRitual::drained))
            .apply(instance, AltarRitual::new));

    AltarRitual drain(float amount) {
        return new AltarRitual(caster, start, drained + amount);
    }
}
