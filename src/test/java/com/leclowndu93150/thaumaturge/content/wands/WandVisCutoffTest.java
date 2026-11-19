package com.leclowndu93150.thaumaturge.content.wands;

import static org.junit.jupiter.api.Assertions.*;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.wands.IWandVisStorage;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class WandVisCutoffTest {
    private static final class Storage implements IWandVisStorage {
        WandVis charge;
        int reads;
        final List<WandVis> writes = new ArrayList<>();

        Storage(WandVis charge) {
            this.charge = charge;
        }

        @Override
        public WandVis getVis(ItemStack stack) {
            reads++;
            return charge;
        }

        @Override
        public void setVis(ItemStack stack, WandVis charge) {
            writes.add(charge);
            this.charge = charge;
        }
    }

    private static WandVis funded() {
        Map<ResourceKey<IAspect>, Integer> pools = new LinkedHashMap<>();
        TTAspects.PRIMALS.forEach(aspect -> pools.put(aspect, 100));
        return new WandVis(pools);
    }

    private static ItemStack customWand(Storage storage) {
        return customWand(storage, 50, false);
    }

    private static ItemStack customWand(Storage storage, int capacity, boolean sceptre) {
        WandCap cap = new WandCap(0.5F, Map.of(), 1, TTIds.rl("test_cap"));
        WandRod rod = new WandRod(capacity, 1, TTIds.rl("test_rod"), null, false, false, false, null, storage);
        ItemStack stack = ItemWand.create(TTItems.WAND.get(), cap, rod, sceptre);
        stack.set(TTDataComponents.WAND_VIS.get(), WandVis.EMPTY);
        return stack;
    }

    private static boolean pay(
            ItemStack stack, Map<ResourceKey<IAspect>, Integer> costs, boolean exact, boolean simulate) {
        return exact
                ? WandVisHelper.payExact(stack, costs, simulate)
                : WandVisHelper.payCosts(stack, null, costs, !simulate, false);
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void wholePaymentReadsCustomStorageOnceAndCommitsOneCompleteSnapshot(boolean exact, boolean simulate) {
        WandVis before = funded();
        Storage storage = new Storage(before);
        ItemStack stack = customWand(storage);
        assertTrue(pay(stack, WandVisHelper.evenSplit(120), exact, simulate));
        assertEquals(1, storage.reads);
        assertEquals(simulate ? 0 : 1, storage.writes.size());
        int after = simulate ? 100 : exact ? 80 : 90;
        TTAspects.PRIMALS.forEach(aspect -> assertEquals(after, storage.charge.amount(aspect)));
        if (simulate) assertSame(before, storage.charge);
        else assertSame(storage.charge, storage.writes.getFirst());
        assertSame(WandVis.EMPTY, stack.get(TTDataComponents.WAND_VIS.get()));
    }

    @ParameterizedTest
    @CsvSource({"false,false", "false,true", "true,false", "true,true"})
    void lateUnaffordableCostNeverPublishesEarlierDeductions(boolean exact, boolean simulate) {
        ResourceKey<IAspect> last = TTAspects.PRIMALS.getLast();
        WandVis before = funded().with(last, 9);
        Storage storage = new Storage(before);
        ItemStack stack = customWand(storage);
        assertFalse(pay(stack, WandVisHelper.evenSplit(120), exact, simulate));
        assertEquals(1, storage.reads);
        assertTrue(storage.writes.isEmpty());
        assertSame(before, storage.charge);
    }

    @Test
    void emptyCostMapsFailWithoutReadingOrWritingStorage() {
        Storage storage = new Storage(funded());
        ItemStack stack = customWand(storage);
        assertFalse(WandVisHelper.payCosts(stack, null, Map.of(), true, false));
        assertFalse(WandVisHelper.payExact(stack, Map.of(), false));
        assertEquals(0, storage.reads);
        assertTrue(storage.writes.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void zeroAndNegativeMapEntriesKeepCutoffScalingAndCreditBehavior(boolean exact) {
        Storage storage = new Storage(funded());
        ItemStack stack = customWand(storage);
        Map<ResourceKey<IAspect>, Integer> costs = new LinkedHashMap<>();
        costs.put(TTAspects.AER, 0);
        costs.put(TTAspects.TERRA, -7);
        assertTrue(pay(stack, costs, exact, false));
        assertEquals(1, storage.reads);
        assertEquals(1, storage.writes.size());
        assertEquals(100, storage.charge.amount(TTAspects.AER));
        assertEquals(exact ? 107 : 103, storage.charge.amount(TTAspects.TERRA));
    }

    @Test
    void nonPositiveWandDrawsBypassCustomStorageEntirely() {
        Storage storage = new Storage(funded());
        ItemStack stack = customWand(storage);
        ItemWand wand = (ItemWand) stack.getItem();
        assertTrue(wand.drawVis(stack, null, 0.0F, false, false));
        assertTrue(wand.drawVis(stack, null, -5.0F, true, true));
        assertEquals(0, storage.reads);
        assertTrue(storage.writes.isEmpty());
    }

    @Test
    void singlePoolDrainUsesTheCutoffScaledCostAndDoesNotWriteWhenUnaffordable() {
        Storage storage = new Storage(funded());
        ItemStack stack = customWand(storage);
        assertFalse(WandVisHelper.drainOne(stack, null, TTAspects.AER, 202, false));
        assertTrue(storage.writes.isEmpty());
        assertEquals(100, storage.charge.amount(TTAspects.AER));
        assertTrue(WandVisHelper.drainOne(stack, null, TTAspects.AER, -7, true));
        assertEquals(103, storage.charge.amount(TTAspects.AER));
        assertEquals(1, storage.writes.size());
    }

    @Test
    void fillingAnExtremelyLargeSceptrePreservesItsExactCapacity() {
        Storage storage = new Storage(WandVis.EMPTY);
        ItemStack stack = customWand(storage, 9_999_999, true);
        assertEquals(1_499_999_850, WandVisHelper.getMaxVis(stack));
        WandVisHelper.fill(stack);
        TTAspects.PRIMALS.forEach(aspect -> assertEquals(1_499_999_850, storage.charge.amount(aspect)));
    }

    @Test
    void chargingAtIntegerLimitCannotWrapOrEraseExistingVis() {
        Storage storage = new Storage(WandVis.EMPTY.with(TTAspects.AER, Integer.MAX_VALUE - 10));
        ItemStack stack = customWand(storage, Integer.MAX_VALUE, true);
        assertEquals(Integer.MAX_VALUE, WandVisHelper.getMaxVis(stack));
        assertEquals(15, WandVisHelper.addRealVis(stack, TTAspects.AER, 25, true));
        assertEquals(Integer.MAX_VALUE, storage.charge.amount(TTAspects.AER));
        assertEquals(Integer.MAX_VALUE, WandVisHelper.addRealVis(stack, TTAspects.AER, Integer.MAX_VALUE, true));
        assertEquals(Integer.MAX_VALUE, storage.charge.amount(TTAspects.AER));
    }

    @Test
    void wholeVisTransfersDoNotRoundAwayTheHalfVisLeftInALargeSceptre() {
        Storage storage = new Storage(WandVis.EMPTY.with(TTAspects.AER, 1_499_999_800));
        ItemStack stack = customWand(storage, 9_999_999, true);
        assertEquals(1, WandVisHelper.addVis(stack, TTAspects.AER, 1, true));
        assertEquals(1_499_999_800, storage.charge.amount(TTAspects.AER));
        assertEquals(50, WandVisHelper.addRealVis(stack, TTAspects.AER, 100, true));
        assertEquals(1_499_999_850, storage.charge.amount(TTAspects.AER));
    }

    @Test
    void hugeWholeVisOffersReturnTheRealLeftoverWithoutIntMultiplicationOverflow() {
        Storage storage = new Storage(WandVis.EMPTY);
        ItemStack stack = customWand(storage);
        assertEquals(Integer.MAX_VALUE - 50, WandVisHelper.addVis(stack, TTAspects.AER, Integer.MAX_VALUE, true));
        assertEquals(5_000, storage.charge.amount(TTAspects.AER));
    }

    @Test
    void negativeCostCreditsClampInsteadOfWrappingStoredVis() {
        Storage storage = new Storage(WandVis.EMPTY.with(TTAspects.AER, Integer.MAX_VALUE - 1));
        ItemStack stack = customWand(storage);
        assertTrue(WandVisHelper.payExact(stack, Map.of(TTAspects.AER, -100), false));
        assertEquals(Integer.MAX_VALUE, storage.charge.amount(TTAspects.AER));
        assertTrue(WandVisHelper.payCosts(stack, null, Map.of(TTAspects.AER, -100), true, false));
        assertEquals(Integer.MAX_VALUE, storage.charge.amount(TTAspects.AER));
    }

    @Test
    void scaledCostsBeyondIntegerMaximumRemainUnaffordableEvenWithFullStorage() {
        Storage storage = new Storage(WandVis.EMPTY.with(TTAspects.AER, Integer.MAX_VALUE));
        WandCap cap = new WandCap(2.0F, Map.of(), 1, TTIds.rl("test_expensive_cap"));
        WandRod rod = new WandRod(
                Integer.MAX_VALUE, 1, TTIds.rl("test_expensive_rod"), null, false, false, false, null, storage);
        ItemStack stack = ItemWand.create(TTItems.WAND.get(), cap, rod, false);
        assertFalse(WandVisHelper.payCosts(stack, null, Map.of(TTAspects.AER, Integer.MAX_VALUE), true, false));
        assertEquals(Integer.MAX_VALUE, storage.charge.amount(TTAspects.AER));
        assertTrue(storage.writes.isEmpty());
    }

    @Test
    void unrepresentablyLargeDrawsFailInsteadOfSilentlyUndercharging() {
        Storage storage = new Storage(funded());
        ItemStack stack = customWand(storage);
        assertFalse(((ItemWand) stack.getItem()).drawVis(stack, null, Float.MAX_VALUE, false, false));
        assertEquals(0, storage.reads);
        assertTrue(storage.writes.isEmpty());
    }

    @Test
    void emptyAspectWeightsUseTheCanonicalEvenSplit() {
        assertEquals(WandVisHelper.evenSplit(17), WandVisHelper.primalSplit(17, AspectList.EMPTY));
    }
}
