package com.leclowndu93150.thaumaturge.content.aura;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aura.BiomeAuraModifier;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCDataMaps;
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

@EventBusSubscriber(modid = TCIds.MODID)
public final class AuraGenHandler {
    private AuraGenHandler() {}

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        LevelChunk chunk = event.getChunk();
        ChunkPos pos = chunk.getPos();
        AuraManager.onChunkLoaded(serverLevel, pos);

        AuraData data = chunk.getData(TCAttachments.AURA.get());
        if (data.getBase() != 0) {
            return;
        }
        if (!event.isNewChunk()) {
            return;
        }
        generate(serverLevel, chunk, data);
    }

    @SubscribeEvent
    public static void onChunkUnload(ChunkEvent.Unload event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        AuraManager.onChunkUnloaded(serverLevel, event.getChunk().getPos());
    }

    private static void generate(ServerLevel level, LevelChunk chunk, AuraData data) {
        int cx = chunk.getPos().x();
        int cz = chunk.getPos().z();
        float life = sampleBiome(level, new BlockPos(cx * 16 + 8, 50, cz * 16 + 8));
        for (int a = 0; a < 4; a++) {
            Direction dir = Direction.from2DDataValue(a);
            life += sampleBiome(level, new BlockPos((cx + dir.getStepX()) * 16 + 8, 50, (cz + dir.getStepZ()) * 16 + 8));
        }
        life /= 5.0F;
        Random rand = new Random(level.getSeed() ^ ChunkPos.pack(cx, cz));
        float noise = (float) (1.0 + rand.nextGaussian() * 0.1F);
        short base = (short) (life * 500.0F * noise);
        base = (short) Mth.clamp(base, 0, 500);
        data.setBase(base);
        data.setVis(base);
        data.setFlux(0.0F);
        data.setChunkPos(chunk.getPos());
        chunk.markUnsaved();
    }

    private static float sampleBiome(ServerLevel level, BlockPos pos) {
        Holder<Biome> biome = level.getBiome(pos);
        BiomeAuraModifier mod = biome.getData(TCDataMaps.BIOME_AURA_MODIFIER);
        return mod != null ? mod.value() : 0.5F;
    }
}
