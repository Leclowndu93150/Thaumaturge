package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.TCIds;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.ChunkPos;

public record ClientboundWardChunkPayload(ChunkPos chunk, List<ClientboundWardChunkPayload.Group> groups) implements CustomPacketPayload {
    public static final Type<ClientboundWardChunkPayload> TYPE = new Type<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "ward_chunk"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ClientboundWardChunkPayload> STREAM_CODEC = StreamCodec.composite(ChunkPos.STREAM_CODEC, ClientboundWardChunkPayload::chunk,
            Group.STREAM_CODEC.apply(ByteBufCodecs.list()), ClientboundWardChunkPayload::groups, ClientboundWardChunkPayload::new);

    private static final int LOCAL_MASK = 15;
    private static final int Z_SHIFT = 4;
    private static final int Y_SHIFT = 8;

    public static ClientboundWardChunkPayload of(ChunkPos chunk, Map<BlockPos, UUID> owners) {
        Map<UUID, List<Integer>> byOwner = new LinkedHashMap<>();
        for (Map.Entry<BlockPos, UUID> entry : owners.entrySet()) {
            byOwner.computeIfAbsent(entry.getValue(), key -> new ArrayList<>()).add(pack(entry.getKey()));
        }
        List<Group> groups = new ArrayList<>(byOwner.size());
        for (Map.Entry<UUID, List<Integer>> entry : byOwner.entrySet()) {
            groups.add(new Group(entry.getKey(), entry.getValue()));
        }
        return new ClientboundWardChunkPayload(chunk, groups);
    }

    public BlockPos unpack(int packed) {
        return new BlockPos(chunk.getMinBlockX() + (packed & LOCAL_MASK), packed >> Y_SHIFT, chunk.getMinBlockZ() + (packed >> Z_SHIFT & LOCAL_MASK));
    }

    private static int pack(BlockPos pos) {
        return pos.getY() << Y_SHIFT | (pos.getZ() & LOCAL_MASK) << Z_SHIFT | pos.getX() & LOCAL_MASK;
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public record Group(UUID owner, List<Integer> positions) {
        private static final StreamCodec<RegistryFriendlyByteBuf, Group> STREAM_CODEC = StreamCodec.composite(UUIDUtil.STREAM_CODEC, Group::owner, ByteBufCodecs.INT.apply(ByteBufCodecs.list()),
                Group::positions, Group::new);
    }
}
