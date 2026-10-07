package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.content.infernalfurnace.BlockEntityInfernalFurnace;
import com.leclowndu93150.thaumaturge.gametest.base.TTTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public final class InfernalFurnaceTests {
    private InfernalFurnaceTests() {}

    public static void register(TTTestRegistrar r) {
        r.add("infernal_furnace/idle_does_not_mark_changed", 20, helper -> {
            CountingFurnace furnace = createFurnace(helper);
            for (int tick = 0; tick < 200; tick++) {
                tick(furnace, helper);
            }
            helper.assertTrue(furnace.changes == 0, "Idle furnace must not repeatedly mark its chunk changed");
            helper.succeed();
        });

        r.add("infernal_furnace/cook_progress_marks_changed", 20, helper -> {
            CountingFurnace furnace = createFurnace(helper);
            furnace.furnaceCookTime = 3;
            tick(furnace, helper);
            helper.assertTrue(furnace.furnaceCookTime == 2, "Cooking must still advance each tick");
            helper.assertTrue(furnace.changes == 1, "Saved cooking progress must mark the furnace changed");
            helper.succeed();
        });

        r.add("infernal_furnace/inventory_commit_marks_changed", 20, helper -> {
            CountingFurnace furnace = createFurnace(helper);
            ItemResource iron = ItemResource.of(new ItemStack(Items.RAW_IRON));
            try (Transaction transaction = Transaction.openRoot()) {
                furnace.inventory().insert(0, iron, 2, transaction);
            }
            helper.assertTrue(furnace.changes == 0, "Aborted insertion must not mark the furnace changed");
            helper.assertTrue(furnace.inventory().getAmountAsInt(0) == 0, "Aborted insertion must leave the inventory empty");
            try (Transaction transaction = Transaction.openRoot()) {
                furnace.inventory().insert(0, iron, 2, transaction);
                transaction.commit();
            }
            helper.assertTrue(furnace.changes == 1, "Committed insertion must mark the furnace changed immediately");
            furnace.inventory().set(0, iron, 1);
            helper.assertTrue(furnace.changes == 2, "Consuming an item must mark the furnace changed");
            helper.succeed();
        });
    }

    private static CountingFurnace createFurnace(GameTestHelper helper) {
        CountingFurnace furnace = new CountingFurnace(helper.absolutePos(new BlockPos(2, 2, 3)));
        furnace.setLevel(helper.getLevel());
        furnace.speedyTime = 20;
        return furnace;
    }

    private static void tick(CountingFurnace furnace, GameTestHelper helper) {
        BlockEntityInfernalFurnace.staticTick(helper.getLevel(), furnace.getBlockPos(), furnace.getBlockState(), furnace);
    }

    private static final class CountingFurnace extends BlockEntityInfernalFurnace {
        private int changes;

        private CountingFurnace(BlockPos pos) {
            super(pos, TTBlocks.INFERNAL_FURNACE.get().defaultBlockState());
        }

        @Override
        public void setChanged() {
            changes++;
        }
    }
}
