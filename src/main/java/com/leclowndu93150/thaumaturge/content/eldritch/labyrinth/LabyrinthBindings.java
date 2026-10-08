package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthEncounterType;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthHelper;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthMarkerType;
import com.leclowndu93150.thaumaturge.api.labyrinth.LabyrinthView;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.api.labyrinth.ObeliskSiteBehaviorType;
import com.leclowndu93150.thaumaturge.content.eldritch.encounter.LabyrinthBinding;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthEncounterTypes;
import com.leclowndu93150.thaumaturge.registry.TTLabyrinthMarkers;
import com.leclowndu93150.thaumaturge.registry.TTObeliskSiteBehaviors;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

public final class LabyrinthBindings implements LabyrinthHelper.Bindings {
    @Override
    public Registry<LabyrinthMarkerType<?>> markerTypes() {
        return TTLabyrinthMarkers.registry();
    }

    @Override
    public Registry<LabyrinthEncounterType<?>> encounterTypes() {
        return TTLabyrinthEncounterTypes.registry();
    }

    @Override
    public Registry<ObeliskSiteBehaviorType<?>> siteBehaviorTypes() {
        return TTObeliskSiteBehaviors.registry();
    }

    @Override
    public Optional<LabyrinthView> find(ServerLevel level, BlockPos pos) {
        return LabyrinthService.find(level, pos).map(MazeView::new);
    }

    @Override
    public Optional<LabyrinthView> byId(MinecraftServer server, MazeId id) {
        return LabyrinthService.byId(server, id).map(MazeView::new);
    }

    @Override
    public Optional<LabyrinthView> open(
            MinecraftServer server, GlobalPos origin, Optional<ResourceLocation> definition) {
        return LabyrinthService.open(server, origin, definition).map(MazeView::new);
    }

    @Override
    public boolean isLabyrinthBound(Entity entity) {
        return LabyrinthBinding.on(entity).isPresent();
    }

    @Override
    public boolean sharesBossBar(Entity entity) {
        return LabyrinthBinding.on(entity).filter(LabyrinthBinding::sharedBar).isPresent();
    }
}
