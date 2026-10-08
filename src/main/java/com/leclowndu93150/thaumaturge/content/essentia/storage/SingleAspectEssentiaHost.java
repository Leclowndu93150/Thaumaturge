package com.leclowndu93150.thaumaturge.content.essentia.storage;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public interface SingleAspectEssentiaHost {
    @Nullable
    ResourceKey<IAspect> aspectKey();

    @Nullable
    ResourceKey<IAspect> aspectFilterKey();

    int amount();

    int capacity();

    AspectList getAspects();

    boolean canInputFrom(Direction face);

    boolean canOutputTo(Direction face);

    int storageInsertLimit(int requested);

    void setStorageContents(@Nullable ResourceKey<IAspect> aspect, int amount);

    void onStorageCommitted();

    default void onStorageVoided(int voided) {}
}
