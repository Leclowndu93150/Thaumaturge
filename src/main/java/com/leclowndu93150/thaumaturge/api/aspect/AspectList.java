package com.leclowndu93150.thaumaturge.api.aspect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;

/**
 * An immutable, ordered list of aspect quantities.
 *
 * <p>Iteration order is insertion order. Two instances are equal when they contain the same
 * (aspect, amount) entries in the same order.
 *
 * <p>All mutating operations return a new instance and leave the receiver untouched. Empty
 * results from {@link #reduce}, {@link #remove}, and similar are returned as {@link #EMPTY}.
 *
 * <p>For datapack serialization use {@link #CODEC}. For network sync use {@link #STREAM_CODEC}.
 *
 * @since 1.0.0
 */
public final class AspectList {
    /** Empty aspect list. Identity comparable. */
    public static final AspectList EMPTY = new AspectList(Collections.emptyList());

    /** Codec; serializes as a JSON array of {@link AspectInstance}. */
    public static final Codec<AspectList> CODEC = AspectInstance.CODEC.listOf().flatXmap(entries -> {
        AspectList result = AspectList.EMPTY;
        for (AspectInstance entry : entries) {
            result = result.add(entry);
        }
        return DataResult.success(result);
    }, list -> DataResult.success(list.entries()));

    /** Non-Empty Codec; serializes as a non-empty JSON array of {@link AspectInstance}. */
    public static final Codec<AspectList> NON_EMPTY_CODEC = CODEC.validate(list -> list.isEmpty() ? DataResult.error(() -> "Aspect List must not be empty", EMPTY) : DataResult.success(list));

    /** Network codec; preserves order. */
    public static final StreamCodec<RegistryFriendlyByteBuf, AspectList> STREAM_CODEC = AspectInstance.STREAM_CODEC.apply(ByteBufCodecs.list()).map(AspectList::ofEntries, AspectList::entries);

    private final List<AspectInstance> entries;

    private AspectList(List<AspectInstance> entries) {
        this.entries = List.copyOf(entries);
    }

    /**
     * Constructs an aspect list from an ordered sequence of entries. Duplicate aspects collapse
     * into a single entry whose amount is the sum.
     *
     * @param entries the entries; null elements are not permitted
     * @return the resulting aspect list, never {@code null}
     */
    public static AspectList ofEntries(List<AspectInstance> entries) {
        AspectList result = EMPTY;
        for (AspectInstance entry : entries) {
            result = result.add(entry);
        }
        return result;
    }

    /**
     * Returns an aspect list containing exactly the given entries. Convenience for code that
     * already knows there are no duplicates.
     *
     * @param entries the entries
     * @return an aspect list, never {@code null}
     */
    public static AspectList of(AspectInstance... entries) {
        return ofEntries(List.of(entries));
    }

    /**
     * Returns the entries of this list in insertion order. The returned view is unmodifiable.
     *
     * @return an unmodifiable list of entries
     */
    public List<AspectInstance> entries() {
        return entries;
    }

    /**
     * Number of distinct aspects in this list.
     *
     * @return the count of distinct aspects
     */
    public int size() {
        return entries.size();
    }

    /**
     * Whether this list contains no aspects.
     *
     * @return {@code true} when {@link #size()} is zero
     */
    public boolean isEmpty() {
        return entries.isEmpty();
    }

    /**
     * Total amount across all entries, in vis units.
     *
     * @return the sum of all amounts
     */
    public int totalAmount() {
        int sum = 0;
        for (AspectInstance entry : entries) {
            sum += entry.amount();
        }
        return sum;
    }

    /**
     * The amount of the given aspect contained in this list.
     *
     * @param aspect the aspect, compared by registry key
     * @return the amount, or zero when absent
     */
    public int amountOf(Holder<IAspect> aspect) {
        int index = indexOf(aspect);
        return index < 0 ? 0 : entries.get(index).amount();
    }

    /**
     * The amount of the given aspect contained in this list, resolving the key against the given
     * registry view. Convenience for callers that hold a {@link ResourceKey} rather than a
     * {@link Holder}.
     *
     * @param key        the aspect key, compared after resolution against {@code registries}
     * @param registries the registry view used to resolve the key
     * @return the amount, or zero when the key is absent or cannot be resolved
     */
    public int amountOf(ResourceKey<IAspect> key, HolderLookup.Provider registries) {
        Holder<IAspect> holder = Aspects.resolve(registries, key);
        return holder == null ? 0 : amountOf(holder);
    }

    /**
     * Returns an aspect list with the given amount added. Existing amounts of the same aspect
     * are summed; otherwise the aspect is appended at the end.
     *
     * @param aspect the aspect
     * @param amount the amount to add, must be positive
     * @return the resulting aspect list
     */
    public AspectList add(Holder<IAspect> aspect, int amount) {
        return add(new AspectInstance(aspect, amount));
    }

