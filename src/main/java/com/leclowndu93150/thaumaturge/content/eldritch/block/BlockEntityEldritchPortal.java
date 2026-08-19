package com.leclowndu93150.thaumaturge.content.eldritch.block;

import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.portal.TeleportTransition;
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
                level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, TCSounds.EVILPORTAL.get(), SoundSource.BLOCKS, 1.0F, 1.0F, false);
            }
            if (opencount < 30) {
                opencount++;
            }
            return;
        }
        if (count % CHECK_INTERVAL != 0 || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        List<ServerPlayer> players = serverLevel.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(0.5, 1.0, 0.5));
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
        int chunkX = portalPos.getX() >> 4;
        int chunkZ = portalPos.getZ() >> 4;
        Vec3 target = new Vec3(chunkX * 16 + 8.5, OuterLands.MAZE_Y + 4, chunkZ * 16 + 8.5);
        player.teleport(new TeleportTransition(outer, target, Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND));
        player.setPortalCooldown(PORTAL_COOLDOWN);
    }

    private static void teleportToOverworld(ServerLevel from, ServerPlayer player, BlockPos portalPos) {
        ServerLevel overworld = from.getServer().overworld();
        BlockPos found = findPortalColumn(overworld, portalPos);
        Vec3 target;
        if (found != null) {
            target = new Vec3(found.getX() + 0.5 + (overworld.getRandom().nextBoolean() ? 1 : -1), found.getY(), found.getZ() + 0.5 + (overworld.getRandom().nextBoolean() ? 1 : -1));
        } else {
            int surface = overworld.getHeight(Heightmap.Types.MOTION_BLOCKING, portalPos.getX(), portalPos.getZ());
            target = new Vec3(portalPos.getX() + 0.5, surface + 1, portalPos.getZ() + 0.5);
        }
        player.teleport(new TeleportTransition(overworld, target, Vec3.ZERO, player.getYRot(), player.getXRot(), TeleportTransition.PLAY_PORTAL_SOUND));
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
                for (int y = level.getMaxY(); y >= RETURN_SCAN_MIN_Y; y--) {
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
