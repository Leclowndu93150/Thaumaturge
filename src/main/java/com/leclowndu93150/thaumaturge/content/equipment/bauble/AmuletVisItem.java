package com.leclowndu93150.thaumaturge.content.equipment.bauble;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.content.wands.ItemWand;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.fml.ModList;

public final class AmuletVisItem extends Item {
    private final int rechargeInterval;

    public AmuletVisItem(Properties properties, int rechargeInterval) {
        super(properties);
        this.rechargeInterval = rechargeInterval;
    }

    public void wornTick(ItemStack stack, LivingEntity wearer) {
        if (!(wearer instanceof Player player)
                || player.level().isClientSide()
                || player.tickCount % rechargeInterval != 0) {
            return;
        }
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (RechargeAccess.rechargeItem(
                            player.level(), player.getInventory().getItem(slot), player.blockPosition(), player, 1)
                    > 0.0F) {
                return;
            }
        }
        if (topUpWand(player)) {
            return;
        }
        if (RechargeAccess.rechargeItem(player.level(), player.getOffhandItem(), player.blockPosition(), player, 1)
                        > 0.0F
                || topUpWand(player, player.getOffhandItem())) {
            return;
        }
        if (ModList.get().isLoaded(TTIds.CURIOS) && ThaumaturgeCuriosCompat.rechargeFirstCurio(player)) {
            return;
        }
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (slot.isArmor()
                    && RechargeAccess.rechargeItem(
                                    player.level(), player.getItemBySlot(slot), player.blockPosition(), player, 1)
                            > 0.0F) {
                return;
            }
        }
    }

    private boolean topUpWand(Player player) {
        for (int slot = 0; slot < Inventory.getSelectionSize(); slot++) {
            if (topUpWand(player, player.getInventory().getItem(slot))) {
                return true;
            }
        }
        return false;
    }

    private boolean topUpWand(Player player, ItemStack candidate) {
        if (!(candidate.getItem() instanceof ItemWand)) {
            return false;
        }
        int max = WandVisHelper.getMaxVis(candidate);
        ResourceKey<IAspect> lowest = null;
        int lowestAmount = Integer.MAX_VALUE;
        for (ResourceKey<IAspect> primal : TTAspects.PRIMALS) {
            int amount = WandVisHelper.getVis(candidate, primal);
            if (amount < max && amount < lowestAmount) {
                lowestAmount = amount;
                lowest = primal;
            }
        }
        if (lowest == null || AuraHelper.drainVis(player.level(), player.blockPosition(), 1.0F, false) <= 0.0F) {
            return false;
        }
        WandVisHelper.addVis(candidate, lowest, 1, true);
        return true;
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.thaumaturge.amulet_vis.text").withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
