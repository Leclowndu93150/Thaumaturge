package com.leclowndu93150.thaumaturge.content.golem.seals.behavior;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

final class ReplantSite {
    static final Codec<ReplantSite> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Codec.LONG.optionalFieldOf("taskloc", 0L).forGetter(site -> site.pos.asLong()),
                    Codec.BYTE.optionalFieldOf("taskface", (byte) 0).forGetter(site -> (byte) site.face.get3DDataValue()), Codec.BOOL.optionalFieldOf("farmland", false).forGetter(site -> site.tilled),
                    ItemStack.OPTIONAL_CODEC.optionalFieldOf("seed", ItemStack.EMPTY).forGetter(site -> site.seed))
            .apply(instance, (pos, face, tilled, seed) -> new ReplantSite(BlockPos.of(pos), Direction.from3DDataValue(face), seed, tilled)));

    private final BlockPos pos;
    private final Direction face;
    private final ItemStack seed;
    private final boolean tilled;
    private int taskId;

    ReplantSite(BlockPos pos, Direction face, ItemStack seed, boolean tilled) {
        this.pos = pos;
        this.face = face;
        this.seed = seed;
        this.tilled = tilled;
    }

    BlockPos pos() {
        return pos;
    }

    Direction face() {
        return face;
    }

    ItemStack seed() {
        return seed;
    }

    boolean tilled() {
        return tilled;
    }

    int taskId() {
        return taskId;
    }

    void assign(int taskId) {
        this.taskId = taskId;
    }
}
