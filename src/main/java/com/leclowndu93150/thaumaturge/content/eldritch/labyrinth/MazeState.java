package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.EncounterState;
import com.leclowndu93150.thaumaturge.content.eldritch.guardian.PostLedger;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ClaimState;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class MazeState {
    public static final Codec<MazeState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    LabyrinthPhase.CODEC.fieldOf("phase").forGetter(MazeState::phase),
                    Codec.LONG.fieldOf("phase_since").forGetter(MazeState::phaseSince),
                    Codec.LONG.fieldOf("created_at").forGetter(MazeState::createdAt),
                    Codec.LONG.fieldOf("last_visited").forGetter(MazeState::lastVisited),
                    PendingTrigger.CODEC.listOf().fieldOf("triggers").forGetter(MazeState::triggers),
                    EncounterState.CODEC.optionalFieldOf("encounter").forGetter(state -> Optional.of(state.encounter)),
                    ClaimState.CODEC.optionalFieldOf("claims").forGetter(state -> Optional.of(state.claims)),
                    PostLedger.CODEC.optionalFieldOf("posts").forGetter(state -> Optional.of(state.posts)))
            .apply(instance, MazeState::new));

    private static final long VISIT_SAVE_INTERVAL_TICKS = 1200L;

    private LabyrinthPhase phase;
    private long phaseSince;
    private final long createdAt;
    private long lastVisited;
    private long lastSavedVisit = Long.MIN_VALUE;
    private final List<PendingTrigger> triggers;
    private final EncounterState encounter;
    private final ClaimState claims;
    private final PostLedger posts;

    private MazeState(
            LabyrinthPhase phase,
            long phaseSince,
            long createdAt,
            long lastVisited,
            List<PendingTrigger> triggers,
            Optional<EncounterState> encounter,
            Optional<ClaimState> claims,
            Optional<PostLedger> posts) {
        this.phase = phase;
        this.phaseSince = phaseSince;
        this.createdAt = createdAt;
        this.lastVisited = lastVisited;
        this.triggers = new ArrayList<>(triggers);
        this.encounter = encounter.orElseGet(EncounterState::new);
        this.claims = claims.orElseGet(ClaimState::new);
        this.posts = posts.orElseGet(PostLedger::new);
    }

    public static MazeState fresh(long gameTime, List<PendingTrigger> triggers) {
        return new MazeState(
                LabyrinthPhase.SEALED,
                gameTime,
                gameTime,
                gameTime,
                triggers,
                Optional.empty(),
                Optional.empty(),
                Optional.empty());
    }

    public LabyrinthPhase phase() {
        return phase;
    }

    public long phaseSince() {
        return phaseSince;
    }

    public long createdAt() {
        return createdAt;
    }

    public long lastVisited() {
        return lastVisited;
    }

    public List<PendingTrigger> triggers() {
        return triggers;
    }

    public EncounterState encounter() {
        return encounter;
    }

    public ClaimState claims() {
        return claims;
    }

    public PostLedger posts() {
        return posts;
    }

    public void setPhase(LabyrinthPhase phase, long gameTime) {
        this.phase = phase;
        this.phaseSince = gameTime;
    }

    public boolean visit(long gameTime) {
        this.lastVisited = gameTime;
        if (gameTime - lastSavedVisit < VISIT_SAVE_INTERVAL_TICKS) {
            return false;
        }
        lastSavedVisit = gameTime;
        return true;
    }
}
