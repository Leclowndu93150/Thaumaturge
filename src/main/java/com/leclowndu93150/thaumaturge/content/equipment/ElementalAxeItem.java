package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IChanneledItem;
import com.leclowndu93150.thaumaturge.client.effect.ClientEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class ElementalAxeItem extends AxeItem implements IChanneledItem {
    private static final int USE_DURATION_TICKS = 72000;
    private static final double MAGNET_RANGE = 10.0;

    public ElementalAxeItem(Properties properties) {
        super(TTMaterials.TOOL_ELEMENTAL, properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION_TICKS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getEnchantmentValue() {
        return TTMaterials.TOOL_ELEMENTAL.getEnchantmentValue();
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return TTMaterials.TOOL_ELEMENTAL.getRepairIngredient().test(repair)
                || super.isValidRepairItem(toRepair, repair);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        return true;
    }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        stack.hurtAndBreak(2, attacker, EquipmentSlot.MAINHAND);
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return true;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remainingUseDuration) {
        Vec3 anchor = new Vec3(user.getX(), user.getY() - user.getBbHeight() / 2.0, user.getZ());
        for (ItemEntity item : level.getEntitiesOfClass(
                ItemEntity.class, user.getBoundingBox().inflate(MAGNET_RANGE), ItemEntity::isAlive)) {
            Vec3 position = item.position();
            if (position.distanceToSqr(anchor) == 0.0) {
                continue;
            }
            item.setDeltaMovement(ItemMagnet.pull(item.getDeltaMovement(), position, anchor));
            if (level.isClientSide()) {
                double jitterX = level.getRandom().triangle(0.0, 0.2);
                double jitterY = level.getRandom().triangle(0.0, 0.2);
                double jitterZ = level.getRandom().triangle(0.0, 0.2);
                ClientEffects.followingBubble(
                        level, position.x + jitterX, position.y + item.getBbHeight() + jitterY, position.z + jitterZ);
            }
        }
    }
}
