package com.leclowndu93150.thaumaturge.api.golems.parts;

import java.util.function.Supplier;
import net.minecraft.world.item.ItemStack;

/**
 * One crafting component of a golem part. A component is either a fixed item stack or a
 * placeholder resolved against the golem's material at assembly time.
 *
 * @since 1.0.0
 */
public final class GolemComponent {
    private enum Kind {
        ITEM, MATERIAL_BASE, MATERIAL_MECHANISM
    }

    private static final GolemComponent BASE = new GolemComponent(Kind.MATERIAL_BASE, null);
    private static final GolemComponent MECHANISM = new GolemComponent(Kind.MATERIAL_MECHANISM, null);

    private final Kind kind;
    private final Supplier<ItemStack> item;

    private GolemComponent(Kind kind, Supplier<ItemStack> item) {
        this.kind = kind;
        this.item = item;
    }

    /**
     * Creates a component for a fixed item stack.
     *
     * @param item supplies the stack; evaluated lazily so items may be referenced before
     *             registries are bound
     * @return the component
     */
    public static GolemComponent of(Supplier<ItemStack> item) {
        return new GolemComponent(Kind.ITEM, item);
    }

    /**
     * @return the placeholder resolved to the material's base component
     */
    public static GolemComponent base() {
        return BASE;
    }

    /**
     * @return the placeholder resolved to the material's mechanism component
     */
    public static GolemComponent mechanism() {
        return MECHANISM;
    }

    /**
     * Resolves this component against a material.
     *
     * @param material the golem's material
     * @return a fresh copy of the resolved stack
     */
    public ItemStack resolve(GolemMaterial material) {
        return switch (kind) {
            case ITEM -> item.get().copy();
            case MATERIAL_BASE -> material.base();
            case MATERIAL_MECHANISM -> material.mechanism();
        };
    }
}
