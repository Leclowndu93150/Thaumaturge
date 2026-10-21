package com.leclowndu93150.thaumaturge.api.items;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;
import org.jspecify.annotations.Nullable;

/**
 * Reads additional equipment discounts without altering the generic discount attribute.
 * Costs subtract the generic and additional fractions from the cap modifier, then apply the
 * sceptre discount and the existing minimum consumption modifier. Final centivis round down.
 * Every query scans live worn equipment; inventory and cosmetic slots do not contribute.
 *
 * @since 1.0.4
 */
public final class VisDiscountAccess {
    private static final EquipmentSlot[] ARMOR_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static @Nullable EquipmentSource curios;

    private VisDiscountAccess() {}

    /** Installs the optional Curios scanner during Thaumaturge initialization. */
    public static void bindCurios(EquipmentSource source) {
        curios = source;
    }

    /**
     * Returns the summed additional fraction, capped at one. Generic attributes are not included.
     * A null aspect represents a scalar-only cost and always returns zero. Client queries return
     * zero; authoritative affordability and payment must be calculated on the server.
     *
     * @param baseCentivis the original cost of this aspect row, in centivis (100 per vis)
     */
    public static float additionalDiscount(@Nullable Player player, @Nullable ResourceKey<IAspect> aspect, boolean crafting, int baseCentivis) {
        if (baseCentivis < 0) {
            throw new IllegalArgumentException("Base centivis must not be negative");
        }
        if (player == null || aspect == null || player.level().isClientSide() || !player.isAlive()) {
            return 0.0F;
        }
        Set<ItemStack> visited = Collections.newSetFromMap(new IdentityHashMap<>());
        float[] total = {0.0F};
        BiConsumer<String, ItemStack> visitor = (slot, stack) -> {
            if (!stack.isEmpty() && visited.add(stack)) {
                VisDiscountEvent event = NeoForge.EVENT_BUS.post(new VisDiscountEvent(player, stack, slot, aspect, crafting, baseCentivis));
                total[0] = Math.min(1.0F, total[0] + event.discount());
            }
        };
        for (EquipmentSlot slot : ARMOR_SLOTS) {
            visitor.accept("equipment:" + slot.getName(), player.getItemBySlot(slot));
        }
        if (curios != null) {
            curios.forEach(player, visitor);
        }
        return total[0];
    }

    @FunctionalInterface
    public interface EquipmentSource {
        /** Visits each functional slot with its identifier and live stack; must not mutate either. */
        void forEach(Player player, BiConsumer<String, ItemStack> visitor);
    }
}
