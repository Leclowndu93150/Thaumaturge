package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import snownee.jade.api.Accessor;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.config.IPluginConfig;

final class JadeConfig {
    static final Identifier CATEGORY = TTIds.rl("display");

    static final Identifier NODES = option("nodes");
    static final Identifier GOLEMS = option("golems");
    static final Identifier VIS_RELAYS = option("vis_relays");
    static final Identifier NODE_TRANSDUCERS = option("node_transducers");
    static final Identifier SMELTERS = option("smelters");
    static final Identifier ADVANCED_ALCHEMICAL_FURNACES = option("advanced_alchemical_furnaces");
    static final Identifier JARS = option("jars");
    static final Identifier ALEMBICS = option("alembics");
    static final Identifier CRUCIBLES = option("crucibles");
    static final Identifier TUBES = option("tubes");
    static final Identifier VALVES = option("valves");
    static final Identifier RESTRICTED_TUBES = option("restricted_tubes");
    static final Identifier FILTER_TUBES = option("filter_tubes");
    static final Identifier ONE_WAY_TUBES = option("one_way_tubes");
    static final Identifier BUFFERS = option("buffers");
    static final Identifier THAUMATORIUMS = option("thaumatoriums");
    static final Identifier CENTRIFUGES = option("centrifuges");
    static final Identifier CRYSTALIZERS = option("crystalizers");
    static final Identifier RESERVOIRS = option("reservoirs");
    static final Identifier FLUX_SCRUBBERS = option("flux_scrubbers");
    static final Identifier GOLEM_BUILDERS = option("golem_builders");
    static final Identifier VOID_SIPHONS = option("void_siphons");
    static final Identifier DECONSTRUCTION_TABLES = option("deconstruction_tables");
    static final Identifier SPAS = option("spas");
    static final Identifier EVERFULL_URNS = option("everfull_urns");
    static final Identifier INFERNAL_FURNACES = option("infernal_furnaces");
    static final Identifier FOCAL_MANIPULATORS = option("focal_manipulators");

    private static final List<Identifier> OPTIONS = List.of(NODES, GOLEMS, VIS_RELAYS, NODE_TRANSDUCERS, SMELTERS, ADVANCED_ALCHEMICAL_FURNACES, JARS, ALEMBICS, CRUCIBLES, TUBES, VALVES,
            RESTRICTED_TUBES, FILTER_TUBES, ONE_WAY_TUBES, BUFFERS, THAUMATORIUMS, CENTRIFUGES, CRYSTALIZERS, RESERVOIRS, FLUX_SCRUBBERS, GOLEM_BUILDERS, VOID_SIPHONS, DECONSTRUCTION_TABLES, SPAS,
            EVERFULL_URNS, INFERNAL_FURNACES, FOCAL_MANIPULATORS);

    private JadeConfig() {}

    static void register(IWailaClientRegistration registration) {
        registration.addConfig(CATEGORY, true);
        for (Identifier option : OPTIONS) {
            registration.addConfig(option, DisplayMode.GOGGLES);
        }
        registration.setConfigCategoryOverride(CATEGORY, Component.translatable("config.jade.thaumaturge"));
    }

    static boolean shouldShow(IPluginConfig config, Identifier option, Accessor<?> accessor) {
        return config.get(CATEGORY) && config.<DisplayMode>getEnum(option).shouldShow(accessor);
    }

    static Identifier option(String name) {
        return TTIds.rl("display." + name);
    }

    enum DisplayMode {
        OFF, ON, GOGGLES;

        boolean shouldShow(Accessor<?> accessor) {
            return switch (this) {
                case OFF -> false;
                case ON -> true;
                case GOGGLES -> GogglesAccess.wearsRevealingGear(accessor.getPlayer());
            };
        }
    }
}
