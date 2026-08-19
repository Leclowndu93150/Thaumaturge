package com.leclowndu93150.thaumaturge.content.taint.spread;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

public final class TaintSeedRegistry extends SavedData {
    public static final Codec<TaintSeedRegistry> CODEC = RecordCodecBuilder.create(builder -> builder
            .group(BlockPos.CODEC.listOf().fieldOf("seeds").forGetter(reg -> Collections.unmodifiableList(reg.seeds))).apply(builder, list -> new TaintSeedRegistry(new ArrayList<>(list))));

    public static final SavedDataType<TaintSeedRegistry> TYPE = new SavedDataType<>(Identifier.fromNamespaceAndPath(TCIds.MODID, "taint_seeds"), TaintSeedRegistry::new, CODEC, DataFixTypes.LEVEL);

    private final List<BlockPos> seeds;

    public TaintSeedRegistry() {
        this(new ArrayList<>());
    }

    private TaintSeedRegistry(List<BlockPos> seeds) {
        this.seeds = seeds;
    }

    public static TaintSeedRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public void addSeed(BlockPos pos) {
        if (!seeds.contains(pos)) {
            seeds.add(pos.immutable());
            setDirty();
        }
    }

    public void removeSeed(BlockPos pos) {
        if (seeds.remove(pos)) {
            setDirty();
        }
    }

    public boolean isNear(BlockPos pos, double radiusSq) {
        for (BlockPos seed : seeds) {
            if (seed.distSqr(pos) <= radiusSq) {
                return true;
            }
        }
        return false;
    }

    public boolean isAtEdge(BlockPos pos, double fringeSq, double radiusSq) {
        for (BlockPos seed : seeds) {
            double d = seed.distSqr(pos);
            if (d >= fringeSq && d <= radiusSq) {
                return true;
            }
        }
        return false;
    }

    public List<BlockPos> all() {
        return Collections.unmodifiableList(seeds);
    }
}
