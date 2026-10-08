package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthView;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

public record MazeView(MazeRecord record) implements LabyrinthView {
    @Override
    public MazeId id() {
        return record.plan().id();
    }

    @Override
    public LabyrinthPhase phase() {
        return record.state().phase();
    }

    @Override
    public GlobalPos origin() {
        return record.plan().origin();
    }

    @Override
    public BoundingBox bounds() {
        return record.plan().geometry().bounds();
    }

    @Override
    public BoundingBox bossHall() {
        return record.plan().landmarks().bossHall();
    }

    @Override
    public Optional<BlockPos> landmark(ResourceLocation id) {
        return record.plan().landmarks().point(id);
    }

    @Override
    public Optional<ResourceKey<LabyrinthEncounter>> encounter() {
        return record.plan().encounter();
    }

    @Override
    public Optional<Direction> directionToward(ResourceLocation landmark, BlockPos from) {
        int target = target(landmark);
        if (target < 0) {
            return Optional.empty();
        }
        MazeGeometry geometry = record.plan().geometry();
        return record.plan().step(geometry.cellOfBlockX(from.getX()), geometry.cellOfBlockZ(from.getZ()), target);
    }

    @Override
    public Optional<Integer> cellsToward(ResourceLocation landmark, BlockPos from) {
        int target = target(landmark);
        if (target < 0) {
            return Optional.empty();
        }
        MazeGeometry geometry = record.plan().geometry();
        return record.plan().distance(geometry.cellOfBlockX(from.getX()), geometry.cellOfBlockZ(from.getZ()), target);
    }

    private static int target(ResourceLocation landmark) {
        return MazeCells.TARGET_LANDMARKS.indexOf(landmark);
    }
}
