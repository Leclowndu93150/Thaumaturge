package com.leclowndu93150.thaumaturge.api.golems.seals;

import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * A seal placed on a block face. Owns the runtime state of its type's parts: filter, settings, work area and behaviour.
 *
 * <p>Placed seals live on the server, saved with their chunk, and are mirrored on clients for rendering and the configuration
 * screen. Setters only change memory; call {@link #markChanged} to save the chunk and update clients.
 *
 * @since 1.0.0
 */
public interface ISealEntity {
    /**
     * @return where the seal sits
     */
    SealPos pos();

    /**
     * @return the seal type
     */
    SealType type();

    /**
     * @return the behaviour instance owned by this placement
     */
    ISealBehavior behavior();

    /**
     * @return the item filter, present when the type declares one
     */
    Optional<ISealFilter> filter();

    /**
     * @param setting a setting declared by this seal's type
     * @return the stored value, or the setting's fallback when the type does not declare it
     */
    boolean setting(SealSetting setting);

    /**
     * Changes a setting. Ignored for settings the type does not declare.
     *
     * @param setting the setting
     * @param value   the new value
     */
    void setSetting(SealSetting setting, boolean value);

    /**
     * @return the priority, from -5 to 5
     */
    byte priority();

    /**
     * @param priority the priority, from -5 to 5
     */
    void setPriority(byte priority);

    /**
     * @return the golem colour this seal serves, from 1 to 16, or 0 for every golem
     */
    byte color();

    /**
     * @param color the golem colour this seal serves, or 0 for every golem
     */
    void setColor(byte color);

    /**
     * @return the work area size along each axis, in blocks; the axis the seal faces counts depth, the others count a radius
     */
    BlockPos area();

    /**
     * @param area the work area size, each axis from 1 to 8
     */
    void setArea(BlockPos area);

    /**
     * @return whether only golems owned by the seal's owner may take its tasks
     */
    boolean isLocked();

    /**
     * @param locked whether only the owner's golems may take the seal's tasks
     */
    void setLocked(boolean locked);

    /**
     * @return whether a redstone signal pauses the seal
     */
    boolean isRedstoneControlled();

    /**
     * @param controlled whether a redstone signal pauses the seal
     */
    void setRedstoneControlled(boolean controlled);

    /**
     * @return the UUID of the player who placed the seal, or null
     */
    @Nullable
    UUID owner();

    /**
     * @param owner the owning player's UUID, or null
     */
    void setOwner(@Nullable UUID owner);

    /**
     * @param level the level
     * @return whether redstone control is on and the seal's block or the block in front of it is powered
     */
    boolean isStoppedByRedstone(Level level);

    /**
     * Saves the change with the chunk and sends the seal to every player in the dimension. Does nothing on the client.
     *
     * @param level the level
     */
    void markChanged(Level level);
}
