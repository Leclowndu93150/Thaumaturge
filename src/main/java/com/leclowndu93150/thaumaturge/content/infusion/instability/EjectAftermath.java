package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum EjectAftermath implements StringRepresentable {
    NONE("none"),
    FLUX("flux"),
    POLLUTE("pollute"),
    EXPLODE("explode");

    public static final Codec<EjectAftermath> CODEC = StringRepresentable.fromEnum(EjectAftermath::values);

    private final String name;

    EjectAftermath(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
