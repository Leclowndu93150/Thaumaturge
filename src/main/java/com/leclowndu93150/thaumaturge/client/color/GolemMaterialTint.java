package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record GolemMaterialTint() implements ItemTintSource {
    public static final MapCodec<GolemMaterialTint> MAP_CODEC = MapCodec.unit(new GolemMaterialTint());
    private static final int OPAQUE = 0xFF000000;
    private static final int DEFAULT = 0xFFFFFFFF;

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        GolemProperties props = stack.get(TTDataComponents.GOLEM_PROPERTIES.get());
        if (props == null) {
            return DEFAULT;
        }
        return OPAQUE | props.material().itemColor();
    }

    @Override
    public MapCodec<GolemMaterialTint> type() {
        return MAP_CODEC;
    }
}
