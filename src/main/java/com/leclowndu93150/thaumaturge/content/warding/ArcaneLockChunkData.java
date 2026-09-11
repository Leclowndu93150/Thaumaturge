package com.leclowndu93150.thaumaturge.content.warding;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import org.jspecify.annotations.Nullable;

/** Persistent owner and key permissions for Arcane Doors and Arcane Pressure Plates. */
public final class ArcaneLockChunkData {
    public static final MapCodec<ArcaneLockChunkData> CODEC = Entry.CODEC
            .listOf()
            .optionalFieldOf("locks", List.of())
            .xmap(ArcaneLockChunkData::new, ArcaneLockChunkData::entries);

    private final Map<BlockPos, UUID> owners = new ConcurrentHashMap<>();
    private final Map<BlockPos, Access> access = new ConcurrentHashMap<>();

    public ArcaneLockChunkData() {}

    private ArcaneLockChunkData(List<Entry> entries) {
        for (Entry entry : entries) {
            owners.put(entry.pos(), entry.owner());
            access.put(entry.pos(), new Access(entry.ironAccess(), entry.goldAccess()));
        }
    }

    public @Nullable UUID owner(BlockPos pos) {
        return owners.get(pos);
    }

    public void put(BlockPos pos, UUID owner) {
        owners.put(pos.immutable(), owner);
    }

    public void remove(BlockPos pos) {
        owners.remove(pos);
        access.remove(pos);
    }

    public boolean canAccess(BlockPos pos, UUID player) {
        Access entry = access.get(pos);
        return entry != null && (entry.iron().contains(player) || entry.gold().contains(player));
    }

    public boolean canDelegateIron(BlockPos pos, UUID player) {
        Access entry = access.get(pos);
        return entry != null && entry.gold().contains(player);
    }

    public boolean grantAccess(BlockPos pos, UUID player, boolean gold) {
        Access entry = access.computeIfAbsent(pos.immutable(), ignored -> new Access(List.of(), List.of()));
        return (gold ? entry.gold() : entry.iron()).add(player);
    }

    private List<Entry> entries() {
        return owners.entrySet().stream()
                .map(entry -> {
                    Access entryAccess = access.get(entry.getKey());
                    return new Entry(
                            entry.getKey(),
                            entry.getValue(),
                            entryAccess == null ? List.of() : List.copyOf(entryAccess.iron()),
                            entryAccess == null ? List.of() : List.copyOf(entryAccess.gold()));
                })
                .toList();
    }

    private record Access(java.util.Set<UUID> iron, java.util.Set<UUID> gold) {
        private Access(List<UUID> iron, List<UUID> gold) {
            this(ConcurrentHashMap.newKeySet(), ConcurrentHashMap.newKeySet());
            iron().addAll(iron);
            gold().addAll(gold);
        }
    }

    private record Entry(BlockPos pos, UUID owner, List<UUID> ironAccess, List<UUID> goldAccess) {
        private static final Codec<Entry> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                        BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                        UUIDUtil.CODEC.fieldOf("owner").forGetter(Entry::owner),
                        UUIDUtil.CODEC
                                .listOf()
                                .optionalFieldOf("iron_access", List.of())
                                .forGetter(Entry::ironAccess),
                        UUIDUtil.CODEC
                                .listOf()
                                .optionalFieldOf("gold_access", List.of())
                                .forGetter(Entry::goldAccess))
                .apply(inst, Entry::new));
    }
}
