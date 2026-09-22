package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.content.essentia.advancedfurnace.BlockEntityAdvancedAlchemicalFurnace;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;

/** Resolves every block occupied by a formed Advanced Alchemical Furnace to its controller. */
final class AdvancedFurnaceJadeAccess {
    private AdvancedFurnaceJadeAccess() {}

    static @Nullable BlockEntityAdvancedAlchemicalFurnace resolve(BlockAccessor accessor) {
        BlockEntity direct = accessor.getBlockEntity();
        if (direct instanceof BlockEntityAdvancedAlchemicalFurnace furnace) return furnace;
        if (!isPart(accessor.getBlockState())) return null;

        BlockPos origin = accessor.getPosition();
        for (int y = -1; y <= 0; y++) {
            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    BlockPos candidate = origin.offset(x, y, z);
                    if (accessor.getLevel().getBlockEntity(candidate)
                            instanceof BlockEntityAdvancedAlchemicalFurnace furnace) {
                        return furnace;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isPart(BlockState state) {
        return state.is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE.get())
                || state.is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE.get())
                || state.is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER.get())
                || state.is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER.get())
                || state.is(TCBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER.get());
    }
}
