package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.wands.AspectOrbAccess;
import com.leclowndu93150.thaumaturge.api.wands.AspectOrbSnapshot;
import java.util.Optional;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public final class AspectOrbAccessBindings implements AspectOrbAccess.Bindings {
    @Override
    public Optional<AspectOrbSnapshot> inspect(Entity entity, ServerPlayer player) {
        if (!(entity instanceof EntityAspectOrb orb) || !(orb.level() instanceof ServerLevel level) || !level.getServer().isSameThread() || player.level() != level || !orb.isAlive()
                || level.getEntity(orb.getUUID()) != orb || !orb.hasKnownAspect()) {
            return Optional.empty();
        }
        return Optional.of(orb.pickupSnapshot(player));
    }

    @Override
    public int collect(Entity entity, ServerPlayer player) {
        return entity instanceof EntityAspectOrb orb ? orb.collect(player) : 0;
    }
}
