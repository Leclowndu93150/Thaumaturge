package com.leclowndu93150.thaumaturge.content.warding;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.particle.WardFlashParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDestroyBlockEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.ChunkEvent;
import net.neoforged.neoforge.event.level.ChunkWatchEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class WardEvents {
    private WardEvents() {}

    @SubscribeEvent
    public static void onBreakBlock(BreakBlockEvent event) {
        if (WardHandler.isWarded(event.getLevel(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        event.getPosition().ifPresent(pos -> {
            if (WardHandler.isWarded(event.getEntity().level(), pos)) {
                event.setCanceled(true);
            }
        });
    }

    @SubscribeEvent
    public static void onDetonate(ExplosionEvent.Detonate event) {
        event.getAffectedBlocks().removeIf(pos -> WardHandler.isWarded(event.getLevel(), pos));
    }

    @SubscribeEvent
    public static void onEntityDestroyBlock(LivingDestroyBlockEvent event) {
        if (WardHandler.isWarded(event.getEntity().level(), event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof BlockHitResult blockHit)) {
            return;
        }
        if (!(event.getProjectile().level() instanceof ServerLevel level)) {
            return;
        }
        BlockPos pos = blockHit.getBlockPos();
        if (!WardHandler.isWarded(level, pos)) {
            return;
        }
        Vec3 centre = Vec3.atCenterOf(pos);
        level.sendParticles(WardFlashParticleOptions.at(pos, blockHit.getDirection(), blockHit.getLocation()), centre.x, centre.y, centre.z, 1, 0.0, 0.0, 0.0, 0.0);
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel && event.getChunk() instanceof LevelChunk chunk) {
            WardHandler.prune(chunk);
        }
    }

    @SubscribeEvent
    public static void onChunkWatch(ChunkWatchEvent.Sent event) {
        BlockPos origin = event.getPos().getWorldPosition();
        if (event.getLevel().hasChunkAt(origin)) {
            WardHandler.syncChunk(event.getPlayer(), event.getLevel().getChunkAt(origin));
        }
    }
}
