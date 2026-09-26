package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;

public final class BathSaltsEvents {
    private BathSaltsEvents() {}

    @SubscribeEvent
    public static void onItemExpire(ItemExpireEvent event) {
        var itemEntity = event.getEntity();
        if (itemEntity.level().isClientSide() || !itemEntity.getItem().is(TCItems.BATH_SALTS.get())) {
            return;
        }

        BlockPos pos = itemEntity.blockPosition();
        var state = itemEntity.level().getBlockState(pos);
        if (state.is(Blocks.WATER) && state.getFluidState().isSource()) {
            itemEntity
                    .level()
                    .setBlockAndUpdate(pos, TCBlocks.PURIFYING_FLUID.get().defaultBlockState());
        }
    }
}
