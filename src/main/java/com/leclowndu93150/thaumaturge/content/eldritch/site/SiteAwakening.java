package com.leclowndu93150.thaumaturge.content.eldritch.site;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;

public record SiteAwakening(EntityType<?> entity, int maxAlive, int interval, int radius) {
    public static final Codec<SiteAwakening> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    BuiltInRegistries.ENTITY_TYPE
                            .byNameCodec()
                            .fieldOf("entity")
                            .forGetter(SiteAwakening::entity),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_alive", 1).forGetter(SiteAwakening::maxAlive),
                    ExtraCodecs.POSITIVE_INT.fieldOf("interval").forGetter(SiteAwakening::interval),
                    ExtraCodecs.POSITIVE_INT.fieldOf("radius").forGetter(SiteAwakening::radius))
            .apply(instance, SiteAwakening::new));
}
