package com.leclowndu93150.thaumaturge.api.labyrinth;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

/**
 * What an {@link ObeliskSiteBehavior} sees on each tick. Every call happens on the server thread, and state changes are saved with the altar.
 *
 * @since 1.0.0
 */
public interface SiteContext {
    /**
     * @return the level holding the site
     */
    ServerLevel level();

    /**
     * @return the altar keystone position
     */
    BlockPos altar();

    /**
     * @return the level's random source
     */
    RandomSource random();

    /**
     * @param range the distance in blocks
     * @return true when a survival or adventure player is within {@code range} of the altar
     */
    boolean playerWithin(double range);

    /**
     * @return true once the garrison has been activated
     */
    boolean activated();

    /**
     * Activates the garrison with a finite reinforcement budget. Has no effect when already activated.
     *
     * @param budget the number of reinforcements the site may still spawn
     */
    void activate(int budget);

    /**
     * @return the reinforcements the site may still spawn
     */
    int budget();

    /**
     * Spends one reinforcement.
     *
     * @return true when the budget allowed it
     */
    boolean consumeBudget();

    /**
     * Ends the garrison for good. The behavior is not ticked again.
     */
    void quell();

    /**
     * @param radius the search radius in blocks
     * @return living mobs spawned by this site within {@code radius} of the altar
     */
    List<Mob> members(double radius);

    /**
     * Spawns a persistent mob homed to the altar on a safe spot in a ring around it, and tags it as a member of this site.
     *
     * @param type      the entity type
     * @param minRadius the inner ring radius
     * @param maxRadius the outer ring radius
     * @return the mob, or empty when no safe spot was found or the type is not a mob
     */
    Optional<Mob> spawnMember(EntityType<?> type, int minRadius, int maxRadius);
}
