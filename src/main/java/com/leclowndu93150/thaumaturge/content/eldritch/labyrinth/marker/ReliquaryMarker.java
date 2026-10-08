package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.marker;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarker;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerPhase;
import com.leclowndu93150.thaumaturge.api.labyrinth.MarkerStampContext;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomSocket;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryPlacement;
import com.leclowndu93150.thaumaturge.content.eldritch.reliquary.ReliquaryRole;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthMarkers;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

public record ReliquaryMarker(Direction facing, Optional<ResourceKey<LootTable>> loot) implements LabyrinthMarker {
    public static final MapCodec<ReliquaryMarker> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    RoomSocket.HORIZONTAL
                            .optionalFieldOf("facing", Direction.NORTH)
                            .forGetter(ReliquaryMarker::facing),
                    ResourceKey.codec(Registries.LOOT_TABLE)
                            .optionalFieldOf("loot")
                            .forGetter(ReliquaryMarker::loot))
            .apply(instance, ReliquaryMarker::new));

    @Override
    public LabyrinthMarkerType<?> type() {
        return TTLabyrinthMarkers.RELIQUARY.get();
    }

    @Override
    public MarkerPhase phase() {
        return MarkerPhase.STAMP;
    }

    @Override
    public void stamp(MarkerStampContext context, BlockPos pos) {
        ReliquaryPlacement.stamp(context, pos, facing, ReliquaryRole.CACHE, loot);
    }
}
