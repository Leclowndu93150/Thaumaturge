package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.core.BlockPos;
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

public final class BlockEntityLampArcane extends BlockEntity {
    private static final int SPAWN_INTERVAL = 5;
    private static final int SPREAD = 16;
    private static final int SURFACE_CLEARANCE = 4;
    private static final int MAX_BLOCK_LIGHT = 11;
    private static final int CLEANUP_RADIUS = 15;

    public BlockEntityLampArcane(BlockPos pos, BlockState state) {
        super(TCBlockEntities.LAMP_ARCANE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityLampArcane lamp) {
        boolean enabled = !level.hasNeighborSignal(pos);
        if (state.getValue(BlockStateProperties.ENABLED) != enabled) {
            level.setBlock(pos, state.setValue(BlockStateProperties.ENABLED, enabled), Block.UPDATE_ALL);
            if (!enabled) {
                lamp.removeLights();
            }
        }
        if (level.getGameTime() % SPAWN_INTERVAL != 0 || !enabled) {
            return;
        }
        int x = level.getRandom().nextInt(SPREAD) - level.getRandom().nextInt(SPREAD);
        int y = level.getRandom().nextInt(SPREAD) - level.getRandom().nextInt(SPREAD);
        int z = level.getRandom().nextInt(SPREAD) - level.getRandom().nextInt(SPREAD);
        BlockPos target = pos.offset(x, y, z);
        BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, target);
        if (target.getY() > surface.getY() + SURFACE_CLEARANCE) {
            target = surface.above(SURFACE_CLEARANCE);
        }
        if (target.getY() < level.getMinBuildHeight() + SURFACE_CLEARANCE + 1) {
            target = new BlockPos(target.getX(), level.getMinBuildHeight() + SURFACE_CLEARANCE + 1, target.getZ());
        }
        if (level.getBlockState(target).isAir()
                && !level.getBlockState(target).is(TCBlocks.EFFECT_GLIMMER.get())
                && level.getBrightness(LightLayer.BLOCK, target) < MAX_BLOCK_LIGHT
                && hasLineOfSight(level, pos, target)) {
            level.setBlock(target, TCBlocks.EFFECT_GLIMMER.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static boolean hasLineOfSight(Level level, BlockPos from, BlockPos to) {
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
                    if (cursor.equals(from)) {
                        return null;
                    }
                    BlockState blockState = level.getBlockState(cursor);
                    return ctx.getBlockShape(blockState, level, cursor).clip(ctx.getFrom(), ctx.getTo(), cursor);
                },
                ctx -> null);
        return hit == null || hit.getBlockPos().equals(to);
    }

    public void removeLights() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        BlockPos pos = getBlockPos();
        for (int x = -CLEANUP_RADIUS; x <= CLEANUP_RADIUS; x++) {
            for (int y = -CLEANUP_RADIUS; y <= CLEANUP_RADIUS; y++) {
                for (int z = -CLEANUP_RADIUS; z <= CLEANUP_RADIUS; z++) {
                    cursor.set(pos.getX() + x, pos.getY() + y, pos.getZ() + z);
                    if (level.getBlockState(cursor).is(TCBlocks.EFFECT_GLIMMER.get())) {
                        level.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                    }
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        removeLights();
        super.setRemoved();
    }
}
