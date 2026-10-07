package com.leclowndu93150.thaumaturge.data.worldgen.research;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.TTResearchCategories;
import com.leclowndu93150.thaumaturge.content.research.ResearchCategory;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;

public final class CategoryBootstrap {
    private static final ResourceLocation BACK_OVER = tex("textures/gui/gui_research_back_over.png");

    private CategoryBootstrap() {}

    public static void bootstrap(BootstrapContext<IResearchCategory> ctx) {
        HolderGetter<IAspect> aspects = ctx.lookup(IAspect.REGISTRY_KEY);

        register(
                ctx,
                TTResearchCategories.BASICS,
                Optional.empty(),
                AspectList.ofEntries(List.of(
                        e(aspects, TTAspects.HERBA, 5),
                        e(aspects, TTAspects.ORDO, 5),
                        e(aspects, TTAspects.PERDITIO, 5),
                        e(aspects, TTAspects.AER, 5),
                        e(aspects, TTAspects.IGNIS, 5),
                        e(aspects, TTAspects.TERRA, 3),
                        e(aspects, TTAspects.AQUA, 5))),
                tex("textures/item/thaumonomicon_cheat.png"),
                tex("textures/gui/gui_research_back_1.png"),
                0);

        register(
                ctx,
                TTResearchCategories.AUROMANCY,
                Optional.of(unlock("auromancy")),
                AspectList.ofEntries(List.of(
                        e(aspects, TTAspects.AURAM, 20),
                        e(aspects, TTAspects.PRAECANTATIO, 20),
                        e(aspects, TTAspects.VITIUM, 15),
                        e(aspects, TTAspects.VITREUS, 5),
                        e(aspects, TTAspects.GELUM, 5),
                        e(aspects, TTAspects.AER, 5))),
                tex("textures/research/cat_auromancy.png"),
                tex("textures/gui/gui_research_back_2.png"),
                1);

        register(
                ctx,
                TTResearchCategories.ALCHEMY,
                Optional.of(unlock("alchemy")),
                AspectList.ofEntries(List.of(
                        e(aspects, TTAspects.ALKIMIA, 30),
                        e(aspects, TTAspects.VITIUM, 10),
                        e(aspects, TTAspects.PRAECANTATIO, 10),
                        e(aspects, TTAspects.VICTUS, 5),
                        e(aspects, TTAspects.AVERSIO, 5),
                        e(aspects, TTAspects.DESIDERIUM, 5),
                        e(aspects, TTAspects.AQUA, 5))),
                tex("textures/research/cat_alchemy.png"),
                tex("textures/gui/gui_research_back_3.png"),
                2);

        register(
                ctx,
                TTResearchCategories.ARTIFICE,
                Optional.of(unlock("artifice")),
                AspectList.ofEntries(List.of(
                        e(aspects, TTAspects.MACHINA, 10),
                        e(aspects, TTAspects.FABRICO, 10),
                        e(aspects, TTAspects.METALLUM, 10),
                        e(aspects, TTAspects.INSTRUMENTUM, 10),
                        e(aspects, TTAspects.POTENTIA, 10),
                        e(aspects, TTAspects.LUX, 5),
                        e(aspects, TTAspects.VOLATUS, 5),
                        e(aspects, TTAspects.VINCULUM, 5),
                        e(aspects, TTAspects.IGNIS, 5))),
                tex("textures/research/cat_artifice.png"),
                tex("textures/gui/gui_research_back_4.png"),
                3);

        register(
                ctx,
                TTResearchCategories.INFUSION,
                Optional.of(unlock("infusion")),
                AspectList.ofEntries(List.of(
                        e(aspects, TTAspects.PRAECANTATIO, 30),
                        e(aspects, TTAspects.PRAEMUNIO, 10),
                        e(aspects, TTAspects.INSTRUMENTUM, 10),
                        e(aspects, TTAspects.VITIUM, 5),
                        e(aspects, TTAspects.FABRICO, 5),
                        e(aspects, TTAspects.SPIRITUS, 5),
                        e(aspects, TTAspects.TERRA, 3))),
                tex("textures/research/cat_infusion.png"),
                tex("textures/gui/gui_research_back_7.png"),
                4);

        register(
                ctx,
                TTResearchCategories.GOLEMANCY,
                Optional.of(unlock("golemancy")),
                AspectList.ofEntries(List.of(
                        e(aspects, TTAspects.HUMANUS, 20),
                        e(aspects, TTAspects.MOTUS, 10),
                        e(aspects, TTAspects.COGNITIO, 10),
                        e(aspects, TTAspects.MACHINA, 10),
                        e(aspects, TTAspects.PERMUTATIO, 5),
                        e(aspects, TTAspects.SENSUS, 5),
                        e(aspects, TTAspects.BESTIA, 5),
                        e(aspects, TTAspects.ORDO, 5))),
                tex("textures/research/cat_golemancy.png"),
                tex("textures/gui/gui_research_back_5.png"),
                5);

        register(
                ctx,
                TTResearchCategories.ELDRITCH,
                Optional.of(unlock("eldritch")),
                AspectList.ofEntries(List.of(
                        e(aspects, TTAspects.ALIENIS, 20),
                        e(aspects, TTAspects.TENEBRAE, 10),
                        e(aspects, TTAspects.PRAECANTATIO, 5),
                        e(aspects, TTAspects.COGNITIO, 5),
                        e(aspects, TTAspects.VACUOS, 5),
                        e(aspects, TTAspects.MORTUUS, 5),
                        e(aspects, TTAspects.EXANIMIS, 5),
                        e(aspects, TTAspects.PERDITIO, 5))),
                tex("textures/research/cat_eldritch.png"),
                tex("textures/gui/gui_research_back_6.png"),
                6);
    }

    private static AspectInstance e(HolderGetter<IAspect> aspects, ResourceKey<IAspect> key, int amount) {
        return new AspectInstance(aspects.getOrThrow(key), amount);
    }

    private static void register(
            BootstrapContext<IResearchCategory> ctx,
            ResourceKey<IResearchCategory> key,
            Optional<ResourceLocation> requiredResearch,
            AspectList formula,
            ResourceLocation icon,
            ResourceLocation background,
            int index) {
        ctx.register(
                key, new ResearchCategory(requiredResearch, formula, icon, background, Optional.of(BACK_OVER), index));
    }

    private static ResourceLocation tex(String path) {
        return ResourceLocation.fromNamespaceAndPath(TTIds.MODID, path);
    }

    private static ResourceLocation unlock(String which) {
        return ResourceLocation.fromNamespaceAndPath(TTIds.MODID, "unlock_" + which);
    }
}
