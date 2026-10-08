package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition;

import com.leclowndu93150.thaumaturge.TTIds;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;

public record LabyrinthDefinition(
        IntProvider size,
        LayoutSettings layout,
        RoomPools rooms,
        List<EncounterEntry> encounters,
        Optional<Holder<StructureProcessorList>> palette,
        Optional<Holder<StructureProcessorList>> decoration,
        GuardianTable guardians,
        LabyrinthGameplay gameplay) {
    public static final ResourceKey<Registry<LabyrinthDefinition>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("labyrinth"));
    public static final Codec<LabyrinthDefinition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    IntProvider.POSITIVE_CODEC.fieldOf("size").forGetter(LabyrinthDefinition::size),
                    LayoutSettings.CODEC
                            .optionalFieldOf("layout", LayoutSettings.DEFAULT)
                            .forGetter(LabyrinthDefinition::layout),
                    RoomPools.CODEC.fieldOf("rooms").forGetter(LabyrinthDefinition::rooms),
                    EncounterEntry.CODEC.listOf().fieldOf("encounters").forGetter(LabyrinthDefinition::encounters),
                    StructureProcessorType.LIST_CODEC
                            .optionalFieldOf("palette")
                            .forGetter(LabyrinthDefinition::palette),
                    StructureProcessorType.LIST_CODEC
                            .optionalFieldOf("decoration")
                            .forGetter(LabyrinthDefinition::decoration),
                    GuardianTable.CODEC.fieldOf("guardians").forGetter(LabyrinthDefinition::guardians),
                    LabyrinthGameplay.CODEC.fieldOf("gameplay").forGetter(LabyrinthDefinition::gameplay))
            .apply(instance, LabyrinthDefinition::new));
}
