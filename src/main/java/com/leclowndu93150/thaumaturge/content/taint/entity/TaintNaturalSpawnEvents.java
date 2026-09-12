package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TCEntityTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.MobSpawnEvent;

/** Converts legacy passive fauna that naturally originates inside Tainted Lands. */
@EventBusSubscriber(modid = TCIds.MODID)
public final class TaintNaturalSpawnEvents {
    private static final String NATURAL_TAINT_SPAWN = TCIds.MODID + ":natural_taint_spawn";

    private TaintNaturalSpawnEvents() {}

    @SubscribeEvent
    public static void onSpawnPositionCheck(MobSpawnEvent.PositionCheck event) {
        Mob mob = event.getEntity();
        MobSpawnType reason = event.getSpawnType();
        if (!isNaturalWorldSpawn(reason) || !hasLegacyPassiveVariant(mob.getType())) {
            return;
        }

        BlockPos pos = BlockPos.containing(event.getX(), event.getY(), event.getZ());
        if (event.getLevel().getBiome(pos).is(TCBiomeTags.IS_TAINTED)) {
            mob.getPersistentData().putBoolean(NATURAL_TAINT_SPAWN, true);
        }
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof LivingEntity living)) {
            return;
        }
        if (!living.getPersistentData().getBoolean(NATURAL_TAINT_SPAWN)) {
            return;
        }
        living.getPersistentData().remove(NATURAL_TAINT_SPAWN);

        TaintMobConversion.Result result = TaintMobConversion.tryConvert(level, living);
        if (result == TaintMobConversion.Result.REPLACED_WITH_LEGACY_VARIANT) {
            // The replacement was added explicitly by TaintMobConversion. Do not also admit the
            // original vanilla entity whose join event we are currently handling.
            event.setCanceled(true);
        }
    }

    private static boolean isNaturalWorldSpawn(MobSpawnType reason) {
        return reason == MobSpawnType.NATURAL
                || reason == MobSpawnType.CHUNK_GENERATION
                || reason == MobSpawnType.STRUCTURE;
    }

    private static boolean hasLegacyPassiveVariant(EntityType<?> type) {
        var holder = type.builtInRegistryHolder();
        return holder.is(TCEntityTags.TAINT_LEGACY_COW)
                || holder.is(TCEntityTags.TAINT_LEGACY_PIG)
                || holder.is(TCEntityTags.TAINT_LEGACY_CHICKEN)
                || holder.is(TCEntityTags.TAINT_LEGACY_SHEEP)
                || holder.is(TCEntityTags.TAINT_LEGACY_VILLAGER);
    }
}
