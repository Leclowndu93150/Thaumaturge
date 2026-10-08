package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.RoomVoxels;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StructureBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.StructureMode;
import org.jspecify.annotations.Nullable;

abstract class AbstractTemplateCanvas implements TemplateSource {
    static final BlockState MARKER =
            Blocks.STRUCTURE_BLOCK.defaultBlockState().setValue(StructureBlock.MODE, StructureMode.DATA);

    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final BlockState[] states;

    AbstractTemplateCanvas(int sizeX, int sizeY, int sizeZ) {
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.states = new BlockState[sizeX * sizeY * sizeZ];
    }

    @Override
    public final int sizeX() {
        return sizeX;
    }

    @Override
    public final int sizeY() {
        return sizeY;
    }

    @Override
    public final int sizeZ() {
        return sizeZ;
    }

    final boolean inside(int x, int y, int z) {
        return x >= 0 && y >= 0 && z >= 0 && x < sizeX && y < sizeY && z < sizeZ;
    }

    @Override
    public final @Nullable BlockState get(int x, int y, int z) {
        return inside(x, y, z) ? states[RoomVoxels.index(x, y, z, sizeY, sizeZ)] : null;
    }

    final void put(int x, int y, int z, BlockState state) {
        states[RoomVoxels.index(x, y, z, sizeY, sizeZ)] = state;
    }
}
