package com.leclowndu93150.thaumaturge.compat.sable;

import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaceProvider;
import com.leclowndu93150.thaumaturge.content.world.space.SpaceFrame;
import com.leclowndu93150.thaumaturge.content.world.space.WorldFrame;
import dev.ryanhcode.sable.api.sublevel.SubLevelContainer;
import dev.ryanhcode.sable.companion.SableCompanion;
import dev.ryanhcode.sable.companion.SubLevelAccess;
import dev.ryanhcode.sable.companion.math.BoundingBox3d;
import dev.ryanhcode.sable.companion.math.JOMLConversion;
import dev.ryanhcode.sable.platform.SableEventPlatform;
import dev.ryanhcode.sable.sublevel.ServerSubLevel;
import dev.ryanhcode.sable.sublevel.tracking_points.SubLevelTrackingPointSavedData;
import dev.ryanhcode.sable.sublevel.tracking_points.TrackingPoint;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.core.Vec3i;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
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

    @Override
    public @Nullable LevelChunk loadedChunk(ServerLevel level, net.minecraft.core.BlockPos pos) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, pos);
        if (subLevel instanceof ServerSubLevel ship) {
            var plot = ship.getPlot();
            return plot.getChunk(plot.toLocal(new ChunkPos(pos)));
        }
        return com.leclowndu93150.thaumaturge.content.world.LoadedChunks.contains(level, pos)
                ? level.getChunkAt(pos)
                : null;
    }

    @Override
    public void rememberBlock(ServerLevel level, UUID id, BlockPos pos) {
        var data = SubLevelTrackingPointSavedData.getOrLoad(level);
        var center = JOMLConversion.toJOML(Vec3.atCenterOf(pos));
        var ship =
                SableCompanion.INSTANCE.getContaining(level, pos) instanceof ServerSubLevel subLevel ? subLevel : null;
        var uuid = ship == null ? null : ship.getUniqueId();
        var previous = data.getTrackingPoint(id);
        if (previous != null && Objects.equals(uuid, previous.subLevelID()) && center.equals(previous.point())) {
            return;
        }
        var pointer = ship == null ? null : ship.getLastSerializationPointer();
        var placeholder = ship != null && pointer == null
                ? ship.logicalPose().transformPosition(JOMLConversion.toJOML(Vec3.atCenterOf(pos)))
                : null;
        data.setTrackingPoint(id, new TrackingPoint(ship != null, uuid, pointer, center, placeholder));
    }

    @Override
    public @Nullable BlockPos rememberedBlockPosition(ServerLevel level, UUID id) {
        var point = SubLevelTrackingPointSavedData.getOrLoad(level).getTrackingPoint(id);
        return point == null ? null : BlockPos.containing(JOMLConversion.toMojang(point.point()));
    }

    @Override
    public @Nullable TrackedBlock trackedBlock(ServerLevel level, UUID id, boolean loadSubLevel) {
        var data = SubLevelTrackingPointSavedData.getOrLoad(level);
        var point = data.getTrackingPoint(id);
        if (point == null) return null;
        if (loadSubLevel && point.subLevelID() != null) {
            var container = SubLevelContainer.getContainer(level);
            if (container.getSubLevel(point.subLevelID()) == null) {
                var holding = container.getHoldingChunkMap();
                var savedShip = holding.getHoldingSubLevel(point.subLevelID());
                if (savedShip != null) {
                    holding.loadHoldingSubLevel(savedShip);
                } else if (point.lastSavedSubLevelPointer() != null) {
                    holding.snatchAndLoad(point.lastSavedSubLevelPointer(), point.subLevelID());
                }
            }
        }
        var resolved = data.take(id, false);
        if (resolved == null || resolved.position() == null) return null;
        BlockPos world = BlockPos.containing(JOMLConversion.toMojang(resolved.position()));
        if (resolved.localAnchor() != null && Objects.equals(resolved.subLevelId(), point.subLevelID())) {
            return new TrackedBlock(BlockPos.containing(JOMLConversion.toMojang(resolved.localAnchor())), world);
        }
        if (point.inSubLevel() && resolved.subLevelId() != null) return null;
        return new TrackedBlock(world, world);
    }

    @Override
    public void forgetBlock(ServerLevel level, UUID id) {
        SubLevelTrackingPointSavedData.getOrLoad(level).removeTrackingPoint(id);
    }

    @Override
    public void sendToTrackingPlayers(ServerLevel level, net.minecraft.core.BlockPos pos, CustomPacketPayload payload) {
        SubLevelAccess subLevel = SableCompanion.INSTANCE.getContaining(level, pos);
        if (subLevel instanceof ServerSubLevel ship) {
            for (UUID id : ship.getTrackingPlayers()) {
                Player player = level.getPlayerByUUID(id);
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundCustomPayloadPacket(payload));
                }
            }
        } else {
            LevelSpaceProvider.super.sendToTrackingPlayers(level, pos, payload);
        }
    }

    private SpaceFrame frame(@Nullable SubLevelAccess subLevel) {
        return subLevel == null ? WorldFrame.INSTANCE : new SubLevelFrame(subLevel, pushes);
    }
}
