package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISeal;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealPos;
import com.leclowndu93150.thaumaturge.api.golems.tasks.Task;
import com.leclowndu93150.thaumaturge.content.golem.tasks.TaskHandler;
import com.leclowndu93150.thaumaturge.network.ClientboundSealPayload;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCSeals;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class SealHandler {
    private static final float DROP_OFFSET = 1.7F;

    private SealHandler() {}

    private static SealWorldIndex index(ServerLevel level) {
        return level.getData(TCAttachments.SEAL_INDEX);
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
            if (seal.getSealPos().pos().distSqr(source) <= (double) range * range) {
                out.add(seal);
            }
        }
        return out;
    }

    public static List<SealEntity> getSealsInChunk(ServerLevel level, ChunkPos chunk) {
        List<SealEntity> out = new ArrayList<>();
        for (SealEntity seal : index(level).seals().values()) {
            if (new ChunkPos(seal.getSealPos().pos().getX() >> 4, seal.getSealPos().pos().getZ() >> 4).equals(chunk)) {
                out.add(seal);
            }
        }
        return out;
    }

    public static boolean addSealEntity(ServerLevel level, BlockPos pos, Direction face, ISeal seal, Player player) {
        SealPos sealPos = new SealPos(pos, face);
        if (index(level).seals().containsKey(sealPos)) {
            return false;
        }
        SealEntity entity = new SealEntity(sealPos, seal.getKey(), seal);
        entity.setOwner(player.getUUID());
        return addSealEntity(level, entity);
    }

    public static boolean addSealEntity(ServerLevel level, SealEntity seal) {
        SealWorldIndex index = index(level);
        if (index.seals().containsKey(seal.getSealPos())) {
            return false;
        }
        index.seals().put(seal.getSealPos(), seal);
        LevelChunk chunk = level.getChunkAt(seal.getSealPos().pos());
        SealsChunkData data = chunk.getData(TCAttachments.SEALS);
        if (!data.seals().contains(seal)) {
            data.seals().add(seal);
        }
        chunk.markUnsaved();
        seal.syncToClient(level);
        return true;
    }

    public static void removeSealEntity(ServerLevel level, SealPos pos, boolean quiet) {
        SealEntity seal = index(level).seals().remove(pos);
        if (seal == null) {
            return;
        }
        seal.getSeal().onRemoval(level, pos.pos(), pos.face());
        if (level.hasChunkAt(pos.pos())) {
            LevelChunk chunk = level.getChunkAt(pos.pos());
            chunk.getData(TCAttachments.SEALS).seals().remove(seal);
            chunk.markUnsaved();
        }
        if (!quiet) {
            ItemStack drop = TCSeals.registry().getOptional(seal.getTypeId()).map(type -> new ItemStack(type.placerItem().get())).orElse(ItemStack.EMPTY);
            if (!drop.isEmpty()) {
                level.addFreshEntity(new ItemEntity(level, pos.pos().getX() + 0.5 + pos.face().getStepX() / DROP_OFFSET, pos.pos().getY() + 0.5 + pos.face().getStepY() / DROP_OFFSET,
                        pos.pos().getZ() + 0.5 + pos.face().getStepZ() / DROP_OFFSET, drop));
            }
        }
        for (Task task : TaskHandler.getTasks(level).values()) {
            if (pos.equals(task.getSealPos())) {
                task.setSuspended(true);
            }
        }
        PacketDistributor.sendToPlayersInDimension(level, ClientboundSealPayload.remove(pos));
    }

    public static void loadChunkSeals(ServerLevel level, LevelChunk chunk) {
        SealWorldIndex index = index(level);
        for (SealEntity seal : chunk.getData(TCAttachments.SEALS).seals()) {
            index.seals().putIfAbsent(seal.getSealPos(), seal);
        }
    }

    public static void unloadChunkSeals(ServerLevel level, LevelChunk chunk) {
        ChunkPos chunkPos = chunk.getPos();
        index(level).seals().values().removeIf(seal -> new ChunkPos(seal.getSealPos().pos().getX() >> 4, seal.getSealPos().pos().getZ() >> 4).equals(chunkPos));
    }

    public static void tickSealEntities(ServerLevel level) {
        boolean validate = level.getGameTime() % 20 == 0;
        for (SealEntity seal : index(level).seals().values()) {
            if (!level.hasChunkAt(seal.getSealPos().pos())) {
                continue;
            }
            try {
                if (validate && !seal.getSeal().canPlaceAt(level, seal.getSealPos().pos(), seal.getSealPos().face())) {
                    removeSealEntity(level, seal.getSealPos(), false);
                    continue;
                }
                seal.tickSealEntity(level);
            } catch (Exception e) {
                Thaumaturge.LOGGER.error("Removing seal at {} after tick failure", seal.getSealPos().pos(), e);
                removeSealEntity(level, seal.getSealPos(), false);
            }
        }
    }

    public static void markDirty(ServerLevel level, BlockPos pos) {
        if (level.hasChunkAt(pos)) {
            level.getChunkAt(pos).markUnsaved();
        }
    }

    public static boolean isSealOwner(SealEntity seal, UUID player) {
        return seal.getOwner() != null && seal.getOwner().equals(player);
    }
}
