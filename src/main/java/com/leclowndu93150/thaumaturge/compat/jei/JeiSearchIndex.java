package com.leclowndu93150.thaumaturge.compat.jei;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.mixin.jei.gui.ingredients.IngredientFilterAccessor;
import com.leclowndu93150.thaumaturge.mixin.jei.gui.ingredients.IngredientFilterApiAccessor;
import com.leclowndu93150.thaumaturge.mixin.jei.gui.search.ElementSearchAccessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import mezz.jei.api.helpers.IModIdHelper;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.runtime.IIngredientFilter;
import mezz.jei.api.runtime.IIngredientManager;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.gui.ingredients.IListElement;
import mezz.jei.gui.ingredients.IListElementInfo;
import mezz.jei.gui.ingredients.ListElementInfo;
import mezz.jei.gui.ingredients.IngredientFilter;
import mezz.jei.gui.search.IElementSearch;
import org.jspecify.annotations.Nullable;

public final class JeiSearchIndex {
    private static boolean reported;
    private static boolean incompatible;
    private static @Nullable Method createFromElement;

    private JeiSearchIndex() {}

    public static void reindex(IJeiRuntime runtime, IIngredientManager ingredients, Predicate<Object> matches) {
        if (incompatible) {
            return;
        }
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

    private static <V> void refresh(IngredientFilter filter, IElementSearch search, IIngredientManager ingredients, IListElement<V> element) {
        if (!(search instanceof ElementSearchAccessor accessor)) {
            return;
        }
        ITypedIngredient<V> typed = element.getTypedIngredient();
        IIngredientHelper<V> helper = ingredients.getIngredientHelper(typed.getType());
        Object uid = helper.getUid(typed.getIngredient(), UidContext.Ingredient);
        IListElementInfo<V> info = createInfo(filter, ingredients, element);
        if (info == null && incompatible) {
            return;
        }
        if (info == null) {
            Thaumaturge.LOGGER.error("Could not refresh the JEI search entry for {}; it keeps its old search name", helper.getErrorInfo(typed.getIngredient()));
            return;
        }
        accessor.thaumaturge$allElements().remove(uid);
        try {
            filter.addIngredient(info);
        } catch (RuntimeException | LinkageError e) {
            accessor.thaumaturge$allElements().putIfAbsent(uid, element);
            Thaumaturge.LOGGER.error("Could not refresh the JEI search entry for {}; it keeps its old search name", helper.getErrorInfo(typed.getIngredient()), e);
        }
    }

    /**
     * Calls JEI's internal {@code ListElementInfo.createFromElement}. Its parameter list differs between JEI releases (newer ones take an extra
     * {@code IIngredientFilterConfig}), so the method is looked up by name and each parameter is filled in by type: the element, the ingredient manager
     * and the mod id helper directly, anything else from the fields the ingredient filter holds. When that is not possible the refresh is switched off
     * after one warning and the old search names are kept.
     */
    @SuppressWarnings("unchecked")
    private static <V> @Nullable IListElementInfo<V> createInfo(IngredientFilter filter, IIngredientManager ingredients, IListElement<V> element) {
        try {
            Method method = createFromElement();
            Class<?>[] types = method.getParameterTypes();
            Object[] args = new Object[types.length];
            for (int i = 0; i < types.length; i++) {
                if (types[i].isAssignableFrom(IListElement.class)) {
                    args[i] = element;
                } else if (types[i].isAssignableFrom(IIngredientManager.class)) {
                    args[i] = ingredients;
                } else if (types[i].isAssignableFrom(IModIdHelper.class)) {
                    args[i] = ((IngredientFilterAccessor) filter).thaumaturge$modIdHelper();
                } else {
                    args[i] = heldBy(filter, types[i]);
                }
            }
            return (IListElementInfo<V>) method.invoke(null, args);
        } catch (ReflectiveOperationException | RuntimeException | LinkageError e) {
            incompatible = true;
            Thaumaturge.LOGGER.warn("This JEI version is incompatible with Thaumaturge's search index refresh; aspect names will keep their old search names in JEI after discovery.", e);
            return null;
        }
    }

    private static Method createFromElement() throws NoSuchMethodException {
        if (createFromElement == null) {
            for (Method m : ListElementInfo.class.getMethods()) {
                if (m.getName().equals("createFromElement") && Modifier.isStatic(m.getModifiers())) {
                    createFromElement = m;
                    break;
                }
            }
            if (createFromElement == null) {
                throw new NoSuchMethodException("ListElementInfo.createFromElement");
            }
        }
        return createFromElement;
    }

    private static Object heldBy(IngredientFilter filter, Class<?> type) throws ReflectiveOperationException {
        for (Field field : IngredientFilter.class.getDeclaredFields()) {
            if (!Modifier.isStatic(field.getModifiers()) && type.isAssignableFrom(field.getType())) {
                field.setAccessible(true);
                return field.get(filter);
            }
        }
        throw new NoSuchFieldException("IngredientFilter has no field of type " + type.getName());
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
