package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraitModifiers;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;

public final class ModifierChampionTrait extends AbstractChampionTrait {
    private final Holder<Attribute> attribute;
    private final double amount;
    private final AttributeModifier.Operation operation;

    public ModifierChampionTrait(Holder<Attribute> attribute, double amount, AttributeModifier.Operation operation) {
        this.attribute = attribute;
        this.amount = amount;
        this.operation = operation;
    }

    @Override
    protected void championModifiers(LivingEntity mob, MobTraitModifiers modifiers) {
        modifiers.add(attribute, amount, operation);
    }
}
