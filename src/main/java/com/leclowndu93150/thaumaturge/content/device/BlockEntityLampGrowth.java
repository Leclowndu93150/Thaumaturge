package com.leclowndu93150.thaumaturge.content.device;

import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaIntake;
import com.leclowndu93150.thaumaturge.content.essentia.flow.EssentiaIntakeHost;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TTBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.jspecify.annotations.Nullable;

public final class BlockEntityLampGrowth extends AbstractSyncedBlockEntity implements EssentiaIntakeHost {
    private static final int INTAKE_SUCTION = 128;
    private static final int INTAKE_INTERVAL = 5;
    private static final int MAX_CHARGES = 20;
    private static final int IDLE_CHARGES = -1;
    private static final int RADIUS = 6;
    private static final int COLUMN_SIDE = RADIUS * 2 + 1;
    private static final int COLUMN_COUNT = COLUMN_SIDE * COLUMN_SIDE;
    private static final int RANGE_SQR = 36;
    private static final int NETHER_WART_MATURE_AGE = 3;
    private static final int FEEDBACK_PARTICLES = 6;
    private static final double FEEDBACK_SPREAD = 0.3;
    private static final double BLOCK_CENTER = 0.5;
    private static final String RESERVE_KEY = "reserve";
    private static final String CHARGES_KEY = "charges";

    private final EssentiaIntake intake = new EssentiaIntake(this, TTAspects.HERBA, INTAKE_SUCTION, INTAKE_INTERVAL);
    private final int[] columnOrder = new int[COLUMN_COUNT];
    private final BlockPos.MutableBlockPos cell = new BlockPos.MutableBlockPos();
    private int columnsLeft;
    private int charges = IDLE_CHARGES;
    private boolean reserve;
    private BlockPos lastTarget = BlockPos.ZERO;
    private @Nullable BlockState lastState;

    public BlockEntityLampGrowth(BlockPos pos, BlockState state) {
        super(TTBlockEntities.LAMP_GROWTH.get(), pos, state);
        for (int index = 0; index < COLUMN_COUNT; index++) {
            columnOrder[index] = index;
        }
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BlockEntityLampGrowth lamp) {
        lamp.pullEssentia();
        lamp.transferReserve();
        lamp.normalizeIdle();
        boolean running = !level.hasNeighborSignal(pos) && lamp.charges > 0;
        BlockLamp.showLit(level, pos, state, running);
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (running) {
            lamp.growOnce(serverLevel, pos);
        }
        lamp.showFeedback(serverLevel);
    }

    private void pullEssentia() {
        if (!wantsEssentia() || !intake.pullOne()) {
            return;
        }
        if (charges > 0) {
            reserve = true;
        } else {
            charges = MAX_CHARGES;
        }
        setChanged();
    }

    private void transferReserve() {
        if (charges > 0 || !reserve) {
            return;
        }
        reserve = false;
        charges = MAX_CHARGES;
        setChanged();
    }

    private void normalizeIdle() {
        if (charges != 0) {
            return;
        }
        charges = IDLE_CHARGES;
        setChanged();
    }

    private void growOnce(ServerLevel level, BlockPos origin) {
        int column = takeColumn(level.getRandom());
        int offsetX = column / COLUMN_SIDE - RADIUS;
        int offsetZ = column % COLUMN_SIDE - RADIUS;
        if (growColumn(level, origin, offsetX, offsetZ)) {
            return;
        }
        for (BlockPos center : LevelSpaces.foreignCentersNear(level, origin, RADIUS)) {
            if (growColumn(level, center, offsetX, offsetZ)) {
                return;
            }
        }
    }

    private boolean growColumn(ServerLevel level, BlockPos origin, int offsetX, int offsetZ) {
        if (!LoadedChunks.contains(level, cell.set(origin.getX() + offsetX, origin.getY(), origin.getZ() + offsetZ))) {
            return false;
        }
        int reach = verticalReach(offsetX * offsetX + offsetZ * offsetZ);
        for (int offsetY = reach; offsetY >= -reach; offsetY--) {
            cell.set(origin.getX() + offsetX, origin.getY() + offsetY, origin.getZ() + offsetZ);
            BlockState target = level.getBlockState(cell);
            if (isValidTarget(level, cell, target)) {
                applyGrowth(level, target);
                return true;
            }
        }
        return false;
    }

    private static int verticalReach(int horizontalSqr) {
        int room = RANGE_SQR - horizontalSqr;
        int reach = -1;
        while ((reach + 1) * (reach + 1) < room && reach < RADIUS) {
            reach++;
        }
        return reach;
    }

    private void applyGrowth(ServerLevel level, BlockState target) {
        charges--;
        lastTarget = cell.immutable();
        lastState = target;
        if (target.isRandomlyTicking()) {
            target.randomTick(level, lastTarget, level.getRandom());
        }
        setChanged();
    }

    private int takeColumn(RandomSource random) {
        if (columnsLeft <= 0) {
            columnsLeft = COLUMN_COUNT;
        }
        int lastSlot = columnsLeft - 1;
        int slot = random.nextInt(columnsLeft);
        int column = columnOrder[slot];
        columnOrder[slot] = columnOrder[lastSlot];
        columnOrder[lastSlot] = column;
        columnsLeft--;
        return column;
    }

    private static boolean isValidTarget(Level level, BlockPos pos, BlockState state) {
        if (state.isAir() || state.is(TTBlockTags.LAMP_GROWTH_BLACKLIST)) {
            return false;
        }
        return isPlant(state.getBlock()) && !isFullyGrown(level, pos, state);
    }

    private static boolean isPlant(Block block) {
        if (block == Blocks.CACTUS || block == Blocks.SUGAR_CANE || block == Blocks.NETHER_WART) {
            return true;
        }
        return block instanceof BonemealableBlock && !(block instanceof GrassBlock);
    }

    private static boolean isFullyGrown(Level level, BlockPos pos, BlockState state) {
        Block block = state.getBlock();
        if (block instanceof StemBlock) {
            return false;
        }
        if (block instanceof CropBlock crop) {
            return crop.isMaxAge(state);
        }
        if (block == Blocks.NETHER_WART) {
            return state.getValue(NetherWartBlock.AGE) >= NETHER_WART_MATURE_AGE;
        }
        return block instanceof BonemealableBlock bonemealable
                && !bonemealable.isValidBonemealTarget(level, pos, state);
    }

    private void showFeedback(ServerLevel level) {
        if (lastState == null) {
            return;
        }
        BlockState current = level.getBlockState(lastTarget);
        if (current == lastState) {
            return;
        }
        lastState = current;
        level.sendParticles(
                ParticleTypes.HAPPY_VILLAGER,
                lastTarget.getX() + BLOCK_CENTER,
                lastTarget.getY() + BLOCK_CENTER,
                lastTarget.getZ() + BLOCK_CENTER,
                FEEDBACK_PARTICLES,
                FEEDBACK_SPREAD,
                FEEDBACK_SPREAD,
                FEEDBACK_SPREAD,
                0.0);
    }

    public EssentiaIntake intake() {
        return intake;
    }

    @Override
    public Direction intakeFace() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }

    @Override
    public boolean wantsEssentia() {
        return !reserve || charges <= 0;
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        reserve = input.getBoolean(RESERVE_KEY);
        charges = input.contains(CHARGES_KEY) ? input.getInt(CHARGES_KEY) : IDLE_CHARGES;
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putBoolean(RESERVE_KEY, reserve);
        output.putInt(CHARGES_KEY, charges);
    }
}
