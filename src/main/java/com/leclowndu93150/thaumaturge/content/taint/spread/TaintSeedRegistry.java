package com.leclowndu93150.thaumaturge.content.taint.spread;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public final class TaintSeedRegistry extends SavedData {
    public static final Codec<TaintSeedRegistry> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    BlockPos.CODEC.listOf().fieldOf("seeds").forGetter(reg -> Collections.unmodifiableList(reg.seeds)))
            .apply(builder, list -> new TaintSeedRegistry(new ArrayList<>(list))));

    public static final SavedData.Factory<TaintSeedRegistry> FACTORY =
            new SavedData.Factory<>(TaintSeedRegistry::new, TaintSeedRegistry::load, DataFixTypes.LEVEL);

    private final List<BlockPos> seeds;

    public TaintSeedRegistry() {
        this(new ArrayList<>());
    }

    private TaintSeedRegistry(List<BlockPos> seeds) {
        this.seeds = seeds;
    }

    public static TaintSeedRegistry get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, "thaumaturge_taint_seeds");
    }

    private static TaintSeedRegistry load(CompoundTag tag, HolderLookup.Provider registries) {
        return CODEC.parse(NbtOps.INSTANCE, tag.get("data")).result().orElseGet(TaintSeedRegistry::new);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        DataResult<Tag> encoded = CODEC.encodeStart(NbtOps.INSTANCE, this);
        tag.put("data", encoded.getOrThrow());
        return tag;
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

    public void removeSeeds(Collection<BlockPos> positions) {
        if (!positions.isEmpty() && seeds.removeAll(positions)) {
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
