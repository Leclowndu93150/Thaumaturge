package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.registry.TCBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TCMobTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TaintNaturalSpawnEvents {
    private TaintNaturalSpawnEvents() {}

    @SubscribeEvent
    public static void onFinalizeSpawn(FinalizeSpawnEvent event) {
        Mob mob = event.getEntity();
        if (!isNaturalWorldSpawn(event.getSpawnType())
                || mob.isSpawnCancelled()
                || !(mob.level() instanceof ServerLevel level)
                || !TaintInfection.canInfect(level, mob)
                || !event.getLevel().getBiome(mob.blockPosition()).is(TCBiomeTags.IS_TAINTED)) {
            return;
        }
        TaintedProfile profile = TaintedProfile.of(mob.getType());
        if (profile != null && profile.naturalSpawns()) {
            MobTraits.add(mob, TCMobTraits.TAINTED);
        }
    }

    private static boolean isNaturalWorldSpawn(MobSpawnType reason) {
        return reason == MobSpawnType.NATURAL
                || reason == MobSpawnType.CHUNK_GENERATION
                || reason == MobSpawnType.STRUCTURE;
    }
}
