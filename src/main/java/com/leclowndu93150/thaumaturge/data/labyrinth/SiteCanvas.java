package com.leclowndu93150.thaumaturge.data.labyrinth;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

final class SiteCanvas extends AbstractTemplateCanvas {
    private final Long2ObjectMap<String> metadata = new Long2ObjectOpenHashMap<>();

    SiteCanvas(int sizeX, int sizeY, int sizeZ) {
        super(sizeX, sizeY, sizeZ);
    }

    void set(int x, int y, int z, BlockState state) {
        put(x, y, z, state);
    }

    void marker(int x, int y, int z, String id) {
        put(x, y, z, MARKER);
        metadata.put(BlockPos.asLong(x, y, z), id);
    }

    @Override
    public Optional<String> metadata(int x, int y, int z) {
        return Optional.ofNullable(metadata.get(BlockPos.asLong(x, y, z)));
    }
}
