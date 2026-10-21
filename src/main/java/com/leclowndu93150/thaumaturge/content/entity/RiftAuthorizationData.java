package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

final class RiftAuthorizationData extends SavedData {
    record Grant(UUID id, UUID owner, UUID instance, Identifier resolver, Identifier dimension, BlockPos position, List<String> actions, int radius, long expires) {
        static final Codec<Grant> CODEC = RecordCodecBuilder.create(builder -> builder
                .group(UUIDUtil.CODEC.fieldOf("id").forGetter(Grant::id), UUIDUtil.CODEC.fieldOf("owner").forGetter(Grant::owner), UUIDUtil.CODEC.fieldOf("instance").forGetter(Grant::instance),
                        Identifier.CODEC.fieldOf("resolver").forGetter(Grant::resolver), Identifier.CODEC.fieldOf("dimension").forGetter(Grant::dimension),
                        BlockPos.CODEC.fieldOf("position").forGetter(Grant::position), Codec.STRING.listOf().fieldOf("actions").forGetter(Grant::actions),
                        Codec.intRange(0, 64).fieldOf("radius").forGetter(Grant::radius), Codec.LONG.fieldOf("expires").forGetter(Grant::expires))
                .apply(builder, Grant::new));

        Grant {
            position = position.immutable();
            actions = List.copyOf(actions);
        }
    }

    private static final Codec<RiftAuthorizationData> CODEC = Grant.CODEC.listOf().xmap(RiftAuthorizationData::new, data -> new ArrayList<>(data.grants.values()));
    private static final SavedDataType<RiftAuthorizationData> TYPE = new SavedDataType<>(TTIds.rl("rift_authorization"), RiftAuthorizationData::new, CODEC, DataFixTypes.LEVEL);
    final Map<UUID, Grant> grants = new HashMap<>();

    private RiftAuthorizationData() {}

    private RiftAuthorizationData(List<Grant> grants) {
        grants.forEach(grant -> this.grants.put(grant.id(), grant));
    }

    static RiftAuthorizationData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }
}
