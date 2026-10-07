package com.leclowndu93150.thaumaturge.content.infernalfurnace;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintTarget;
import com.leclowndu93150.thaumaturge.content.recipe.dust.BlueprintMatrix;
import com.leclowndu93150.thaumaturge.content.recipe.dust.MultiblockMatcher;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

public final class ItemInfernalFurnace extends BlockItem {
    private static final int RADIUS = 1;

    public ItemInfernalFurnace(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        Blueprint blueprint = lookupBlueprint(level);
        if (blueprint == null) {
            return super.place(context);
        }
        BlockPos core = context.getClickedPos().above();
        BlockPos origin = core.offset(-RADIUS, -RADIUS, -RADIUS);
        for (int a = -RADIUS; a <= RADIUS; a++) {
            for (int b = -RADIUS; b <= RADIUS; b++) {
                for (int c = -RADIUS; c <= RADIUS; c++) {
                    if (!level.getBlockState(core.offset(a, b, c)).canBeReplaced()) {
                        return InteractionResult.FAIL;
                    }
                }
            }
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Direction placementFacing = context.getHorizontalDirection().getOpposite();
        int rotations = MultiblockMatcher.rotationsFor(placementFacing);
        int ys = blueprint.ySize();
        for (int y = 0; y < ys; y++) {
            BlueprintMatrix matrix = new BlueprintMatrix(blueprint, y);
            matrix.rotate90DegRight(rotations);
            for (int x = 0; x < matrix.rows(); x++) {
                for (int z = 0; z < matrix.cols(); z++) {
                    BlueprintPart part = matrix.get(x, z);
                    if (part == null) {
                        continue;
                    }
                    BlockPos cellPos = origin.offset(x, -y + (ys - 1), z);
                    placeTarget(level, cellPos, part.target(), placementFacing);
                }
            }
        }
        level.playSound(null, core, SoundType.STONE.getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        Player player = context.getPlayer();
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static void placeTarget(Level level, BlockPos pos, BlueprintTarget target, Direction placementFacing) {
        if (target instanceof BlueprintTarget.Air) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        if (target instanceof BlueprintTarget.BlockTarget blockTarget) {
            BlockState placed = blockTarget.block().defaultBlockState();
            Direction facing = blockTarget.opposite() ? placementFacing.getOpposite() : placementFacing;
            if (placed.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                placed = placed.setValue(BlockStateProperties.HORIZONTAL_FACING, facing);
            } else if (placed.hasProperty(BlockStateProperties.FACING)) {
                placed = placed.setValue(BlockStateProperties.FACING, facing);
            }
            level.setBlock(pos, placed, Block.UPDATE_ALL);
            return;
        }
        if (target instanceof BlueprintTarget.StateTarget stateTarget) {
            level.setBlock(pos, stateTarget.state(), Block.UPDATE_ALL);
        }
    }

    private static @Nullable Blueprint lookupBlueprint(Level level) {
        Registry<Blueprint> registry =
                level.registryAccess().registry(Blueprint.REGISTRY_KEY).orElse(null);
        if (registry == null) {
            return null;
        }
        return registry.getHolder(ResourceKey.create(Blueprint.REGISTRY_KEY, TTIds.rl("infernal_furnace")))
                .map(Holder::value)
                .orElse(null);
    }
}
