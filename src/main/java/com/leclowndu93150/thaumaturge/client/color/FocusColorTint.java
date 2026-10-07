package com.leclowndu93150.thaumaturge.client.color;

import net.minecraft.world.item.ItemStackTemplate;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.client.casters.FocusColors;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public record FocusColorTint() implements ItemTintSource {
    public static final MapCodec<FocusColorTint> MAP_CODEC = MapCodec.unit(new FocusColorTint());
    private static final int OPAQUE = 0xFF000000;

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        ItemStackTemplate socketed = stack.get(TTDataComponents.SOCKETED_FOCUS.get());
        ItemStack focus = socketed != null ? socketed.create() : stack;
        return OPAQUE | FocusColors.of(focus);
    }

    @Override
    public MapCodec<FocusColorTint> type() {
        return MAP_CODEC;
    }
}
