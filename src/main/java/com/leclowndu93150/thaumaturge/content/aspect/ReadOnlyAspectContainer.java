package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectContainer;
import net.minecraft.core.Holder;

public interface ReadOnlyAspectContainer extends IAspectContainer {

    @Override
    default void setAspects(AspectList aspects) {}

    @Override
    default boolean accepts(Holder<IAspect> aspect) {
        return false;
    }

    @Override
    default int fill(Holder<IAspect> aspect, int amount) {
        return amount;
    }

    @Override
    default boolean drain(Holder<IAspect> aspect, int amount) {
        return false;
    }

    @Override
    default boolean holds(Holder<IAspect> aspect, int amount) {
        return false;
    }

    @Override
    default int amountOf(Holder<IAspect> aspect) {
        return 0;
    }
}
