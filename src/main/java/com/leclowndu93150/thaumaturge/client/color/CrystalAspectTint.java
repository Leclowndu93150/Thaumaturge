package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
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

public record CrystalAspectTint(int fallback) implements ItemTintSource {
    public static final MapCodec<CrystalAspectTint> MAP_CODEC = RecordCodecBuilder
            .mapCodec(instance -> instance.group(ExtraCodecs.RGB_COLOR_CODEC.optionalFieldOf("fallback", 0xFFFFFF).forGetter(CrystalAspectTint::fallback)).apply(instance, CrystalAspectTint::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner) {
        AspectInstance instance = stack.get(TCDataComponents.CRYSTAL_ASPECT.get());
        if (instance == null) {
            return ARGB.opaque(fallback);
        }
        return ARGB.opaque(instance.aspect().value().color());
    }

    @Override
    public MapCodec<CrystalAspectTint> type() {
        return MAP_CODEC;
    }
}
