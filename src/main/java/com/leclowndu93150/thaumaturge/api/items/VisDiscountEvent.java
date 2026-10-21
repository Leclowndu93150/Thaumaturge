package com.leclowndu93150.thaumaturge.api.items;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.Objects;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.Event;

/**
 * Queries an additional aspect-specific discount for one equipped stack on the logical server.
 * Listeners on the NeoForge event bus must be pure and must not repeat discounts already supplied
 * by {@link IVisDiscountGear} or the generic vis discount attribute. Stack access returns a copy.
 * Contributions use fractional units: {@code 0.03} means three percent, not {@code 3}.
 *
 * @since 1.0.4
 */
public final class VisDiscountEvent extends Event {
    private final Player player;
    private final ItemStack stack;
    private final String slot;
    private final ResourceKey<IAspect> aspect;
    private final boolean crafting;
    private final int baseCentivis;
    private float discount;

    public VisDiscountEvent(Player player, ItemStack stack, String slot, ResourceKey<IAspect> aspect, boolean crafting, int baseCentivis) {
        if (baseCentivis < 0) {
            throw new IllegalArgumentException("Base centivis must not be negative");
        }
        this.player = Objects.requireNonNull(player);
        this.stack = stack.copy();
        this.slot = Objects.requireNonNull(slot);
        this.aspect = Objects.requireNonNull(aspect);
        this.crafting = crafting;
        this.baseCentivis = baseCentivis;
    }

    public Player player() {
        return player;
    }

    public ItemStack stack() {
        return stack.copy();
    }

    /** @return equipment slot name or {@code curios:<identifier>:<index>} */
    public String slot() {
        return slot;
    }

    public ResourceKey<IAspect> aspect() {
        return aspect;
    }

    /** @return true for crafting, false for casting */
    public boolean crafting() {
        return crafting;
    }

    /** @return this aspect row's cost before modifiers, in centivis (100 per vis) */
    public int baseCentivis() {
        return baseCentivis;
    }

    /** Adds a finite fraction between zero and one; contributions sum up to one. */
    public void addDiscount(float fraction) {
        if (!Float.isFinite(fraction) || fraction < 0.0F || fraction > 1.0F) {
            throw new IllegalArgumentException("Discount must be a finite fraction between zero and one");
        }
        discount = Math.min(1.0F, discount + fraction);
    }

    public float discount() {
        return discount;
    }
}
