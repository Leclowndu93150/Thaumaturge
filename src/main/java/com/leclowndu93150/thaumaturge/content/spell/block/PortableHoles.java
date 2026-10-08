package com.leclowndu93150.thaumaturge.content.spell.block;

import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class PortableHoles {
    private static final float UNBREAKABLE = -1.0F;

    private PortableHoles() {}

    public static boolean canOpen(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir()
                && !state.canBeReplaced()
                && level.getBlockEntity(pos) == null
                && !state.is(TTBlockTags.PORTABLE_HOLE_BLACKLIST)
                && !state.is(Blocks.BEDROCK)
                && !state.is(TTBlocks.HOLE.get())
                && state.getDestroySpeed(level, pos) != UNBREAKABLE;
    }

    public static boolean open(Level level, BlockPos pos, @Nullable Direction heading, int depth, int ticks) {
        if (level.isClientSide() || !canOpen(level, pos)) {
            return false;
        }
        BlockState replaced = level.getBlockState(pos);
        if (level.setBlock(pos, TTBlocks.HOLE.get().defaultBlockState(), Block.UPDATE_ALL)
                && level.getBlockEntity(pos) instanceof BlockEntityHole hole) {
            hole.configure(replaced, ticks, depth, heading);
            hole.setChanged();
            level.sendBlockUpdated(pos, hole.getBlockState(), hole.getBlockState(), Block.UPDATE_CLIENTS);
        }
        return true;
    }
}