    /**
     * Returns an aspect list with the given entry added.
     *
     * @param entry the entry to add
     * @return the resulting aspect list
     */
    public AspectList add(AspectInstance entry) {
        int index = indexOf(entry.aspect());
        List<AspectInstance> next = new ArrayList<>(entries);
        if (index < 0) {
            next.add(entry);
        } else {
            AspectInstance existing = next.get(index);
            next.set(index, existing.withAmount(existing.amount() + entry.amount()));
        }
        return new AspectList(next);
    }

    /**
     * Returns an aspect list with every entry from {@code other} added.
     *
     * @param other the other list
     * @return the resulting aspect list
     */
    public AspectList add(AspectList other) {
        AspectList result = this;
        for (AspectInstance entry : other.entries) {
            result = result.add(entry);
        }
        return result;
    }

    /**
     * Returns an aspect list with the given amount of {@code aspect} removed. The aspect is
     * dropped when its remaining amount would be zero or negative.
     *
     * @param aspect the aspect to remove from
     * @param amount the amount to remove
     * @return the resulting aspect list
     */
    public AspectList remove(Holder<IAspect> aspect, int amount) {
        int index = indexOf(aspect);
        if (index < 0) {
            return this;
        }
        AspectInstance existing = entries.get(index);
        int newAmount = existing.amount() - amount;
        List<AspectInstance> next = new ArrayList<>(entries);
        if (newAmount <= 0) {
            next.remove(index);
        } else {
            next.set(index, existing.withAmount(newAmount));
        }
        return next.isEmpty() ? EMPTY : new AspectList(next);
    }

    /**
     * Returns an aspect list with the given aspect removed entirely.
     *
     * @param aspect the aspect to drop
     * @return the resulting aspect list
     */
    public AspectList without(Holder<IAspect> aspect) {
        int index = indexOf(aspect);
        if (index < 0) {
            return this;
        }
        List<AspectInstance> next = new ArrayList<>(entries);
        next.remove(index);
        return next.isEmpty() ? EMPTY : new AspectList(next);
    }

    /**
     * Returns an aspect list with the given amount reduced from {@code aspect}, only when the
     * receiver has enough. Otherwise the receiver is returned unchanged.
     *
     * @param aspect the aspect
     * @param amount the amount to remove
     * @return the resulting aspect list, or the receiver when the operation would underflow
     */
    public AspectList reduce(Holder<IAspect> aspect, int amount) {
        if (amountOf(aspect) < amount) {
            return this;
        }
        return remove(aspect, amount);
    }

    /**
     * Returns an aspect list whose entries are the maximum of this list's amounts and the
     * given list's amounts. The legacy operation was named {@code merge}.
     *
     * @param other the other list
     * @return the merged aspect list
     */
    public AspectList merge(AspectList other) {
        Map<Holder<IAspect>, Integer> max = new LinkedHashMap<>();
        for (AspectInstance entry : entries) {
            max.put(entry.aspect(), entry.amount());
        }
        for (AspectInstance entry : other.entries) {
            max.merge(entry.aspect(), entry.amount(), Math::max);
        }
        List<AspectInstance> next = new ArrayList<>(max.size());
        for (Map.Entry<Holder<IAspect>, Integer> entry : max.entrySet()) {
            next.add(new AspectInstance(entry.getKey(), entry.getValue()));
        }
        return new AspectList(next);
    }

    /**
     * Returns the entries sorted by aspect tag, ascending.
     *
     * @return a sorted copy of the entries
     */
    public List<AspectInstance> sortedByTag() {
        List<AspectInstance> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparing(entry -> entry.aspect().value().tag()));
        return List.copyOf(sorted);
    }

    /**
     * Returns the entries sorted by amount, descending. Stable for equal amounts.
     *
     * @return a sorted copy of the entries
     */
    public List<AspectInstance> sortedByAmount() {
        List<AspectInstance> sorted = new ArrayList<>(entries);
        sorted.sort(Comparator.comparingInt(AspectInstance::amount).reversed());
        return List.copyOf(sorted);
    }

    /**
     * Returns the set of aspects in this list.
     *
     * @return an unmodifiable set of aspect holders, in insertion order
     */
    public Set<Holder<IAspect>> aspects() {
        Object2IntMap<Holder<IAspect>> map = new Object2IntLinkedOpenHashMap<>();
        for (AspectInstance entry : entries) {
            map.put(entry.aspect(), entry.amount());
        }
        return Collections.unmodifiableSet(map.keySet());
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AspectList other && entries.equals(other.entries);
    }

    @Override
    public int hashCode() {
        return entries.hashCode();
    }

    @Override
    public String toString() {
        return "AspectList" + entries;
    }

    private int indexOf(Holder<IAspect> aspect) {
        for (int i = 0; i < entries.size(); i++) {
            if (entries.get(i).aspect().equals(aspect)) {
                return i;
            }
        }
        return -1;
    }
}
