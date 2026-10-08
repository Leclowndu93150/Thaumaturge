package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;

public final class Participation {
    public static final Codec<Participation> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.INT)
                            .optionalFieldOf("presence", Map.of())
                            .forGetter(state -> state.presence),
                    Codec.unboundedMap(UUIDUtil.STRING_CODEC, Codec.FLOAT)
                            .optionalFieldOf("damage", Map.of())
                            .forGetter(state -> state.damage),
                    Codec.FLOAT.optionalFieldOf("pool", 0.0F).forGetter(state -> state.pool),
                    UUIDUtil.CODEC.listOf().optionalFieldOf("eligible").forGetter(Participation::eligibleList),
                    UUIDUtil.CODEC.optionalFieldOf("top").forGetter(state -> state.top))
            .apply(instance, Participation::new));

    private final Map<UUID, Integer> presence;
    private final Map<UUID, Float> damage;
    private float pool;
    private Optional<Set<UUID>> eligible;
    private Optional<UUID> top;

    Participation() {
        this(Map.of(), Map.of(), 0.0F, Optional.empty(), Optional.empty());
    }

    private Participation(
            Map<UUID, Integer> presence,
            Map<UUID, Float> damage,
            float pool,
            Optional<List<UUID>> eligible,
            Optional<UUID> top) {
        this.presence = new HashMap<>(presence);
        this.damage = new HashMap<>(damage);
        this.pool = pool;
        this.eligible = eligible.map(HashSet::new);
        this.top = top;
    }

    private Optional<List<UUID>> eligibleList() {
        return eligible.map(ArrayList::new);
    }

    public boolean eligible(UUID player) {
        return eligible.map(set -> set.contains(player)).orElse(false);
    }

    public Optional<UUID> top() {
        return top;
    }

    void reset() {
        presence.clear();
        damage.clear();
        this.pool = 0.0F;
        eligible = Optional.empty();
        top = Optional.empty();
    }

    void setPool(float pool) {
        this.pool = pool;
    }

    void addPresence(UUID player, int ticks) {
        presence.merge(player, ticks, Integer::sum);
    }

    void addDamage(UUID player, float amount) {
        damage.merge(player, amount, Float::sum);
    }

    void freeze(int minPresence, float minDamageFraction, Collection<UUID> fallback) {
        Set<UUID> chosen = new HashSet<>();
        float threshold = pool * minDamageFraction;
        presence.forEach((player, ticks) -> {
            if (ticks >= minPresence) {
                chosen.add(player);
            }
        });
        damage.forEach((player, amount) -> {
            if (amount >= threshold) {
                chosen.add(player);
            }
        });
        if (chosen.isEmpty()) {
            chosen.addAll(fallback);
        }
        eligible = Optional.of(chosen);
        top = damage.entrySet().stream().max(Map.Entry.comparingByValue()).map(Map.Entry::getKey);
    }
}
