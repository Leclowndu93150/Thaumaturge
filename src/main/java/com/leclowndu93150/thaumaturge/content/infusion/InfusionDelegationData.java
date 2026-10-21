package com.leclowndu93150.thaumaturge.content.infusion;

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

final class InfusionDelegationData extends SavedData {
    record Grant(UUID id, UUID owner, UUID device, Identifier addon, Identifier dimension, BlockPos position, BlockPos matrix, List<Identifier> recipes, long revision, long expires, int surcharge) {
        static final Codec<Grant> CODEC = RecordCodecBuilder.create(builder -> builder.group(UUIDUtil.CODEC.fieldOf("id").forGetter(Grant::id), UUIDUtil.CODEC.fieldOf("owner").forGetter(Grant::owner),
                UUIDUtil.CODEC.fieldOf("device").forGetter(Grant::device), Identifier.CODEC.fieldOf("addon").forGetter(Grant::addon), Identifier.CODEC.fieldOf("dimension").forGetter(Grant::dimension),
                BlockPos.CODEC.fieldOf("position").forGetter(Grant::position), BlockPos.CODEC.fieldOf("matrix").forGetter(Grant::matrix),
                Identifier.CODEC.listOf().fieldOf("recipes").forGetter(Grant::recipes), Codec.LONG.fieldOf("revision").forGetter(Grant::revision),
                Codec.LONG.fieldOf("expires").forGetter(Grant::expires), Codec.intRange(0, 100).fieldOf("surcharge").forGetter(Grant::surcharge)).apply(builder, Grant::new));

        Grant {
            position = position.immutable();
            matrix = matrix.immutable();
            recipes = List.copyOf(recipes);
        }
    }

    private static final Codec<InfusionDelegationData> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(Codec.BOOL.fieldOf("enabled").forGetter(data -> data.enabled), Codec.BOOL.fieldOf("offline").forGetter(data -> data.offline),
                    Codec.LONG.fieldOf("revision").forGetter(data -> data.revision), Grant.CODEC.listOf().fieldOf("grants").forGetter(data -> new ArrayList<>(data.grants.values())))
            .apply(builder, InfusionDelegationData::new));
    private static final SavedDataType<InfusionDelegationData> TYPE = new SavedDataType<>(TTIds.rl("infusion_delegation"), InfusionDelegationData::new, CODEC, DataFixTypes.LEVEL);

    boolean enabled;
    boolean offline;
    long revision;
    final Map<UUID, Grant> grants = new HashMap<>();

    private InfusionDelegationData() {}

    private InfusionDelegationData(boolean enabled, boolean offline, long revision, List<Grant> grants) {
        this.enabled = enabled;
        this.offline = offline;
        this.revision = revision;
        grants.forEach(grant -> this.grants.put(grant.id(), grant));
    }

    static InfusionDelegationData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(TYPE);
    }
}
