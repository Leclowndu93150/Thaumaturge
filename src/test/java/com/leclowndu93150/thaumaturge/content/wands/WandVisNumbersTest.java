package com.leclowndu93150.thaumaturge.content.wands;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.text.DecimalFormatSymbols;
import org.junit.jupiter.api.Test;

class WandVisNumbersTest {
    @Test
    void largeSceptreCapacityStaysExact() {
        assertEquals(999_999_900, WandVisNumbers.capacity(9_999_999, WandEconomy.CENTIVIS_PER_VIS));
        assertEquals(1_499_999_850, WandVisNumbers.capacity(9_999_999, WandEconomy.SCEPTRE_CAPACITY_PER_VIS));
    }

    @Test
    void capacitiesBeyondTheStorageRepresentationSaturateWithoutWrapping() {
        assertEquals(Integer.MAX_VALUE, WandVisNumbers.capacity(Integer.MAX_VALUE, 150));
        assertEquals(0, WandVisNumbers.capacity(-100, 150));
        assertEquals(Integer.MAX_VALUE, WandVisNumbers.clampToStorage((long) Integer.MAX_VALUE + 20));
        assertEquals(0, WandVisNumbers.clampToStorage(-50));
    }

    @Test
    void unrepresentableCostsAreNotRoundedDownToAffordableIntValues() {
        assertEquals(2_147_483_648L, WandVisNumbers.scaledCost(Integer.MAX_VALUE, 2.0F));
        assertEquals(50L, WandVisNumbers.scaledCost(100, 0.5F));
        assertEquals(-3L, WandVisNumbers.scaledCost(-7, 0.5F));
        assertEquals(2_147_483_648L, WandVisNumbers.scaledCost(100, Float.NaN));
    }

    @Test
    void tooltipUsesCentivisExactlyForCapacityAndStoredValues() {
        String decimal = String.valueOf(DecimalFormatSymbols.getInstance().getDecimalSeparator());
        assertEquals("14999998" + decimal + "5", WandVisNumbers.format(1_499_999_850));
        assertEquals("21474836" + decimal + "47", WandVisNumbers.format(Integer.MAX_VALUE));
        assertEquals("50", WandVisNumbers.format(5_000));
    }

    @Test
    void wholeVisDoesNotConsumeAnItemWhenOnlyHalfVisFits() {
        assertEquals(0L, WandVisNumbers.accepted(1_499_999_800, 1_499_999_850, 100L, 100));
        assertEquals(50L, WandVisNumbers.accepted(1_499_999_800, 1_499_999_850, 100L, 1));
    }

    @Test
    void largeTransfersAreBoundedByActualSpaceWithoutIntegerOverflow() {
        long offered = (long) Integer.MAX_VALUE * WandEconomy.CENTIVIS_PER_VIS;
        assertEquals(2_147_483_600L, WandVisNumbers.accepted(0, Integer.MAX_VALUE, offered, 100));
        assertEquals(10L, WandVisNumbers.accepted(Integer.MAX_VALUE - 10, Integer.MAX_VALUE, offered, 1));
        assertEquals(0L, WandVisNumbers.accepted(Integer.MAX_VALUE, Integer.MAX_VALUE, offered, 100));
    }
}
