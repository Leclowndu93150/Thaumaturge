package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterContext;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterRole;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterSettings;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounter;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounterType;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthEncounterTypes;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;

public record SwarmEncounter(EncounterSettings settings, EntityType<?> entity, int count, float extraPerParticipant)
        implements LabyrinthEncounter {
    public static final MapCodec<SwarmEncounter> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    EncounterSettings.CODEC.forGetter(SwarmEncounter::settings),
                    BuiltInRegistries.ENTITY_TYPE
                            .byNameCodec()
                            .fieldOf("entity")
                            .forGetter(SwarmEncounter::entity),
                    ExtraCodecs.POSITIVE_INT.fieldOf("count").forGetter(SwarmEncounter::count),
                    Codec.floatRange(0.0F, Float.MAX_VALUE)
                            .optionalFieldOf("extra_per_participant", 0.0F)
                            .forGetter(SwarmEncounter::extraPerParticipant))
            .apply(instance, SwarmEncounter::new));

    @Override
    public LabyrinthEncounterType<?> type() {
        return TTLabyrinthEncounterTypes.SWARM.get();
    }

    @Override
    public void begin(EncounterContext context) {
        int total = count + (int) Math.floor(extraPerParticipant * Math.max(0, context.participants() - 1));
        List<BlockPos> points = context.spawnPoints();
        for (int i = 0; i < total; i++) {
            BlockPos pos = points.get(i % points.size());
            context.spawn(entity, pos, EncounterRole.PRIMARY);
        }
    }
}
