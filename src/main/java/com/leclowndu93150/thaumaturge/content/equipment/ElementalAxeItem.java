package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IChanneledItem;
import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ElementalAxeItem extends AxeItem implements IChanneledItem {
    private static final int USE_DURATION_TICKS = 72000;
    private static final double MAGNET_RANGE = 10.0;
    private static final double HALF = 2.0;

    public ElementalAxeItem(ToolMaterial material, float attackDamageBaseline, float attackSpeedBaseline, Properties properties) {
        super(material, attackDamageBaseline, attackSpeedBaseline, properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return USE_DURATION_TICKS;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        Vec3 anchor = new Vec3(user.getX(), user.getY() - user.getBbHeight() / HALF, user.getZ());
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, user.getBoundingBox().inflate(MAGNET_RANGE), ItemEntity::isAlive)) {
            Vec3 position = item.position();
            if (position.distanceToSqr(anchor) == 0.0) {
                continue;
            }
            item.setDeltaMovement(ItemMagnet.pull(item.getDeltaMovement(), position, anchor));
            if (level.isClientSide()) {
                ClientEffects.followingBubbleAbove(level, item);
            }
        }
    }
}
