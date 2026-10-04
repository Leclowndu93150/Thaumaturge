package com.leclowndu93150.thaumaturge.content.wands;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import java.util.LinkedHashMap;
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
            ChatFormatting color = registries
                    .lookupOrThrow(IAspect.REGISTRY_KEY)
                    .getOrThrow(primal)
                    .value()
                    .chatColor()
                    .map(code -> ChatFormatting.getByCode(code.charAt(0)))
                    .orElse(null);
            if (color != null) {
                return color;
            }
        }
        return ChatFormatting.GRAY;
    }

    public static Component primalName(HolderLookup.@Nullable Provider registries, ResourceKey<IAspect> primal) {
        return Component.translatable("aspect.thaumaturge." + primal.location().getPath())
                .withStyle(primalColor(registries, primal));
    }

    public static @Nullable Component costSummary(
            HolderLookup.@Nullable Provider registries, Map<ResourceKey<IAspect>, Integer> pctByPrimal) {
        if (pctByPrimal.isEmpty()) {
            return null;
        }
        int firstDiscount = 100 - pctByPrimal.values().iterator().next();
        boolean uniform = true;
        for (int costPercent : pctByPrimal.values()) {
            if (100 - costPercent != firstDiscount) {
                uniform = false;
                break;
            }
        }
        if (uniform) {
            return firstDiscount == 0
                    ? null
                    : Component.translatable("tooltip.thaumaturge.wand.discount", firstDiscount)
                            .withStyle(ChatFormatting.GOLD);
        }
        MutableComponent discounts = Component.empty();
        boolean hasDiscount = false;
        for (Map.Entry<ResourceKey<IAspect>, Integer> entry : pctByPrimal.entrySet()) {
            int discount = 100 - entry.getValue();
            if (discount == 0) {
                continue;
            }
            if (hasDiscount) {
                discounts.append(Component.literal(", ").withStyle(ChatFormatting.GOLD));
            }
            ChatFormatting color = primalColor(registries, entry.getKey());
            if (color == ChatFormatting.DARK_GREEN) {
                color = ChatFormatting.GREEN;
            }
            Component name = primalName(registries, entry.getKey()).copy().withStyle(color);
            discounts.append(Component.translatable("tooltip.thaumaturge.wand.discount.aspect", name, discount)
                    .withStyle(color));
            hasDiscount = true;
        }
        return hasDiscount
                ? Component.translatable("tooltip.thaumaturge.wand.discount.aspects", discounts)
                        .withStyle(ChatFormatting.GOLD)
                : null;
    }

    public static @Nullable Component capCostSummary(HolderLookup.@Nullable Provider registries, WandCap cap) {
        Map<ResourceKey<IAspect>, Integer> pctByPrimal = new LinkedHashMap<>();
        for (ResourceKey<IAspect> primal : TCAspects.PRIMALS) {
            pctByPrimal.put(primal, Math.round(cap.costModifier(primal) * 100.0F));
        }
        return costSummary(registries, pctByPrimal);
    }
}
