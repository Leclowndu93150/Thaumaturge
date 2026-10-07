package com.leclowndu93150.thaumaturge.content.spell.casting;

import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class CasterHands {
    private CasterHands() {}

    public static ItemStack held(@Nullable LivingEntity entity) {
        if (entity == null) {
            return ItemStack.EMPTY;
        }
        if (entity.getMainHandItem().getItem() instanceof ICaster) {
            return entity.getMainHandItem();
        }
        if (entity.getOffhandItem().getItem() instanceof ICaster) {
            return entity.getOffhandItem();
        }
        return ItemStack.EMPTY;
    }
}
