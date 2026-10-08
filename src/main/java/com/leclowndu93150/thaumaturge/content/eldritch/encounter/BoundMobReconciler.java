package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class BoundMobReconciler {
    private BoundMobReconciler() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        Entity entity = event.getEntity();
        Optional<LabyrinthBinding> binding = LabyrinthBinding.on(entity);
        if (binding.isEmpty()) {
            return;
        }
        Optional<MazeRecord> record =
                LabyrinthService.byId(level.getServer(), binding.get().mazeId());
        if (record.isEmpty() || !tracked(record.get(), entity.getUUID())) {
            event.setCanceled(true);
        }
    }

    private static boolean tracked(MazeRecord record, UUID uuid) {
        return record.state().encounter().binds(uuid) || record.state().posts().isGuard(uuid);
    }
}
