package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSeed;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.gametest.base.TCTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

public final class TaintTests {
    private static final BlockPos SEED_POS = new BlockPos(2, 2, 3);
    private static final int SETTLE_TICKS = 10;
    private static final int SPREAD_ATTEMPTS = 500;
    private static final int SCAN_RADIUS = 3;

    private TaintTests() {}

    public static void register(TCTestRegistrar r) {
        r.add("taint/seed_enables_spread", 80, helper -> {
            buildPlatform(helper);
            AuraHelper.addFlux(helper.getLevel(), helper.absolutePos(SEED_POS), 50.0F);
            EntityTaintSeed seed = helper.spawn(TCEntities.TAINT_SEED.get(), SEED_POS);
            helper.runAfterDelay(SETTLE_TICKS, () -> {
                BlockPos center = helper.absolutePos(SEED_POS);
                for (int i = 0; i < SPREAD_ATTEMPTS; i++) {
                    TaintHelper.spreadFibres(helper.getLevel(), center, true);
                }
                if (countFibres(helper) == 0) {
                    helper.fail("No taint fibres appeared after " + SPREAD_ATTEMPTS + " forced spread attempts");
                    return;
                }
                seed.discard();
                helper.succeed();
            });
        });
    }

    private static void buildPlatform(GameTestHelper helper) {
        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 6; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }
    }

    private static int countFibres(GameTestHelper helper) {
        int found = 0;
        BlockPos center = helper.absolutePos(SEED_POS);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-SCAN_RADIUS, -1, -SCAN_RADIUS), center.offset(SCAN_RADIUS, SCAN_RADIUS, SCAN_RADIUS))) {
            if (helper.getLevel().getBlockState(pos).is(TCBlocks.TAINT_FIBRE.get())) {
                found++;
            }
        }
        return found;
    }
}
