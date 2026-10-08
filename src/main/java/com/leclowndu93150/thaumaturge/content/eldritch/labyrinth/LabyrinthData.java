package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public final class LabyrinthData extends SavedData {
    public static final Codec<LabyrinthData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    MazeRecord.CODEC.listOf().fieldOf("mazes").forGetter(data -> List.copyOf(data.mazes.values())),
                    Codec.INT.fieldOf("next_id").forGetter(data -> data.nextId),
                    Codec.INT.fieldOf("next_region").forGetter(data -> data.nextRegion))
            .apply(instance, LabyrinthData::new));
    public static final SavedData.Factory<LabyrinthData> TYPE =
            new SavedData.Factory<>(LabyrinthData::new, LabyrinthData::load, DataFixTypes.LEVEL);

    private final Int2ObjectMap<MazeRecord> mazes = new Int2ObjectLinkedOpenHashMap<>();
    private int nextId;
    private int nextRegion;

    private LabyrinthData() {}

    private LabyrinthData(List<MazeRecord> records, int nextId, int nextRegion) {
        for (MazeRecord record : records) {
            mazes.put(record.plan().id().value(), record);
        }
        this.nextId = nextId;
        this.nextRegion = nextRegion;
    }

    public static LabyrinthData get(ServerLevel outer) {
        return outer.getDataStorage().computeIfAbsent(TYPE, "thaumaturge_labyrinths");
    }

    public Optional<MazeRecord> get(MazeId id) {
        return Optional.ofNullable(mazes.get(id.value()));
    }

    public Collection<MazeRecord> records() {
        return mazes.values();
    }

    public int size() {
        return mazes.size();
    }

    public MazeId nextId() {
        return new MazeId(nextId);
    }

    public int nextRegion() {
        return nextRegion;
    }

    public void commit(MazeRecord record) {
        mazes.put(record.plan().id().value(), record);
        nextId = Math.max(nextId, record.plan().id().value() + 1);
        nextRegion++;
        setDirty();
    }

    public boolean remove(MazeId id) {
        boolean removed = mazes.remove(id.value()) != null;
        if (removed) {
            setDirty();
        }
        return removed;
    }

    private static LabyrinthData load(CompoundTag tag, HolderLookup.Provider registries) {
        return TTNbt.read(tag, "labyrinth", CODEC, registries).orElseGet(LabyrinthData::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        TTNbt.store(tag, "labyrinth", CODEC, registries, this);
        return tag;
    }
}
