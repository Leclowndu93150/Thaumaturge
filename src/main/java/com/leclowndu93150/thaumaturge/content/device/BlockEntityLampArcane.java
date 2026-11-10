package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class BlockEntityLampArcane extends BlockEntity {
    private static final int PLACE_INTERVAL = 5;
    private static final int OFFSET_SPAN = 16;
    private static final int LIGHT_RADIUS = 15;
    private static final int SURFACE_CEILING = 4;
    private static final int FLOOR_MARGIN = 5;
    private static final int MAX_LIGHT_LEVEL = 11;

    private final BlockPos.MutableBlockPos target = new BlockPos.MutableBlockPos();

    public BlockEntityLampArcane(BlockPos pos, BlockState state) {
        super(TTBlockEntities.LAMP_ARCANE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityLampArcane lamp) {
        boolean lit = !level.hasNeighborSignal(pos);
        boolean wasLit = state.getValue(BlockStateProperties.ENABLED);
        BlockLamp.showLit(level, pos, state, lit);
        if (!lit) {
            if (wasLit) {
                lamp.removeLights();
            }
            return;
        }
        if (level.getGameTime() % PLACE_INTERVAL == 0) {
            lamp.tryPlaceGlimmer(level, pos, level.getRandom());
        }
    }

    private void tryPlaceGlimmer(Level level, BlockPos origin, RandomSource random) {
        target.set(origin.getX() + spread(random), origin.getY() + spread(random), origin.getZ() + spread(random));
        if (!LoadedChunks.contains(level, target)) {
            return;
        }
        adjustHeight(level);
        if (isFreeDarkCell(level) && hasClearLine(level, origin, target)) {
            level.setBlock(target, TTBlocks.EFFECT_GLIMMER.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static int spread(RandomSource random) {
        return random.nextInt(OFFSET_SPAN) - random.nextInt(OFFSET_SPAN);
    }

    private void adjustHeight(Level level) {
        int ceiling = level.getHeight(Heightmap.Types.MOTION_BLOCKING, target.getX(), target.getZ()) + SURFACE_CEILING;
        int floor = level.getMinBuildHeight() + FLOOR_MARGIN;
        target.setY(Math.max(floor, Math.min(ceiling, target.getY())));
    }

    private boolean isFreeDarkCell(Level level) {
        return level.getBlockState(target).isAir() && level.getBrightness(LightLayer.BLOCK, target) < MAX_LIGHT_LEVEL;
    }

    private static boolean hasClearLine(Level level, BlockPos from, BlockPos to) {
        ClipContext context = new ClipContext(
                Vec3.atCenterOf(from),
                Vec3.atCenterOf(to),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                CollisionContext.empty());
        BlockHitResult hit = BlockGetter.traverseBlocks(
                context.getFrom(),
                context.getTo(),
                context,
                (ctx, cursor) -> {
                    if (cursor.equals(from) || cursor.equals(to)) {
                        return null;
                    }
                    BlockState blockState = level.getBlockState(cursor);
                    VoxelShape shape = ctx.getBlockShape(blockState, level, cursor);
                    return shape.clip(ctx.getFrom(), ctx.getTo(), cursor);
                },
                ctx -> null);
        return hit == null;
    }

    public void removeLights() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockPos origin = worldPosition;
        for (int dx = -LIGHT_RADIUS; dx <= LIGHT_RADIUS; dx++) {
            for (int dz = -LIGHT_RADIUS; dz <= LIGHT_RADIUS; dz++) {
                target.set(origin.getX() + dx, origin.getY(), origin.getZ() + dz);
                if (!LoadedChunks.contains(level, target)) {
                    continue;
                }
                for (int dy = -LIGHT_RADIUS; dy <= LIGHT_RADIUS; dy++) {
                    target.setY(origin.getY() + dy);
                    if (level.getBlockState(target).is(TTBlocks.EFFECT_GLIMMER.get())) {
                        level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }
}
