package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

public final class BathSaltsEvents {
    private static final double FLOAT_RESPONSE = 0.35;
    private static final double MAX_VERTICAL_SPEED = 0.08;

    private BathSaltsEvents() {}

    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity)
                || !itemEntity.getItem().is(TCItems.BATH_SALTS.get())
                || !itemEntity.isInWater()) {
            return;
        }

        BlockPos pos = itemEntity.blockPosition();
        var fluid = itemEntity.level().getFluidState(pos);
        if (!fluid.is(FluidTags.WATER)) {
            pos = pos.below();
            fluid = itemEntity.level().getFluidState(pos);
            if (!fluid.is(FluidTags.WATER)) {
                return;
            }
        }

        double fluidHeight = fluid.getHeight(itemEntity.level(), pos);
        double targetY = pos.getY() + Math.max(0.0, (fluidHeight - itemEntity.getBbHeight()) * 0.5);
        double verticalSpeed =
                Mth.clamp((targetY - itemEntity.getY()) * FLOAT_RESPONSE, -MAX_VERTICAL_SPEED, MAX_VERTICAL_SPEED);
        Vec3 motion = itemEntity.getDeltaMovement();
        itemEntity.setDeltaMovement(motion.x, verticalSpeed, motion.z);
    }

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
