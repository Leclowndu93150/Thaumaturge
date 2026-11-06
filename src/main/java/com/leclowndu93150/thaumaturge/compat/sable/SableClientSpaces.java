package com.leclowndu93150.thaumaturge.compat.sable;

import com.leclowndu93150.thaumaturge.client.space.ClientSpaceProvider;
import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public final class SableClientSpaces implements ClientSpaceProvider {
    @Override
    public Vec3 renderPosition(Level level, Vec3 position) {
        return SableCompanion.INSTANCE.getContaining(level, position) instanceof ClientSubLevelAccess client
                ? client.renderPose().transformPosition(position)
                : position;
    }

    @Override
    public Quaternionfc renderOrientation(Level level, Vec3 position) {
        return SableCompanion.INSTANCE.getContaining(level, position) instanceof ClientSubLevelAccess client
                ? new Quaternionf(client.renderPose().orientation())
                : ClientSpaces.IDENTITY;
    }
}
