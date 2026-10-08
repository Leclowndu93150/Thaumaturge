package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.world.level.block.Rotation;

public record RoomTransforms(List<Rotation> rotations, boolean mirror) {
    public static final RoomTransforms ALL = new RoomTransforms(List.of(Rotation.values()), true);
    public static final Codec<RoomTransforms> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Rotation.CODEC
                            .listOf(1, Rotation.values().length)
                            .optionalFieldOf("rotations", ALL.rotations)
                            .forGetter(RoomTransforms::rotations),
                    Codec.BOOL.optionalFieldOf("mirror", true).forGetter(RoomTransforms::mirror))
            .apply(instance, RoomTransforms::new));
}
