package com.leclowndu93150.thaumaturge.content.world.mound;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockLoot extends Block {
    public enum LootType {
        COMMON,
        UNCOMMON,
        RARE
    }

    private static final VoxelShape URN_SHAPE = Shapes.or(
            Block.box(1.0, 7.0, 7.0, 15.0, 11.0, 9.0),
            Block.box(2.0, 3.0, 2.0, 14.0, 7.0, 14.0),
            Block.box(2.0, 7.0, 2.0, 14.0, 12.0, 7.0),
            Block.box(2.0, 7.0, 9.0, 14.0, 12.0, 14.0),
            Block.box(2.0, 11.0, 7.0, 14.0, 12.0, 9.0),
            Block.box(3.0, 12.0, 3.0, 13.0, 13.0, 13.0),
            Block.box(4.0, 1.0, 4.0, 12.0, 3.0, 12.0),
            Block.box(4.0, 15.0, 4.0, 12.0, 16.0, 12.0),
            Block.box(5.0, 0.0, 5.0, 11.0, 1.0, 11.0),
            Block.box(5.0, 13.0, 5.0, 11.0, 15.0, 11.0));
    private static final VoxelShape CRATE_SHAPE = Shapes.or(
            Block.box(1.0, 11.0, 1.0, 15.0, 14.0, 15.0),
            Block.box(2.0, 0.0, 2.0, 14.0, 11.0, 14.0),
            Block.box(7.0, 8.0, 1.0, 9.0, 11.0, 2.0));

    private final LootType type;
    private final boolean crate;

    public BlockLoot(LootType type, boolean crate, Properties properties) {
        super(properties);
        this.type = type;
        this.crate = crate;
    }

    public LootType lootType() {
        return type;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return crate ? CRATE_SHAPE : URN_SHAPE;
    }
}
