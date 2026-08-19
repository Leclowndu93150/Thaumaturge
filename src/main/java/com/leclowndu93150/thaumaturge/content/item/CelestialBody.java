package com.leclowndu93150.thaumaturge.content.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

public enum CelestialBody implements StringRepresentable {
    SUN("sun"), STARS_1("stars_1"), STARS_2("stars_2"), STARS_3("stars_3"), STARS_4("stars_4"), MOON_1("moon_1"), MOON_2("moon_2"), MOON_3("moon_3"), MOON_4("moon_4"), MOON_5("moon_5"), MOON_6(
            "moon_6"), MOON_7("moon_7"), MOON_8("moon_8");

    private static final CelestialBody[] VALUES = values();
    private static final int FIRST_STAR = 1;
    private static final int FIRST_MOON = 5;

    public static final Codec<CelestialBody> CODEC = StringRepresentable.fromEnum(CelestialBody::values);
    public static final StreamCodec<ByteBuf, CelestialBody> STREAM_CODEC = ByteBufCodecs.idMapper(index -> VALUES[index], CelestialBody::ordinal);

    private final String name;

    CelestialBody(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    public static CelestialBody byIndex(int index) {
        return VALUES[index];
    }

    public static CelestialBody star(int quadrant) {
        return VALUES[FIRST_STAR + quadrant];
    }

    public static CelestialBody moon(int phase) {
        return VALUES[FIRST_MOON + phase];
    }
}
