package com.leclowndu93150.thaumaturge.content.eldritch.site;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.ObeliskSiteBehavior;
import com.leclowndu93150.thaumaturge.api.nodes.NodeType;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.LabyrinthDefinition;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public record ObeliskSite(
        ResourceLocation template,
        Optional<Holder<StructureProcessorList>> processors,
        ObeliskSiteBehavior behavior,
        Optional<SiteAwakening> awakening,
        NodeType node,
        SimpleWeightedRandomList<ResourceKey<LabyrinthDefinition>> labyrinths) {
    public static final ResourceKey<Registry<ObeliskSite>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("obelisk_site"));
    public static final ResourceKey<ObeliskSite> DORMANT = ResourceKey.create(REGISTRY_KEY, TTIds.rl("dormant"));

    public static Optional<ObeliskSite> lookup(HolderLookup.Provider registries, ResourceKey<ObeliskSite> key) {
        return registries.lookupOrThrow(REGISTRY_KEY).get(key).map(Holder::value);
    }

    public static final Codec<ObeliskSite> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ResourceLocation.CODEC.fieldOf("template").forGetter(ObeliskSite::template),
                    StructureProcessorType.LIST_CODEC
                            .optionalFieldOf("processors")
                            .forGetter(ObeliskSite::processors),
                    ObeliskSiteBehavior.CODEC.fieldOf("behavior").forGetter(ObeliskSite::behavior),
                    SiteAwakening.CODEC.optionalFieldOf("awakening").forGetter(ObeliskSite::awakening),
                    NodeType.CODEC.optionalFieldOf("node", NodeType.DARK).forGetter(ObeliskSite::node),
                    SimpleWeightedRandomList.wrappedCodecAllowingEmpty(
                                    ResourceKey.codec(LabyrinthDefinition.REGISTRY_KEY))
                            .optionalFieldOf("labyrinths", SimpleWeightedRandomList.empty())
                            .forGetter(ObeliskSite::labyrinths))
            .apply(instance, ObeliskSite::new));
}
