package com.leclowndu93150.thaumaturge.content.taint;

import com.leclowndu93150.thaumaturge.api.taint.TaintApi;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

public final class TaintApiBindings implements TaintApi.Bindings {

    @Override
    public void addTaintSeed(ServerLevel level, BlockPos pos) {
        TaintHelper.addTaintSeed(level, pos);
    }

    @Override
    public void removeTaintSeed(ServerLevel level, BlockPos pos) {
        TaintHelper.removeTaintSeed(level, pos);
    }

    @Override
    public boolean isNearTaintSeed(Level level, BlockPos pos) {
        return TaintHelper.isNearTaintSeed(level, pos);
    }

    @Override
    public boolean isAtTaintSeedEdge(Level level, BlockPos pos) {
        return TaintHelper.isAtTaintSeedEdge(level, pos);
    }

    @Override
    public void spreadFibres(ServerLevel level, BlockPos pos, boolean force) {
        TaintHelper.spreadFibres(level, pos, force);
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
        return TaintHelper.isNearTaintSeed(level, pos);
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
