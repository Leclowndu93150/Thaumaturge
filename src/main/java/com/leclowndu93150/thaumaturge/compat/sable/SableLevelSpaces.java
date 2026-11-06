package com.leclowndu93150.thaumaturge.compat.sable;

import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaceProvider;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import com.leclowndu93150.thaumaturge.content.world.space.WorldFrame;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.platform.SableEventPlatform;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.jspecify.annotations.Nullable;

public final class SableLevelSpaces implements LevelSpaceProvider {
    private final SablePushes pushes = new SablePushes();

    public SableLevelSpaces() {
        SableEventPlatform.INSTANCE.onPhysicsTick(pushes);
    }

    @Override
    public SpaceFrame frameOf(Level level, Position pos) {
        return frame(SableCompanion.INSTANCE.getContaining(level, pos));
    }

    @Override
    public SpaceFrame frameOf(Level level, Vec3i pos) {
        return frame(SableCompanion.INSTANCE.getContaining(level, pos));
    }

    @Override
    public SpaceFrame trackedFrame(Entity entity) {
        return frame(SableCompanion.INSTANCE.getTrackingSubLevel(entity));
    }

    @Override
    public boolean isPlotChunk(Level level, ChunkPos pos) {
        return SableCompanion.INSTANCE.isInPlotGrid(level, pos);
    }

    @Override
    public List<SpaceFrame> framesTouching(Level level, AABB worldBox) {
        List<SpaceFrame> frames = new ArrayList<>();
        frames.add(WorldFrame.INSTANCE);
        for (SubLevelAccess subLevel : SableCompanion.INSTANCE.getAllIntersecting(level, new BoundingBox3d(worldBox))) {
            frames.add(new SubLevelFrame(subLevel, pushes));
        }
        return frames;
    }

    private SpaceFrame frame(@Nullable SubLevelAccess subLevel) {
        return subLevel == null ? WorldFrame.INSTANCE : new SubLevelFrame(subLevel, pushes);
    }
}
