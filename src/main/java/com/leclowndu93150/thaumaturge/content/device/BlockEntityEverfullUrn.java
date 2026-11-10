package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

public final class BlockEntityEverfullUrn extends AbstractSyncedBlockEntity {
    public static final int CAPACITY = 1000;
    private static final int WORK_INTERVAL = 5;
    private static final int PUSH_AMOUNT = 25;
    private static final int CAULDRON_COST = 333;
    private static final float MAX_REFILL_VIS = 0.1F;
    private static final int PROBE_REACH = 2;
    private static final int PROBE_SIDE = PROBE_REACH * 2 + 1;
    private static final int PROBE_LAYERS = 3;
    private static final int PROBE_LAYER_AREA = PROBE_SIDE * PROBE_SIDE;
    private static final int CELL_COUNT = PROBE_LAYERS * PROBE_LAYER_AREA;
    private static final int OWN_CELL = (PROBE_LAYERS / 2) * PROBE_LAYER_AREA + PROBE_REACH * PROBE_SIDE + PROBE_REACH;
    private static final int FIRST_CELL = 0;
    private static final int FIRST_CAULDRON_LEVEL = 1;
    private static final int TRAIL_COLOR = 0x4AB8FF;
    private static final int TRAIL_TYPE = 0;
    private static final int TRAIL_COUNT = 4;
    private static final double TRAIL_SOURCE_Y = 0.66;
    private static final float TRAIL_SCALE = 0.1F;
    private static final int TRAIL_EXTEND = 0;
    private static final double TRAIL_UPWARD = 0.2;
    private static final int SPLASH_COUNT = 4;
    private static final double SPLASH_SPREAD_HORIZONTAL = 0.2;
    private static final double SPLASH_SPREAD_VERTICAL = 0.1;
    private static final String TANK_KEY = "Tank";
    private static final String HANDLERS_KEY = "Handlers";

    private final UrnTank tank = new UrnTank();
    private final List<Integer> targets = new ArrayList<>();
    private int ticks;
    private int probeCursor = FIRST_CELL;

    public BlockEntityEverfullUrn(BlockPos pos, BlockState state) {
        super(TTBlockEntities.EVERFULL_URN.get(), pos, state);
    }

    public FluidTank getTank() {
        return tank;
    }

    public int waterAmount() {
        return tank.getFluidAmount();
    }

    public void drainWater(int amount) {
        tank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
    }

    private void fillWater(int amount) {
        tank.fillInternally(new FluidStack(Fluids.WATER, amount));
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEverfullUrn urn) {
        if (++urn.ticks % WORK_INTERVAL != 0 || !(level instanceof ServerLevel server)) {
            return;
        }
        urn.probe(server);
        urn.distribute(server);
        urn.refill(server, pos);
    }

    private void probe(ServerLevel level) {
        int cell = probeCursor;
        probeCursor = (probeCursor + 1) % CELL_COUNT;
        if (cell == OWN_CELL) {
            return;
        }
        BlockPos candidate = positionOf(cell);
        if (targets.contains(cell) || !LoadedChunks.contains(level, candidate) || !acceptsWater(level, candidate)) {
            return;
        }
        targets.add(cell);
        setChanged();
    }

    private BlockPos positionOf(int cell) {
        int layer = cell / PROBE_LAYER_AREA;
        int x = cell % PROBE_LAYER_AREA / PROBE_SIDE;
        int z = cell % PROBE_SIDE;
        return worldPosition.offset(x - PROBE_REACH, layer - PROBE_LAYERS / 2, z - PROBE_REACH);
    }

