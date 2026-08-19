package com.leclowndu93150.thaumaturge.content.eldritch.maze;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;
import org.jspecify.annotations.Nullable;

public final class MazeSavedData extends SavedData {
    private static final Codec<Map<Long, Short>> CELLS_CODEC = Codec.unboundedMap(Codec.STRING.xmap(Long::parseLong, String::valueOf), Codec.SHORT);

    public static final Codec<MazeSavedData> CODEC = RecordCodecBuilder.create(
            builder -> builder.group(CELLS_CODEC.fieldOf("cells").forGetter(data -> data.cells), Codec.INT.fieldOf("boss_count").forGetter(data -> data.bossCount)).apply(builder, MazeSavedData::new));

    public static final SavedDataType<MazeSavedData> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "labyrinth"), MazeSavedData::new, CODEC, DataFixTypes.LEVEL);

    private final Map<Long, Short> cells;
    private int bossCount;

    public MazeSavedData() {
        this(new ConcurrentHashMap<>());
    }

    private MazeSavedData(Map<Long, Short> cells) {
        this.cells = new ConcurrentHashMap<>(cells);
    }

    private MazeSavedData(Map<Long, Short> cells, int bossCount) {
        this(cells);
        this.bossCount = bossCount;
    }

    public static MazeSavedData get(ServerLevel anyLevel) {
        MinecraftServer server = anyLevel.getServer();
        ServerLevel outer = server.getLevel(OuterLands.DIMENSION);
        ServerLevel target = outer != null ? outer : server.overworld();
        return target.getDataStorage().computeIfAbsent(TYPE);
    }

    public @Nullable MazeCell getCell(int chunkX, int chunkZ) {
        Short packed = cells.get(ChunkPos.pack(chunkX, chunkZ));
        return packed == null ? null : new MazeCell(packed);
    }

    public short getCellRaw(int chunkX, int chunkZ) {
        Short packed = cells.get(ChunkPos.pack(chunkX, chunkZ));
        return packed == null ? 0 : packed;
    }

    public void putCellRaw(int chunkX, int chunkZ, short packed) {
        cells.put(ChunkPos.pack(chunkX, chunkZ), packed);
        setDirty();
    }

    public void removeCell(int chunkX, int chunkZ) {
        cells.remove(ChunkPos.pack(chunkX, chunkZ));
        setDirty();
    }

    public boolean hasCell(int chunkX, int chunkZ) {
        return cells.containsKey(ChunkPos.pack(chunkX, chunkZ));
    }

    public boolean mazesInRange(int chunkX, int chunkZ, int w, int h) {
        for (int x = -w; x <= w; x++) {
            for (int z = -h; z <= h; z++) {
                if (hasCell(chunkX + x, chunkZ + z)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean generateMaze(int chunkX, int chunkZ, int w, int h, long seed) {
        putCellRaw(chunkX, chunkZ, (short) 0);
        putCellRaw(chunkX - w, chunkZ - h, (short) 0);
        putCellRaw(chunkX + w, chunkZ + h, (short) 0);
        putCellRaw(chunkX - w, chunkZ + h, (short) 0);
        putCellRaw(chunkX + w, chunkZ - h, (short) 0);
        MazeLayoutGenerator gen = new MazeLayoutGenerator(w, h, seed++);
        while (!gen.generate()) {
            gen = new MazeLayoutGenerator(w, h, seed++);
        }
        int col = chunkX - (1 + w / 2);
        int row = chunkZ - (1 + h / 2);
        for (int a = 0; a < w; a++) {
            for (int b = 0; b < h; b++) {
                if (gen.grid[b][a] > 0) {
                    putCellRaw(a + col, b + row, (short) gen.grid[b][a]);
                }
            }
        }
        cleanSentinel(chunkX, chunkZ);
        cleanSentinel(chunkX - w, chunkZ - h);
        cleanSentinel(chunkX + w, chunkZ + h);
        cleanSentinel(chunkX - w, chunkZ + h);
        cleanSentinel(chunkX + w, chunkZ - h);
        setDirty();
        return true;
    }

    private void cleanSentinel(int chunkX, int chunkZ) {
        if (getCellRaw(chunkX, chunkZ) == 0) {
            removeCell(chunkX, chunkZ);
        }
    }

    public int nextBossCount(boolean bonus) {
        bossCount++;
        if (bonus) {
            bossCount++;
        }
        setDirty();
        return bossCount;
    }
}
