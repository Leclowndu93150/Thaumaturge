package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

final class LooseItems {
    private LooseItems() {}

    static void spawn(ServerLevel level, Vec3 at, ItemStack stack, Vec3 motion) {
        if (!stack.isEmpty()) {
            level.addFreshEntity(new ItemEntity(level, at.x, at.y, at.z, stack, motion.x, motion.y, motion.z));
        }
    }

    static Vec3 inFrontOf(BlockPos pos, Direction face) {
        return Vec3.atCenterOf(pos.relative(face));
    }

    static void giveOrDrop(ServerLevel level, IGolemAPI golem, ItemStack stack) {
        ItemStack overflow = golem.hands().hold(stack);
        if (!overflow.isEmpty()) {
            golem.asEntity().spawnAtLocation(level, overflow);
        }
    }
}
