package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

public final class BlockEntityEverfullUrn extends AbstractSyncedBlockEntity {
    public static final int CAPACITY = 1000;

    private static final int TANK_SLOT = 0;
    private static final int WORK_INTERVAL = 5;
    private static final int PUSH_AMOUNT = 25;
    private static final int CAULDRON_COST = 333;
    private static final float MAX_VIS_PER_REFILL = 0.1F;
    private static final int PROBE_REACH = 2;
    private static final int PROBE_SIDE = PROBE_REACH * 2 + 1;
    private static final int PROBE_LAYERS = 3;
    private static final int PROBE_LAYER_AREA = PROBE_SIDE * PROBE_SIDE;
    private static final int CELL_COUNT = PROBE_LAYERS * PROBE_LAYER_AREA;
    private static final int OWN_CELL = (PROBE_LAYERS / 2) * PROBE_LAYER_AREA + (PROBE_SIDE / 2) * PROBE_SIDE + PROBE_SIDE / 2;
    private static final int FIRST_CELL = 0;
    private static final int FIRST_CAULDRON_LEVEL = 1;
    private static final int STREAM_COLOR = 0x4AB8FF;
    private static final int STREAM_TYPE = 0;
    private static final int STREAM_COUNT = 4;
    private static final float STREAM_SCALE = 0.1F;
    private static final int STREAM_EXTEND = 0;
    private static final double STREAM_LIFT = 0.2;
    private static final double SOURCE_HEIGHT = 0.66;
    private static final double BLOCK_CENTER = 0.5;
    private static final int SPLASH_COUNT = 4;
    private static final double SPLASH_SPREAD_HORIZONTAL = 0.2;
    private static final double SPLASH_SPREAD_VERTICAL = 0.1;
    private static final String TANK_KEY = "Tank";
    private static final String HANDLERS_KEY = "Handlers";

    private final UrnTank tank = new UrnTank();
    private final List<BlockPos> targets = new ArrayList<>();
    private int ticks;
    private int probeCursor = FIRST_CELL;

    public BlockEntityEverfullUrn(BlockPos pos, BlockState state) {
        super(TTBlockEntities.EVERFULL_URN.get(), pos, state);
    }

    public FluidStacksResourceHandler getTank() {
        return tank;
    }

    public int waterAmount() {
        return tank.getAmountAsInt(TANK_SLOT);
    }

