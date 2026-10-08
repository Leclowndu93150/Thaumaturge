package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class StateOrientation {
    private StateOrientation() {}

    public static BlockState face(BlockState state, Direction facing) {
        if (state.hasProperty(BlockStateProperties.FACING)) {
            return state.setValue(BlockStateProperties.FACING, facing);
        }
        if (facing.getAxis().isHorizontal() && state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
            return state.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
        }
        return state;
    }

    public static int faceBit(Direction direction) {
        return 1 << direction.get3DDataValue();
    }

    public static BlockState exposeFaces(BlockState state, int exposedMask) {
        BlockState result = state;
        for (Direction direction : Direction.values()) {
            BooleanProperty property = PipeBlock.PROPERTY_BY_DIRECTION.get(direction);
            if (result.hasProperty(property)) {
                result = result.setValue(property, (exposedMask & faceBit(direction)) != 0);
            }
        }
        return result;
    }
}
