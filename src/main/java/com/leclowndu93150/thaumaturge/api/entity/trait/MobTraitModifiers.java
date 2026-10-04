package com.leclowndu93150.thaumaturge.api.entity.trait;

import net.minecraft.core.Holder;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

/**
 * Receives the attribute modifiers a {@link MobTrait} applies to its carrier. The engine assigns
 * each modifier a stable id derived from the trait, so modifiers never collide between traits
 * and are removed cleanly when the trait goes away. Attributes the carrier does not have are
 * ignored.
 *
 * @since 1.0.0
 */
public interface MobTraitModifiers {
    /**
     * Adds a modifier.
     *
     * @param attribute the attribute to modify
     * @param amount    the modifier amount
     * @param operation how the amount combines with the attribute value
     */
    void add(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation);

    /**
     * Adds a modifier that moves the attribute's base value to {@code value}. The engine
     * computes the difference from the carrier's current base value.
     *
     * @param attribute the attribute to modify
     * @param value     the effective base value while the trait is present
     */
    void setBase(Holder<Attribute> attribute, double value);
}
