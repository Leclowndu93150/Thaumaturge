package com.leclowndu93150.thaumaturge.content.taint;

import com.leclowndu93150.thaumaturge.api.taint.ITaintOrigin;
import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.content.taint.spread.TaintOrigins;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class TaintApiBindings implements TaintApi.Bindings {

    @Override
    public void addTaintSeed(ServerLevel level, BlockPos pos) {
        TaintOrigins.register(level, pos);
    }

    @Override
    public void removeTaintSeed(ServerLevel level, BlockPos pos) {
        TaintOrigins.unregister(level, pos);
    }

    @Override
    public boolean isNearTaintSeed(Level level, BlockPos pos) {
        return getClaimingOrigin(level, pos) != null;
    }

    @Override
    public boolean isAtTaintSeedEdge(Level level, BlockPos pos) {
        return level instanceof ServerLevel server && TaintOrigins.isOnFringe(server, pos);
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
        return getClaimingOrigin(level, pos) != null;
    }

    @Override
    public void addEcologicalPressure(ServerLevel level, BlockPos pos, float amount) {
        TaintEcology.addPressure(level, pos, amount);
    }

    @Override
    public void cleanEcologicalPressure(ServerLevel level, BlockPos pos, float amount) {
        TaintEcology.clean(level, pos, amount);
    }

    @Override
    public @Nullable ITaintOrigin getClaimingOrigin(Level level, BlockPos pos) {
        return level instanceof ServerLevel server ? TaintOrigins.claimingOrigin(server, pos) : null;
    }

    @Override
    public boolean isSustained(Level level, BlockPos pos) {
        return level instanceof ServerLevel server && TaintOrigins.isSustained(server, pos);
    }
}
