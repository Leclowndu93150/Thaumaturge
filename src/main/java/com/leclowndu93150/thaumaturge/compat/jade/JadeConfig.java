package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.Accessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.config.IPluginConfig;

final class JadeConfig {
    static final ResourceLocation CATEGORY = TCIds.rl("display");

    static final ResourceLocation NODES = option("nodes");
    static final ResourceLocation GOLEMS = option("golems");
    static final ResourceLocation VIS_RELAYS = option("vis_relays");
    static final ResourceLocation NODE_TRANSDUCERS = option("node_transducers");
    static final ResourceLocation SMELTERS = option("smelters");
    static final ResourceLocation JARS = option("jars");
    static final ResourceLocation ALEMBICS = option("alembics");
    static final ResourceLocation CRUCIBLES = option("crucibles");
    static final ResourceLocation TUBES = option("tubes");
    static final ResourceLocation VALVES = option("valves");
    static final ResourceLocation RESTRICTED_TUBES = option("restricted_tubes");
    static final ResourceLocation FILTER_TUBES = option("filter_tubes");
    static final ResourceLocation ONE_WAY_TUBES = option("one_way_tubes");
    static final ResourceLocation BUFFERS = option("buffers");
    static final ResourceLocation THAUMATORIUMS = option("thaumatoriums");
    static final ResourceLocation CENTRIFUGES = option("centrifuges");
    static final ResourceLocation GOLEM_BUILDERS = option("golem_builders");
    static final ResourceLocation VOID_SIPHONS = option("void_siphons");
    static final ResourceLocation DECONSTRUCTION_TABLES = option("deconstruction_tables");
    static final ResourceLocation SPAS = option("spas");
    static final ResourceLocation EVERFULL_URNS = option("everfull_urns");
    static final ResourceLocation INFERNAL_FURNACES = option("infernal_furnaces");
    static final ResourceLocation FOCAL_MANIPULATORS = option("focal_manipulators");

    private static final List<ResourceLocation> OPTIONS = List.of(
            NODES,
            GOLEMS,
            VIS_RELAYS,
            NODE_TRANSDUCERS,
            SMELTERS,
            JARS,
            ALEMBICS,
            CRUCIBLES,
            TUBES,
            VALVES,
            RESTRICTED_TUBES,
            FILTER_TUBES,
            ONE_WAY_TUBES,
            BUFFERS,
            THAUMATORIUMS,
            CENTRIFUGES,
            GOLEM_BUILDERS,
            VOID_SIPHONS,
            DECONSTRUCTION_TABLES,
            SPAS,
            EVERFULL_URNS,
            INFERNAL_FURNACES,
            FOCAL_MANIPULATORS);

    private JadeConfig() {}

    static void register(IWailaClientRegistration registration) {
        registration.addConfig(CATEGORY, true);
        for (ResourceLocation option : OPTIONS) {
            registration.addConfig(option, DisplayMode.GOGGLES);
        }
        registration.setConfigCategoryOverride(CATEGORY, Component.translatable("config.jade.thaumaturge"));
    }

    static boolean shouldShow(IPluginConfig config, ResourceLocation option, Accessor<?> accessor) {
        return config.get(CATEGORY) && config.<DisplayMode>getEnum(option).shouldShow(accessor);
    }

    static ResourceLocation option(String name) {
        return TCIds.rl("display." + name);
    }

    enum DisplayMode {
        OFF,
        ALWAYS,
        GOGGLES;

        boolean shouldShow(Accessor<?> accessor) {
            return switch (this) {
                case OFF -> false;
                case ALWAYS -> true;
                case GOGGLES -> GogglesAccess.wearsGoggles(accessor.getPlayer());
            };
        }
    }
}
