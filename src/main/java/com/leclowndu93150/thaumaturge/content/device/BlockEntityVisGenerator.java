package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class BlockEntityVisGenerator extends BlockEntity implements EnergyHandler {
    private static final int CAPACITY = 1000;
    private static final int MAX_PUSH = 20;
    private static final float VIS_PER_CHARGE = 1.0F;
    private static final int ENERGY_PER_VIS = 1000;

    private int energy;

    public BlockEntityVisGenerator(BlockPos pos, BlockState state) {
        super(TCBlockEntities.VIS_GENERATOR.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityVisGenerator generator) {
        if (!state.getValue(BlockStateProperties.ENABLED)) {
            return;
        }
        if (generator.energy == 0) {
            float vis = AuraHelper.drainVis(level, pos, VIS_PER_CHARGE, false);
            generator.energy = (int) (vis * ENERGY_PER_VIS);
            generator.setChanged();
        }
        Direction facing = state.getValue(BlockStateProperties.FACING);
        EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, pos.relative(facing), facing.getOpposite());
        if (target != null) {
            int pushed;
            try (Transaction ctx = Transaction.openRoot()) {
                pushed = target.insert(Math.min(generator.energy, MAX_PUSH), ctx);
                if (pushed > 0) {
                    ctx.commit();
                }
            }
            if (pushed > 0) {
                generator.energy -= pushed;
                generator.setChanged();
            }
        }
    }

    public Direction outputFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public long getAmountAsLong() {
        return energy;
    }

    @Override
    public long getCapacityAsLong() {
        return CAPACITY;
    }

    @Override
    public int insert(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    public int extract(int amount, TransactionContext transaction) {
        return 0;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energy = input.getIntOr("energy", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("energy", energy);
    }
}
