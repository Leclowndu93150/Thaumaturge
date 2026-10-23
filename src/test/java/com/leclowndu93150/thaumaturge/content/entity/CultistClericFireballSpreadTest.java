package com.leclowndu93150.thaumaturge.content.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.leclowndu93150.thaumaturge.content.entity.cult.CultSpells;
import org.junit.jupiter.api.Test;

class CultistClericFireballSpreadTest {
    @Test
    void fanSpreadScalesWithTheSquareRootOfDistanceFactor() {
        assertEquals(0.0F, CultSpells.fireballFanSpread(0.0F));
        assertEquals(2.5F, CultSpells.fireballFanSpread(1.0F));
        assertEquals(5.0F, CultSpells.fireballFanSpread(4.0F));
    }
}
