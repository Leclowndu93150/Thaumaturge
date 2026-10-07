package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.List;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

public final class FocusPackages {
    private static final String PACK_KEY = "pack";

    private FocusPackages() {}

    public static void write(RegistryFriendlyByteBuf buffer, @Nullable FocusPackage focusPackage) {
        buffer.writeBoolean(focusPackage != null);
        if (focusPackage != null) {
            FocusPackage.STREAM_CODEC.encode(buffer, focusPackage);
        }
    }

    public static @Nullable FocusPackage read(RegistryFriendlyByteBuf buffer) {
        if (!buffer.readBoolean()) {
            return null;
        }
        try {
            return FocusPackage.STREAM_CODEC.decode(buffer);
        } catch (Exception e) {
            Thaumaturge.LOGGER.error("Failed to decode focus package from spawn data", e);
            return null;
        }
    }

    public static void save(CompoundTag output, HolderLookup.Provider registries, @Nullable FocusPackage focusPackage) {
        TTNbt.storeNullable(output, PACK_KEY, FocusPackage.CODEC, registries, focusPackage);
    }

    public static @Nullable FocusPackage load(CompoundTag input, HolderLookup.Provider registries) {
        return TTNbt.read(input, PACK_KEY, FocusPackage.CODEC, registries).orElse(null);
    }

    public static List<ResourceLocation> effects(@Nullable FocusPackage focusPackage) {
        return focusPackage != null ? FocusEngine.effectIds(focusPackage) : List.of();
    }
}
