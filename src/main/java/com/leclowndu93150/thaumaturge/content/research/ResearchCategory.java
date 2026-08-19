package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ExtraCodecs;

public record ResearchCategory(Optional<Identifier> requiredResearch, AspectList formula, Identifier icon, Identifier background, Optional<Identifier> overlayBackground,
        int index) implements IResearchCategory {
    public static final Codec<ResearchCategory> DIRECT_CODEC = RecordCodecBuilder.create(instance -> instance
            .group(Identifier.CODEC.optionalFieldOf("required_research").forGetter(ResearchCategory::requiredResearch), AspectList.CODEC.fieldOf("formula").forGetter(ResearchCategory::formula),
                    Identifier.CODEC.fieldOf("icon").forGetter(ResearchCategory::icon), Identifier.CODEC.fieldOf("background").forGetter(ResearchCategory::background),
                    Identifier.CODEC.optionalFieldOf("overlay_background").forGetter(ResearchCategory::overlayBackground),
                    ExtraCodecs.NON_NEGATIVE_INT.optionalFieldOf("index", 0).forGetter(ResearchCategory::index))
            .apply(instance, ResearchCategory::new));

    public static final Codec<IResearchCategory> CODEC = DIRECT_CODEC.xmap(c -> (IResearchCategory) c, ResearchCategory::ofInterface);

    private static ResearchCategory ofInterface(IResearchCategory category) {
        if (category instanceof ResearchCategory concrete) {
            return concrete;
        }
        return new ResearchCategory(category.requiredResearch(), category.formula(), category.icon(), category.background(), category.overlayBackground(), category.index());
    }
}
