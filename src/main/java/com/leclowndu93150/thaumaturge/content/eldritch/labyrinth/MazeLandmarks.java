package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record MazeLandmarks(
        Map<ResourceLocation, BlockPos> points, List<BlockPos> barriers, List<BlockPos> glyphs, BoundingBox bossHall) {
    public static final Codec<MazeLandmarks> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.unboundedMap(ResourceLocation.CODEC, BlockPos.CODEC)
                            .fieldOf("points")
                            .forGetter(MazeLandmarks::points),
                    BlockPos.CODEC.listOf().fieldOf("barriers").forGetter(MazeLandmarks::barriers),
                    BlockPos.CODEC.listOf().fieldOf("glyphs").forGetter(MazeLandmarks::glyphs),
                    BoundingBox.CODEC.fieldOf("boss_hall").forGetter(MazeLandmarks::bossHall))
            .apply(instance, MazeLandmarks::new));

    public Optional<BlockPos> point(ResourceLocation id) {
        return Optional.ofNullable(points.get(id));
    }
}
