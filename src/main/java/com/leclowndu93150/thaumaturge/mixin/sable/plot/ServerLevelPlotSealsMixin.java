package com.leclowndu93150.thaumaturge.mixin.sable.plot;

import com.leclowndu93150.thaumaturge.content.golem.seals.SealEntity;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHandler;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealsChunkData;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.mojang.serialization.Codec;
import dev.ryanhcode.sable.sublevel.plot.PlotChunkHolder;
import dev.ryanhcode.sable.sublevel.plot.ServerLevelPlot;
import java.util.List;
import java.util.Optional;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerLevelPlot.class)
public abstract class ServerLevelPlotSealsMixin {
    @Unique
    private static final String SEAL_CHUNKS_KEY = "thaumaturge_seal_chunks";

    @Unique
    private static final String CHUNK_POS_KEY = "pos";

    @Unique
    private static final String SEALS_KEY = "seals";

    @Unique
    private static final Codec<List<SealEntity>> SEALS_CODEC = SealEntity.CODEC.listOf();

    @Inject(method = "save", at = @At("RETURN"))
    private void thaumaturge$saveSeals(CallbackInfoReturnable<CompoundTag> cir) {
        ServerLevelPlot plot = (ServerLevelPlot) (Object) this;
        ServerLevel level = plot.getSubLevel().getLevel();
        ListTag savedChunks = new ListTag();
        for (PlotChunkHolder holder : plot.getLoadedChunks()) {
            LevelChunk chunk = holder.getChunk();
            List<SealEntity> seals = SealHandler.getSealsInChunk(chunk);
            if (seals.isEmpty()) {
                continue;
            }
            CompoundTag savedChunk = new CompoundTag();
            savedChunk.putLong(CHUNK_POS_KEY, plot.toLocal(chunk.getPos()).toLong());
            TTNbt.store(savedChunk, SEALS_KEY, SEALS_CODEC, level.registryAccess(), seals);
            if (savedChunk.contains(SEALS_KEY)) {
                savedChunks.add(savedChunk);
            }
        }
        cir.getReturnValue().put(SEAL_CHUNKS_KEY, savedChunks);
    }

    @Inject(method = "load", at = @At("TAIL"))
    private void thaumaturge$loadSeals(CompoundTag tag, CallbackInfo ci) {
        ServerLevelPlot plot = (ServerLevelPlot) (Object) this;
        ServerLevel level = plot.getSubLevel().getLevel();
        for (PlotChunkHolder holder : plot.getLoadedChunks()) {
            LevelChunk chunk = holder.getChunk();
            SealHandler.unloadChunkSeals(level, chunk);
            chunk.getData(TTAttachments.SEALS).seals().clear();
        }

        ListTag savedChunks = tag.getList(SEAL_CHUNKS_KEY, Tag.TAG_COMPOUND);
        for (int i = 0; i < savedChunks.size(); i++) {
            CompoundTag savedChunk = savedChunks.getCompound(i);
            ChunkPos localPos = new ChunkPos(savedChunk.getLong(CHUNK_POS_KEY));
            LevelChunk chunk = plot.getChunk(localPos);
            if (chunk == null) {
                continue;
            }
            Optional<List<SealEntity>> decoded = TTNbt.read(savedChunk, SEALS_KEY, SEALS_CODEC, level.registryAccess());
            if (decoded.isEmpty()) {
                continue;
            }
            SealsChunkData data = chunk.getData(TTAttachments.SEALS);
            data.seals().addAll(decoded.get());
            chunk.setUnsaved(true);
            SealHandler.loadChunkSeals(level, chunk);
        }
    }
}
