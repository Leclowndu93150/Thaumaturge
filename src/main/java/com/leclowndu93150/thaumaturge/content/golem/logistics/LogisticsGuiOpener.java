package com.leclowndu93150.thaumaturge.content.golem.logistics;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class LogisticsGuiOpener {
    private static final ResourceLocation RESEARCH = TCIds.rl("golem_logistics");

    private LogisticsGuiOpener() {}

    public static boolean canOpen(Player player) {
        return player.isShiftKeyDown() && KnowledgeAccess.of(player).isResearchComplete(RESEARCH);
    }

    public static void open(Player player, @Nullable LogisticsTarget target) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        serverPlayer.openMenu(
                new SimpleMenuProvider(
                        (containerId, inventory, menuPlayer) -> new MenuGolemLogistics(containerId, inventory, target),
                        Component.translatable("gui.thaumaturge.logistics")),
                buf -> {});
    }
}
