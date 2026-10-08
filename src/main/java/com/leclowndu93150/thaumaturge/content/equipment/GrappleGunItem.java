package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.ChargeDisplay;
import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.content.entity.projectile.EntityGrapple;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class GrappleGunItem extends Item {
    private static final int MAX_CHARGE = 100;
    private static final float LAUNCH_PITCH_OFFSET = -5.0F;
    private static final float LAUNCH_VELOCITY = 1.5F;

    public GrappleGunItem(Properties properties) {
        super(properties.component(
                TTDataComponents.RECHARGEABLE.get(), new ChargeProfile(MAX_CHARGE, ChargeDisplay.ALWAYS)));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        if (level.isClientSide() || !Boolean.TRUE.equals(stack.get(TTDataComponents.GRAPPLE_LOADED))) {
            return;
        }
        int tracked = entity.getData(TTAttachments.GRAPPLE_ID.get());
        if (tracked < 0 || !(level.getEntity(tracked) instanceof EntityGrapple grapple) || !grapple.isAlive()) {
            stack.remove(TTDataComponents.GRAPPLE_LOADED);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.playSound(TTSounds.ICE.get(), 3.0F, 0.8F + level.getRandom().nextFloat() * 0.1F);
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && RechargeAccess.getCharge(stack) > 0) {
            EntityGrapple grapple = new EntityGrapple(TTEntities.GRAPPLE.get(), level, player, hand);
            grapple.shootFromRotation(
                    player, player.getXRot(), player.getYRot(), LAUNCH_PITCH_OFFSET, LAUNCH_VELOCITY, 0.0F);
            int handSign = hand == InteractionHand.MAIN_HAND ? 1 : -1;
            double px = -Mth.cos((player.getYRot() - 0.5F) / 180.0F * (float) Math.PI) * 0.2F * handSign;
            double pz = -Mth.sin((player.getYRot() - 0.5F) / 180.0F * (float) Math.PI) * 0.3F * handSign;
            Vec3 look = player.getLookAngle();
            grapple.setPos(grapple.getX() + px + look.x, grapple.getY(), grapple.getZ() + pz + look.z);
            if (level.addFreshEntity(grapple)) {
                RechargeAccess.consumeCharge(stack, player, 1);
                stack.set(TTDataComponents.GRAPPLE_LOADED, true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
