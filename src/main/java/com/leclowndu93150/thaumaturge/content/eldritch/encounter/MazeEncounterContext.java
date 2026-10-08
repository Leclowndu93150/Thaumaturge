package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterContext;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterRole;
import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterSettings;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.MazeRecord;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

record MazeEncounterContext(ServerLevel level, MazeRecord record, EncounterSettings settings, int participants)
        implements EncounterContext {
    @Override
    public RandomSource random() {
        return level.getRandom();
    }

    @Override
    public MazeId maze() {
        return record.plan().id();
    }

    @Override
    public BlockPos anchor() {
        return EncounterGeometry.anchor(record);
    }

    @Override
    public List<BlockPos> spawnPoints() {
        return EncounterGeometry.spawnPointsOrAnchor(record);
    }

    @Override
    public BoundingBox arena() {
        return record.plan().landmarks().bossHall();
    }

    @Override
    public Optional<Entity> spawn(EntityType<?> type, BlockPos pos, EncounterRole role) {
        return EncounterSpawner.spawn(level, record, type, pos, role, settings.scaling(), participants)
                .map(Entity.class::cast);
    }

    @Override
    public void announce(Component message) {
        LabyrinthAnnouncer.actionBar(level, record, message);
    }

    @Override
    public long age() {
        return level.getGameTime() - record.state().phaseSince();
    }
}
