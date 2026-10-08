package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthHelper;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

final class BossHooks {
    private BossHooks() {}

    static void syncSharedBar(LivingEntity boss, ServerBossEvent bar) {
        if (LabyrinthHelper.sharesBossBar(boss) && !bar.getPlayers().isEmpty()) {
            bar.removeAllPlayers();
        }
    }

    static void showBar(LivingEntity boss, ServerBossEvent bar, ServerPlayer player) {
        if (!LabyrinthHelper.sharesBossBar(boss)) {
            bar.addPlayer(player);
        }
    }

    static void dropPearl(ServerLevel level, LivingEntity boss) {
        level.addFreshEntity(new EntitySpecialItem(
                level,
                boss.getX(),
                boss.getY() + boss.getBbHeight() / 2.0F,
                boss.getZ(),
                new ItemStack(TTItems.PRIMORDIAL_PEARL.get())));
    }
}
