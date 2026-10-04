package com.leclowndu93150.thaumaturge.api.items;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * When the recharge HUD shows a rechargeable stack's charge meter.
 *
 * @since 1.0.0
 */
public enum ChargeDisplay implements StringRepresentable {
    /** Never. */
    NEVER("never"),
    /** Whenever the stack is held or worn. */
    ALWAYS("always"),
    /** While held, and for a short while after the charge of a worn stack changes. */
    ON_CHANGE("on_change");

    /** The codec, by lowercase name. */
    public static final Codec<ChargeDisplay> CODEC = StringRepresentable.fromEnum(ChargeDisplay::values);

    private final String name;

    ChargeDisplay(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