    public void drainWater(int amount) {
        try (Transaction transaction = Transaction.openRoot()) {
            tank.extract(water(), amount, transaction);
            transaction.commit();
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityEverfullUrn urn) {
        if (++urn.ticks % WORK_INTERVAL != 0 || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        urn.probe(serverLevel);
        urn.distribute(serverLevel);
        urn.refill(serverLevel, pos);
    }

    private static FluidResource water() {
        return FluidResource.of(Fluids.WATER);
    }

    private static boolean isCauldron(BlockState state) {
        return state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON);
    }

    private static boolean acceptsWater(Level level, BlockPos pos) {
        return isCauldron(level.getBlockState(pos)) || level.getCapability(Capabilities.Fluid.BLOCK, pos, Direction.UP) != null;
    }

    private void probe(ServerLevel level) {
        int cell = probeCursor;
        probeCursor = (probeCursor + 1) % CELL_COUNT;
        if (cell == OWN_CELL) {
            return;
        }
        BlockPos candidate = positionOf(cell);
        if (targets.contains(candidate) || !level.hasChunkAt(candidate) || !acceptsWater(level, candidate)) {
            return;
        }
        targets.add(candidate);
        setChanged();
    }

    private BlockPos positionOf(int cell) {
        int layer = cell / PROBE_LAYER_AREA;
        int row = cell % PROBE_LAYER_AREA / PROBE_SIDE;
        int column = cell % PROBE_SIDE;
        return worldPosition.offset(column - PROBE_REACH, layer - PROBE_LAYERS / 2, row - PROBE_REACH);
    }

    private int cellOf(BlockPos target) {
        int column = target.getX() - worldPosition.getX() + PROBE_REACH;
        int layer = target.getY() - worldPosition.getY() + PROBE_LAYERS / 2;
        int row = target.getZ() - worldPosition.getZ() + PROBE_REACH;
        return layer * PROBE_LAYER_AREA + row * PROBE_SIDE + column;
    }

    private void distribute(ServerLevel level) {
        Iterator<BlockPos> walk = targets.iterator();
        while (walk.hasNext() && waterAmount() >= PUSH_AMOUNT) {
            BlockPos target = walk.next();
            if (!level.hasChunkAt(target)) {
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
            ResourceHandler<FluidResource> receiver = level.getCapability(Capabilities.Fluid.BLOCK, target, Direction.UP);
            if (receiver == null) {
                walk.remove();
                setChanged();
                continue;
            }
            if (push(receiver) > 0) {
                pour(level, target);
                return;
            }
        }
    }

    private void refill(ServerLevel level, BlockPos pos) {
        int stored = waterAmount();
        if (stored >= CAPACITY) {
            return;
        }
        float request = Math.min(MAX_VIS_PER_REFILL, (CAPACITY - stored) / (float) CAPACITY);
        float received = AuraHelper.drainVis(level, pos, request, false);
        int gained = Math.min(CAPACITY - stored, Math.round(received * CAPACITY));
        if (gained > 0) {
            tank.set(TANK_SLOT, water(), stored + gained);
        }
    }

    private void fillCauldron(ServerLevel level, BlockPos target, BlockState state) {
        if (state.is(Blocks.CAULDRON)) {
            level.setBlock(target, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, FIRST_CAULDRON_LEVEL), Block.UPDATE_CLIENTS);
        } else {
            int fill = state.getValue(LayeredCauldronBlock.LEVEL);
            if (fill >= LayeredCauldronBlock.MAX_FILL_LEVEL) {
                return;
            }
            level.setBlock(target, state.setValue(LayeredCauldronBlock.LEVEL, fill + 1), Block.UPDATE_CLIENTS);
            level.updateNeighbourForOutputSignal(target, state.getBlock());
        }
        drainWater(CAULDRON_COST);
        pour(level, target);
    }

    private int push(ResourceHandler<FluidResource> receiver) {
        try (Transaction transaction = Transaction.openRoot()) {
            int accepted = receiver.insert(water(), PUSH_AMOUNT, transaction);
            if (accepted > 0) {
                tank.extract(water(), accepted, transaction);
                transaction.commit();
            }
            return accepted;
        }
    }

    private void pour(ServerLevel level, BlockPos target) {
        Vec3 mouth = new Vec3(worldPosition.getX() + BLOCK_CENTER, worldPosition.getY() + SOURCE_HEIGHT, worldPosition.getZ() + BLOCK_CENTER);
        Vec3 inlet = new Vec3(target.getX() + BLOCK_CENTER, target.getY() + 1.0, target.getZ() + BLOCK_CENTER);
        EffectDispatch.spawnEssentiaStream(level, mouth, inlet, STREAM_COLOR, STREAM_TYPE, STREAM_COUNT, STREAM_SCALE, STREAM_EXTEND, STREAM_LIFT);
        level.sendParticles(ParticleTypes.SPLASH, inlet.x, inlet.y, inlet.z, SPLASH_COUNT, SPLASH_SPREAD_HORIZONTAL, SPLASH_SPREAD_VERTICAL, SPLASH_SPREAD_HORIZONTAL, 0.0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        tank.serialize(output.child(TANK_KEY));
        int[] cells = new int[targets.size()];
        for (int index = 0; index < cells.length; index++) {
            cells[index] = cellOf(targets.get(index));
        }
        output.putIntArray(HANDLERS_KEY, cells);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child(TANK_KEY).ifPresent(tank::deserialize);
        targets.clear();
        input.getIntArray(HANDLERS_KEY).ifPresent(this::loadTargets);
    }

    private void loadTargets(int[] cells) {
        for (int cell : cells) {
            if (cell >= 0 && cell < CELL_COUNT && cell != OWN_CELL) {
                BlockPos restored = positionOf(cell);
                if (!targets.contains(restored)) {
                    targets.add(restored);
                }
            }
        }
    }

    private final class UrnTank extends FluidStacksResourceHandler {
        private UrnTank() {
            super(TANK_SLOT + 1, CAPACITY);
        }

        @Override
        public boolean isValid(int index, FluidResource resource) {
            return resource.getFluid() == Fluids.WATER;
        }

        @Override
        public int insert(int index, FluidResource resource, int amount, TransactionContext transaction) {
            return 0;
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousContents) {
            setChanged();
        }
    }
}
