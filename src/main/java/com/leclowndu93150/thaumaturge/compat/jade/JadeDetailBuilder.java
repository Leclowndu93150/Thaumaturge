package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

final class JadeDetailBuilder {
    private final ResourceLocation option;
    private final Player player;
    private final List<Component> summary = new ArrayList<>();
    private final List<Component> details = new ArrayList<>();
    private Optional<Component> title = Optional.empty();
    private boolean hideFluid;

    JadeDetailBuilder(ResourceLocation option, Player player) {
        this.option = option;
        this.player = player;
    }

    Component aspectName(Holder<IAspect> aspect) {
        return AspectPools.isDiscovered(player, aspect)
                ? AspectComponents.trueName(aspect)
                : Component.translatable("tooltip.thaumaturge.aspect.unknown");
    }

    void summary(String key, Object... args) {
        summary.add(Component.translatable(key, args));
    }

    void detail(String key, Object... args) {
        details.add(Component.translatable(key, args));
    }

    void title(Component name) {
        title = Optional.of(name);
    }

    void hideFluid() {
        hideFluid = true;
    }

    void storage(AspectList aspects, int capacity, boolean detailed) {
        List<Component> lines = detailed ? details : summary;
        if (aspects.isEmpty()) {
            lines.add(
                    capacity > 1
                            ? Component.translatable("jade.thaumaturge.essentia.empty_capacity", capacity)
                            : Component.translatable("jade.thaumaturge.essentia.empty"));
        } else {
            for (AspectInstance entry : aspects.entries()) {
                lines.add(
                        capacity > 1
                                ? Component.translatable(
                                        "jade.thaumaturge.essentia.fill",
                                        aspectName(entry.aspect()),
                                        entry.amount(),
                                        capacity)
                                : Component.translatable(
                                        "jade.thaumaturge.aspect_amount", aspectName(entry.aspect()), entry.amount()));
            }
        }
    }

    JadeBlockDetails build() {
        return new JadeBlockDetails(option, List.copyOf(summary), List.copyOf(details), title, hideFluid);
    }
}
