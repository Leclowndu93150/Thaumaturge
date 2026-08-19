package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import java.util.List;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ElementalAxeItem extends Item {
    private static final int USE_DURATION = 72000;
    private static final double MAGNET_RANGE = 10.0;
    private static final double MAGNET_STRENGTH = 0.3;
    private static final double MAGNET_SPEED_CAP = 0.25;

    public ElementalAxeItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int ticksRemaining) {
        super.onUseTick(level, player, stack, ticksRemaining);
        List<ItemEntity> stuff = level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(MAGNET_RANGE));
        for (ItemEntity e : stuff) {
            if (!e.isAlive()) {
                continue;
            }
            double dx = e.getX() - player.getX();
            double dy = e.getY() - player.getY() + player.getBbHeight() / 2.0F;
            double dz = e.getZ() - player.getZ();
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            dx /= distance;
            dy /= distance;
            dz /= distance;
            Vec3 motion = e.getDeltaMovement();
            double mx = Mth.clamp(motion.x - dx * MAGNET_STRENGTH, -MAGNET_SPEED_CAP, MAGNET_SPEED_CAP);
            double my = Mth.clamp(motion.y - (dy * MAGNET_STRENGTH - 0.1), -MAGNET_SPEED_CAP, MAGNET_SPEED_CAP);
            double mz = Mth.clamp(motion.z - dz * MAGNET_STRENGTH, -MAGNET_SPEED_CAP, MAGNET_SPEED_CAP);
            e.setDeltaMovement(mx, my, mz);
            if (level.isClientSide()) {
                ClientEffects.followingBubble(level, e.getX() + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F,
                        e.getY() + e.getBbHeight() + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F,
                        e.getZ() + (level.getRandom().nextFloat() - level.getRandom().nextFloat()) * 0.2F);
            }
        }
    }
}
