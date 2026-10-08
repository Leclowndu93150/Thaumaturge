package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

public final class CompiledRoom implements RoomVoxels {
    private static final Kind[] KINDS = Kind.values();

    private final ResourceLocation id;
    private final StructureTemplate template;
    private final Vec3i size;
    private final byte[] kinds;
    private final List<Marker> markers;
    private final List<BlockPos> barriers;
    private final Map<String, Boolean> validity = new ConcurrentHashMap<>();

    CompiledRoom(
            ResourceLocation id,
            StructureTemplate template,
            Vec3i size,
            byte[] kinds,
            List<Marker> markers,
            List<BlockPos> barriers) {
        this.id = id;
        this.template = template;
        this.size = size;
        this.kinds = kinds;
        this.markers = markers;
        this.barriers = barriers;
    }

    public ResourceLocation id() {
        return id;
    }

    public StructureTemplate template() {
        return template;
    }

    public Vec3i size() {
        return size;
    }

    public List<Marker> markers() {
        return markers;
    }

    public List<BlockPos> barriers() {
        return barriers;
    }

    public boolean validFor(String roomId, Predicate<CompiledRoom> check) {
        return validity.computeIfAbsent(roomId, ignored -> check.test(this));
    }

    @Override
    public int sizeX() {
        return size.getX();
    }

    @Override
    public int sizeY() {
        return size.getY();
    }

    @Override
    public int sizeZ() {
        return size.getZ();
    }

    @Override
    public Kind kind(int x, int y, int z) {
        if (x < 0 || y < 0 || z < 0 || x >= size.getX() || y >= size.getY() || z >= size.getZ()) {
            return Kind.UNSPECIFIED;
        }
        return KINDS[kinds[index(x, y, z, size)]];
    }

    static int index(int x, int y, int z, Vec3i size) {
        return RoomVoxels.index(x, y, z, size.getY(), size.getZ());
    }

    public record Marker(BlockPos local, LabyrinthMarker marker) {}
}
