package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public record MazePlan(
        MazeId id,
        GlobalPos origin,
        MazeGeometry geometry,
        long seed,
        ResourceKey<LabyrinthDefinition> definition,
        Optional<ResourceKey<LabyrinthEncounter>> encounter,
        LabyrinthTuning tuning,
        List<ResourceKey<RoomType>> roomPalette,
        List<ResourceLocation> templatePalette,
        long[] rooms,
        int[] cells,
        MazeLandmarks landmarks) {
    private static final Codec<long[]> LONGS = Codec.LONG_STREAM.xmap(LongStream::toArray, Arrays::stream);
    private static final Codec<int[]> INTS = Codec.INT_STREAM.xmap(IntStream::toArray, Arrays::stream);
    public static final Codec<MazePlan> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    MazeId.CODEC.fieldOf("id").forGetter(MazePlan::id),
                    GlobalPos.CODEC.fieldOf("origin").forGetter(MazePlan::origin),
                    MazeGeometry.CODEC.fieldOf("geometry").forGetter(MazePlan::geometry),
                    Codec.LONG.fieldOf("seed").forGetter(MazePlan::seed),
                    ResourceKey.codec(LabyrinthDefinition.REGISTRY_KEY)
                            .fieldOf("definition")
                            .forGetter(MazePlan::definition),
                    ResourceKey.codec(LabyrinthEncounter.REGISTRY_KEY)
                            .optionalFieldOf("encounter")
                            .forGetter(MazePlan::encounter),
                    LabyrinthTuning.CODEC.fieldOf("tuning").forGetter(MazePlan::tuning),
                    ResourceKey.codec(RoomType.REGISTRY_KEY)
                            .listOf()
                            .fieldOf("room_palette")
                            .forGetter(MazePlan::roomPalette),
                    ResourceLocation.CODEC.listOf().fieldOf("template_palette").forGetter(MazePlan::templatePalette),
                    LONGS.fieldOf("rooms").forGetter(MazePlan::rooms),
                    INTS.fieldOf("cells").forGetter(MazePlan::cells),
                    MazeLandmarks.CODEC.fieldOf("landmarks").forGetter(MazePlan::landmarks))
            .apply(instance, MazePlan::new));

    public int cell(int cellX, int cellZ) {
        return cells[geometry.index(cellX, cellZ)];
    }

    public Optional<PlacedRoom> roomAt(int cellX, int cellZ) {
        if (!geometry.containsCell(cellX, cellZ)) {
            return Optional.empty();
        }
        int index = MazeCells.room(cell(cellX, cellZ));
        return index == MazeCells.NO_ROOM ? Optional.empty() : Optional.of(placedRoom(index));
    }

    public PlacedRoom placedRoom(int index) {
        long room = rooms[index];
        return new PlacedRoom(
                index,
                roomPalette.get(MazeCells.palette(room)),
                templatePalette.get(MazeCells.template(room)),
                MazeCells.transform(room),
                MazeCells.anchorX(room),
                MazeCells.anchorZ(room));
    }

    public Optional<Direction> step(int cellX, int cellZ, int target) {
        if (!geometry.containsCell(cellX, cellZ)) {
            return Optional.empty();
        }
        return MazeCells.direction(cell(cellX, cellZ), target);
    }

    public Optional<Integer> distance(int cellX, int cellZ, int target) {
        if (!geometry.containsCell(cellX, cellZ)) {
            return Optional.empty();
        }
        int x = cellX;
        int z = cellZ;
        int steps = 0;
        int limit = geometry.width() * geometry.depth();
        Optional<Direction> next = MazeCells.direction(cell(x, z), target);
        while (next.isPresent() && steps < limit) {
            x += next.get().getStepX();
            z += next.get().getStepZ();
            steps++;
            next = geometry.containsCell(x, z) ? MazeCells.direction(cell(x, z), target) : Optional.empty();
        }
        return Optional.of(steps);
    }

    public record PlacedRoom(
            int index,
            ResourceKey<RoomType> type,
            ResourceLocation template,
            int transform,
            int anchorX,
            int anchorZ) {}
}
