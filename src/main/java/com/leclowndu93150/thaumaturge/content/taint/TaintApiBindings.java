package com.leclowndu93150.thaumaturge.content.taint;

import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.api.taint.TaintBiomeEvent;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBlooms;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

public final class TaintApiBindings implements TaintApi.Bindings {

    @Override
    public TaintApi.BiomeChange tryTaintBiome(ServerLevel level, BlockPos pos, @Nullable ServerPlayer player) {
        if (!level.getServer().isSameThread()) {
            return TaintApi.BiomeChange.NOT_SERVER_THREAD;
        }
        if (!level.isInWorldBounds(pos) || !level.getWorldBorder().isWithinBounds(pos)) {
            return TaintApi.BiomeChange.DENIED;
        }
        var chunk = level.getChunkSource().getChunkNow(pos.getX() >> 4, pos.getZ() >> 4);
        if (chunk == null) {
            return TaintApi.BiomeChange.UNLOADED;
        }
        if (ThaumaturgeCommonConfig.WUSS_MODE.get() || ThaumaturgeCommonConfig.TAINT_SPREAD_RATE.get() <= 0 || ThaumaturgeCommonConfig.TAINT_FRONTIER_RATE.get() <= 0) {
            return TaintApi.BiomeChange.DISABLED;
        }
        if (player != null && (player.level() != level || !player.isAlive() || player.isSpectator() || !player.mayInteract(level, pos))) {
            return TaintApi.BiomeChange.DENIED;
        }
        if (level.registryAccess().lookupOrThrow(Registries.BIOME).get(TTBiomes.TAINTED_LANDS).isEmpty()) {
            return TaintApi.BiomeChange.UNSUPPORTED;
        }
        int startX = QuartPos.toBlock(QuartPos.fromBlock(pos.getX()));
        int startZ = QuartPos.toBlock(QuartPos.fromBlock(pos.getZ()));
        for (int blockX = startX; blockX < startX + 4; blockX++) {
            for (int blockZ = startZ; blockZ < startZ + 4; blockZ++) {
                for (int blockY = level.getMinY(); blockY <= level.getMaxY(); blockY++) {
                    BlockPos affected = new BlockPos(blockX, blockY, blockZ);
                    if (!level.getWorldBorder().isWithinBounds(affected) || TaintBlooms.isProtected(level, affected) || player != null && !player.mayInteract(level, affected)) {
                        return TaintApi.BiomeChange.DENIED;
                    }
                }
            }
        }
        if (chunk.getNoiseBiome(QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getY()), QuartPos.fromBlock(pos.getZ())).is(BiomeTags.IS_RIVER)) {
            return TaintApi.BiomeChange.DENIED;
        }
        if (TaintBiomeManager.isTainted(level, pos)) {
            return TaintApi.BiomeChange.UNCHANGED;
        }
        if (NeoForge.EVENT_BUS.post(new TaintBiomeEvent(level, pos, player)).isCanceled()) {
            return TaintApi.BiomeChange.DENIED;
        }
        return TaintBiomeManager.replaceColumn(level, pos, TTBiomes.TAINTED_LANDS) ? TaintApi.BiomeChange.CHANGED : TaintApi.BiomeChange.UNCHANGED;
    }

    @Override
    public void addTaintSeed(ServerLevel level, BlockPos pos) {
        TaintHelper.registerSeedAnchor(level, pos);
    }

    @Override
    public void removeTaintSeed(ServerLevel level, BlockPos pos) {
        TaintHelper.unregisterSeedAnchor(level, pos);
    }

    @Override
    public boolean isNearTaintSeed(Level level, BlockPos pos) {
        return TaintHelper.isWithinSeedInfluence(level, pos);
    }

    @Override
    public boolean isAtTaintSeedEdge(Level level, BlockPos pos) {
        return TaintHelper.isOnSeedFringe(level, pos);
    }

    @Override
    public void spreadFibres(ServerLevel level, BlockPos pos, boolean force) {
        TaintHelper.attemptFibreGrowth(level, pos, force);
    }

    @Override
    public float getEcologicalPressure(Level level, BlockPos pos) {
        return level instanceof ServerLevel server ? TaintEcology.getSaturation(server, pos) : 0.0F;
    }

    @Override
    public boolean isTainted(Level level, BlockPos pos) {
        return level instanceof ServerLevel server && TaintEcology.isTainted(server, pos);
    }

    @Override
    public boolean hasActiveSource(Level level, BlockPos pos) {
        return TaintHelper.isWithinSeedInfluence(level, pos);
    }

    @Override
    public void addEcologicalPressure(ServerLevel level, BlockPos pos, float amount) {
        TaintEcology.addPressure(level, pos, amount);
    }

    @Override
    public void cleanEcologicalPressure(ServerLevel level, BlockPos pos, float amount) {
        TaintEcology.clean(level, pos, amount);
    }
}
