package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.world;

import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerStampContext;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthTuning;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeCells;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

record StampContext(
        ServerLevelAccessor level,
        BoundingBox box,
        MazeId maze,
        int transform,
        LabyrinthTuning tuning,
        RandomSource random)
        implements MarkerStampContext {
    @Override
    public Direction orient(Direction local) {
        return MazeCells.orient(transform, local);
    }

    @Override
    public float lootScale() {
        return tuning.lootScale();
    }

    @Override
    public float decorationScale() {
        return tuning.decorationScale();
    }

    @Override
    public boolean place(BlockPos pos, BlockState state) {
        return box.isInside(pos) && level.setBlock(pos, state, Block.UPDATE_CLIENTS);
    }
}
