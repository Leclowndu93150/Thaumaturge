package com.leclowndu93150.thaumaturge.data.worldgen.research;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.TCResearchCategories;
import com.leclowndu93150.thaumaturge.content.research.ResearchCategory;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

public final class CategoryBootstrap {
    private static final Identifier BACK_OVER = tex("textures/gui/gui_research_back_over.png");

    private CategoryBootstrap() {}

    public static void bootstrap(BootstrapContext<IResearchCategory> ctx) {
        HolderGetter<IAspect> aspects = ctx.lookup(IAspect.REGISTRY_KEY);

        register(ctx, TCResearchCategories.BASICS, Optional.empty(),
                AspectList.ofEntries(List.of(e(aspects, TCAspects.HERBA, 5), e(aspects, TCAspects.ORDO, 5), e(aspects, TCAspects.PERDITIO, 5), e(aspects, TCAspects.AER, 5),
                        e(aspects, TCAspects.IGNIS, 5), e(aspects, TCAspects.TERRA, 3), e(aspects, TCAspects.AQUA, 5))),
                tex("textures/research/thaumonomicon_cheat.png"), tex("textures/gui/gui_research_back_1.png"), 0);

        register(
                ctx, TCResearchCategories.AUROMANCY, Optional.of(unlock("auromancy")), AspectList.ofEntries(List.of(e(aspects, TCAspects.AURAM, 20), e(aspects, TCAspects.PRAECANTATIO, 20),
                        e(aspects, TCAspects.VITIUM, 15), e(aspects, TCAspects.VITREUS, 5), e(aspects, TCAspects.GELUM, 5), e(aspects, TCAspects.AER, 5))),
                tex("textures/research/cat_auromancy.png"), tex("textures/gui/gui_research_back_2.png"), 1);

        register(ctx, TCResearchCategories.ALCHEMY, Optional.of(unlock("alchemy")),
                AspectList.ofEntries(List.of(e(aspects, TCAspects.ALKIMIA, 30), e(aspects, TCAspects.VITIUM, 10), e(aspects, TCAspects.PRAECANTATIO, 10), e(aspects, TCAspects.VICTUS, 5),
                        e(aspects, TCAspects.AVERSIO, 5), e(aspects, TCAspects.DESIDERIUM, 5), e(aspects, TCAspects.AQUA, 5))),
                tex("textures/research/cat_alchemy.png"), tex("textures/gui/gui_research_back_3.png"), 2);

        register(ctx, TCResearchCategories.ARTIFICE, Optional.of(unlock("artifice")),
                AspectList.ofEntries(List.of(e(aspects, TCAspects.MACHINA, 10), e(aspects, TCAspects.FABRICO, 10), e(aspects, TCAspects.METALLUM, 10), e(aspects, TCAspects.INSTRUMENTUM, 10),
                        e(aspects, TCAspects.POTENTIA, 10), e(aspects, TCAspects.LUX, 5), e(aspects, TCAspects.VOLATUS, 5), e(aspects, TCAspects.VINCULUM, 5), e(aspects, TCAspects.IGNIS, 5))),
                tex("textures/research/cat_artifice.png"), tex("textures/gui/gui_research_back_4.png"), 3);

        register(ctx, TCResearchCategories.INFUSION, Optional.of(unlock("infusion")),
                AspectList.ofEntries(List.of(e(aspects, TCAspects.PRAECANTATIO, 30), e(aspects, TCAspects.PRAEMUNIO, 10), e(aspects, TCAspects.INSTRUMENTUM, 10), e(aspects, TCAspects.VITIUM, 5),
                        e(aspects, TCAspects.FABRICO, 5), e(aspects, TCAspects.SPIRITUS, 5), e(aspects, TCAspects.TERRA, 3))),
                tex("textures/research/cat_infusion.png"), tex("textures/gui/gui_research_back_7.png"), 4);

        register(ctx, TCResearchCategories.GOLEMANCY, Optional.of(unlock("golemancy")),
                AspectList.ofEntries(List.of(e(aspects, TCAspects.HUMANUS, 20), e(aspects, TCAspects.MOTUS, 10), e(aspects, TCAspects.COGNITIO, 10), e(aspects, TCAspects.MACHINA, 10),
                        e(aspects, TCAspects.PERMUTATIO, 5), e(aspects, TCAspects.SENSUS, 5), e(aspects, TCAspects.BESTIA, 5), e(aspects, TCAspects.ORDO, 5))),
                tex("textures/research/cat_golemancy.png"), tex("textures/gui/gui_research_back_5.png"), 5);

        register(ctx, TCResearchCategories.ELDRITCH, Optional.of(unlock("eldritch")),
                AspectList.ofEntries(List.of(e(aspects, TCAspects.ALIENIS, 20), e(aspects, TCAspects.TENEBRAE, 10), e(aspects, TCAspects.PRAECANTATIO, 5), e(aspects, TCAspects.COGNITIO, 5),
                        e(aspects, TCAspects.VACUOS, 5), e(aspects, TCAspects.MORTUUS, 5), e(aspects, TCAspects.EXANIMIS, 5), e(aspects, TCAspects.PERDITIO, 5))),
                tex("textures/research/cat_eldritch.png"), tex("textures/gui/gui_research_back_6.png"), 6);
    }

    private static AspectInstance e(HolderGetter<IAspect> aspects, ResourceKey<IAspect> key, int amount) {
        return new AspectInstance(aspects.getOrThrow(key), amount);
    }

    private static void register(BootstrapContext<IResearchCategory> ctx, ResourceKey<IResearchCategory> key, Optional<Identifier> requiredResearch, AspectList formula, Identifier icon, Identifier background, int index) {
        ctx.register(key, new ResearchCategory(requiredResearch, formula, icon, background, Optional.of(BACK_OVER), index));
    }

    private static Identifier tex(String path) {
        return Identifier.fromNamespaceAndPath(TCIds.MODID, path);
    }

    private static Identifier unlock(String which) {
        return Identifier.fromNamespaceAndPath(TCIds.MODID, "unlock_" + which);
    }
}
