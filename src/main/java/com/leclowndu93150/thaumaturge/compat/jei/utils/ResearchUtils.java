package com.leclowndu93150.thaumaturge.compat.jei.utils;

import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.compat.jei.ThaumaturgeJEIPlugin;
import com.leclowndu93150.thaumaturge.content.research.ResearchManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;

public final class ResearchUtils {

    public static List<Component> generateMissingResearchList(ResearchGate... research) {
        List<Component> list = new ArrayList<>();
        list.add(Component.translatable("jei.thaumaturge.research.missing_research").withStyle(ChatFormatting.GOLD));
        for (ResearchGate gate : research) {
            if (!ResearchManager.doesPassGate(Minecraft.getInstance().player, gate)) {
                RegistryAccess access = ThaumaturgeJEIPlugin.clientRegistryAccess();
                if (access == null) {
                    list.add(Component.literal("- ").append(gate.entry().toString()).withStyle(ChatFormatting.RED));
                } else {
                    IResearchEntry entry = access.lookupOrThrow(IResearchEntry.REGISTRY_KEY).get(gate.entry()).map(Holder::value).orElse(null);
                    if (entry != null) {
                        list.add(Component.literal("- ").append(Component.translatable(entry.nameKey())).withStyle(ChatFormatting.RED));
                    } else {
                        list.add(Component.literal("- ").append(gate.entry().toString()).withStyle(ChatFormatting.RED));
                    }
                }
            }
        }
        return list;
    }
}
