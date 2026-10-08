package com.leclowndu93150.thaumaturge.content.eldritch.encounter;

import com.leclowndu93150.thaumaturge.api.labyrinth.EncounterRole;
import com.leclowndu93150.thaumaturge.api.labyrinth.MazeId;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.Optional;
import net.minecraft.world.entity.Entity;

public record LabyrinthBinding(int maze, EncounterRole role, boolean sharedBar) {
    public static final LabyrinthBinding NONE = new LabyrinthBinding(-1, EncounterRole.MINION, false);
    public static final MapCodec<LabyrinthBinding> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.INT.fieldOf("maze").forGetter(LabyrinthBinding::maze),
                    EncounterRole.CODEC.fieldOf("role").forGetter(LabyrinthBinding::role),
                    Codec.BOOL.optionalFieldOf("shared_bar", false).forGetter(LabyrinthBinding::sharedBar))
            .apply(instance, LabyrinthBinding::new));

    public static Optional<LabyrinthBinding> on(Entity entity) {
        return entity.getExistingData(TTAttachments.LABYRINTH_BINDING).filter(LabyrinthBinding::bound);
    }

    public static LabyrinthBinding of(MazeId maze, EncounterRole role) {
        return new LabyrinthBinding(maze.value(), role, false);
    }

    public boolean bound() {
        return maze >= 0;
    }

    public MazeId mazeId() {
        return new MazeId(maze);
    }

    LabyrinthBinding withSharedBar() {
        return new LabyrinthBinding(maze, role, true);
    }
}
