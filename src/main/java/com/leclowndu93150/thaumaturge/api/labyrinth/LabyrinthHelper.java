package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;

/**
 * Static facade for Outer Lands labyrinths. The mod binds it during construction through {@link #bind}; calls made before that throw {@link IllegalStateException}.
 *
 * <p>Maze lookups and {@link #open} are server-side and must run on the server thread.
 *
 * @since 1.0.0
 */
public final class LabyrinthHelper {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("LabyrinthHelper");

    private LabyrinthHelper() {}

    /**
     * Installs the implementation. Called once by the mod.
     *
     * @param impl the implementation
     * @throws IllegalStateException when an implementation is already bound
     */
    public static void bind(Bindings impl) {
        BINDING.bind(impl);
    }

    /**
     * @return the marker type registry
     */
    public static Registry<LabyrinthMarkerType<?>> markerTypes() {
        return BINDING.get().markerTypes();
    }

    /**
     * @return the encounter type registry
     */
    public static Registry<LabyrinthEncounterType<?>> encounterTypes() {
        return BINDING.get().encounterTypes();
    }

    /**
     * @return the obelisk site behavior type registry
     */
    public static Registry<ObeliskSiteBehaviorType<?>> siteBehaviorTypes() {
        return BINDING.get().siteBehaviorTypes();
    }

    /**
     * Finds the maze containing a position.
     *
     * @param level the Outer Lands level
     * @param pos   a world position
     * @return the maze, or empty when {@code level} is not the Outer Lands or no maze covers {@code pos}
     */
    public static Optional<LabyrinthView> find(ServerLevel level, BlockPos pos) {
        return BINDING.get().find(level, pos);
    }

    /**
     * @param server the server
     * @param id     a maze id
     * @return the maze, or empty when the id is unknown or retired
     */
    public static Optional<LabyrinthView> byId(MinecraftServer server, MazeId id) {
        return BINDING.get().byId(server, id);
    }

    /**
     * Creates a new maze. Layout runs immediately on the calling thread; chunks generate later as players approach.
     *
     * @param server     the server
     * @param origin     the altar the maze belongs to; returns lead back here
     * @param definition the labyrinth definition id, or empty for the server default
     * @return the new maze, or empty when the definition is unknown or the server is at its maze limit
     */
    public static Optional<LabyrinthView> open(
            MinecraftServer server, GlobalPos origin, Optional<ResourceLocation> definition) {
        return BINDING.get().open(server, origin, definition);
    }

    /**
     * @param entity an entity
     * @return true when the entity was spawned by a labyrinth encounter or guardian post
     */
    public static boolean isLabyrinthBound(Entity entity) {
        return BINDING.get().isLabyrinthBound(entity);
    }

    /**
     * Reports whether a bound entity is one of several primary encounter entities that share a single maze-wide boss bar. Bosses that draw their own
     * bar skip it for such entities so players see one bar for the whole group.
     *
     * @param entity an entity
     * @return true when the entity's health is shown on a shared encounter bar
     */
    public static boolean sharesBossBar(Entity entity) {
        return BINDING.get().sharesBossBar(entity);
    }

    /**
     * Implementation hook supplied by Thaumaturge at mod init. Each method on this interface corresponds to a public static on {@link LabyrinthHelper}. Addons must not
     * implement this interface.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        Registry<LabyrinthMarkerType<?>> markerTypes();

        Registry<LabyrinthEncounterType<?>> encounterTypes();

        Registry<ObeliskSiteBehaviorType<?>> siteBehaviorTypes();

        Optional<LabyrinthView> find(ServerLevel level, BlockPos pos);

        Optional<LabyrinthView> byId(MinecraftServer server, MazeId id);

        Optional<LabyrinthView> open(MinecraftServer server, GlobalPos origin, Optional<ResourceLocation> definition);

        boolean isLabyrinthBound(Entity entity);

        boolean sharesBossBar(Entity entity);
    }
}
