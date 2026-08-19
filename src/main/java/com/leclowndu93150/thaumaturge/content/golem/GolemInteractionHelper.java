package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.server.TCFakePlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;

public final class GolemInteractionHelper {
    private GolemInteractionHelper() {}

    public static void golemClick(Level level, IGolemAPI golem, BlockPos pos, Direction face, ItemStack clickStack, boolean sneaking, boolean rightClick) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        FakePlayer player = TCFakePlayer.GOLEM.at(serverLevel, golem.getGolemEntity());
        player.setItemInHand(InteractionHand.MAIN_HAND, clickStack);
        player.setShiftKeyDown(sneaking);
        if (!rightClick) {
            try {
                player.gameMode.destroyBlock(pos);
            } catch (Exception e) {
                Thaumaturge.LOGGER.error("Golem left-click at {} failed", pos, e);
            }
        } else {
            if (player.getMainHandItem().getItem() instanceof BlockItem && !serverLevel.noCollision(null, new AABB(pos))) {
                golem.getGolemEntity().setPos(golem.getGolemEntity().getX() + face.getStepX(), golem.getGolemEntity().getY() + face.getStepY(), golem.getGolemEntity().getZ() + face.getStepZ());
            }
            try {
                BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), face, pos, false);
                player.gameMode.useItemOn(player, serverLevel, player.getMainHandItem(), InteractionHand.MAIN_HAND, hit);
            } catch (Exception e) {
                Thaumaturge.LOGGER.error("Golem right-click at {} failed", pos, e);
            }
        }
        golem.addRankXp(1);
        if (!player.getMainHandItem().isEmpty() && player.getMainHandItem().getCount() <= 0) {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
        returnItems(player, golem);
        golem.swingArm();
    }

    private static void returnItems(FakePlayer player, IGolemAPI golem) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (golem.canCarry(stack, true)) {
                stack = golem.holdItem(stack);
            }
            if (!stack.isEmpty()) {
                InvHelper.dropItemAtEntity(golem.getGolemWorld(), stack, golem.getGolemEntity());
            }
            inventory.setItem(slot, ItemStack.EMPTY);
        }
    }
}
