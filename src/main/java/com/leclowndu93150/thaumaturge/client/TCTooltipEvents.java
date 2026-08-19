package com.leclowndu93150.thaumaturge.client;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.infusion.IInfusionStabiliser;
import com.leclowndu93150.thaumaturge.api.items.IRechargable;
import com.leclowndu93150.thaumaturge.api.items.InfusionEnchantment;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.api.warp.WarpHelper;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantmentHelper;
import com.leclowndu93150.thaumaturge.content.infusion.InfusionRunicAugmentRecipe;
import com.leclowndu93150.thaumaturge.registry.TCBlockTags;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCTooltipEvents {
    private static final Identifier INFUSION_RESEARCH = TCIds.rl("unlock_infusion");

    private TCTooltipEvents() {}

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        if (event.getEntity() == null) {
            return;
        }
        for (Map.Entry<InfusionEnchantment, Integer> entry : InfusionEnchantmentHelper.get(event.getItemStack()).levels().entrySet()) {
            InfusionEnchantment enchantment = entry.getKey();
            Component line = Component.translatable("enchantment.thaumaturge." + enchantment.getSerializedName());
            if (enchantment.maxLevel() > 1) {
                line = Component.translatable("enchantment.thaumaturge." + enchantment.getSerializedName()).append(" ").append(Component.translatable("enchantment.level." + entry.getValue()));
            }
            event.getToolTip().add(1, line.copy().withStyle(ChatFormatting.GOLD));
        }
        int runic = InfusionRunicAugmentRecipe.charge(event.getItemStack());
        if (runic > 0) {
            event.getToolTip().add(1, Component.translatable("tooltip.thaumaturge.runic_charge", runic).withStyle(ChatFormatting.GOLD));
        }
        int warp = WarpHelper.getFinalWarp(event.getItemStack(), event.getEntity());
        if (warp > 0) {
            event.getToolTip().add(1, Component.translatable("item.thaumaturge.warping").withStyle(ChatFormatting.DARK_PURPLE));
        }
        if (event.getItemStack().getItem() instanceof IRechargable rechargable) {
            event.getToolTip().add(1,
                    Component.translatable("tooltip.thaumaturge.charge", RechargeAccess.getCharge(event.getItemStack()), rechargable.getMaxCharge(event.getItemStack(), event.getEntity()))
                            .withStyle(ChatFormatting.AQUA));
        }
        if (event.getItemStack().getItem() instanceof BlockItem blockItem && isStabiliser(blockItem.getBlock()) && KnowledgeAccess.of(event.getEntity()).isResearchComplete(INFUSION_RESEARCH)) {
            event.getToolTip().add(Component.translatable("tooltip.thaumaturge.infusion_stabiliser").withStyle(ChatFormatting.DARK_PURPLE));
        }
    }

    private static boolean isStabiliser(Block block) {
        return block instanceof IInfusionStabiliser || block.defaultBlockState().is(TCBlockTags.INFUSION_STABILISERS);
    }
}
