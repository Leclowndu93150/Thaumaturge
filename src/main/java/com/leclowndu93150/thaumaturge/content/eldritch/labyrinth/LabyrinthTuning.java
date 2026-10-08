package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthHelper;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.config.labyrinth.LabyrinthConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;

public record LabyrinthTuning(
        float sizeScale,
        float loopScale,
        float decorationScale,
        float lootScale,
        List<ResourceLocation> disabledMarkers) {
    public static final LabyrinthTuning DEFAULT = new LabyrinthTuning(1.0F, 1.0F, 1.0F, 1.0F, List.of());
    public static final Codec<LabyrinthTuning> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.FLOAT.fieldOf("size_scale").forGetter(LabyrinthTuning::sizeScale),
                    Codec.FLOAT.fieldOf("loop_scale").forGetter(LabyrinthTuning::loopScale),
                    Codec.FLOAT.fieldOf("decoration_scale").forGetter(LabyrinthTuning::decorationScale),
                    Codec.FLOAT.fieldOf("loot_scale").forGetter(LabyrinthTuning::lootScale),
                    ResourceLocation.CODEC
                            .listOf()
                            .optionalFieldOf("disabled_markers", List.of())
                            .forGetter(LabyrinthTuning::disabledMarkers))
            .apply(instance, LabyrinthTuning::new));

    public static LabyrinthTuning fromConfig() {
        LabyrinthConfig config = ThaumaturgeServerConfig.LABYRINTH;
        List<ResourceLocation> markers = config.disabledMarkers.get().stream()
                .map(ResourceLocation::tryParse)
                .filter(Objects::nonNull)
                .toList();
        return new LabyrinthTuning(
                config.sizeScale.get().floatValue(),
                config.loopScale.get().floatValue(),
                config.decorationDensity.get().floatValue(),
                config.lootScale.get().floatValue(),
                markers);
    }

    public boolean disables(LabyrinthMarker marker) {
        ResourceLocation type = LabyrinthHelper.markerTypes().getKey(marker.type());
        return type != null && disabledMarkers.contains(type);
    }
}
