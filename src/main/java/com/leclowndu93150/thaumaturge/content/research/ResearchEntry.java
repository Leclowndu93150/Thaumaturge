package com.leclowndu93150.thaumaturge.content.research;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.research.IResearchCategory;
import com.leclowndu93150.thaumaturge.api.research.IResearchEntry;
import com.leclowndu93150.thaumaturge.api.research.IResearchStage;
import com.leclowndu93150.thaumaturge.api.research.ResearchAddendum;
import com.leclowndu93150.thaumaturge.api.research.ResearchEntryMeta;
import com.leclowndu93150.thaumaturge.api.research.ResearchIcon;
import com.leclowndu93150.thaumaturge.api.research.ResearchParent;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryFixedCodec;

public record ResearchEntry(Holder<IResearchCategory> category, String nameKey, Set<ResearchParent> parents, Set<Identifier> siblings, int column, int row, List<IResearchStage> stages,
        Set<ResearchEntryMeta> meta, List<ResearchIcon> icons, List<ResearchAddendum> addenda, AspectList noteAspects, int complexity) implements IResearchEntry {
    public static final Codec<ResearchEntry> DIRECT_CODEC = RecordCodecBuilder
            .<ResearchEntry>create(instance -> instance.group(RegistryFixedCodec.create(IResearchCategory.REGISTRY_KEY).fieldOf("category").forGetter(ResearchEntry::category),
                    Codec.STRING.fieldOf("name").forGetter(ResearchEntry::nameKey), ResearchParent.CODEC.listOf().optionalFieldOf("parents", List.of()).forGetter(e -> List.copyOf(e.parents())),
                    Identifier.CODEC.listOf().optionalFieldOf("siblings", List.of()).forGetter(e -> List.copyOf(e.siblings())), Codec.INT.fieldOf("column").forGetter(ResearchEntry::column),
                    Codec.INT.fieldOf("row").forGetter(ResearchEntry::row), ResearchStage.CODEC.listOf().fieldOf("stages").forGetter(e -> e.stages().stream().map(s -> (ResearchStage) s).toList()),
                    ResearchEntryMeta.CODEC.listOf().optionalFieldOf("meta", List.of()).forGetter(e -> List.copyOf(e.meta())),
                    ResearchIcon.CODEC.listOf().optionalFieldOf("icons", List.of()).forGetter(ResearchEntry::icons),
                    ResearchAddendum.CODEC.listOf().optionalFieldOf("addenda", List.of()).forGetter(ResearchEntry::addenda),
                    AspectList.CODEC.optionalFieldOf("note_aspects", AspectList.EMPTY).forGetter(ResearchEntry::noteAspects),
                    Codec.intRange(1, 3).optionalFieldOf("complexity", 1).forGetter(ResearchEntry::complexity)).apply(instance, ResearchEntry::create))
            .validate(ResearchEntry::validate);

    public static final Codec<IResearchEntry> CODEC = DIRECT_CODEC.xmap(e -> (IResearchEntry) e, ResearchEntry::ofInterface);

    private static ResearchEntry create(Holder<IResearchCategory> category, String nameKey, List<ResearchParent> parents, List<Identifier> siblings, int column, int row, List<ResearchStage> stages, List<ResearchEntryMeta> meta, List<ResearchIcon> icons, List<ResearchAddendum> addenda, AspectList noteAspects, int complexity) {
        return new ResearchEntry(category, nameKey, new LinkedHashSet<>(parents), new LinkedHashSet<>(siblings), column, row, List.copyOf(stages),
                meta.isEmpty() ? EnumSet.noneOf(ResearchEntryMeta.class) : EnumSet.copyOf(meta), List.copyOf(icons), List.copyOf(addenda), noteAspects, complexity);
    }

    private static DataResult<ResearchEntry> validate(ResearchEntry entry) {
        if (entry.stages.isEmpty()) {
            return DataResult.error(() -> "Research entry '" + entry.nameKey + "' must declare at least one stage");
        }
        return DataResult.success(entry);
    }

    private static ResearchEntry ofInterface(IResearchEntry entry) {
        if (entry instanceof ResearchEntry concrete) {
            return concrete;
        }
        return new ResearchEntry(entry.category(), entry.nameKey(), entry.parents(), entry.siblings(), entry.column(), entry.row(), entry.stages(), entry.meta(), entry.icons(), entry.addenda(),
                entry.noteAspects(), entry.complexity());
    }
}
