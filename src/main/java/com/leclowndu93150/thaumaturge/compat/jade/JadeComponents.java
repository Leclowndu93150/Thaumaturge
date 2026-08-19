package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

final class JadeComponents {
    private JadeComponents() {}

    static Component aspectLine(String key, AspectList aspects) {
        MutableComponent list = Component.empty();
        boolean first = true;
        for (AspectInstance entry : aspects.entries()) {
            if (!first) {
                list.append(Component.translatable("jade.thaumaturge.aspect_separator"));
            }
            list.append(Component.translatable("jade.thaumaturge.aspect_amount", AspectComponents.name(entry.aspect()), entry.amount()));
            first = false;
        }
        return Component.translatable(key, list);
    }
}
