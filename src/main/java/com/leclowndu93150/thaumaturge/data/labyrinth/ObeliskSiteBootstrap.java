package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.site.ObeliskSite;
import com.leclowndu93150.thaumaturge.content.eldritch.site.SiteAwakening;
import com.leclowndu93150.thaumaturge.content.eldritch.site.behavior.CultRitualBehavior;
import com.leclowndu93150.thaumaturge.content.eldritch.site.behavior.DormantBehavior;
import com.leclowndu93150.thaumaturge.content.eldritch.site.behavior.GuardianWatchBehavior;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public final class ObeliskSiteBootstrap {
    public static final ResourceKey<ObeliskSite> CULT_RITUAL = key("cult_ritual");
    public static final ResourceKey<ObeliskSite> GUARDIAN_WATCH = key("guardian_watch");

    private static final int RITUALISTS = 4;
    private static final int KNIGHT_WEIGHT = 3;
    private static final int CLERIC_WEIGHT = 1;
    private static final int MAX_GUARDS = 8;
    private static final int CULT_REINFORCEMENTS = 6;
    private static final int CULT_RANGE = 24;
    private static final int GUARDIAN_BUDGET = 4;
    private static final int GUARDIAN_RANGE = 32;
    private static final int GUARDIAN_SPAWN_RADIUS = 6;
    private static final int AWAKENING_INTERVAL = 200;
    private static final int AWAKENING_RADIUS = 32;

    private ObeliskSiteBootstrap() {}

    public static void bootstrap(BootstrapContext<ObeliskSite> context) {
        Optional<Holder<StructureProcessorList>> weathering = Optional.of(
                context.lookup(Registries.PROCESSOR_LIST).getOrThrow(LabyrinthProcessorBootstrap.OBELISK_WEATHERING));
        EntityType<?> guardian = TTEntities.ELDRITCH_GUARDIAN.get();
        Optional<SiteAwakening> awakening =
                Optional.of(new SiteAwakening(guardian, 1, AWAKENING_INTERVAL, AWAKENING_RADIUS));
        SimpleWeightedRandomList<ResourceKey<LabyrinthDefinition>> labyrinths =
                SimpleWeightedRandomList.single(LabyrinthDefinitionBootstrap.ELDRITCH);
        SimpleWeightedRandomList<EntityType<?>> guards = SimpleWeightedRandomList.<EntityType<?>>builder()
                .add(TTEntities.CULTIST_KNIGHT.get(), KNIGHT_WEIGHT)
                .add(TTEntities.CULTIST_CLERIC.get(), CLERIC_WEIGHT)
                .build();
        context.register(
                CULT_RITUAL,
                new ObeliskSite(
                        ObeliskSiteTemplates.SITE_CULT,
                        weathering,
                        new CultRitualBehavior(RITUALISTS, guards, MAX_GUARDS, CULT_REINFORCEMENTS, CULT_RANGE),
                        awakening,
                        NodeType.DARK,
                        labyrinths));
        context.register(
                GUARDIAN_WATCH,
                new ObeliskSite(
                        ObeliskSiteTemplates.SITE,
                        weathering,
                        new GuardianWatchBehavior(guardian, GUARDIAN_BUDGET, 1, GUARDIAN_RANGE, GUARDIAN_SPAWN_RADIUS),
                        awakening,
                        NodeType.DARK,
                        labyrinths));
        context.register(
                ObeliskSite.DORMANT,
                new ObeliskSite(
                        ObeliskSiteTemplates.SITE,
                        weathering,
                        DormantBehavior.INSTANCE,
                        awakening,
                        NodeType.DARK,
                        labyrinths));
    }

    private static ResourceKey<ObeliskSite> key(String path) {
        return ResourceKey.create(ObeliskSite.REGISTRY_KEY, TTIds.rl(path));
    }
}
