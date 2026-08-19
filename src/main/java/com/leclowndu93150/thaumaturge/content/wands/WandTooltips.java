package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

public final class WandTooltips {
    private WandTooltips() {}

    public static ChatFormatting primalColor(HolderLookup.@Nullable Provider registries, ResourceKey<IAspect> primal) {
        if (registries != null) {
            ChatFormatting color = registries.lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(primal).value().chatColor().map(code -> ChatFormatting.getByCode(code.charAt(0))).orElse(null);
            if (color != null) {
                return color;
            }
        }
        return ChatFormatting.GRAY;
    }

    public static Component primalName(HolderLookup.@Nullable Provider registries, ResourceKey<IAspect> primal) {
        return Component.translatable("aspect.thaumaturge." + primal.identifier().getPath()).withStyle(primalColor(registries, primal));
    }

    public static Component costSummary(HolderLookup.@Nullable Provider registries, Map<ResourceKey<IAspect>, Integer> pctByPrimal) {
        Map<Integer, List<ResourceKey<IAspect>>> groups = new LinkedHashMap<>();
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : pctByPrimal.entrySet()) {
            groups.computeIfAbsent(entry.getValue(), pct -> new ArrayList<>()).add(entry.getKey());
        }
        int basePct = 100;
        int baseSize = -1;
        for (Map.Entry<Integer, List<ResourceKey<IAspect>>> group : groups.entrySet()) {
            if (group.getValue().size() > baseSize) {
                baseSize = group.getValue().size();
                basePct = group.getKey();
            }
        }
        MutableComponent exceptions = null;
        for (Map.Entry<Integer, List<ResourceKey<IAspect>>> group : groups.entrySet()) {
            if (group.getKey() == basePct) {
                continue;
            }
            MutableComponent names = Component.empty();
            List<ResourceKey<IAspect>> primals = group.getValue();
            for (int i = 0; i < primals.size(); i++) {
                if (i > 0) {
                    names.append(Component.literal(", "));
                }
                names.append(primalName(registries, primals.get(i)));
            }
            names.append(Component.literal(" " + group.getKey() + "%"));
            if (exceptions == null) {
                exceptions = names;
            } else {
                exceptions.append(Component.literal("; ")).append(names);
            }
        }
        if (exceptions == null) {
            return Component.translatable("tooltip.thaumaturge.wand.cost", basePct).withStyle(ChatFormatting.GRAY);
        }
        return Component.translatable("tooltip.thaumaturge.wand.cost.except", basePct, exceptions).withStyle(ChatFormatting.GRAY);
    }

    public static Component capCostSummary(HolderLookup.@Nullable Provider registries, WandCap cap) {
        Map<ResourceKey<IAspect>, Integer> pctByPrimal = new LinkedHashMap<>();
        for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
            pctByPrimal.put(primal, Math.round(cap.costModifier(primal) * 100.0F));
        }
        return costSummary(registries, pctByPrimal);
    }
}
