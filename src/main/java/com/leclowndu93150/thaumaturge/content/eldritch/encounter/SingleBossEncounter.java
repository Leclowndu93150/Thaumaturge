package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterContext;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterRole;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterSettings;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounterType;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthEncounterTypes;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;

public record SingleBossEncounter(EncounterSettings settings, EntityType<?> entity) implements LabyrinthEncounter {
    public static final MapCodec<SingleBossEncounter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EncounterSettings.CODEC.forGetter(SingleBossEncounter::settings),
                    BuiltInRegistries.ENTITY_TYPE
                            .byNameCodec()
                            .fieldOf("entity")
                            .forGetter(SingleBossEncounter::entity))
            .apply(instance, SingleBossEncounter::new));

    @Override
    public LabyrinthEncounterType<?> type() {
        return TTLabyrinthEncounterTypes.SINGLE_BOSS.get();
    }

    @Override
    public void begin(EncounterContext context) {
        context.spawn(entity, context.anchor(), EncounterRole.PRIMARY);
    }
}
