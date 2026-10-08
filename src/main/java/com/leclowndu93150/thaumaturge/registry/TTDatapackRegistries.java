package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanEntry;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.content.aspect.Aspect;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import com.leclowndu93150.thaumaturge.content.eldritch.site.ObeliskSite;
import com.leclowndu93150.thaumaturge.content.pech.PechTradeTable;
import com.leclowndu93150.thaumaturge.content.research.ResearchCategory;
import com.leclowndu93150.thaumaturge.content.research.ResearchEntry;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.DataPackRegistryEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TTDatapackRegistries {
    private TTDatapackRegistries() {}

    @SubscribeEvent
    public static void onRegister(DataPackRegistryEvent.NewRegistry event) {
        event.dataPackRegistry(IAspect.REGISTRY_KEY, Aspect.CODEC, Aspect.CODEC);
        event.dataPackRegistry(Blueprint.REGISTRY_KEY, Blueprint.CODEC, Blueprint.CODEC);
        event.dataPackRegistry(IResearchCategory.REGISTRY_KEY, ResearchCategory.CODEC, ResearchCategory.CODEC);
        event.dataPackRegistry(IResearchEntry.REGISTRY_KEY, ResearchEntry.CODEC, ResearchEntry.CODEC);
        event.dataPackRegistry(ScanEntry.REGISTRY_KEY, ScanEntry.CODEC, ScanEntry.CODEC);
        event.dataPackRegistry(PechTradeTable.REGISTRY_KEY, PechTradeTable.CODEC);
        event.dataPackRegistry(LabyrinthEncounter.REGISTRY_KEY, LabyrinthEncounter.DIRECT_CODEC);
        event.dataPackRegistry(RoomType.REGISTRY_KEY, RoomType.CODEC);
        event.dataPackRegistry(LabyrinthDefinition.REGISTRY_KEY, LabyrinthDefinition.CODEC);
        event.dataPackRegistry(ObeliskSite.REGISTRY_KEY, ObeliskSite.CODEC);
        event.dataPackRegistry(SpellPart.REGISTRY_KEY, SpellPart.CODEC, SpellPart.CODEC);
        event.dataPackRegistry(AspectAffinity.REGISTRY_KEY, AspectAffinity.CODEC, AspectAffinity.CODEC);
    }
}
