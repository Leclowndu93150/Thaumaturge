package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerTriggerContext;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.guardian.GuardianPosts;
import com.leclowndu93150.thaumaturge.content.eldritch.guardian.WardedMarker;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthMarkers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;

public record GuardianPostMarker(Optional<Integer> radius, Optional<String> ward)
        implements LabyrinthMarker, WardedMarker {
    private static final int MAX_RADIUS = 64;
    public static final MapCodec<GuardianPostMarker> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.intRange(1, MAX_RADIUS).optionalFieldOf("radius").forGetter(GuardianPostMarker::radius),
                    Codec.STRING.optionalFieldOf("ward").forGetter(GuardianPostMarker::ward))
            .apply(instance, GuardianPostMarker::new));

    @Override
    public LabyrinthMarkerType<?> type() {
        return TTLabyrinthMarkers.GUARDIAN_POST.get();
    }

    @Override
    public MarkerPhase phase() {
        return MarkerPhase.TRIGGER;
    }

    @Override
    public int triggerRadius() {
        return radius.orElseGet(ThaumaturgeServerConfig.LABYRINTH.postActivationRadius::get);
    }

    @Override
    public boolean canTrigger(MarkerTriggerContext context) {
        return GuardianPosts.canFire(context);
    }

    @Override
    public void trigger(MarkerTriggerContext context, BlockPos pos) {
        GuardianPosts.fire(context, pos, ward);
    }
}
