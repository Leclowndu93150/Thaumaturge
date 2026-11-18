package com.leclowndu93150.thaumaturge.client.model;

import java.util.ArrayList;
import java.util.List;

final class WandCapLayout {
    private static final float WAND_OUTER_XZ = 1.0F;
    private static final float WAND_OUTER_Y = 1.0F;
    private static final float STAFF_OUTER_XZ = 1.2F;
    private static final float STAFF_OUTER_Y = 1.1F;
    private static final float COLLAR_XZ = 1.3F;
    private static final float COLLAR_Y = 1.5F;
    private static final float CAP_PIVOT_PIXELS = 1.0F;
    private static final float PIXEL = 1.0F / 16.0F;
    private static final Placement SCEPTRE_HEAD = new Placement(-1.3F * PIXEL, 1.75F, 1.8F, CAP_PIVOT_PIXELS);
    private static final Placement WAND_HEAD = new Placement(-PIXEL, COLLAR_XZ, COLLAR_Y, CAP_PIVOT_PIXELS);

    private WandCapLayout() {}

    static float outerScaleXZ(boolean staff) {
        return staff ? STAFF_OUTER_XZ : WAND_OUTER_XZ;
    }

    static float outerScaleY(boolean staff) {
        return staff ? STAFF_OUTER_Y : WAND_OUTER_Y;
    }

    static List<Placement> create(boolean staff, boolean sceptre) {
        List<Placement> placements = new ArrayList<>();
        if (sceptre) {
            placements.add(SCEPTRE_HEAD);
            placements.add(new Placement(2.8F * PIXEL, 1.55F, 0.45F, CAP_PIVOT_PIXELS));
        } else {
            placements.add(WAND_HEAD);
        }
        if (staff) {
            placements.add(new Placement(4.4F * PIXEL / STAFF_OUTER_Y, 1.1F, 0.5F, CAP_PIVOT_PIXELS));
            placements.add(new Placement(32.6F * PIXEL / STAFF_OUTER_Y, COLLAR_XZ, COLLAR_Y, CAP_PIVOT_PIXELS));
        } else {
            placements.add(new Placement(16.0F * PIXEL, COLLAR_XZ, COLLAR_Y, CAP_PIVOT_PIXELS));
        }
        return List.copyOf(placements);
    }

    static float emittingEndY(boolean staff, boolean sceptre) {
        Placement cap = sceptre ? SCEPTRE_HEAD : WAND_HEAD;
        return outerScaleY(staff) * (cap.offsetY() + (cap.pivotPixels() - 1.0F) * PIXEL * cap.scaleY());
    }

    record Placement(float offsetY, float scaleXZ, float scaleY, float pivotPixels) {}
}