    private static boolean isCauldron(BlockState state) {
        return state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON);
    }

    private static boolean acceptsWater(Level level, BlockPos pos) {
        return isCauldron(level.getBlockState(pos))
                || level.getCapability(Capabilities.FluidHandler.BLOCK, pos, Direction.UP) != null;
    }

    private void distribute(ServerLevel level) {
        Iterator<Integer> walk = targets.iterator();
        while (walk.hasNext() && waterAmount() >= PUSH_AMOUNT) {
            BlockPos target = positionOf(walk.next());
            if (!LoadedChunks.contains(level, target)) {
                continue;
            }
            BlockState state = level.getBlockState(target);
            if (isCauldron(state)) {
                if (waterAmount() < CAULDRON_COST) {
                    walk.remove();
                    setChanged();
                } else {
                    fillCauldron(level, target, state);
                }
                continue;
            }
            IFluidHandler receiver = level.getCapability(Capabilities.FluidHandler.BLOCK, target, Direction.UP);
            if (receiver == null) {
                walk.remove();
                setChanged();
                continue;
            }
            int accepted = receiver.fill(new FluidStack(Fluids.WATER, PUSH_AMOUNT), IFluidHandler.FluidAction.EXECUTE);
            if (accepted > 0) {
                drainWater(accepted);
                pourInto(level, target);
                return;
            }
        }
    }

    private void fillCauldron(ServerLevel level, BlockPos target, BlockState state) {
        if (state.is(Blocks.CAULDRON)) {
            level.setBlock(
                    target,
                    Blocks.WATER_CAULDRON
                            .defaultBlockState()
                            .setValue(LayeredCauldronBlock.LEVEL, FIRST_CAULDRON_LEVEL),
                    Block.UPDATE_CLIENTS);
        } else {
            int fill = state.getValue(LayeredCauldronBlock.LEVEL);
            if (fill >= LayeredCauldronBlock.MAX_FILL_LEVEL) {
                return;
            }
            level.setBlock(target, state.setValue(LayeredCauldronBlock.LEVEL, fill + 1), Block.UPDATE_CLIENTS);
            level.updateNeighbourForOutputSignal(target, state.getBlock());
        }
        drainWater(CAULDRON_COST);
        pourInto(level, target);
    }

    private void refill(ServerLevel level, BlockPos pos) {
        int stored = waterAmount();
        if (stored >= CAPACITY) {
            return;
        }
        float request = Math.min(MAX_REFILL_VIS, (CAPACITY - stored) / (float) CAPACITY);
        float received = AuraHelper.drainVis(level, pos, request, false);
        int gained = Math.min(CAPACITY - stored, Math.round(received * CAPACITY));
        if (gained > 0) {
            fillWater(gained);
        }
    }

    private void pourInto(ServerLevel server, BlockPos target) {
        Vec3 from = new Vec3(
                getBlockPos().getX() + 0.5,
                getBlockPos().getY() + TRAIL_SOURCE_Y,
                getBlockPos().getZ() + 0.5);
        EffectDispatch.spawnEssentiaStream(
                server,
                from,
                Vec3.atCenterOf(target),
                TRAIL_COLOR,
                TRAIL_TYPE,
                TRAIL_COUNT,
                TRAIL_SCALE,
                TRAIL_EXTEND,
                TRAIL_UPWARD);
        server.sendParticles(
                ParticleTypes.SPLASH,
                target.getX() + 0.5,
                target.getY() + 1.0,
                target.getZ() + 0.5,
                SPLASH_COUNT,
                SPLASH_SPREAD_HORIZONTAL,
                SPLASH_SPREAD_VERTICAL,
                SPLASH_SPREAD_HORIZONTAL,
                0.0);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.put(TANK_KEY, tank.writeToNBT(registries, new CompoundTag()));
        int[] cells = new int[targets.size()];
        for (int i = 0; i < cells.length; i++) {
            cells[i] = targets.get(i);
        }
        output.putIntArray(HANDLERS_KEY, cells);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        if (input.contains(TANK_KEY)) {
            tank.readFromNBT(registries, input.getCompound(TANK_KEY));
        }
        targets.clear();
        for (int cell : input.getIntArray(HANDLERS_KEY)) {
            if (cell >= 0 && cell < CELL_COUNT && cell != OWN_CELL && !targets.contains(cell)) {
                targets.add(cell);
            }
        }
    }

    private final class UrnTank extends FluidTank {
        private UrnTank() {
            super(CAPACITY);
        }

        @Override
        public boolean isFluidValid(FluidStack resource) {
            return resource.is(Fluids.WATER);
        }

        @Override
        public int fill(FluidStack resource, IFluidHandler.FluidAction action) {
            return 0;
        }

        private void fillInternally(FluidStack resource) {
            super.fill(resource, IFluidHandler.FluidAction.EXECUTE);
        }

        @Override
        protected void onContentsChanged() {
            super.onContentsChanged();
            setChanged();
        }
    }
}
