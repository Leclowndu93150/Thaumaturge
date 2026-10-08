package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import snownee.jade.api.ITooltip;

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
                if (Minecraft.getInstance().font.width(candidate) > MAX_ASPECT_LINE_WIDTH) break;
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
}
