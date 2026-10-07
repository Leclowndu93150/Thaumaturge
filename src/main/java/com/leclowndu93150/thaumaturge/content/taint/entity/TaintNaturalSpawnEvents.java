package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TaintNaturalSpawnEvents {
    private TaintNaturalSpawnEvents() {}

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        if (!isNaturalWorldSpawn(event.getSpawnType()) || mob.isSpawnCancelled() || !(mob.level() instanceof ServerLevel level) || !TaintInfection.canInfect(level, mob)
                || !event.getLevel().getBiome(mob.blockPosition()).is(TTBiomeTags.IS_TAINTED)) {
            return;
        }
        TaintedProfile profile = TaintedProfile.of(mob.getType());
        if (profile != null && profile.naturalSpawns()) {
            MobTraits.add(mob, TTMobTraits.TAINTED);
        }
    }

    private static boolean isNaturalWorldSpawn(EntitySpawnReason reason) {
        return reason == EntitySpawnReason.NATURAL || reason == EntitySpawnReason.CHUNK_GENERATION || reason == EntitySpawnReason.STRUCTURE;
    }
}
