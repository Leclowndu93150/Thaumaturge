package com.leclowndu93150.thaumaturge.content.golem;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.CaveVinesPlantBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class CropUtils {
    private static final int SWEET_BERRY_PICKABLE_AGE = 2;

    private CropUtils() {}

    public static boolean isGrownCrop(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (state.getBlock() instanceof NetherWartBlock) {
            return state.getValue(NetherWartBlock.AGE) >= NetherWartBlock.MAX_AGE;
        }
        if (state.getBlock() instanceof CocoaBlock) {
            return state.getValue(CocoaBlock.AGE) >= CocoaBlock.MAX_AGE;
        }
        return isClickableCrop(state);
    }

    public static boolean isClickableCrop(BlockState state) {
        if (state.getBlock() instanceof SweetBerryBushBlock) {
            return state.getValue(SweetBerryBushBlock.AGE) >= SWEET_BERRY_PICKABLE_AGE;
        }
        if (state.getBlock() instanceof CaveVinesBlock || state.getBlock() instanceof CaveVinesPlantBlock) {
            return state.getValue(BlockStateProperties.BERRIES);
        }
        return false;
    }

    public static Optional<Direction> plantingSupport(BlockState state) {
        if (state.getBlock() instanceof CocoaBlock) {
            return Optional.of(state.getValue(CocoaBlock.FACING));
        }
        if (state.getBlock() instanceof CropBlock || state.getBlock() instanceof NetherWartBlock) {
            return Optional.of(Direction.DOWN);
        }
        return Optional.empty();
    }

    public static ItemStack getSeed(Level level, BlockPos pos, BlockState state) {
        return state.getCloneItemStack(level, pos, false);
    }
}
