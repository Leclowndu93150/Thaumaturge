package com.leclowndu93150.thaumaturge.data.trade;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.TradeCost;
import net.minecraft.world.item.trading.VillagerTrade;

public final class TTVillagerTradeBootstrap {
    public static final ResourceKey<VillagerTrade> WANDERING_TRADER_ARCANE_TRIM = ResourceKey.create(Registries.VILLAGER_TRADE,
            TTIds.rl("wandering_trader/emerald_arcane_armor_trim_smithing_template"));

    private static final int ARCANE_TRIM_EMERALDS = 16;
    private static final int ARCANE_TRIM_MAX_USES = 1;
    private static final int ARCANE_TRIM_XP = 1;
    private static final float ARCANE_TRIM_REPUTATION_DISCOUNT = 0.05F;

    private TTVillagerTradeBootstrap() {}

    public static void bootstrap(BootstrapContext<VillagerTrade> context) {
        context.register(WANDERING_TRADER_ARCANE_TRIM,
                new VillagerTrade(new TradeCost(Items.EMERALD, ARCANE_TRIM_EMERALDS), new ItemStackTemplate(TTItems.ARCANE_ARMOR_TRIM_SMITHING_TEMPLATE.get()), ARCANE_TRIM_MAX_USES,
                        ARCANE_TRIM_XP, ARCANE_TRIM_REPUTATION_DISCOUNT, Optional.empty(), List.of()));
    }
}
