package com.leclowndu93150.thaumaturge.content.decor;

import com.leclowndu93150.thaumaturge.api.items.IScribeTools;
import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import com.leclowndu93150.thaumaturge.content.research.table.BlockEntityResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.BlockResearchTable;
import com.leclowndu93150.thaumaturge.content.research.table.ResearchTablePart;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.mojang.serialization.MapCodec;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.transfer.item.ItemResource;

public final class BlockTable extends Block {
    public static final MapCodec<BlockTable> CODEC = simpleCodec(BlockTable::new);

    private static final VoxelShape SHAPE = Shapes.or(Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0), Block.box(1.0, 0.0, 1.0, 5.0, 12.0, 5.0), Block.box(11.0, 0.0, 1.0, 15.0, 12.0, 5.0),
            Block.box(1.0, 0.0, 11.0, 5.0, 12.0, 15.0), Block.box(11.0, 0.0, 11.0, 15.0, 12.0, 15.0));

    public BlockTable(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<BlockTable> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    private static final List<Direction> CONVERT_SCAN_ORDER = List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (this != TCBlocks.TABLE_WOOD.get() || !(stack.getItem() instanceof IScribeTools)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        for (Direction dir : CONVERT_SCAN_ORDER) {
            BlockPos partnerPos = pos.relative(dir);
            if (!level.getBlockState(partnerPos).is(TCBlocks.TABLE_WOOD.get())) {
                continue;
            }
            level.setBlock(pos, TCBlocks.RESEARCH_TABLE.get().defaultBlockState().setValue(BlockResearchTable.FACING, dir).setValue(BlockResearchTable.PART, ResearchTablePart.MAIN), 3);
            level.setBlock(partnerPos,
                    TCBlocks.RESEARCH_TABLE.get().defaultBlockState().setValue(BlockResearchTable.FACING, dir.getOpposite()).setValue(BlockResearchTable.PART, ResearchTablePart.EXT), 3);
            if (level.getBlockEntity(pos) instanceof BlockEntityResearchTable researchTable) {
                ItemStack tools = stack.copy();
                researchTable.items().set(BlockEntityResearchTable.SLOT_SCRIBE_TOOLS, ItemResource.of(tools), tools.getCount());
                researchTable.setChanged();
            }
            player.setItemInHand(hand, ItemStack.EMPTY);
            if (player instanceof ServerPlayer serverPlayer) {
                ResearchProgressionEvents.recordCrafted(serverPlayer, new ItemStack(TCBlocks.RESEARCH_TABLE.get()));
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }
}
