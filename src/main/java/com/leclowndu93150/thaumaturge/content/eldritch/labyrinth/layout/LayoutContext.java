package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.layout;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthTuning;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

public record LayoutContext(
        LabyrinthDefinition definition,
        LabyrinthTuning tuning,
        Predicate<Holder<RoomType>> enabled,
        RandomSource random) {
    ResourceLocation template(Holder<RoomType> room) {
        return room.value().templates().getRandomValue(random).orElseThrow();
    }

    int randomTransform(Holder<RoomType> room) {
        return WeightedPick.any(Dihedral.allowedTransforms(room.value().transforms()), random);
    }
}
