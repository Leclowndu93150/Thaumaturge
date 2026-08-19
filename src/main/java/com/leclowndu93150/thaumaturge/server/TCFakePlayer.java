package com.leclowndu93150.thaumaturge.server;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;

public enum TCFakePlayer {
    GOLEM("[ThaumaturgeGolem]"), BORE("[ThaumaturgeBore]");

    private final GameProfile profile;

    TCFakePlayer(String name) {
        this.profile = new GameProfile(UUID.randomUUID(), name);
    }

    public FakePlayer get(ServerLevel level) {
        return FakePlayerFactory.get(level, profile);
    }

    public FakePlayer at(ServerLevel level, Entity entity) {
        FakePlayer player = get(level);
        player.snapTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), entity.getXRot());
        return player;
    }
}
