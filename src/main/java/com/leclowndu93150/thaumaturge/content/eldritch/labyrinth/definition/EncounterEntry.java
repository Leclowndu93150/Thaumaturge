package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;

public record EncounterEntry(Holder<LabyrinthEncounter> encounter, int weight, Optional<HolderSet<RoomType>> halls) {
    public static final Codec<EncounterEntry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    LabyrinthEncounter.CODEC.fieldOf("encounter").forGetter(EncounterEntry::encounter),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("weight", 1).forGetter(EncounterEntry::weight),
                    RoomType.SET_CODEC.optionalFieldOf("halls").forGetter(EncounterEntry::halls))
            .apply(instance, EncounterEntry::new));
}
