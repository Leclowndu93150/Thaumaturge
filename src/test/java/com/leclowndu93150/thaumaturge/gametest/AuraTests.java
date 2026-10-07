package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import net.minecraft.core.BlockPos;

public final class AuraTests {
    private static final BlockPos SPOT = new BlockPos(2, 1, 3);
    private static final float EPSILON = 0.01F;

    private AuraTests() {}

    public static void register(TTTestRegistrar r) {
        r.add("aura/vis_add_drain_roundtrip", 20, helper -> {
            BlockPos pos = helper.absolutePos(SPOT);
            float before = AuraHelper.getVis(helper.getLevel(), pos);
            AuraHelper.addVis(helper.getLevel(), pos, 25.0F);
            float afterAdd = AuraHelper.getVis(helper.getLevel(), pos);
            if (afterAdd < before + 25.0F - EPSILON) {
                helper.fail("Vis add did not register: before " + before + " after " + afterAdd);
                return;
            }
            float drained = AuraHelper.drainVis(helper.getLevel(), pos, 25.0F, false);
            if (Math.abs(drained - 25.0F) > EPSILON) {
                helper.fail("Expected to drain 25 vis, drained " + drained);
                return;
            }
            float afterDrain = AuraHelper.getVis(helper.getLevel(), pos);
            if (Math.abs(afterDrain - before) > EPSILON) {
                helper.fail("Vis not restored to baseline: before " + before + " after " + afterDrain);
                return;
            }
            helper.succeed();
        });

        r.add("aura/drain_simulate_is_pure", 20, helper -> {
            BlockPos pos = helper.absolutePos(SPOT);
            AuraHelper.addVis(helper.getLevel(), pos, 10.0F);
            float before = AuraHelper.getVis(helper.getLevel(), pos);
            AuraHelper.drainVis(helper.getLevel(), pos, 5.0F, true);
            float after = AuraHelper.getVis(helper.getLevel(), pos);
            if (Math.abs(after - before) > EPSILON) {
                helper.fail("Simulated drain changed vis: before " + before + " after " + after);
                return;
            }
            helper.succeed();
        });

        r.add("aura/flux_accumulates", 20, helper -> {
            BlockPos pos = helper.absolutePos(SPOT);
            float before = AuraHelper.getFlux(helper.getLevel(), pos);
            AuraHelper.addFlux(helper.getLevel(), pos, 7.0F);
            float after = AuraHelper.getFlux(helper.getLevel(), pos);
            if (after < before + 7.0F - EPSILON) {
                helper.fail("Flux add did not register: before " + before + " after " + after);
                return;
            }
            helper.succeed();
        });
    }
}
