package com.leclowndu93150.thaumaturge.content.entity.boss;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthHelper;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;

public final class BossBar {
    private final LivingEntity boss;
    private final ServerBossEvent event;

    public BossBar(LivingEntity boss) {
        this.boss = boss;
        this.event = new ServerBossEvent(
                boss.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        this.event.setDarkenScreen(true);
    }

    public ServerBossEvent event() {
        return event;
    }

    public void rename() {
        event.setName(boss.getDisplayName());
    }

    public void update() {
        event.setProgress(boss.getHealth() / boss.getMaxHealth());
        if (LabyrinthHelper.sharesBossBar(boss) && !event.getPlayers().isEmpty()) {
            event.removeAllPlayers();
        }
    }

    public void show(ServerPlayer player) {
        if (!LabyrinthHelper.sharesBossBar(boss)) {
            event.addPlayer(player);
        }
    }

    public void hide(ServerPlayer player) {
        event.removePlayer(player);
    }
}
