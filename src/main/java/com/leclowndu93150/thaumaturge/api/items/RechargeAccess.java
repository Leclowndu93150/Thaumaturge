package com.leclowndu93150.thaumaturge.api.items;

import com.leclowndu93150.thaumaturge.api.ApiBinding;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Static accessor for the vis charge of rechargeable stacks.
 *
 * <p>A stack is rechargeable when it carries a {@link ChargeProfile} in the {@code thaumaturge:rechargeable} component. Its charge is
 * the {@code thaumaturge:charge} component. Every mutator changes the given stack in place. Draining the aura is server-authoritative:
 * call {@link #rechargeItem} on the logical server only.
 *
 * @since 1.0.0
 */
public final class RechargeAccess {
    private static final ApiBinding<Bindings> BINDING = new ApiBinding<>("RechargeAccess");

    private RechargeAccess() {}

    /**
     * Installs the implementation. Called once by Thaumaturge during mod construction; addons must not call it.
     *
     * @param impl the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings impl) {
        BINDING.bind(impl);
    }

    /**
     * @param stack the stack to inspect
     * @return the stack's charge profile, or null when the stack is not rechargeable
     */
    public static @Nullable ChargeProfile profile(ItemStack stack) {
        return stack.isEmpty() ? null : stack.get(BINDING.get().profile());
    }

    /**
     * @param stack the stack to inspect
     * @return whether the stack is rechargeable
     */
    public static boolean isRechargeable(ItemStack stack) {
        return profile(stack) != null;
    }

    /**
     * Recharges a stack by draining vis from the aura at a position. Nothing moves when the player is in an aura-preserving state, the
     * stack is full or the aura is empty.
     *
     * @param level  the level to drain
     * @param stack  the stack to recharge
     * @param pos    the position whose aura chunk is drained
     * @param player the player causing the recharge, or null
     * @param amount the requested amount, clamped to the remaining capacity
     * @return the amount added
     */
    public static float rechargeItem(Level level, ItemStack stack, BlockPos pos, @Nullable Player player, int amount) {
        ChargeProfile profile = profile(stack);
        if (profile == null || player != null && AuraHelper.shouldPreserveAura(level, player, pos)) {
            return 0.0F;
        }
        int drained =
                (int) AuraHelper.drainVis(level, pos, Math.min(amount, profile.capacity() - getCharge(stack)), false);
        if (drained <= 0) {
            return 0.0F;
        }
        store(stack, profile, getCharge(stack) + drained);
        return drained;
    }

    /**
     * Recharges a stack without touching the aura.
     *
     * @param stack  the stack to recharge
     * @param holder the holder, or null
     * @param amount the requested amount, clamped to the remaining capacity
     * @return the amount added; 0 for a stack that is not rechargeable
     */
    public static float rechargeItemBlindly(ItemStack stack, @Nullable LivingEntity holder, int amount) {
        ChargeProfile profile = profile(stack);
        if (profile == null) {
            return 0.0F;
        }
        int added = Math.min(amount, profile.capacity() - getCharge(stack));
        if (added > 0) {
            store(stack, profile, getCharge(stack) + added);
        }
        return added;
    }

    /**
     * @param stack the stack to inspect
     * @return the current charge, or -1 when the stack is not rechargeable
     */
    public static int getCharge(ItemStack stack) {
        return isRechargeable(stack) ? stack.getOrDefault(BINDING.get().charge(), 0) : -1;
    }

    /**
     * @param stack  the stack to inspect
     * @param holder the holder, or null
     * @return the charge as a fraction of the capacity, from 0 to 1, or -1 when the stack is not rechargeable
     */
    public static float getChargePercentage(ItemStack stack, @Nullable LivingEntity holder) {
        ChargeProfile profile = profile(stack);
        return profile == null ? -1.0F : getCharge(stack) / (float) profile.capacity();
    }

    /**
     * Consumes charge when the stack holds at least the requested amount.
     *
     * @param stack  the stack to drain
     * @param holder the holder, or null
     * @param amount the amount to consume
     * @return whether the charge was there and was consumed
     */
    public static boolean consumeCharge(ItemStack stack, @Nullable LivingEntity holder, int amount) {
        int charge = getCharge(stack);
        if (charge < amount || charge < 0) {
            return false;
        }
        stack.set(BINDING.get().charge(), charge - amount);
        return true;
    }

    private static void store(ItemStack stack, ChargeProfile profile, int charge) {
        stack.set(BINDING.get().charge(), Math.min(profile.capacity(), charge));
    }

    /**
     * The component types Thaumaturge registers behind this facade.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * @return the {@code thaumaturge:charge} component type
         */
        DataComponentType<Integer> charge();

        /**
         * @return the {@code thaumaturge:rechargeable} component type
         */
        DataComponentType<ChargeProfile> profile();
    }
}
