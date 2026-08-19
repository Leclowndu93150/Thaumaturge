package com.leclowndu93150.thaumaturge.content.aura.relay;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.VisRelayHelper;
import com.leclowndu93150.thaumaturge.api.recipe.IArcaneWorkbench;
import com.leclowndu93150.thaumaturge.api.recipe.IWorkbenchVisSource;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

public final class VisRelayWorkbenchSource implements IWorkbenchVisSource {
    @Override
    public int supply(Player player, IArcaneWorkbench workbench, Holder<IAspect> aspect, int need, boolean simulate) {
        if (!(player.level() instanceof ServerLevel level)) {
            return 0;
        }
        return VisRelayHelper.drainCentivis(level, player.blockPosition(), aspect.unwrapKey().orElseThrow(), need, simulate);
    }
}
