package com.leclowndu93150.thaumaturge.api.items;

import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Static accessor for detecting goggles, revealers, and vis-discount gear worn by an entity.
 *
 * <p>Side-agnostic: the head slot lookup uses {@link LivingEntity#getItemBySlot(EquipmentSlot)},
 * which works identically on client and server.
 *
 * @since 1.0.0
 */
public final class GogglesAccess {
    private static Supplier<Holder<Attribute>> visDiscountBinding;
    private static Curios curios;

    /**
     * Binds the vis discount attribute. Called by Thaumaturge during mod init; addons must not
     * call this.
     *
     * @param impl supplies the vis discount attribute holder
     * @throws IllegalStateException when already bound
     */
    public static void bind(Supplier<Holder<Attribute>> impl) {
        if (visDiscountBinding != null) {
            throw new IllegalStateException("GogglesAccess already bound");
        }
        visDiscountBinding = impl;
    }

    /**
     * Binds curios slot scanning. Called by Thaumaturge when Curios is present; addons must
     * not call this. When never bound, only vanilla equipment slots are considered.
     *
     * @param impl the curios hook
     */
    public static void bindCurios(Curios impl) {
        curios = impl;
    }

    private static Holder<Attribute> visDiscountAttribute() {
        if (visDiscountBinding == null) {
            throw new IllegalStateException("GogglesAccess accessed before binding");
        }
        return visDiscountBinding.get();
    }

    /**
     * Curios integration hook. Addons must not implement this interface.
     *
     * @since 1.0.0
     */
    public interface Curios {
        boolean wearsGoggles(LivingEntity entity);

        boolean anyCurioMatches(LivingEntity entity, Predicate<ItemStack> predicate);
    }

    private GogglesAccess() {}

    /**
     * Returns whether the entity wears a head-slot stack implementing {@link IGoggles} with
     * popups enabled.
     *
     * @param entity the entity to query; null returns {@code false}
     * @return {@code true} when the wearer should see in-game popups and HUD overlays
     */
    public static boolean wearsGoggles(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        if (curios != null && curios.wearsGoggles(entity)) {
            return true;
        }
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (head.isEmpty()) {
            return false;
        }
        if (head.getItem() instanceof IGoggles g) {
            return g.showIngamePopups(head, entity);
        }
        return false;
    }

    /**
     * Returns whether the entity wears a stack implementing {@link IRevealer} that reveals
     * aura nodes. Held revealers do not count; the gear must occupy the head slot or an
     * equipped curio slot.
     *
     * @param entity the entity to query; null returns {@code false}
     * @return {@code true} when nodes should be revealed for the entity
     */
    public static boolean revealsNodes(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        ItemStack head = entity.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.isEmpty() && head.getItem() instanceof IRevealer r && r.showNodes(head, entity)) {
            return true;
        }
        if (curios != null) {
            return curios.anyCurioMatches(entity, stack -> stack.getItem() instanceof IRevealer r && r.showNodes(stack, entity));
        }
        return false;
    }

    /**
     * Sums the vis discount percentages contributed by ever held/worn item that has a modifier for the attribute the vis discount attribute by the player.
     * Each piece implementing {@link IVisDiscountGear} automatically obtain an attribute modifier for the the vis discount attribute attribute.
     *
     * @param player the player to query; null returns zero
     * @return the total discount in whole percent units, never negative
     */
    public static int totalVisDiscount(Player player) {
        if (player == null) {
            return 0;
        }
        AttributeInstance attribute = player.getAttribute(visDiscountAttribute());
        if (attribute == null)
            return 0;
        return (int) (attribute.getValue() * 100);
    }
}
