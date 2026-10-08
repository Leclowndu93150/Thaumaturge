package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAuraModifier;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ChunkEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class AuraGenHandler {
    private AuraGenHandler() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(event.getChunk() instanceof LevelChunk chunk)) {
            return;
        }
        AuraManager.onChunkLoaded(serverLevel, chunk.getPos());
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        AuraManager.onChunkUnloaded(serverLevel, event.getChunk().getPos());
    }

    static void initializeIfNeeded(ServerLevel level, LevelChunk chunk, AuraData data) {
        if (data.isInitialized()) {
            return;
        }
        int cx = chunk.getPos().x;
        int cz = chunk.getPos().z;
        float life = sampleBiome(level, new BlockPos(cx * 16 + 8, 50, cz * 16 + 8));
        for (int a = 0; a < 4; a++) {
            Direction dir = Direction.from2DDataValue(a);
            life += sampleBiome(
                    level, new BlockPos((cx + dir.getStepX()) * 16 + 8, 50, (cz + dir.getStepZ()) * 16 + 8));
        }
        life /= 5.0F;
        Random rand = new Random(level.getSeed() ^ ChunkPos.asLong(cx, cz));
        float noise = (float) (1.0 + rand.nextGaussian() * 0.1F);
        short base = (short) (life * 500.0F * noise);
        base = (short) Mth.clamp(base, 0, 500);
        data.setBase(base);
        if (data.getVis() == 0.0F && data.getFlux() == 0.0F) {
            data.setVis(base);
        }
        data.setChunkPos(chunk.getPos());
        chunk.setUnsaved(true);
    }

    private static float sampleBiome(ServerLevel level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        BiomeAuraModifier mod = biome.getData(TTDataMaps.BIOME_AURA_MODIFIER);
        return mod != null ? mod.value() : 0.5F;
    }
}
