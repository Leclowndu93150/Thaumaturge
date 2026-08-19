package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.KnowledgeReward;
import com.leclowndu93150.thaumaturge.api.research.ResearchConstruct;
import com.leclowndu93150.thaumaturge.api.research.ResearchRequirement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.Identifier;

public record ResearchStage(String textKey, List<Identifier> recipes, Optional<ResearchConstruct> construct, List<ResearchRequirement> obtain, List<ResearchRequirement> craft,
        List<KnowledgeReward> knowledge, List<KnowledgeReward> requiredKnowledge, List<Identifier> requiredResearch, int warp) implements IResearchStage {
    public static final Codec<ResearchStage> CODEC = RecordCodecBuilder.create(instance -> instance.group(Codec.STRING.fieldOf("text").forGetter(ResearchStage::textKey),
            Identifier.CODEC.listOf().optionalFieldOf("recipes", List.of()).forGetter(ResearchStage::recipes), ResearchConstruct.CODEC.optionalFieldOf("construct").forGetter(ResearchStage::construct),
            ResearchRequirement.CODEC.listOf().optionalFieldOf("obtain", List.of()).forGetter(ResearchStage::obtain),
            ResearchRequirement.CODEC.listOf().optionalFieldOf("craft", List.of()).forGetter(ResearchStage::craft),
            KnowledgeReward.CODEC.listOf().optionalFieldOf("knowledge", List.of()).forGetter(ResearchStage::knowledge),
            KnowledgeReward.CODEC.listOf().optionalFieldOf("required_knowledge", List.of()).forGetter(ResearchStage::requiredKnowledge),
            Identifier.CODEC.listOf().optionalFieldOf("required_research", List.of()).forGetter(ResearchStage::requiredResearch), Codec.INT.optionalFieldOf("warp", 0).forGetter(ResearchStage::warp))
            .apply(instance, ResearchStage::new));
}
