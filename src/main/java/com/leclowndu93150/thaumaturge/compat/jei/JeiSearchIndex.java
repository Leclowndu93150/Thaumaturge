package com.leclowndu93150.thaumaturge.compat.jei;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.mixin.jei.gui.ingredients.IngredientFilterAccessor;
import com.leclowndu93150.thaumaturge.mixin.jei.gui.ingredients.IngredientFilterApiAccessor;
import com.leclowndu93150.thaumaturge.mixin.jei.gui.search.ElementSearchAccessor;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
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
        IElementSearch search = ((IngredientFilterAccessor) filter).thaumaturge$elementSearch();
        List<IListElement<?>> stale = new ArrayList<>();
        for (IListElement<?> element : List.copyOf(search.getAllIngredients())) {
            if (matches.test(element.getTypedIngredient().getIngredient())) {
                stale.add(element);
            }
        }
        for (IListElement<?> element : stale) {
            refresh(filter, search, ingredients, element);
        }
    }

    private static <V> void refresh(
            IngredientFilter filter, IElementSearch search, IIngredientManager ingredients, IListElement<V> element) {
        if (!(search instanceof ElementSearchAccessor accessor)) {
            return;
        }
        ITypedIngredient<V> typed = element.getTypedIngredient();
        IIngredientHelper<V> helper = ingredients.getIngredientHelper(typed.getType());
        Object uid = helper.getUid(typed.getIngredient(), UidContext.Ingredient);
        IListElementInfo<V> info = ListElementInfo.createFromElement(
                element, ingredients, ((IngredientFilterAccessor) filter).thaumaturge$modIdHelper());
        if (info == null) {
            Thaumaturge.LOGGER.error(
                    "Could not refresh the JEI search entry for {}; it keeps its old search name",
                    helper.getErrorInfo(typed.getIngredient()));
            return;
        }
        accessor.thaumaturge$allElements().remove(uid);
        try {
            filter.addIngredient(info);
        } catch (RuntimeException | LinkageError e) {
            accessor.thaumaturge$allElements().putIfAbsent(uid, element);
            Thaumaturge.LOGGER.error(
                    "Could not refresh the JEI search entry for {}; it keeps its old search name",
                    helper.getErrorInfo(typed.getIngredient()),
                    e);
        }
    }

    private static @Nullable IngredientFilter filterOf(IJeiRuntime runtime) {
        IIngredientFilter api = runtime.getIngredientFilter();
        if (api instanceof IngredientFilterApiAccessor accessor) {
            return accessor.thaumaturge$ingredientFilter();
        }
        if (!reported) {
            reported = true;
            Thaumaturge.LOGGER.warn(
                    "JEI ingredient filter is a {}; aspect names will stay masked in JEI search after discovery."
                            + " The accessor mixin likely no longer matches this JEI version.",
                    api.getClass().getName());
        }
        return null;
    }
}
