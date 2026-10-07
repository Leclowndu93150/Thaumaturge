package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.item.ItemExpireEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class BathSaltsEvents {
    private static final double FLOAT_RESPONSE = 0.35;
    private static final double MAX_VERTICAL_SPEED = 0.08;
    private static final int DISSOLVE_TICKS = 200;

    private BathSaltsEvents() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity && itemEntity.getItem().is(TTItems.BATH_SALTS.get())) {
            itemEntity.lifespan = DISSOLVE_TICKS;
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (!(event.getEntity() instanceof ItemEntity itemEntity) || !(itemEntity.level() instanceof ServerLevel level) || !itemEntity.getItem().is(TTItems.BATH_SALTS.get())
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
        double verticalSpeed = Mth.clamp((targetY - itemEntity.getY()) * FLOAT_RESPONSE, -MAX_VERTICAL_SPEED, MAX_VERTICAL_SPEED);
        Vec3 motion = itemEntity.getDeltaMovement();
        itemEntity.setDeltaMovement(motion.x, verticalSpeed, motion.z);
        if (itemEntity.tickCount % 2 == 0) {
            BubbleParticleOptions bubble = new BubbleParticleOptions(0xFFE8E8FF, 1.0F, 1.5F + level.getRandom().nextFloat() * 0.5F, 18 + level.getRandom().nextInt(10), -0.025F, false);
            level.sendParticles(bubble, itemEntity.getX(), itemEntity.getY() + itemEntity.getBbHeight() * 0.5, itemEntity.getZ(), 2 + level.getRandom().nextInt(3), 0.15, 0.1, 0.15, 0.0);
        }
    }

    @SubscribeEvent
    public static void onItemExpire(ItemExpireEvent event) {
        var itemEntity = event.getEntity();
        if (itemEntity.level().isClientSide() || !itemEntity.getItem().is(TTItems.BATH_SALTS.get())) {
            return;
        }

        BlockPos pos = itemEntity.blockPosition();
        var state = itemEntity.level().getBlockState(pos);
        if (state.is(Blocks.WATER) && state.getFluidState().isSource()) {
            itemEntity.level().setBlockAndUpdate(pos, TTBlocks.PURIFYING_FLUID.get().defaultBlockState());
        }
    }
}
