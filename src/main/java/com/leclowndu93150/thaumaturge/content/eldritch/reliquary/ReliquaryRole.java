package com.leclowndu93150.thaumaturge.content.eldritch.reliquary;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum ReliquaryRole implements StringRepresentable {
    KEY_ROOM("key_room"),
    BOSS("boss"),
    CACHE("cache");

    public static final Codec<ReliquaryRole> CODEC = StringRepresentable.fromEnum(ReliquaryRole::values);

    private final String name;

    ReliquaryRole(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
