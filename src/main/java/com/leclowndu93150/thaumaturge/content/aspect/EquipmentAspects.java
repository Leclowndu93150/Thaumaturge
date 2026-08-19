package com.leclowndu93150.thaumaturge.content.aspect;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

final class EquipmentAspects {
    private static final int ASPECT_PER_POINT = 4;
    private static final int BOW_AVERSIO = 10;
    private static final int BOW_VOLATUS = 5;
    private static final int SWORD_BASE_MODIFIER = 2;
    private static final int WOOD_TIER_DURABILITY = 59;
    private static final int STONE_TIER_DURABILITY = 131;
    private static final int IRON_TIER_DURABILITY = 250;

    private EquipmentAspects() {}

    static AspectList bonusFor(Item item, HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(item);
        AspectList bonus = AspectList.EMPTY;
        int defense = (int) attributeTotal(stack, Attributes.ARMOR);
        if (defense > 0 && isHumanoidArmor(stack)) {
            bonus = with(bonus, registries, TCAspects.PRAEMUNIO, defense * ASPECT_PER_POINT);
        }
        if (stack.is(ItemTags.SWORDS)) {
            int attack = (int) attributeTotal(stack, Attributes.ATTACK_DAMAGE);
            if (attack > 0) {
                int amount = Math.max(1, attack - SWORD_BASE_MODIFIER) * ASPECT_PER_POINT;
                bonus = with(bonus, registries, TCAspects.AVERSIO, amount);
            }
        } else if (item instanceof ProjectileWeaponItem) {
            bonus = with(bonus, registries, TCAspects.AVERSIO, BOW_AVERSIO);
            bonus = with(bonus, registries, TCAspects.VOLATUS, BOW_VOLATUS);
        } else if (stack.is(ItemTags.PICKAXES) || stack.is(ItemTags.AXES) || stack.is(ItemTags.SHOVELS) || stack.is(ItemTags.HOES) || item instanceof ShearsItem) {
            int durability = stack.getMaxDamage();
            int amount;
            if (durability <= WOOD_TIER_DURABILITY) {
                amount = ASPECT_PER_POINT;
            } else if (durability <= STONE_TIER_DURABILITY) {
                amount = 2 * ASPECT_PER_POINT;
            } else if (durability <= IRON_TIER_DURABILITY) {
                amount = 3 * ASPECT_PER_POINT;
            } else {
                amount = 4 * ASPECT_PER_POINT;
            }
            bonus = with(bonus, registries, TCAspects.INSTRUMENTUM, amount);
        }
        return bonus;
    }

    private static AspectList with(AspectList list, HolderLookup.Provider registries, ResourceKey<IAspect> key, int amount) {
        Holder<IAspect> aspect = Aspects.resolve(registries, key);
        if (aspect == null || amount <= 0) {
            return list;
        }
        return list.add(aspect, amount);
    }

    private static double attributeTotal(ItemStack stack, Holder<Attribute> attribute) {
        ItemAttributeModifiers modifiers = stack.get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) {
            return 0.0;
        }
        double total = 0.0;
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().equals(attribute) && entry.modifier().operation() == AttributeModifier.Operation.ADD_VALUE) {
                total += entry.modifier().amount();
            }
        }
        return total;
    }

    private static boolean isHumanoidArmor(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        return equippable != null && equippable.slot().getType() == EquipmentSlot.Type.HUMANOID_ARMOR;
    }
}
