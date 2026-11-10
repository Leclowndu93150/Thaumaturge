package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskBoard;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.network.ClientboundSealPayload;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class SealHandler {
    private static final float DROP_OFFSET = 1.7F;

    private SealHandler() {}

    private static SealWorldIndex index(ServerLevel level) {
        return level.getData(TTAttachments.SEAL_INDEX);
    }

    public static @Nullable SealEntity getSealEntity(Level level, @Nullable SealPos pos) {
        if (pos == null) {
            return null;
        }
        if (level.isClientSide()) {
            return ClientSealHolder.get(pos);
        }
        if (level instanceof ServerLevel serverLevel) {
            return index(serverLevel).seals().get(pos);
        }
        return null;
    }

    public static List<SealEntity> getSealsInRange(ServerLevel level, BlockPos source, int range) {
        List<SealEntity> out = new ArrayList<>();
        for (SealEntity seal : index(level).seals().values()) {
            if (LevelSpaces.distanceSqr(level, seal.pos().pos(), source) <= (double) range * range) {
                out.add(seal);
            }
        }
        return out;
    }

    public static List<SealEntity> getSealsInChunk(LevelChunk chunk) {
        return chunk.getData(TTAttachments.SEALS).seals();
    }

    public static boolean place(ServerLevel level, SealPos pos, ResourceLocation typeId, SealType type, Player player) {
        return !index(level).seals().containsKey(pos)
                && addSealEntity(level, SealEntity.place(pos, typeId, type, player.getUUID()));
    }

    public static boolean addSealEntity(ServerLevel level, SealEntity seal) {
        SealWorldIndex index = index(level);
        if (index.seals().containsKey(seal.pos())) {
            return false;
        }
        index.seals().put(seal.pos(), seal);
        LevelChunk chunk = level.getChunkAt(seal.pos().pos());
        SealsChunkData data = chunk.getData(TTAttachments.SEALS);
        if (!data.seals().contains(seal)) {
            data.seals().add(seal);
        }
        chunk.setUnsaved(true);
        seal.markChanged(level);
        return true;
    }

    public static void removeSealEntity(ServerLevel level, SealPos pos, boolean quiet) {
        SealEntity seal = index(level).seals().remove(pos);
        if (seal == null) {
            return;
        }
        seal.behavior().onRemoved(level, seal);
        if (LoadedChunks.contains(level, pos.pos())) {
            LevelChunk chunk = level.getChunkAt(pos.pos());
            chunk.getData(TTAttachments.SEALS).seals().remove(seal);
            chunk.setUnsaved(true);
        }
        if (!quiet) {
            Vec3 spot = Vec3.atCenterOf(pos.pos()).relative(pos.face(), 1.0 / DROP_OFFSET);
            level.addFreshEntity(new ItemEntity(
                    level, spot.x, spot.y, spot.z, new ItemStack(seal.type().placer())));
        }
        TaskBoard.of(level).endAllFrom(pos);
        PacketDistributor.sendToPlayersTrackingChunk(
                level, new ChunkPos(pos.pos()), ClientboundSealPayload.remove(pos));
    }

    public static void carry(ServerLevel from, ServerLevel to, Map<BlockPos, BlockPos> moves, Rotation rotation) {
        List<SealEntity> moving = index(from).seals().values().stream()
                .filter(seal -> moves.containsKey(seal.pos().pos()))
                .toList();
        for (SealEntity seal : moving) {
            SealPos target = new SealPos(
                    moves.get(seal.pos().pos()), rotation.rotate(seal.pos().face()));
            removeSealEntity(from, seal.pos(), true);
            addSealEntity(to, seal.relocated(target, rotation));
        }
    }

    public static void loadChunkSeals(ServerLevel level, LevelChunk chunk) {
        SealWorldIndex index = index(level);
        for (SealEntity seal : chunk.getData(TTAttachments.SEALS).seals()) {
            index.seals().put(seal.pos(), seal);
        }
    }

    public static void unloadChunkSeals(ServerLevel level, LevelChunk chunk) {
        SealWorldIndex index = index(level);
        for (SealEntity seal : chunk.getData(TTAttachments.SEALS).seals()) {
            index.seals().remove(seal.pos(), seal);
        }
    }

    public static void tickSealEntities(ServerLevel level) {
        boolean validate = level.getGameTime() % 20 == 0;
        for (SealEntity seal : index(level).seals().values()) {
            if (!LoadedChunks.contains(level, seal.pos().pos())) {
                continue;
            }
            try {
                if (validate
                        && !seal.type()
                                .placement()
                                .allows(level, seal.pos().pos(), seal.pos().face())) {
                    removeSealEntity(level, seal.pos(), false);
                    continue;
                }
                seal.tick(level);
            } catch (Exception e) {
                Thaumaturge.LOGGER.error(
                        "Removing seal at {} after tick failure", seal.pos().pos(), e);
                removeSealEntity(level, seal.pos(), false);
            }
        }
    }

    public static void markDirty(ServerLevel level, BlockPos pos) {
        if (LoadedChunks.contains(level, pos)) {
            level.getChunkAt(pos).setUnsaved(true);
        }
    }

    public static boolean isSealOwner(SealEntity seal, UUID player) {
        return player.equals(seal.owner());
    }
}
