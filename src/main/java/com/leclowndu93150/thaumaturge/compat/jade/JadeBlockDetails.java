package com.leclowndu93150.thaumaturge.compat.jade;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.ResourceLocation;

record JadeBlockDetails(
        ResourceLocation option,
        List<Component> summary,
        List<Component> details,
        Optional<Component> title,
        boolean hideFluid) {
    static final Codec<JadeBlockDetails> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("option").forGetter(JadeBlockDetails::option),
                    ComponentSerialization.CODEC.listOf().fieldOf("summary").forGetter(JadeBlockDetails::summary),
                    ComponentSerialization.CODEC.listOf().fieldOf("details").forGetter(JadeBlockDetails::details),
                    ComponentSerialization.CODEC.optionalFieldOf("title").forGetter(JadeBlockDetails::title),
                    Codec.BOOL.optionalFieldOf("hide_fluid", false).forGetter(JadeBlockDetails::hideFluid))
            .apply(instance, JadeBlockDetails::new));
}
