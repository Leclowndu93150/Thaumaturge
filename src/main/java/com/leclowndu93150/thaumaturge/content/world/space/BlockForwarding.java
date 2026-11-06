package com.leclowndu93150.thaumaturge.content.world.space;

import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;

public final class BlockForwarding extends SavedData {
    private static final String DATA_NAME = "thaumaturge_block_forwarding";
    private static final String FORWARDS_KEY = "forwards";
    private static final Codec<List<Forward>> FORWARDS_CODEC = Forward.CODEC.listOf();
    private static final SavedData.Factory<BlockForwarding> FACTORY =
            new SavedData.Factory<>(BlockForwarding::new, BlockForwarding::load, DataFixTypes.LEVEL);

    private final Map<BlockPos, BlockPos> forwards = new HashMap<>();

    private static BlockForwarding get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static BlockPos follow(ServerLevel level, BlockPos pos) {
        if (LevelSpaces.isWorldOnly()) {
            return pos;
        }
        return get(level).forwards.getOrDefault(pos, pos);
    }

    public static boolean leadsTo(ServerLevel level, GlobalPos link, BlockPos pos) {
        return link.dimension() == level.dimension()
                && follow(level, link.pos()).equals(pos);
    }

    public static void record(ServerLevel level, BlockPos from, BlockPos to) {
        BlockForwarding data = get(level);
        data.forwards.replaceAll((origin, target) -> target.equals(from) ? to : target);
        data.forwards.put(from.immutable(), to.immutable());
        data.forwards.remove(to);
        data.setDirty();
    }

    private static BlockForwarding load(CompoundTag tag, HolderLookup.Provider registries) {
        BlockForwarding data = new BlockForwarding();
        TTNbt.read(tag, FORWARDS_KEY, FORWARDS_CODEC, registries)
                .ifPresent(forwards -> forwards.forEach(forward -> data.forwards.put(forward.from(), forward.to())));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        List<Forward> list = forwards.entrySet().stream()
                .map(entry -> new Forward(entry.getKey(), entry.getValue()))
                .toList();
        TTNbt.store(tag, FORWARDS_KEY, FORWARDS_CODEC, registries, list);
        return tag;
    }

    private record Forward(BlockPos from, BlockPos to) {
        private static final Codec<Forward> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                        BlockPos.CODEC.fieldOf("from").forGetter(Forward::from),
                        BlockPos.CODEC.fieldOf("to").forGetter(Forward::to))
                .apply(instance, Forward::new));
    }
}
