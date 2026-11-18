package com.leclowndu93150.thaumaturge.client.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class WandCapLayoutTest {
    private static final float EPSILON = 0.0001F;

    @Test
    void plainWandUsesTopAndBottomCollars() {
        List<WandCapLayout.Placement> placements = WandCapLayout.create(false, false);

        assertEquals(2, placements.size());
        assertPlacement(placements.get(0), -1.0F / 16.0F, 1.3F, 1.5F);
        assertPlacement(placements.get(1), 16.0F / 16.0F, 1.3F, 1.5F);
        assertEquals(1.0F, WandCapLayout.outerScaleXZ(false), EPSILON);
        assertEquals(1.0F, WandCapLayout.outerScaleY(false), EPSILON);
    }

    @Test
    void sceptreUsesHeadAndCollarBeforeItsLowerCap() {
        List<WandCapLayout.Placement> placements = WandCapLayout.create(false, true);

        assertEquals(3, placements.size());
        assertPlacement(placements.get(0), -1.3F / 16.0F, 1.75F, 1.8F);
        assertPlacement(placements.get(1), 2.8F / 16.0F, 1.55F, 0.45F);
        assertPlacement(placements.get(2), 16.0F / 16.0F, 1.3F, 1.5F);
    }

    @Test
    void staffHasASeparateUpperCollarAndBottomCap() {
        List<WandCapLayout.Placement> placements = WandCapLayout.create(true, false);

        assertEquals(3, placements.size());
        assertPlacement(placements.get(0), -1.0F / 16.0F, 1.3F, 1.5F);
        assertPlacement(placements.get(1), 4.4F / 16.0F / 1.1F, 1.1F, 0.5F);
        assertPlacement(placements.get(2), 32.6F / 16.0F / 1.1F, 1.3F, 1.5F);
        assertEquals(1.2F, WandCapLayout.outerScaleXZ(true), EPSILON);
        assertEquals(1.1F, WandCapLayout.outerScaleY(true), EPSILON);
    }

    @Test
    void emittingEndIsOnTheSelectedCapFace() {
        assertEquals(-1.0F / 16, WandCapLayout.emittingEndY(false, false), EPSILON);
        assertEquals(-1.1F / 16, WandCapLayout.emittingEndY(true, false), EPSILON);
        assertEquals(-1.3F / 16, WandCapLayout.emittingEndY(false, true), EPSILON);
    }

    private static void assertPlacement(WandCapLayout.Placement placement, float offsetY, float scaleXZ, float scaleY) {
        assertEquals(offsetY, placement.offsetY(), EPSILON);
        assertEquals(scaleXZ, placement.scaleXZ(), EPSILON);
        assertEquals(scaleY, placement.scaleY(), EPSILON);
        assertEquals(1.0F, placement.pivotPixels(), EPSILON);
    }
}
