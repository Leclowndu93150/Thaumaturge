package com.leclowndu93150.thaumaturge.api.items;

import java.util.function.Predicate;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Static accessor for revealing gear and vis discounts.
 *
 * <p>A stack is revealing gear when it carries the {@code thaumaturge:goggles_upgrade} data component. Revealing gear worn on the head
 * or in a Curios slot shows in-game popups, the aura HUD and hidden aura nodes. Goggles of revealing and the void robe hood carry the
 * component by default and fortress helms gain it from their goggles upgrade; any other item becomes revealing gear by gaining the
 * component, including as a default component set by a datapack or KubeJS.
 *
 * <p>All queries are side-agnostic and read live equipment.
 *
 * @since 1.0.0
 */
public final class GogglesAccess {
    private static @Nullable Bindings bindings;
    private static @Nullable Curios curios;

    private GogglesAccess() {}

    /**
     * Installs the implementation. Called once by Thaumaturge during mod construction; addons must not call it.
     *
     * @param impl the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings impl) {
        if (bindings != null) {
            throw new IllegalStateException("GogglesAccess already bound");
        }
        bindings = impl;
    }

    /**
     * Installs Curios slot scanning. Called by Thaumaturge when Curios is present; addons must not call it. Without it only the head
     * slot counts.
     *
     * @param impl the Curios hook
     */
    public static void bindCurios(Curios impl) {
        curios = impl;
    }

    private static Bindings impl() {
        if (bindings == null) {
            throw new IllegalStateException("GogglesAccess accessed before binding");
        }
        return bindings;
    }

    /**
     * @param stack the stack to test
     * @return whether the stack is revealing gear; false for an empty stack
     */
    public static boolean isRevealing(ItemStack stack) {
        return !stack.isEmpty() && impl().isRevealing(stack);
    }

    /**
     * @param entity the entity to query, or null
     * @return whether the entity wears revealing gear on its head or in a Curios slot; held gear does not count
     */
    public static boolean wearsRevealingGear(@Nullable LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        return isRevealing(entity.getItemBySlot(EquipmentSlot.HEAD)) || curios != null && curios.anyCurioMatches(entity, GogglesAccess::isRevealing);
    }

    /**
     * Reads the player's total vis discount. Every item granting a discount does so through an attribute modifier on the vis discount
     * attribute, so the attribute value already sums held, worn and Curios gear.
     *
     * @param player the player to query, or null
     * @return the total discount in whole percent, never negative; 0 for a null player
     */
    public static int totalVisDiscount(@Nullable Player player) {
        if (player == null) {
            return 0;
        }
        AttributeInstance attribute = player.getAttribute(impl().visDiscount());
        return attribute == null ? 0 : (int) (attribute.getValue() * 100);
    }

    /**
     * The hooks Thaumaturge implements behind this facade.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * @return the vis discount attribute
         */
        Holder<Attribute> visDiscount();

        /**
         * @param stack a non-empty stack
         * @return whether the stack is revealing gear
         */
        boolean isRevealing(ItemStack stack);
    }

    /**
     * The Curios hook. Addons must not implement it.
     *
     * @since 1.0.0
     */
    @FunctionalInterface
    public interface Curios {
        /**
         * @param entity    the wearer
         * @param predicate the test applied to every equipped curio
         * @return whether any equipped curio passes
         */
        boolean anyCurioMatches(LivingEntity entity, Predicate<ItemStack> predicate);
    }
}
