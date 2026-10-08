package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceKey;

public final class EncounterState {
    private static final Codec<ResourceKey<LabyrinthEncounter>> KEY =
            ResourceKey.codec(LabyrinthEncounter.REGISTRY_KEY);
    public static final Codec<EncounterState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    KEY.optionalFieldOf("override").forGetter(EncounterState::override),
                    KEY.optionalFieldOf("active").forGetter(EncounterState::active),
                    BoundEntity.CODEC
                            .listOf()
                            .optionalFieldOf("bound", List.of())
                            .forGetter(EncounterState::bound),
                    Codec.INT.optionalFieldOf("scaled_for", 0).forGetter(EncounterState::scaledFor),
                    Codec.LONG.optionalFieldOf("charged_at", -1L).forGetter(EncounterState::chargedAt),
                    Codec.LONG.optionalFieldOf("empty_since", -1L).forGetter(EncounterState::emptySince),
                    Codec.LONG.optionalFieldOf("victory_at", -1L).forGetter(EncounterState::victoryAt),
                    Participation.CODEC
                            .optionalFieldOf("participation")
                            .forGetter(state -> Optional.of(state.participation)))
            .apply(instance, EncounterState::new));

    private Optional<ResourceKey<LabyrinthEncounter>> override;
    private Optional<ResourceKey<LabyrinthEncounter>> active;
    private final List<BoundEntity> bound;
    private int scaledFor;
    private long chargedAt;
    private long emptySince;
    private long victoryAt;
    private final Participation participation;

    public EncounterState() {
        this(Optional.empty(), Optional.empty(), List.of(), 0, -1L, -1L, -1L, Optional.empty());
    }

    private EncounterState(
            Optional<ResourceKey<LabyrinthEncounter>> override,
            Optional<ResourceKey<LabyrinthEncounter>> active,
            List<BoundEntity> bound,
            int scaledFor,
            long chargedAt,
            long emptySince,
            long victoryAt,
            Optional<Participation> participation) {
        this.override = override;
        this.active = active;
        this.bound = new ArrayList<>(bound);
        this.scaledFor = scaledFor;
        this.chargedAt = chargedAt;
        this.emptySince = emptySince;
        this.victoryAt = victoryAt;
        this.participation = participation.orElseGet(Participation::new);
    }

    public Participation participation() {
        return participation;
    }

    public Optional<ResourceKey<LabyrinthEncounter>> override() {
        return override;
    }

    public Optional<ResourceKey<LabyrinthEncounter>> active() {
        return active;
    }

    public List<BoundEntity> bound() {
        return bound;
    }

    public boolean binds(UUID uuid) {
        for (BoundEntity entry : bound) {
            if (entry.uuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public int scaledFor() {
        return scaledFor;
    }

    long chargedAt() {
        return chargedAt;
    }

    long emptySince() {
        return emptySince;
    }

    long victoryAt() {
        return victoryAt;
    }

    public void setOverride(Optional<ResourceKey<LabyrinthEncounter>> override) {
        this.override = override;
    }

    void choose(ResourceKey<LabyrinthEncounter> key) {
        this.active = Optional.of(key);
    }

    void start(ResourceKey<LabyrinthEncounter> key, int participants) {
        this.active = Optional.of(key);
        this.scaledFor = participants;
        clearRun();
    }

    void remove(int index) {
        bound.remove(index);
    }

    void charge(long gameTime) {
        this.chargedAt = gameTime;
    }

    void setScaledFor(int participants) {
        this.scaledFor = participants;
    }

    void setEmptySince(long gameTime) {
        this.emptySince = gameTime;
    }

    void setVictoryAt(long gameTime) {
        this.victoryAt = gameTime;
    }

    void add(BoundEntity entity) {
        bound.add(entity);
    }

    void set(int index, BoundEntity entity) {
        bound.set(index, entity);
    }

    boolean tracks(UUID uuid) {
        for (BoundEntity entity : bound) {
            if (entity.uuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    boolean defeat(UUID uuid) {
        for (int i = 0; i < bound.size(); i++) {
            BoundEntity entity = bound.get(i);
            if (entity.uuid().equals(uuid) && !entity.defeated()) {
                bound.set(i, entity.defeat());
                return true;
            }
        }
        return false;
    }

    int primaryCount() {
        int count = 0;
        for (BoundEntity entity : bound) {
            if (entity.primary()) {
                count++;
            }
        }
        return count;
    }

    boolean primariesDefeated() {
        boolean any = false;
        for (BoundEntity entity : bound) {
            if (entity.primary()) {
                if (!entity.defeated()) {
                    return false;
                }
                any = true;
            }
        }
        return any;
    }

    void reset() {
        this.active = Optional.empty();
        this.scaledFor = 0;
        this.chargedAt = -1L;
        clearRun();
    }

    private void clearRun() {
        this.bound.clear();
        this.emptySince = -1L;
        this.victoryAt = -1L;
        this.participation.reset();
    }
}
