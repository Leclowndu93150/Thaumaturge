package com.leclowndu93150.thaumaturge.compat.sable;

import com.leclowndu93150.thaumaturge.TTIds;
import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

public final class SableCompat {
    private SableCompat() {}

    public static Vec3 worldPosition(Level level, Vec3 position) {
        if (!ModList.get().isLoaded(TTIds.SABLE)) {
            return position;
        }
        return SableCompanion.INSTANCE.projectOutOfSubLevel(level, position);
    }
}
