package com.leclowndu93150.thaumaturge.api.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.resources.ResourceLocation;

/**
 * Standard landmark ids recorded from {@link MarkerPhase#LANDMARK} markers. Addons may record their own ids next to these.
 *
 * @since 1.0.0
 */
public final class LabyrinthLandmarks {
    /**
     * Where players appear when they enter the maze.
     */
    public static final ResourceLocation ARRIVAL = TTIds.rl("arrival");
    /**
     * The portal inside the maze that leads back to the origin.
     */
    public static final ResourceLocation ENTRY_PORTAL = TTIds.rl("entry_portal");
    /**
     * The key room's reliquary.
     */
    public static final ResourceLocation KEY = TTIds.rl("key");
    /**
     * The centre of the boss hall floor.
     */
    public static final ResourceLocation BOSS_CENTER = TTIds.rl("boss_center");
    /**
     * The lock beside the boss hall door.
     */
    public static final ResourceLocation BOSS_DOOR = TTIds.rl("boss_door");
    /**
     * Where the exit rift opens once the encounter is beaten.
     */
    public static final ResourceLocation EXIT = TTIds.rl("exit");
    /**
     * Where the boss reliquary appears once the encounter is beaten.
     */
    public static final ResourceLocation REWARD = TTIds.rl("reward");
    /**
     * Path prefix for encounter spawn points in a boss hall. Landmarks named {@code <namespace>:spawn/<n>} are handed to encounters through
     * {@link EncounterContext#spawnPoints()} in id order.
     */
    public static final String SPAWN_PREFIX = "spawn/";

    private LabyrinthLandmarks() {}
}
