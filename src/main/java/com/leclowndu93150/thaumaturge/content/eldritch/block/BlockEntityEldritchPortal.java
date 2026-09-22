package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.gen.MazeChunkStamper;
import com.leclowndu93150.thaumaturge.content.eldritch.maze.MazeCell;
import com.leclowndu93150.thaumaturge.content.eldritch.maze.MazeSavedData;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BlockEntityEldritchPortal extends BlockEntity {
    private static final int CHECK_INTERVAL = 5;
    private static final int SOUND_INTERVAL = 250;
    private static final int PORTAL_COOLDOWN = 100;
    private static final int RETURN_SCAN_MIN_Y = 0;

    public int opencount = -1;
    private int count;

    public BlockEntityEldritchPortal(BlockPos pos, BlockState state) {
        super(TCBlockEntities.ELDRITCH_PORTAL.get(), pos, state);
    }

    public void tick(Level level, BlockPos pos) {
        count++;
        if (level.isClientSide()) {
            if (count % SOUND_INTERVAL == 0 || count == 1) {
                level.playLocalSound(
                        pos.getX() + 0.5,
                        pos.getY() + 0.5,
                        pos.getZ() + 0.5,
                        TCSounds.EVILPORTAL.get(),
                        SoundSource.BLOCKS,
                        1.0F,
                        1.0F,
                        false);
            }
            if (opencount < 30) {
                opencount++;
            }
            return;
        }
        if (count % CHECK_INTERVAL != 0 || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<ServerPlayer> players =
                serverLevel.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(0.5, 1.0, 0.5));
        for (ServerPlayer player : players) {
            if (player.isPassenger() || player.isVehicle()) {
                continue;
            }
            if (player.getPortalCooldown() > 0) {
                player.setPortalCooldown(PORTAL_COOLDOWN);
                continue;
            }
            player.setPortalCooldown(PORTAL_COOLDOWN);
            if (serverLevel.dimension() != OuterLands.DIMENSION) {
                teleportToOuterLands(serverLevel, player, pos);
            } else {
                teleportToOverworld(serverLevel, player, pos);
            }
        }
    }

    private static void teleportToOuterLands(ServerLevel from, ServerPlayer player, BlockPos portalPos) {
        ServerLevel outer = from.getServer().getLevel(OuterLands.DIMENSION);
        if (outer == null) {
            return;
        }

        BlockEntityEldritchAltar altar =
                from.getBlockEntity(portalPos.below()) instanceof BlockEntityEldritchAltar found ? found : null;
        MazeTarget maze = linkedMaze(from, portalPos, altar);
        BlockPos targetPos = prepareSafeEntry(outer, maze);
        if (targetPos == null) {
            maze = freshMaze(from, portalPos, altar);
            targetPos = prepareSafeEntry(outer, maze);
        }

        Vec3 target = new Vec3(targetPos.getX() + 0.5, targetPos.getY(), targetPos.getZ() + 0.5);
        player.changeDimension(new DimensionTransition(
                outer, target, Vec3.ZERO, player.getYRot(), player.getXRot(), DimensionTransition.PLAY_PORTAL_SOUND));
        player.setPortalCooldown(PORTAL_COOLDOWN);
    }

    private static MazeTarget linkedMaze(ServerLevel from, BlockPos portalPos, BlockEntityEldritchAltar altar) {
        if (altar != null) {
            ChunkPos linked = altar.findMazeLink(from);
            return linked != null ? new MazeTarget(linked, false) : freshMaze(from, portalPos, altar);
        }

        MazeSavedData maze = MazeSavedData.get(from);
        ChunkPos local = new ChunkPos(portalPos);
        MazeCell cell = maze.getCell(local.x, local.z);
        return cell != null && cell.feature == MazeCell.FEATURE_PORTAL
                ? new MazeTarget(local, false)
                : freshMaze(from, portalPos, null);
    }

    private static MazeTarget freshMaze(ServerLevel from, BlockPos portalPos, BlockEntityEldritchAltar altar) {
        if (altar != null) {
            return new MazeTarget(altar.replaceMazeLink(from), true);
        }

        MazeSavedData maze = MazeSavedData.get(from);
        ChunkPos fresh = BlockEntityEldritchAltar.createFreshMaze(from, maze);
        maze.setReturn(fresh, portalPos.below());
        return new MazeTarget(fresh, true);
    }

    private static BlockPos prepareSafeEntry(ServerLevel outer, MazeTarget maze) {
        ChunkPos anchor = maze.anchor();
        outer.getChunk(anchor.x, anchor.z, ChunkStatus.FULL, true);

        BlockPos destinationPortal =
                new BlockPos(anchor.getMiddleBlockX(), OuterLands.MAZE_Y + 3, anchor.getMiddleBlockZ());
        boolean hasPortal = outer.getBlockState(destinationPortal).is(TCBlocks.ELDRITCH_PORTAL.get());
        BlockPos safe = hasPortal ? findSafeArrival(outer, destinationPortal) : null;
        if (safe != null) {
            return safe;
        }

        if (!maze.freshlyAllocated()) {
            return null;
        }

        MazeCell cell = MazeSavedData.get(outer).getCell(anchor.x, anchor.z);
        if (cell != null && cell.feature == MazeCell.FEATURE_PORTAL) {
            long seed = outer.getSeed() + anchor.x * 341873128712L + anchor.z * 132897987541L;
            MazeChunkStamper.stamp(outer, RandomSource.create(seed), anchor.x, anchor.z, cell);
        }

        if (!outer.getBlockState(destinationPortal).is(TCBlocks.ELDRITCH_PORTAL.get())) {
            outer.setBlock(
                    destinationPortal.below(), TCBlocks.ELDRITCH_CAPSTONE.get().defaultBlockState(), 3);
            outer.setBlock(destinationPortal, TCBlocks.ELDRITCH_PORTAL.get().defaultBlockState(), 3);
        }

        safe = findSafeArrival(outer, destinationPortal);
        return safe != null ? safe : createEmergencyLanding(outer, destinationPortal);
    }

    private static BlockPos createEmergencyLanding(ServerLevel level, BlockPos portal) {
        BlockPos center = new BlockPos(portal.getX() + 3, OuterLands.MAZE_Y + 1, portal.getZ());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos support = center.offset(dx, 0, dz);
                level.setBlock(support, TCBlocks.ELDRITCH_STONE.get().defaultBlockState(), 3);
                level.removeBlock(support.above(), false);
                level.removeBlock(support.above(2), false);
            }
        }
        return center.above();
    }

    private static BlockPos findSafeArrival(ServerLevel level, BlockPos portal) {
        for (int radius = 1; radius <= 4; radius++) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != radius) {
                        continue;
                    }
                    int x = portal.getX() + dx;
                    int z = portal.getZ() + dz;
                    for (int y = portal.getY() + 1; y >= OuterLands.MAZE_Y + 1; y--) {
                        BlockPos feet = new BlockPos(x, y, z);
                        BlockPos support = feet.below();
                        if (!level.getBlockState(support).isSolidRender(level, support)
                                || !level.getBlockState(feet)
                                        .getCollisionShape(level, feet)
                                        .isEmpty()
                                || !level.getBlockState(feet.above())
                                        .getCollisionShape(level, feet.above())
                                        .isEmpty()
                                || !level.getFluidState(feet).isEmpty()
                                || !level.getFluidState(feet.above()).isEmpty()) {
                            continue;
                        }
                        return feet;
                    }
                }
            }
        }
        return null;
    }

    private record MazeTarget(ChunkPos anchor, boolean freshlyAllocated) {}

    private static void teleportToOverworld(ServerLevel from, ServerPlayer player, BlockPos portalPos) {
        ServerLevel overworld = from.getServer().overworld();
        BlockPos altar = MazeSavedData.get(from).getReturn(portalPos.getX() >> 4, portalPos.getZ() >> 4);
        BlockPos found = altar != null ? altar.above() : findPortalColumn(overworld, portalPos);
        Vec3 target;
        if (found != null) {
            target = new Vec3(
                    found.getX() + 0.5 + (overworld.getRandom().nextBoolean() ? 1 : -1),
                    found.getY(),
                    found.getZ() + 0.5 + (overworld.getRandom().nextBoolean() ? 1 : -1));
        } else {
            int surface = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING, portalPos.getX(), portalPos.getZ());
            target = new Vec3(portalPos.getX() + 0.5, surface + 1, portalPos.getZ() + 0.5);
        }
        player.changeDimension(new DimensionTransition(
                overworld,
                target,
                Vec3.ZERO,
                player.getYRot(),
                player.getXRot(),
                DimensionTransition.PLAY_PORTAL_SOUND));
        player.setPortalCooldown(PORTAL_COOLDOWN);
    }

    private static BlockPos findPortalColumn(ServerLevel level, BlockPos portalPos) {
        ChunkAccess chunk = level.getChunk(portalPos.getX() >> 4, portalPos.getZ() >> 4, ChunkStatus.FULL, true);
        if (chunk == null) {
            return null;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        int baseX = (portalPos.getX() >> 4) * 16;
        int baseZ = (portalPos.getZ() >> 4) * 16;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = level.getMaxBuildHeight(); y >= RETURN_SCAN_MIN_Y; y--) {
                    cursor.set(baseX + x, y, baseZ + z);
                    if (chunk.getBlockState(cursor).is(TCBlocks.ELDRITCH_PORTAL.get())) {
                        return cursor.immutable();
                    }
                }
            }
        }
        return null;
    }
}
