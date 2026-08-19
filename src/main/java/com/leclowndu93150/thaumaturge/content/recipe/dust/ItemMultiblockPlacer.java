package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintTarget;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

public abstract class ItemMultiblockPlacer extends BlockItem {
    protected ItemMultiblockPlacer(Block block, Properties properties) {
        super(block, properties);
    }

    protected abstract ResourceKey<Blueprint> blueprint();

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        Blueprint blueprint = lookupBlueprint(level);
        if (blueprint == null) {
            return super.place(context);
        }
        Direction placementFacing = context.getHorizontalDirection().getOpposite();
        List<BlueprintMatrix> layers = rotatedLayers(blueprint, MultiblockMatcher.rotationsFor(placementFacing));
        Vec3i anchor = anchorOffset(layers, blueprint.ySize());
        if (anchor == null) {
            return super.place(context);
        }
        BlockPos origin = context.getClickedPos().offset(-anchor.getX(), 0, -anchor.getZ());
        BlueprintMatrix footprint = layers.get(0);
        for (int x = 0; x < footprint.rows(); x++) {
            for (int y = 0; y < blueprint.ySize(); y++) {
                for (int z = 0; z < footprint.cols(); z++) {
                    if (!level.getBlockState(origin.offset(x, y, z)).canBeReplaced()) {
                        return InteractionResult.FAIL;
                    }
                }
            }
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        int ys = blueprint.ySize();
        for (int y = 0; y < ys; y++) {
            BlueprintMatrix matrix = layers.get(y);
            for (int x = 0; x < matrix.rows(); x++) {
                for (int z = 0; z < matrix.cols(); z++) {
                    BlueprintPart part = matrix.get(x, z);
                    if (part == null) {
                        continue;
                    }
                    BlockPos cellPos = origin.offset(x, -y + (ys - 1), z);
                    placeTarget(level, cellPos, part.target(), placementFacing, context, player);
                }
            }
        }
        BlockPos corePos = origin.offset(anchor.getX(), anchor.getY(), anchor.getZ());
        level.playSound(null, corePos, getBlock().defaultBlockState().getSoundType().getPlaceSound(), SoundSource.BLOCKS, 1.0F, 1.0F);
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.SUCCESS;
    }

    private static List<BlueprintMatrix> rotatedLayers(Blueprint blueprint, int rotations) {
        List<BlueprintMatrix> layers = new ArrayList<>(blueprint.ySize());
        for (int y = 0; y < blueprint.ySize(); y++) {
            BlueprintMatrix matrix = new BlueprintMatrix(blueprint, y);
            matrix.rotate90DegRight(rotations);
            layers.add(matrix);
        }
        return layers;
    }

    private @Nullable Vec3i anchorOffset(List<BlueprintMatrix> layers, int ys) {
        for (int y = 0; y < ys; y++) {
            BlueprintMatrix matrix = layers.get(y);
            for (int x = 0; x < matrix.rows(); x++) {
                for (int z = 0; z < matrix.cols(); z++) {
                    BlueprintPart part = matrix.get(x, z);
                    if (part != null && part.target() instanceof BlueprintTarget.BlockTarget blockTarget && blockTarget.block() == getBlock()) {
                        return new Vec3i(x, -y + (ys - 1), z);
                    }
                }
            }
        }
        return null;
    }

    private static void placeTarget(Level level, BlockPos pos, BlueprintTarget target, Direction placementFacing, BlockPlaceContext context, @Nullable Player player) {
        if (target instanceof BlueprintTarget.Air) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            return;
        }
        if (target instanceof BlueprintTarget.BlockTarget blockTarget) {
            BlockState placed = blockTarget.block().defaultBlockState();
            Direction facing = facingFor(blockTarget, placementFacing, context, player);
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

    private static Direction facingFor(BlueprintTarget.BlockTarget blockTarget, Direction placementFacing, BlockPlaceContext context, @Nullable Player player) {
        if (blockTarget.applyPlayerFacing() && player != null) {
            Direction useFace = context.getClickedFace();
            return useFace.getAxis().isHorizontal() ? useFace : player.getDirection().getOpposite();
        }
        return blockTarget.opposite() ? placementFacing.getOpposite() : placementFacing;
    }

    private @Nullable Blueprint lookupBlueprint(Level level) {
        Registry<Blueprint> registry = level.registryAccess().lookup(Blueprint.REGISTRY_KEY).orElse(null);
        if (registry == null) {
            return null;
        }
        return registry.get(blueprint()).map(Holder::value).orElse(null);
    }
}
