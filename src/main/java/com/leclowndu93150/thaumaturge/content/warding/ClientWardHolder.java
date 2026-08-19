package com.leclowndu93150.thaumaturge.content.warding;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.ChunkPos;

public final class ClientWardHolder {
    private static final Map<BlockPos, Boolean> WARDS = new ConcurrentHashMap<>();
    private static final Map<SectionPos, Set<BlockPos>> SECTIONS = new ConcurrentHashMap<>();

    private ClientWardHolder() {}

    public static boolean isWarded(BlockPos pos) {
        return WARDS.containsKey(pos);
    }

    public static boolean isOwned(BlockPos pos) {
        return Boolean.TRUE.equals(WARDS.get(pos));
    }

    public static Set<SectionPos> sections() {
        return SECTIONS.keySet();
    }

    public static Set<BlockPos> wardsIn(SectionPos section) {
        Set<BlockPos> wards = SECTIONS.get(section);
        return wards == null ? Set.of() : wards;
    }

    public static void put(BlockPos pos, boolean owned) {
        BlockPos immutable = pos.immutable();
        WARDS.put(immutable, owned);
        SECTIONS.computeIfAbsent(SectionPos.of(immutable), key -> ConcurrentHashMap.newKeySet()).add(immutable);
    }

    public static void remove(BlockPos pos) {
        if (WARDS.remove(pos) == null) {
            return;
        }
        SectionPos section = SectionPos.of(pos);
        Set<BlockPos> wards = SECTIONS.get(section);
        if (wards != null) {
            wards.remove(pos);
            if (wards.isEmpty()) {
                SECTIONS.remove(section);
            }
        }
    }

    public static void replaceChunk(ChunkPos chunk, List<BlockPos> owned, List<BlockPos> foreign) {
        forgetChunk(chunk);
        for (BlockPos pos : owned) {
            put(pos, true);
        }
        for (BlockPos pos : foreign) {
            put(pos, false);
        }
    }

    public static void forgetChunk(ChunkPos chunk) {
        SECTIONS.keySet().removeIf(section -> {
            if (section.x() != chunk.x() || section.z() != chunk.z()) {
                return false;
            }
            SECTIONS.get(section).forEach(WARDS::remove);
            return true;
        });
    }

    public static void clear() {
        WARDS.clear();
        SECTIONS.clear();
    }
}
