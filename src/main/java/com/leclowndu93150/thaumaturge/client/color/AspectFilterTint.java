package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record AspectFilterTint(int defaultColor) implements ItemTintSource {
    public static final MapCodec<AspectFilterTint> MAP_CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(ExtraCodecs.RGB_COLOR_CODEC.fieldOf("default").forGetter(AspectFilterTint::defaultColor)).apply(instance, AspectFilterTint::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        ResourceKey<IAspect> aspect = stack.get(TCDataComponents.ASPECT_FILTER.get());
        if (aspect != null && level != null) {
            return level.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).get(aspect).map(holder -> 0xFF000000 | holder.value().color()).orElse(0xFF000000 | defaultColor);
        }
        return 0xFF000000 | defaultColor;
    }

    @Override
    public MapCodec<AspectFilterTint> type() {
        return MAP_CODEC;
    }
}
