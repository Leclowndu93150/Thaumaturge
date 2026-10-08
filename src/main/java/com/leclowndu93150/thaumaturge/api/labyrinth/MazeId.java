package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Identifies one labyrinth in the Outer Lands. Ids are assigned in creation order and never reused, so a stale id resolves to nothing rather than to a newer maze.
 *
 * @param value the numeric id
 * @since 1.0.0
 */
public record MazeId(int value) {
    /**
     * Codec that stores the id as a plain integer.
     */
    public static final Codec<MazeId> CODEC = Codec.INT.xmap(MazeId::new, MazeId::value);
    /**
     * Network codec that writes the id as a variable-length integer.
     */
    public static final StreamCodec<ByteBuf, MazeId> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(MazeId::new, MazeId::value);
}
