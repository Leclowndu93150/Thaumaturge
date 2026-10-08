package com.leclowndu93150.thaumaturge.content.eldritch.altar;

import com.leclowndu93150.thaumaturge.config.ThaumaturgeServerConfig;
import com.leclowndu93150.thaumaturge.content.eldritch.site.ObeliskSite;
import com.leclowndu93150.thaumaturge.content.eldritch.site.SiteAwakening;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;

final class AltarSite {
    private static final int AWAKENING_MIN_RADIUS = 3;
    private static final int AWAKENING_MAX_RADIUS = 7;

    private AltarSite() {}

    static Optional<ObeliskSite> definition(ServerLevel level, BlockEntityEldritchAltar altar) {
        return ObeliskSite.lookup(level.registryAccess(), altar.site());
    }

    static void tick(ServerLevel level, BlockEntityEldritchAltar altar) {
        Optional<ObeliskSite> site = definition(level, altar);
        if (site.isEmpty()) {
            return;
        }
        if (!altar.garrison().quelled() && level.getDifficulty() != Difficulty.PEACEFUL) {
            site.get().behavior().tick(new AltarSiteContext(level, altar));
        }
        site.get().awakening().ifPresent(awakening -> awaken(level, altar, awakening));
    }

    private static void awaken(ServerLevel level, BlockEntityEldritchAltar altar, SiteAwakening awakening) {
        int threshold = ThaumaturgeServerConfig.LABYRINTH.awakenEyeThreshold.get();
        if (threshold <= 0
                || altar.getEyes() < threshold
                || level.getGameTime() - altar.awakenedAt() < awakening.interval()) {
            return;
        }
        altar.setAwakenedAt(level.getGameTime());
        long alive = SiteSpawns.members(level, altar.getBlockPos(), awakening.radius()).stream()
                .filter(mob -> mob.getType() == awakening.entity())
                .count();
        if (alive < awakening.maxAlive()) {
            SiteSpawns.spawn(
                    level,
                    altar.getBlockPos(),
                    awakening.entity(),
                    AWAKENING_MIN_RADIUS,
                    AWAKENING_MAX_RADIUS,
                    level.getRandom());
        }
    }
}
