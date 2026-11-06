package com.leclowndu93150.thaumaturge.content.equipment.trim;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.BasicItemListing;
import net.neoforged.neoforge.event.village.WandererTradesEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TrimTemplateTrades {
    private static final int ARCANE_TRIM_EMERALDS = 16;
    private static final int ARCANE_TRIM_MAX_USES = 1;
    private static final int ARCANE_TRIM_XP = 1;
    private static final float ARCANE_TRIM_REPUTATION_DISCOUNT = 0.05F;

    private TrimTemplateTrades() {}

    @SubscribeEvent
    public static void onWandererTrades(WandererTradesEvent event) {
        event.getRareTrades()
                .add(new BasicItemListing(
                        ARCANE_TRIM_EMERALDS,
                        new ItemStack(TTItems.ARCANE_ARMOR_TRIM_SMITHING_TEMPLATE.get()),
                        ARCANE_TRIM_MAX_USES,
                        ARCANE_TRIM_XP,
                        ARCANE_TRIM_REPUTATION_DISCOUNT));
    }
}
