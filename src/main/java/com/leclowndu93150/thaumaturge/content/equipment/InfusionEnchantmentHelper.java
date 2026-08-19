package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.IRechargable;
import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import java.util.List;
import java.util.Set;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;

public final class InfusionEnchantmentHelper {
    private InfusionEnchantmentHelper() {}

    public static InfusionEnchantments get(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.INFUSION_ENCHANTMENTS.get(), InfusionEnchantments.EMPTY);
    }

    public static int level(ItemStack stack, InfusionEnchantment enchantment) {
        return get(stack).level(enchantment);
    }

    public static boolean has(ItemStack stack, InfusionEnchantment enchantment) {
        return get(stack).has(enchantment);
    }

    public static List<InfusionEnchantment> list(ItemStack stack) {
        return List.copyOf(get(stack).levels().keySet());
    }

    public static void add(ItemStack stack, InfusionEnchantment enchantment, int level) {
        if (stack.isEmpty() || level > enchantment.maxLevel()) {
            return;
        }
        InfusionEnchantments current = get(stack);
        if (current.level(enchantment) >= level) {
            return;
        }
        stack.set(TCDataComponents.INFUSION_ENCHANTMENTS.get(), current.with(enchantment, level));
    }

    public static boolean canApply(ItemStack stack, InfusionEnchantment enchantment) {
        if (stack.isEmpty()) {
            return false;
        }
        Set<String> classes = enchantment.toolClasses();
        if (classes.contains("all")) {
            return true;
        }
        if (classes.contains("weapon") && dealsAttackDamage(stack)) {
            return true;
        }
        if (classes.contains("pickaxe") && stack.is(ItemTags.PICKAXES)) {
            return true;
        }
        if (classes.contains("axe") && stack.is(ItemTags.AXES)) {
            return true;
        }
        if (classes.contains("shovel") && stack.is(ItemTags.SHOVELS)) {
            return true;
        }
        if (classes.contains("hoe") && stack.is(ItemTags.HOES)) {
            return true;
        }
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable != null) {
            String slot = armorSlotToken(equippable.slot());
            if (classes.contains("armor") || classes.contains(slot)) {
                return true;
            }
        }
        return stack.getItem() instanceof IRechargable && classes.contains("chargable");
    }

    private static boolean dealsAttackDamage(ItemStack stack) {
        ItemAttributeModifiers modifiers = stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY);
        for (ItemAttributeModifiers.Entry entry : modifiers.modifiers()) {
            if (entry.attribute().is(Attributes.ATTACK_DAMAGE) && entry.slot().test(EquipmentSlot.MAINHAND)) {
                return true;
            }
        }
        return false;
    }

    private static String armorSlotToken(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> "helm";
            case CHEST -> "chest";
            case LEGS -> "legs";
            case FEET -> "boots";
            default -> "none";
        };
    }
}
