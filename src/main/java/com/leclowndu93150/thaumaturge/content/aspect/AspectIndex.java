package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspectIndex;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaCapabilities;
import com.leclowndu93150.thaumaturge.api.essentia.IEssentiaContainerItem;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.Reference2ObjectMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.advancements.criterion.DataComponentMatchers;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class AspectIndex implements IAspectIndex {
    public static final AspectIndex EMPTY = new AspectIndex(Map.of());

    public record Variant(DataComponentMatchers matcher, AspectList aspects) {
        public static final Codec<Variant> CODEC = RecordCodecBuilder.create(
                builder -> builder.group(DataComponentMatchers.CODEC.forGetter(Variant::matcher), AspectList.CODEC.fieldOf("aspects").forGetter(Variant::aspects)).apply(builder, Variant::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, Variant> STREAM_CODEC = StreamCodec.composite(DataComponentMatchers.STREAM_CODEC, Variant::matcher, AspectList.STREAM_CODEC,
                Variant::aspects, Variant::new);
    }

    public record ItemEntry(AspectList base, List<Variant> variants) {
        public static final ItemEntry EMPTY = new ItemEntry(AspectList.EMPTY, List.of());

        private static final Codec<ItemEntry> FULL_CODEC = RecordCodecBuilder.create(builder -> builder.group(AspectList.CODEC.optionalFieldOf("base", AspectList.EMPTY).forGetter(ItemEntry::base),
                Variant.CODEC.listOf().optionalFieldOf("variants", List.of()).forGetter(ItemEntry::variants)).apply(builder, ItemEntry::new));

        public static final Codec<ItemEntry> CODEC = Codec.either(AspectList.CODEC, FULL_CODEC).xmap(either -> either.map(base -> new ItemEntry(base, List.of()), full -> full),
                entry -> entry.variants().isEmpty() ? Either.left(entry.base()) : Either.right(entry));

        public static final StreamCodec<RegistryFriendlyByteBuf, ItemEntry> STREAM_CODEC = StreamCodec.composite(AspectList.STREAM_CODEC, ItemEntry::base,
                Variant.STREAM_CODEC.apply(ByteBufCodecs.list()), ItemEntry::variants, ItemEntry::new);

        public boolean isEmpty() {
            return base.isEmpty() && variants.isEmpty();
        }

        public static ItemEntry of(AspectList base) {
            return new ItemEntry(base, List.of());
        }
    }

    public static final Codec<AspectIndex> CODEC = Codec.unboundedMap(Identifier.CODEC, ItemEntry.CODEC).xmap(AspectIndex::fromIdMap, AspectIndex::toIdMap);

    public static final StreamCodec<RegistryFriendlyByteBuf, AspectIndex> STREAM_CODEC = StreamCodec.composite(Entry.STREAM_CODEC.apply(ByteBufCodecs.list()), AspectIndex::entries,
            AspectIndex::fromEntries);

    private final Reference2ObjectMap<Item, ItemEntry> byItem;

    private AspectIndex(Map<Item, ItemEntry> source) {
        Reference2ObjectMap<Item, ItemEntry> map = new Reference2ObjectOpenHashMap<>(source.size());
        for (Map.Entry<Item, ItemEntry> entry : source.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                map.put(entry.getKey(), entry.getValue());
            }
        }
        this.byItem = map;
    }

    public static AspectIndex of(Map<Item, AspectList> source) {
        return of(source, Map.of());
    }

    public static AspectIndex of(Map<Item, AspectList> source, Map<Item, List<Variant>> variants) {
        if (source.isEmpty() && variants.isEmpty()) {
            return EMPTY;
        }
        Map<Item, ItemEntry> entries = new HashMap<>(source.size());
        source.forEach((item, aspects) -> entries.put(item, ItemEntry.of(aspects)));
        variants.forEach((item, list) -> entries.merge(item, new ItemEntry(AspectList.EMPTY, List.copyOf(list)), (existing, added) -> new ItemEntry(existing.base(), added.variants())));
        return new AspectIndex(entries);
    }

    public static AspectIndex ofEntries(Map<Item, ItemEntry> source) {
        return source.isEmpty() ? EMPTY : new AspectIndex(source);
    }

    @Override
    public AspectList of(Item item) {
        return byItem.getOrDefault(item, ItemEntry.EMPTY).base();
    }

    @Override
    public AspectList of(ItemStack stack) {
        if (stack.isEmpty()) {
            return AspectList.EMPTY;
        }

        IEssentiaContainerItem container = stack.getCapability(EssentiaCapabilities.CONTAINER);
        if (container != null && !container.ignoreContainedAspects()) {
            return container.getAspects(stack);
        }

        ItemEntry entry = byItem.get(stack.getItem());
        if (entry == null) {
            return AspectList.EMPTY;
        }
        for (Variant variant : entry.variants()) {
            if (variant.matcher().test(stack)) {
                return variant.aspects();
            }
        }
        return entry.base();
    }

    public int size() {
        return byItem.size();
    }

    public boolean isEmpty() {
        return byItem.isEmpty();
    }

    private List<Entry> entries() {
        List<Entry> list = new java.util.ArrayList<>(byItem.size());
        byItem.forEach((item, entry) -> list.add(new Entry(BuiltInRegistries.ITEM.getKey(item), entry)));
        return list;
    }

    private static AspectIndex fromEntries(List<Entry> entries) {
        Map<Item, ItemEntry> map = new HashMap<>(entries.size());
        for (Entry entry : entries) {
            Item item = BuiltInRegistries.ITEM.getValue(entry.itemId);
            if (item != null) {
                map.put(item, entry.entry);
            }
        }
        return ofEntries(map);
    }

    private Map<Identifier, ItemEntry> toIdMap() {
        Map<Identifier, ItemEntry> out = new HashMap<>(byItem.size());
        byItem.forEach((item, entry) -> out.put(BuiltInRegistries.ITEM.getKey(item), entry));
        return out;
    }

    private static AspectIndex fromIdMap(Map<Identifier, ItemEntry> map) {
        Map<Item, ItemEntry> out = new HashMap<>(map.size());
        map.forEach((id, entry) -> {
            Item item = BuiltInRegistries.ITEM.getValue(id);
            if (item != null) {
                out.put(item, entry);
            }
        });
        return ofEntries(out);
    }

    private record Entry(Identifier itemId, ItemEntry entry) {
        static final StreamCodec<RegistryFriendlyByteBuf, Entry> STREAM_CODEC = StreamCodec.composite(Identifier.STREAM_CODEC, Entry::itemId, ItemEntry.STREAM_CODEC, Entry::entry, Entry::new);
    }
}
