package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerStampContext;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthMarkers;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;

public record SpawnerMarker(EntityType<?> entity) implements LabyrinthMarker {
    public static final MapCodec<SpawnerMarker> CODEC = BuiltInRegistries.ENTITY_TYPE
            .byNameCodec()
            .fieldOf("entity")
            .xmap(SpawnerMarker::new, SpawnerMarker::entity);

    @Override
    public LabyrinthMarkerType<?> type() {
        return TTLabyrinthMarkers.SPAWNER.get();
    }

    @Override
    public MarkerPhase phase() {
        return MarkerPhase.STAMP;
    }

    @Override
    public void stamp(MarkerStampContext context, BlockPos pos) {
        context.placeEntity(pos, Blocks.SPAWNER.defaultBlockState(), BlockEntityType.MOB_SPAWNER)
                .ifPresent(spawner -> spawner.setEntityId(entity, context.random()));
    }
}
