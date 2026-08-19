package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IRechargable;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.content.entity.projectile.EntityGrapple;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

public final class GrappleGunItem extends Item implements IRechargable {
    private static final int MAX_CHARGE = 100;
    private static final float LAUNCH_PITCH_OFFSET = -5.0F;
    private static final float LAUNCH_VELOCITY = 1.5F;

    public GrappleGunItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaxCharge(ItemStack stack, LivingEntity holder) {
        return MAX_CHARGE;
    }

    @Override
    public ChargeDisplay showInHud(ItemStack stack, LivingEntity holder) {
        return ChargeDisplay.NORMAL;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, @Nullable EquipmentSlot slot) {
        if (!Boolean.TRUE.equals(stack.get(TCDataComponents.GRAPPLE_LOADED))) {
            return;
        }
        int tracked = entity.getData(TCAttachments.GRAPPLE_ID.get());
        if (tracked < 0 || !(level.getEntity(tracked) instanceof EntityGrapple grapple) || !grapple.isAlive()) {
            stack.set(TCDataComponents.GRAPPLE_LOADED, false);
        }
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        player.playSound(TCSounds.ICE.get(), 3.0F, 0.8F + level.getRandom().nextFloat() * 0.1F);
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && RechargeAccess.getCharge(stack) > 0) {
            EntityGrapple grapple = new EntityGrapple(TCEntities.GRAPPLE.get(), level, player, hand);
            grapple.shootFromRotation(player, player.getXRot(), player.getYRot(), LAUNCH_PITCH_OFFSET, LAUNCH_VELOCITY, 0.0F);
            int handSign = hand == InteractionHand.MAIN_HAND ? 1 : -1;
            double px = -Mth.cos((player.getYRot() - 0.5F) / 180.0F * (float) Math.PI) * 0.2F * handSign;
            double pz = -Mth.sin((player.getYRot() - 0.5F) / 180.0F * (float) Math.PI) * 0.3F * handSign;
            Vec3 look = player.getLookAngle();
            grapple.setPos(grapple.getX() + px + look.x, grapple.getY(), grapple.getZ() + pz + look.z);
            if (level.addFreshEntity(grapple)) {
                RechargeAccess.consumeCharge(stack, player, 1);
                stack.set(TCDataComponents.GRAPPLE_LOADED, true);
            }
        }
        return InteractionResult.SUCCESS;
    }
}
