package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.ARGB;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record AspectColorTint(int fallback) implements ItemTintSource {
    public static final MapCodec<AspectColorTint> MAP_CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("fallback", 0xFFFFFF).forGetter(AspectColorTint::fallback)).apply(instance, AspectColorTint::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        AspectList list = stack.get(TCDataComponents.ASPECTS.get());
        if (list == null || list.isEmpty()) {
            return ARGB.opaque(fallback);
        }
        AspectInstance first = list.entries().get(0);
        return ARGB.opaque(first.aspect().value().color());
    }

    @Override
    public MapCodec<AspectColorTint> type() {
        return MAP_CODEC;
    }
}
