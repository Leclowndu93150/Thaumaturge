package com.leclowndu93150.thaumaturge.api.golems;

import com.leclowndu93150.thaumaturge.api.golems.parts.GolemAddon;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemArm;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemHead;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import java.util.List;
import java.util.Set;
import net.minecraft.world.item.ItemStack;

/**
 * The build of a golem: its material, four parts and experience rank.
 *
 * <p>Builds are immutable values. The {@code with} methods return a new build and leave this one untouched, so a build can be
 * shared between an entity and an item data component. Apply a changed build with {@link IGolemAPI#setProperties}.
 *
 * @since 1.0.0
 */
public interface IGolemProperties {
    /**
     * @return the effective traits: the union of every part's traits, where a trait and its opposite cancel each other out
     */
    Set<GolemTrait> traits();

    /**
     * @param trait the trait to test
     * @return whether the effective traits contain it
     */
    default boolean hasTrait(GolemTrait trait) {
        return traits().contains(trait);
    }

    /**
     * @return the crafting components consumed to assemble this build, with equal stacks merged; a fresh list each call
     */
    List<ItemStack> components();

    /**
     * @return the material
     */
    GolemMaterial material();

    /**
     * @return the head
     */
    GolemHead head();

    /**
     * @return the arms
     */
    GolemArm arms();

    /**
     * @return the legs
     */
    GolemLeg legs();

    /**
     * @return the addon
     */
    GolemAddon addon();

    /**
     * @return the experience rank, from 0 to 10
     */
    int rank();

    /**
     * @param material the new material
     * @return a copy of this build with the material replaced
     */
    IGolemProperties withMaterial(GolemMaterial material);

    /**
     * @param head the new head
     * @return a copy of this build with the head replaced
     */
    IGolemProperties withHead(GolemHead head);

    /**
     * @param arms the new arms
     * @return a copy of this build with the arms replaced
     */
    IGolemProperties withArms(GolemArm arms);

    /**
     * @param legs the new legs
     * @return a copy of this build with the legs replaced
     */
    IGolemProperties withLegs(GolemLeg legs);

    /**
     * @param addon the new addon
     * @return a copy of this build with the addon replaced
     */
    IGolemProperties withAddon(GolemAddon addon);

    /**
     * @param rank the new rank, from 0 to 10
     * @return a copy of this build with the rank replaced
     */
    IGolemProperties withRank(int rank);
}
