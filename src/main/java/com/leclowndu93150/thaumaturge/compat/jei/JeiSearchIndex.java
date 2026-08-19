package com.leclowndu93150.thaumaturge.compat.jei;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.mixin.jei.gui.ingredients.IngredientFilterAccessor;
import com.leclowndu93150.thaumaturge.mixin.jei.gui.ingredients.IngredientFilterApiAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;
import mezz.jei.gui.ingredients.IngredientFilter;
import mezz.jei.gui.ingredients.ListElementInfo;
import mezz.jei.gui.search.IElementSearch;
import org.jspecify.annotations.Nullable;

public final class JeiSearchIndex {
    private static boolean reported;

    private JeiSearchIndex() {}

    public static void reindex(IJeiRuntime runtime, IIngredientManager ingredients, Predicate<Object> matches) {
        IngredientFilter filter = filterOf(runtime);
        if (filter == null) {
            return;
        }
        IngredientFilterAccessor accessor = (IngredientFilterAccessor) filter;
        IElementSearch search = accessor.thaumaturge$elementSearch();
        List<ITypedIngredient<?>> stale = new ArrayList<>();
        for (IListElement<?> element : List.copyOf(search.getAllIngredients())) {
            ITypedIngredient<?> typed = element.getTypedIngredient();
            if (matches.test(typed.getIngredient())) {
                element.setVisible(false);
                stale.add(typed);
            }
        }
        for (ITypedIngredient<?> typed : stale) {
            IListElementInfo<?> rebuilt = ListElementInfo.create(typed, ingredients, accessor.thaumaturge$modIdHelper());
            if (rebuilt != null) {
                filter.addIngredient(rebuilt);
            }
        }
    }

    private static @Nullable IngredientFilter filterOf(IJeiRuntime runtime) {
        IIngredientFilter api = runtime.getIngredientFilter();
        if (api instanceof IngredientFilterApiAccessor accessor) {
            return accessor.thaumaturge$ingredientFilter();
        }
        if (!reported) {
            reported = true;
            Thaumaturge.LOGGER.warn("JEI ingredient filter is a {}; aspect names will stay masked in JEI search after discovery." + " The accessor mixin likely no longer matches this JEI version.",
                    api.getClass().getName());
        }
        return null;
    }
}
