package com.leclowndu93150.thaumaturge.content.eldritch.portal;

import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.util.StringRepresentable;

public record PortalLink(Kind kind, MazeId maze) {
    public static final Codec<PortalLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Kind.CODEC.fieldOf("kind").forGetter(PortalLink::kind),
                    MazeId.CODEC.fieldOf("maze").forGetter(PortalLink::maze))
            .apply(instance, PortalLink::new));

    public static PortalLink intoLabyrinth(MazeId maze) {
        return new PortalLink(Kind.TO_LABYRINTH, maze);
    }

    public static PortalLink toOrigin(MazeId maze) {
        return new PortalLink(Kind.TO_ORIGIN, maze);
    }

    public enum Kind implements StringRepresentable {
        TO_LABYRINTH("to_labyrinth"),
        TO_ORIGIN("to_origin");

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String name;

        Kind(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
