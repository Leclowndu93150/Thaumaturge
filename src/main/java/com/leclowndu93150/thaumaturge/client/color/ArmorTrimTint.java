package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.client.equipment.ArmorTrimColors;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.trim.ArmorTrim;
import org.jspecify.annotations.Nullable;

public record ArmorTrimTint() implements ItemTintSource {
    public static final MapCodec<ArmorTrimTint> MAP_CODEC = MapCodec.unit(new ArmorTrimTint());
    private static final int UNTINTED = -1;

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity) {
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        return trim == null ? UNTINTED : ArmorTrimColors.of(trim.material());
    }

    @Override
    public MapCodec<ArmorTrimTint> type() {
        return MAP_CODEC;
    }
}
