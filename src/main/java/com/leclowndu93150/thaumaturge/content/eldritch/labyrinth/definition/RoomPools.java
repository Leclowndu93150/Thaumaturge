package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.HolderSet;

public record RoomPools(
        HolderSet<RoomType> portal,
        HolderSet<RoomType> key,
        HolderSet<RoomType> passages,
        HolderSet<RoomType> fallback,
        HolderSet<RoomType> bossHalls,
        List<FeatureQuota> features) {
    public static final Codec<RoomPools> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    RoomType.SET_CODEC.fieldOf("portal").forGetter(RoomPools::portal),
                    RoomType.SET_CODEC.fieldOf("key").forGetter(RoomPools::key),
                    RoomType.SET_CODEC.fieldOf("passages").forGetter(RoomPools::passages),
                    RoomType.SET_CODEC.fieldOf("fallback").forGetter(RoomPools::fallback),
                    RoomType.SET_CODEC.fieldOf("boss_halls").forGetter(RoomPools::bossHalls),
                    FeatureQuota.CODEC
                            .listOf()
                            .optionalFieldOf("features", List.of())
                            .forGetter(RoomPools::features))
            .apply(instance, RoomPools::new));
}
