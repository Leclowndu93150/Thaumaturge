package com.leclowndu93150.thaumaturge.content.device.bore;

import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantmentHelper;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.Tool;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public final class ArcaneBoreTool {
    private static final int MIN_RADIUS = 2;
    private static final int ENCHANTABILITY_PER_RADIUS = 3;
    private static final int RADIUS_PER_DESTRUCTIVE = 2;
    private static final int DEPTH_PER_RADIUS = 8;
    private static final int DEPTH_PER_BURROWING = 16;
    private static final int DESTROY_SPEED_DIVISOR = 2;

    private ArcaneBoreTool() {}

    public static boolean isPickaxe(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.is(ItemTags.PICKAXES)) {
            return true;
        }
        Tool tool = stack.get(DataComponents.TOOL);
        if (tool == null) {
            return false;
        }
        for (Tool.Rule rule : tool.rules()) {
            if (rule.blocks()
                    .unwrapKey()
                    .map(key -> key.equals(BlockTags.MINEABLE_WITH_PICKAXE))
                    .orElse(false)) {
                return true;
            }
        }
        return false;
    }

    public static boolean valid(ItemStack stack) {
        if (!isPickaxe(stack)) {
            return false;
        }
        return !stack.isDamageableItem() || stack.getDamageValue() + 1 < stack.getMaxDamage();
    }

    public static int digRadius(ItemStack stack) {
        int radius = 0;
        if (isPickaxe(stack)) {
            radius = stack.getItem().getEnchantmentValue() / ENCHANTABILITY_PER_RADIUS;
            radius += InfusionEnchantmentHelper.level(stack, InfusionEnchantment.DESTRUCTIVE) * RADIUS_PER_DESTRUCTIVE;
        }
        return radius <= 1 ? MIN_RADIUS : radius;
    }

    public static int digDepth(ItemStack stack) {
        return digRadius(stack) * DEPTH_PER_RADIUS
                + InfusionEnchantmentHelper.level(stack, InfusionEnchantment.BURROWING) * DEPTH_PER_BURROWING;
    }

    public static int fortune(Level level, ItemStack stack) {
        if (!valid(stack)) {
            return 0;
        }
        int fortune = EnchantmentHelper.getItemEnchantmentLevel(
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE), stack);
        return Math.max(fortune, InfusionEnchantmentHelper.level(stack, InfusionEnchantment.SOUNDING));
    }

    public static int digSpeed(Level level, ItemStack stack, BlockState state) {
        if (!valid(stack)) {
            return 0;
        }
        int speed = (int) (stack.getDestroySpeed(state) / DESTROY_SPEED_DIVISOR);
        speed += EnchantmentHelper.getItemEnchantmentLevel(
                level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.EFFICIENCY),
                stack);
        return speed;
    }

    public static int refining(ItemStack stack) {
        return InfusionEnchantmentHelper.level(stack, InfusionEnchantment.REFINING);
    }

    public static boolean silkTouch(Level level, ItemStack stack) {
        return !stack.isEmpty()
                && EnchantmentHelper.getItemEnchantmentLevel(
                                level.registryAccess()
                                        .lookupOrThrow(Registries.ENCHANTMENT)
                                        .getOrThrow(Enchantments.SILK_TOUCH),
                                stack)
                        > 0;
    }
}
