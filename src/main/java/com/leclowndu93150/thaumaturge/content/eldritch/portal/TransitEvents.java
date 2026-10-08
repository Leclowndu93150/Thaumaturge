package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.OuterLands;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TransitEvents {
    private TransitEvents() {}

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || player.level().dimension() != OuterLands.DIMENSION
                || !ThaumaturgeServerConfig.LABYRINTH.evacuateStrandedOnLogin.get()) {
            return;
        }
        ServerLevel outer = player.serverLevel();
        BlockPos feet = player.blockPosition();
        Optional<MazeRecord> record = LabyrinthService.find(outer, feet);
        if (record.isEmpty() || !record.get().plan().geometry().bounds().isInside(feet)) {
            player.changeDimension(LabyrinthTransit.returnTransition(player, record, DimensionTransition.DO_NOTHING));
        } else if (!SafeSpot.clear(outer, feet) || !SafeSpot.clear(outer, feet.above())) {
            ArrivalPad.Arrival arrival = ArrivalPad.prepare(outer, record.get());
            player.changeDimension(new DimensionTransition(
                    outer, arrival.pos(), Vec3.ZERO, arrival.yaw(), 0.0F, DimensionTransition.DO_NOTHING));
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LabyrinthTransit.abandon(player);
        }
    }
}
