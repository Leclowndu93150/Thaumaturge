package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.List;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.ITooltip;
import snownee.jade.api.ui.IElementHelper;

final class JadeComponents {
    private static final int MAX_ASPECT_LINE_WIDTH = 180;

    private JadeComponents() {}

    static void addAspectLines(ITooltip tooltip, String key, AspectList aspects) {
        List<AspectInstance> entries = aspects.entries();
        int start = 0;
        boolean firstLine = true;
        while (start < entries.size()) {
            int end = start + 1;
            Component line = aspectLine(key, entries, start, end, firstLine);
            while (end < entries.size()) {
                Component candidate = aspectLine(key, entries, start, end + 1, firstLine);
                if (IElementHelper.get().text(candidate).getSize().x > MAX_ASPECT_LINE_WIDTH) break;
                line = candidate;
                end++;
            }
            tooltip.add(line);
            start = end;
            firstLine = false;
        }
    }

    private static Component aspectLine(
            String key, List<AspectInstance> entries, int start, int end, boolean firstLine) {
        MutableComponent list = Component.empty();
        for (int i = start; i < end; i++) {
            if (i > start) {
                list.append(Component.translatable("jade.thaumaturge.aspect_separator"));
            }
            AspectInstance entry = entries.get(i);
            list.append(Component.translatable(
                    "jade.thaumaturge.aspect_amount", AspectComponents.name(entry.aspect()), entry.amount()));
        }
        return firstLine
                ? Component.translatable(key, list)
                : Component.literal("  ").append(list);
    }

    static AspectList decodeAspects(ListTag encoded, RegistryAccess registries) {
        AspectList result = AspectList.EMPTY;
        HolderLookup.RegistryLookup<IAspect> aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
        for (int i = 0; i < encoded.size(); i++) {
            CompoundTag entry = encoded.getCompound(i);
            ResourceLocation id = ResourceLocation.tryParse(entry.getString(EssentiaDataProvider.ASPECT_ID));
            if (id == null) continue;
            var holder = aspects.get(ResourceKey.create(IAspect.REGISTRY_KEY, id));
            if (holder.isPresent()) {
                result = result.add(new AspectInstance(holder.get(), entry.getInt(EssentiaDataProvider.ASPECT_AMOUNT)));
            }
        }
        return result;
    }

    static Component aspectName(String id, RegistryAccess registries) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        if (location == null) return Component.translatable("tc.aspect.unknown");
        return registries
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .get(ResourceKey.create(IAspect.REGISTRY_KEY, location))
                .<Component>map(AspectComponents::name)
                .orElseGet(() -> Component.translatable("tc.aspect.unknown"));
    }

    static Component direction(String name) {
        return Component.translatable("jade.thaumaturge.direction." + name);
    }

    static Component directions(int mask) {
        MutableComponent result = Component.empty();
        boolean first = true;
        for (Direction direction : Direction.values()) {
            if ((mask & (1 << direction.ordinal())) == 0) continue;
            if (!first) result.append(Component.literal(", "));
            result.append(direction(direction.getSerializedName()));
            first = false;
        }
        return result;
    }
}
