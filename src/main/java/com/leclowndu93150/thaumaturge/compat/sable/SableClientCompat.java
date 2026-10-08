package com.leclowndu93150.thaumaturge.compat.sable;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ryanhcode.sable.companion.ClientSubLevelAccess;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.jspecify.annotations.Nullable;

public final class SableClientCompat {
    private SableClientCompat() {}

    public static Vec3 renderPosition(@Nullable Level level, Vec3 position) {
        if (level == null || !ModList.get().isLoaded(TTIds.SABLE)) {
            return position;
        }
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, position);
        return subLevel instanceof ClientSubLevelAccess client
                ? client.renderPose().transformPosition(position)
                : position;
    }

    public static void faceCamera(@Nullable Level level, Vec3 position, PoseStack poseStack, Quaternionfc rotation) {
        if (level != null && ModList.get().isLoaded(TTIds.SABLE)) {
            SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, position);
            if (subLevel instanceof ClientSubLevelAccess client) {
                poseStack.mulPose(new Quaternionf(client.renderPose().orientation()).conjugate());
            }
        }
        poseStack.mulPose(new Quaternionf(rotation));
    }
}
