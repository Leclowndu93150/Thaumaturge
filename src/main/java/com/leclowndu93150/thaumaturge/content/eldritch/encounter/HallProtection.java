package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import com.leclowndu93150.thaumaturge.content.misc.TTActionBar;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.BlockEvent.BreakEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class HallProtection {
    private static final String MESSAGE = "gui.thaumaturge.labyrinth.hall_protected";

    private HallProtection() {}

    @SubscribeEvent
    public static void onBreak(BreakEvent event) {
        Player player = event.getPlayer();
        if (player.isCreative() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (Guard.at(level, event.getPos())
                .map(guard -> guard.protects(event.getPos()))
                .orElse(false)) {
            event.setCanceled(true);
            TTActionBar.sendPurple(player, MESSAGE);
        }
    }

    @SubscribeEvent
    public static void onPlace(BlockEvent.EntityPlaceEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player player && player.isCreative()
                || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (Guard.at(level, event.getPos())
                .map(guard -> guard.protects(event.getPos()))
                .orElse(false)) {
            event.setCanceled(true);
            if (entity instanceof ServerPlayer player) {
                TTActionBar.sendPurple(player, MESSAGE);
                player.containerMenu.sendAllDataToRemote();
            }
        }
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Vec3 center = event.getExplosion().center();
        Guard.at(level, BlockPos.containing(center))
                .ifPresent(guard -> event.getAffectedBlocks().removeIf(guard::protects));
    }

    private record Guard(BoundingBox hall, boolean protectHall, List<Vec3> bosses, double radiusSquared) {
        static Optional<Guard> at(ServerLevel level, BlockPos pos) {
            Optional<MazeRecord> record = LabyrinthService.find(level, pos);
            if (record.isEmpty()) {
                return Optional.empty();
            }
            if (!record.get().state().phase().isContested()) {
                return Optional.empty();
            }
            double radius = ThaumaturgeServerConfig.LABYRINTH.noBuildRadius.get();
            List<Vec3> bosses = new ArrayList<>();
            if (radius > 0.0) {
                for (LivingEntity living :
                        EncounterMonitor.living(level, record.get().state().encounter(), true)) {
                    bosses.add(living.position());
                }
            }
            return Optional.of(new Guard(
                    EncounterGeometry.hall(record.get()),
                    ThaumaturgeServerConfig.LABYRINTH.protectHall.get(),
                    bosses,
                    radius * radius));
        }

        boolean protects(BlockPos pos) {
            if (protectHall && hall.isInside(pos)) {
                return true;
            }
            Vec3 center = Vec3.atCenterOf(pos);
            for (Vec3 boss : bosses) {
                if (boss.distanceToSqr(center) <= radiusSquared) {
                    return true;
                }
            }
            return false;
        }
    }
}
