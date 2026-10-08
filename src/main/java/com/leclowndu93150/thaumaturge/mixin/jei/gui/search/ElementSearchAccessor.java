package com.leclowndu93150.thaumaturge.mixin.jei.gui.search;

import java.util.Map;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.search.ElementSearch;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ElementSearch.class)
public interface ElementSearchAccessor {
    @Accessor("allElements")
    Map<Object, IListElement<?>> thaumaturge$allElements();
}
