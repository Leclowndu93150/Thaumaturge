package com.leclowndu93150.thaumaturge.content.eldritch.reliquary;

import com.mojang.serialization.Codec;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

public final class ClaimState {
    public static final Codec<ClaimState> CODEC =
            Codec.unboundedMap(Codec.STRING, UUIDUtil.CODEC.listOf()).xmap(ClaimState::new, ClaimState::export);

    private final Map<String, Set<UUID>> claims = new HashMap<>();

    public ClaimState() {}

    private ClaimState(Map<String, List<UUID>> stored) {
        stored.forEach((key, players) -> claims.put(key, new HashSet<>(players)));
    }

    private Map<String, List<UUID>> export() {
        Map<String, List<UUID>> out = new HashMap<>();
        claims.forEach((key, players) -> out.put(key, List.copyOf(players)));
        return out;
    }

    boolean claimed(String reliquary, UUID player) {
        Set<UUID> players = claims.get(reliquary);
        return players != null && players.contains(player);
    }

    void claim(String reliquary, UUID player) {
        claims.computeIfAbsent(reliquary, key -> new HashSet<>()).add(player);
    }
}
