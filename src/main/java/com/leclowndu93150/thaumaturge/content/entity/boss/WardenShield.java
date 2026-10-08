package com.leclowndu93150.thaumaturge.content.entity.boss;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

final class WardenShield {
    private static final double SHARE_OF_HEALTH = 0.66;
    private static final int REGROWTH_INTERVAL = 25;
    private static final float REGROWTH = 1.0F;

    private final LivingEntity bearer;
    private final ServerBossEvent bar;

    WardenShield(LivingEntity bearer) {
        this.bearer = bearer;
        this.bar = new ServerBossEvent(
                Component.empty(), BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.NOTCHED_10);
    }

    static double capacityFor(double health) {
        return health * SHARE_OF_HEALTH;
    }

    private int capacity() {
        return (int) capacityFor(bearer.getAttributeBaseValue(Attributes.MAX_HEALTH));
    }

    void raise() {
        bearer.setAbsorptionAmount(bearer.getAbsorptionAmount() + capacity());
    }

    boolean broken() {
        return bearer.getAbsorptionAmount() <= 0.0F;
    }

    void tick() {
        int capacity = capacity();
        if (bearer.invulnerableTime <= 0
                && bearer.tickCount % REGROWTH_INTERVAL == 0
                && bearer.getAbsorptionAmount() < capacity) {
            bearer.setAbsorptionAmount(bearer.getAbsorptionAmount() + REGROWTH);
        }
        bar.setProgress(Mth.clamp(bearer.getAbsorptionAmount() / capacity, 0.0F, 1.0F));
    }

    void show(ServerPlayer player) {
        bar.addPlayer(player);
    }

    void hide(ServerPlayer player) {
        bar.removePlayer(player);
    }
}
