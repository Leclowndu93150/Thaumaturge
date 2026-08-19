package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class WandIsStaffProperty implements ConditionalItemModelProperty {
    public static final MapCodec<WandIsStaffProperty> MAP_CODEC = MapCodec.unit(WandIsStaffProperty::new);

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity owner, int seed, ItemDisplayContext displayContext) {
        return WandVisHelper.getParts(stack).rod().staff();
    }

    @Override
    public MapCodec<WandIsStaffProperty> type() {
        return MAP_CODEC;
    }
}
