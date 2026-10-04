package com.leclowndu93150.thaumaturge.content.pech;

import com.leclowndu93150.thaumaturge.content.entity.EntityPech;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public final class PechBarter {
    private static final int GRUMPINESS_ROLL = 100;
    private static final int GENEROSITY_ODDS = 5;
    private static final int MOOD_SWING = 3;
    private static final int RICHEST_TIER = 5;
    private static final int HOARD_TIER = 1;
    private static final int RARE_TIER = 4;
    private static final float HOARD_CHANCE = 0.5F;
    private static final float GRUMBLE_VOLUME = 0.4F;

    private PechBarter() {}

    public static List<ItemStack> haggle(EntityPech pech, ItemStack offering, RandomSource random, HolderLookup.Provider registries) {
        int budget = pech.getValue(offering);
        if (random.nextInt(GRUMPINESS_ROLL) <= budget / 2) {
            pech.setTamed(false);
            pech.playSound(TCSounds.PECH_TRADE.get(), GRUMBLE_VOLUME, 1.0F);
        }
        budget += moodSwing(random);
        List<PechTrades.Entry> table = PechTrades.tradesFor(pech.getPechType(), registries);
        List<ItemStack> payout = new ArrayList<>();
        while (budget > 0) {
            int tier = Math.min(RICHEST_TIER, Math.max((budget + 1) / 2, random.nextInt(budget) + 1));
            budget -= tier;
            if (tier == HOARD_TIER && random.nextFloat() < HOARD_CHANCE && hoardsAnything(pech)) {
                payout.add(takeFromHoard(pech, random));
            } else if (tier < RARE_TIER || !random.nextBoolean()) {
                List<PechTrades.Entry> offers = table.stream().filter(entry -> entry.tier() == tier).toList();
                if (!offers.isEmpty()) {
                    payout.add(offers.get(random.nextInt(offers.size())).stack().copy());
                }
            }
        }
        return payout;
    }

    private static int moodSwing(RandomSource random) {
        if (random.nextInt(GENEROSITY_ODDS) == 0) {
            return random.nextInt(MOOD_SWING);
        }
        return random.nextBoolean() ? -random.nextInt(MOOD_SWING) : 0;
    }

    private static boolean hoardsAnything(EntityPech pech) {
        return pech.loot.stream().anyMatch(stack -> !stack.isEmpty());
    }

    private static ItemStack takeFromHoard(EntityPech pech, RandomSource random) {
        List<Integer> stocked = IntStream.range(0, pech.loot.size()).filter(index -> !pech.loot.get(index).isEmpty()).boxed().toList();
        int index = stocked.get(random.nextInt(stocked.size()));
        ItemStack taken = pech.loot.get(index).split(1);
        if (pech.loot.get(index).isEmpty()) {
            pech.loot.set(index, ItemStack.EMPTY);
        }
        return taken;
    }
}
