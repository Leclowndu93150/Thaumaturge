package com.leclowndu93150.thaumaturge.content.eldritch.maze;

import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.saveddata.SavedData;
import org.jspecify.annotations.Nullable;

public final class MazeSavedData extends SavedData {
    private static final Codec<Map<Long, Short>> CELLS_CODEC =
            Codec.unboundedMap(Codec.STRING.xmap(Long::parseLong, String::valueOf), Codec.SHORT);
    private static final Codec<Map<Long, BlockPos>> RETURNS_CODEC =
            Codec.unboundedMap(Codec.STRING.xmap(Long::parseLong, String::valueOf), BlockPos.CODEC);
    private static final int REGION_STRIDE_CHUNKS = 64;
    private static final int MAX_LOCAL_ALLOCATION_ATTEMPTS = 64;

    public static final Codec<MazeSavedData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    CELLS_CODEC.fieldOf("cells").forGetter(data -> data.cells),
                    Codec.INT.fieldOf("boss_count").forGetter(data -> data.bossCount),
                    RETURNS_CODEC.optionalFieldOf("returns", Map.of()).forGetter(data -> data.returns),
                    Codec.INT.optionalFieldOf("alloc_cursor", 0).forGetter(data -> data.allocCursor))
            .apply(builder, MazeSavedData::new));

    public static final SavedData.Factory<MazeSavedData> FACTORY =
            new SavedData.Factory<>(MazeSavedData::new, MazeSavedData::load, DataFixTypes.LEVEL);

    private final Map<Long, Short> cells;
    private final Map<Long, BlockPos> returns;
    private int bossCount;
    private int allocCursor;

    public MazeSavedData() {
        this(new ConcurrentHashMap<>(), 0, Map.of(), 0);
    }

    private MazeSavedData(Map<Long, Short> cells, int bossCount, Map<Long, BlockPos> returns, int allocCursor) {
        this.cells = new ConcurrentHashMap<>(cells);
        this.returns = new ConcurrentHashMap<>(returns);
        this.bossCount = bossCount;
        this.allocCursor = allocCursor;
    }

    public static MazeSavedData get(ServerLevel anyLevel) {
        MinecraftServer server = anyLevel.getServer();
        ServerLevel outer = server.getLevel(OuterLands.DIMENSION);
        ServerLevel target = outer != null ? outer : server.overworld();
        return target.getDataStorage().computeIfAbsent(FACTORY, "thaumaturge_labyrinth");
    }

    private static MazeSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
        return CODEC.parse(NbtOps.INSTANCE, tag.get("data")).result().orElseGet(MazeSavedData::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        DataResult<Tag> encoded = CODEC.encodeStart(NbtOps.INSTANCE, this);
        tag.put("data", encoded.getOrThrow());
        return tag;
    }

    public @Nullable MazeCell getCell(int chunkX, int chunkZ) {
        Short packed = cells.get(ChunkPos.asLong(chunkX, chunkZ));
        return packed == null ? null : new MazeCell(packed);
    }

    public short getCellRaw(int chunkX, int chunkZ) {
        Short packed = cells.get(ChunkPos.asLong(chunkX, chunkZ));
        return packed == null ? 0 : packed;
    }

    public void putCellRaw(int chunkX, int chunkZ, short packed) {
        cells.put(ChunkPos.asLong(chunkX, chunkZ), packed);
        setDirty();
    }

    public void removeCell(int chunkX, int chunkZ) {
        cells.remove(ChunkPos.asLong(chunkX, chunkZ));
        setDirty();
    }

    public boolean hasCell(int chunkX, int chunkZ) {
        return cells.containsKey(ChunkPos.asLong(chunkX, chunkZ));
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

    public ChunkPos allocateRegion(int w, int h) {
        int attempts = 0;
        while (attempts < MAX_LOCAL_ALLOCATION_ATTEMPTS) {
            long index = (long) allocCursor + attempts;
            if (index > Integer.MAX_VALUE) {
                break;
            }
            ChunkPos candidate = spiral((int) index);
            if (!mazesInRange(candidate.x, candidate.z, w, h)) {
                advanceAllocCursor(attempts + 1);
                setDirty();
                return candidate;
            }
            attempts++;
        }

        advanceAllocCursor(attempts);
        ChunkPos fallback = allocatePastOccupiedBounds(w, h);
        setDirty();
        return fallback;
    }

    private ChunkPos allocatePastOccupiedBounds(int w, int h) {
        if (cells.isEmpty()) {
            return new ChunkPos(0, 0);
        }

        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (long packed : cells.keySet()) {
            int x = (int) packed;
            int z = (int) (packed >> 32);
            minX = Math.min(minX, x);
            maxX = Math.max(maxX, x);
            minZ = Math.min(minZ, z);
            maxZ = Math.max(maxZ, z);
        }

        long east = (long) maxX + w + 1L;
        if (east <= Integer.MAX_VALUE) {
            return new ChunkPos((int) east, 0);
        }
        long west = (long) minX - w - 1L;
        if (west >= Integer.MIN_VALUE) {
            return new ChunkPos((int) west, 0);
        }
        long south = (long) maxZ + h + 1L;
        if (south <= Integer.MAX_VALUE) {
            return new ChunkPos(0, (int) south);
        }
        long north = (long) minZ - h - 1L;
        if (north >= Integer.MIN_VALUE) {
            return new ChunkPos(0, (int) north);
        }

        throw new IllegalStateException("Labyrinth allocation exhausted the ChunkPos coordinate range");
    }

    private void advanceAllocCursor(int amount) {
        if (amount <= 0) {
            return;
        }
        long next = (long) allocCursor + amount;
        allocCursor = next > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) next;
    }

    public void setReturn(ChunkPos anchor, BlockPos overworldPos) {
        returns.put(anchor.toLong(), overworldPos);
        setDirty();
    }

    public @Nullable BlockPos getReturn(int chunkX, int chunkZ) {
        return returns.get(ChunkPos.asLong(chunkX, chunkZ));
    }

    private static ChunkPos spiral(int index) {
        int x = 0;
        int z = 0;
        int dx = 0;
        int dz = -1;
        for (int step = 0; step < index; step++) {
            if (x == z || (x < 0 && x == -z) || (x > 0 && x == 1 - z)) {
                int swap = dx;
                dx = -dz;
                dz = swap;
            }
            x += dx;
            z += dz;
        }
        return new ChunkPos(x * REGION_STRIDE_CHUNKS, z * REGION_STRIDE_CHUNKS);
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
