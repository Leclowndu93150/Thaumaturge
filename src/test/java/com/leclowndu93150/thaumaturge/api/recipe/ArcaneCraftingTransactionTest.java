package com.leclowndu93150.thaumaturge.api.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.junit.jupiter.api.Test;

class ArcaneCraftingTransactionTest {
    @Test
    void resultsDoNotExposeMutableStacks() {
        ItemStack output = new ItemStack(Items.STONE, 2);
        ItemStack remainder = new ItemStack(Items.DIRT, 3);
        ArcaneCraftingTransaction.Result result = new ArcaneCraftingTransaction.Result(
                true, false, ArcaneCraftingTransaction.Failure.NONE, output, List.of(remainder), null);
        ArcaneCraftingTransaction.Inspection inspection = new ArcaneCraftingTransaction.Inspection(
                true, ArcaneCraftingTransaction.Failure.NONE, null, output, List.of(remainder), null);

        output.setCount(64);
        remainder.setCount(64);
        assertDefensive(result::output, result::remainders);
        assertDefensive(inspection::output, inspection::remainders);
    }

    @Test
    void workbenchContextCopiesPositionsAndEnforcesKind() {
        ServerLevel level = mock(ServerLevel.class);
        BlockPos.MutableBlockPos position = new BlockPos.MutableBlockPos(1, 2, 3);
        ArcaneWorkbenchContext context = new ArcaneWorkbenchContext(
                level, UUID.randomUUID(), position, null, UUID.randomUUID(), ArcaneWorkbenchContext.Kind.PLACED);
        position.move(5, 5, 5);

        assertEquals(new BlockPos(1, 2, 3), context.blockPosition().orElseThrow());
        assertThrows(
                IllegalArgumentException.class,
                () -> new ArcaneWorkbenchContext(
                        level, UUID.randomUUID(), null, null, UUID.randomUUID(), ArcaneWorkbenchContext.Kind.PLACED));
        assertThrows(
                IllegalArgumentException.class,
                () -> new ArcaneWorkbenchContext(
                        level,
                        UUID.randomUUID(),
                        BlockPos.ZERO,
                        null,
                        UUID.randomUUID(),
                        ArcaneWorkbenchContext.Kind.VIRTUAL));
    }

    private static void assertDefensive(
            Supplier<ItemStack> outputSupplier, Supplier<List<ItemStack>> remaindersSupplier) {
        ItemStack output = outputSupplier.get();
        List<ItemStack> remainders = remaindersSupplier.get();
        assertEquals(2, output.getCount());
        assertEquals(3, remainders.getFirst().getCount());
        output.setCount(1);
        remainders.getFirst().setCount(1);
        assertEquals(2, outputSupplier.get().getCount());
        assertEquals(3, remaindersSupplier.get().getFirst().getCount());
    }
}
