package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthPhase;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.LabyrinthService;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jspecify.annotations.Nullable;

@EventBusSubscriber(modid = TTIds.MODID)
public final class EncounterCombatEvents {
    private EncounterCombatEvents() {}

    @SubscribeEvent
    public static void onDamaged(LivingDamageEvent.Post event) {
        LivingEntity entity = event.getEntity();
        Optional<UUID> credited = creditFor(event.getSource().getEntity());
        if (event.getNewDamage() <= 0.0F || credited.isEmpty()) {
            return;
        }
        boundRecord(entity)
                .filter(record -> record.state().phase() == LabyrinthPhase.ACTIVE
                        && record.state().encounter().tracks(entity.getUUID()))
                .ifPresent(record -> {
                    record.state().encounter().participation().addDamage(credited.get(), event.getNewDamage());
                    markDirty(entity);
                });
    }

    private static Optional<UUID> creditFor(@Nullable Entity attacker) {
        if (attacker instanceof Player player) {
            return Optional.of(player.getUUID());
        }
        if (attacker instanceof OwnableEntity ownable && ownable.getOwner() instanceof Player owner) {
            return Optional.of(owner.getUUID());
        }
        return Optional.empty();
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (event.isCanceled()) {
            return;
        }
        boundRecord(entity)
                .filter(record -> record.state().encounter().defeat(entity.getUUID()))
                .ifPresent(record -> markDirty(entity));
    }

    private static Optional<MazeRecord> boundRecord(LivingEntity entity) {
        if (!(entity.level() instanceof ServerLevel level)) {
            return Optional.empty();
        }
        return LabyrinthBinding.on(entity)
                .flatMap(binding -> LabyrinthService.byId(level.getServer(), binding.mazeId()));
    }

    private static void markDirty(LivingEntity entity) {
        if (entity.level() instanceof ServerLevel level) {
            LabyrinthService.outer(level.getServer()).ifPresent(EncounterPhases::dirty);
        }
    }
}
