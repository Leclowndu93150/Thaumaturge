package com.leclowndu93150.thaumaturge.api.golems;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The surface a golem entity exposes to seals, tasks and part functions.
 *
 * @since 1.0.0
 */
public interface IGolemAPI {
    /**
     * @return the golem as a living entity
     */
    LivingEntity getGolemEntity();

    /**
     * @return the golem's current composition
     */
    IGolemProperties getProperties();

    /**
     * Replaces the golem's composition and refreshes derived attributes.
     *
     * @param properties the new composition
     */
    void setProperties(IGolemProperties properties);

    /**
     * @return the level the golem lives in
     */
    Level getGolemWorld();

    /**
     * Attempts to store a stack in the golem's carry slots.
     *
     * @param stack the stack to store; may be partially consumed
     * @return the remainder that did not fit
     */
    ItemStack holdItem(ItemStack stack);

    /**
     * Removes carried items.
     *
     * @param stack the stack to match and count against, or an empty stack to remove any
     * @return the removed items
     */
    ItemStack dropItem(ItemStack stack);

    /**
     * @param stack   the stack to test
     * @param partial whether carrying only part of the stack counts
     * @return whether the golem has room for the stack
     */
    boolean canCarry(ItemStack stack, boolean partial);

    /**
     * @param stack the stack to test
     * @return how many items of the stack the golem could still carry
     */
    int canCarryAmount(ItemStack stack);

    /**
     * @param stack the stack to match
     * @return whether the golem currently carries a matching stack
     */
    boolean isCarrying(ItemStack stack);

    /**
     * @return the golem's carry slots
     */
    List<ItemStack> getCarrying();

    /**
     * Awards rank experience. Only golems with the {@link GolemTrait#SMART} trait accumulate it.
     *
     * @param xp the experience amount
     */
    void addRankXp(int xp);

    /**
     * @return the golem's assigned dye color index, or 0 when uncolored
     */
    byte getGolemColor();

    /**
     * Plays the golem's arm swing animation and syncs it to watchers.
     */
    void swingArm();

    /**
     * @return whether the golem currently has an attack target
     */
    boolean isInCombat();

    /** Stable owner identity, including while the owner is offline. */
    default Optional<UUID> ownerIdentity() {
        return Optional.empty();
    }

    /** Whether behavior should treat the golem as disabled, dead, or otherwise inactive. */
    default boolean isInactive() {
        return false;
    }

    /** Defensive bounded client-visible state for one accessory namespace. */
    default CompoundTag accessorySynchronizedData(ResourceLocation accessoryId) {
        return new CompoundTag();
    }
}
