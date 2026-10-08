package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.stream.IntStream;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record MazeGeometry(ChunkPos origin, int width, int depth, int baseY, int height, int floorY) {
    public static final int CELL = 16;
    public static final int CELL_SHIFT = 4;
    private static final Codec<ChunkPos> CHUNK_POS_CODEC = Codec.INT_STREAM.comapFlatMap(
            stream -> Util.fixedSize(stream, 2).map(values -> new ChunkPos(values[0], values[1])),
            pos -> IntStream.of(pos.x, pos.z));
    public static final Codec<MazeGeometry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    CHUNK_POS_CODEC.fieldOf("origin").forGetter(MazeGeometry::origin),
                    Codec.INT.fieldOf("width").forGetter(MazeGeometry::width),
                    Codec.INT.fieldOf("depth").forGetter(MazeGeometry::depth),
                    Codec.INT.fieldOf("base_y").forGetter(MazeGeometry::baseY),
                    Codec.INT.fieldOf("height").forGetter(MazeGeometry::height),
                    Codec.INT.fieldOf("floor_y").forGetter(MazeGeometry::floorY))
            .apply(instance, MazeGeometry::new));

    public int minX() {
        return origin.x << CELL_SHIFT;
    }

    public int minZ() {
        return origin.z << CELL_SHIFT;
    }

    public BoundingBox bounds() {
        return new BoundingBox(
                minX(), baseY, minZ(), minX() + width * CELL - 1, baseY + height - 1, minZ() + depth * CELL - 1);
    }

    public boolean containsCell(int cellX, int cellZ) {
        return cellX >= 0 && cellZ >= 0 && cellX < width && cellZ < depth;
    }

    public boolean containsChunk(int chunkX, int chunkZ, int margin) {
        int x = chunkX - origin.x;
        int z = chunkZ - origin.z;
        return x >= -margin && z >= -margin && x < width + margin && z < depth + margin;
    }

    public int cellOfChunkX(int chunkX) {
        return chunkX - origin.x;
    }

    public int cellOfChunkZ(int chunkZ) {
        return chunkZ - origin.z;
    }

    public int cellOfBlockX(int blockX) {
        return cellOfChunkX(blockX >> CELL_SHIFT);
    }

    public int cellOfBlockZ(int blockZ) {
        return cellOfChunkZ(blockZ >> CELL_SHIFT);
    }

    public int index(int cellX, int cellZ) {
        return cellX + cellZ * width;
    }

    public BlockPos cellMin(int cellX, int cellZ) {
        return new BlockPos((origin.x + cellX) << CELL_SHIFT, baseY, (origin.z + cellZ) << CELL_SHIFT);
    }

    public BlockPos cellCenter(int cellX, int cellZ) {
        return new BlockPos(
                ((origin.x + cellX) << CELL_SHIFT) + CELL / 2,
                floorY + 1,
                ((origin.z + cellZ) << CELL_SHIFT) + CELL / 2);
    }
}
