package com.leclowndu93150.thaumaturge.content.aura;

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

final class AuraBaselineDelegations extends SavedData {
    record Grant(UUID token, UUID owner, UUID machine, Identifier dimension, BlockPos position, long chunk, long expires) {
        static final Codec<Grant> CODEC = RecordCodecBuilder.create(builder -> builder.group(UUIDUtil.CODEC.fieldOf("token").forGetter(Grant::token),
                UUIDUtil.CODEC.fieldOf("owner").forGetter(Grant::owner), UUIDUtil.CODEC.fieldOf("machine").forGetter(Grant::machine), Identifier.CODEC.fieldOf("dimension").forGetter(Grant::dimension),
                BlockPos.CODEC.fieldOf("position").forGetter(Grant::position), Codec.LONG.fieldOf("chunk").forGetter(Grant::chunk), Codec.LONG.fieldOf("expires").forGetter(Grant::expires))
                .apply(builder, Grant::new));

        Grant {
            position = position.immutable();
        }
    }

    private static final Codec<AuraBaselineDelegations> CODEC = Grant.CODEC.listOf().xmap(AuraBaselineDelegations::new, data -> new ArrayList<>(data.grants.values()));
    private static final SavedDataType<AuraBaselineDelegations> TYPE = new SavedDataType<>(TTIds.rl("aura_baseline_delegations"), AuraBaselineDelegations::new, CODEC, DataFixTypes.LEVEL);
    final Map<UUID, Grant> grants = new HashMap<>();

    private AuraBaselineDelegations() {}

    private AuraBaselineDelegations(List<Grant> grants) {
        grants.forEach(grant -> this.grants.put(grant.token(), grant));
    }

    static AuraBaselineDelegations get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }
}
