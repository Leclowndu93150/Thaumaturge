package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Direction;
import net.minecraft.util.ExtraCodecs;

public record RoomSocket(int cellX, int cellZ, Direction side) {
    public static final Codec<Direction> HORIZONTAL =
            Direction.CODEC.validate(direction -> direction.getAxis().isHorizontal()
                    ? DataResult.success(direction)
                    : DataResult.error(() -> "Expected a horizontal direction, got " + direction));
    public static final Codec<RoomSocket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("cell_x", 0).forGetter(RoomSocket::cellX),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("cell_z", 0).forGetter(RoomSocket::cellZ),
                    HORIZONTAL.fieldOf("side").forGetter(RoomSocket::side))
            .apply(instance, RoomSocket::new));
}
