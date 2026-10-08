package com.leclowndu93150.thaumaturge.content.eldritch.altar;

import com.leclowndu93150.thaumaturge.content.eldritch.block.BlockEldritchStructure;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

final class SiteSealing {
    private static final int RADIUS = 4;
    private static final int BELOW = 1;
    private static final int ABOVE = 8;

    private SiteSealing() {}

    static void apply(ServerLevel level, BlockPos altar, boolean sealed) {
        for (BlockPos pos :
                BlockPos.betweenClosed(altar.offset(-RADIUS, -BELOW, -RADIUS), altar.offset(RADIUS, ABOVE, RADIUS))) {
            BlockState state = level.getBlockState(pos);
            if (state.is(TTBlockTags.ELDRITCH_OBELISK_PARTS)
                    && state.hasProperty(BlockEldritchStructure.SEALED)
                    && state.getValue(BlockEldritchStructure.SEALED) != sealed) {
                level.setBlock(pos, state.setValue(BlockEldritchStructure.SEALED, sealed), Block.UPDATE_CLIENTS);
            }
        }
    }
}
