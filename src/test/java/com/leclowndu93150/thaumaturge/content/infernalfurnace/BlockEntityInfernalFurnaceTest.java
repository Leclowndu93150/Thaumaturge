package com.leclowndu93150.thaumaturge.content.infernalfurnace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class BlockEntityInfernalFurnaceTest {
    private final ServerLevel level = mock(ServerLevel.class);

    @Test
    void idleDoesNotMarkChanged() {
        CountingFurnace furnace = createFurnace();
        for (int tick = 0; tick < 200; tick++) {
            tick(furnace);
        }
        assertEquals(0, furnace.changes, "Idle furnace must not repeatedly mark its chunk changed");
    }

    @Test
    void cookProgressMarksChanged() {
        CountingFurnace furnace = createFurnace();
        furnace.furnaceCookTime = 3;
        tick(furnace);
        assertEquals(2, furnace.furnaceCookTime, "Cooking must still advance each tick");
        assertEquals(1, furnace.changes, "Saved cooking progress must mark the furnace changed");
    }

    @Test
    void inventoryChangesMarkChangedButSimulationDoesNot() {
        CountingFurnace furnace = createFurnace();
        ItemStack iron = new ItemStack(Items.RAW_IRON, 2);
        assertTrue(furnace.inventory().insertItem(0, iron, true).isEmpty());
        assertEquals(0, furnace.changes, "Simulated insertion must not mark the furnace changed");
        assertTrue(furnace.inventory().getStackInSlot(0).isEmpty());
        assertTrue(furnace.inventory().insertItem(0, iron, false).isEmpty());
        assertEquals(1, furnace.changes, "Real insertion must mark the furnace changed immediately");
        furnace.inventory().setStackInSlot(0, iron.copyWithCount(1));
        assertEquals(2, furnace.changes, "Consuming an item must mark the furnace changed");
    }

    private CountingFurnace createFurnace() {
        CountingFurnace furnace = new CountingFurnace();
        furnace.setLevel(level);
        furnace.speedyTime = 20;
        return furnace;
    }

    private void tick(CountingFurnace furnace) {
        BlockEntityInfernalFurnace.staticTick(level, furnace.getBlockPos(), furnace.getBlockState(), furnace);
    }

    private static final class CountingFurnace extends BlockEntityInfernalFurnace {
        private int changes;

        private CountingFurnace() {
            super(BlockPos.ZERO, TTBlocks.INFERNAL_FURNACE.get().defaultBlockState());
        }

        @Override
        public void setChanged() {
            changes++;
        }
    }
}
