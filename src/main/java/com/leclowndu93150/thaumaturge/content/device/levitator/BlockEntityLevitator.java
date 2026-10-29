package com.leclowndu93150.thaumaturge.content.device.levitator;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BlockEntityLevitator extends AbstractSyncedBlockEntity {
    private static final String REACH_KEY = "reach";
    private static final String CHARGE_KEY = "charge";
    private static final int REFUEL_THRESHOLD = 10;
    private static final int CLEARANCE_INTERVAL = 10;
    private static final float VIS_DRAW = 1.0F;
    private static final int CHARGE_PER_VIS = 1200;
    private static final double THRUST = 0.1;
    private static final double SPEED_CAP = 0.35;
    private static final double HOVER_DAMPING = 0.9;
    private static final double LIFT = 0.08;
    private static final int RIDER_SYNC_INTERVAL = 20;
    private static final float IDLE_MIST_CHANCE = 0.1F;
    private static final float WORKING_MIST_CHANCE = 0.6F;

    private final BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
    private LevitatorReach reach = LevitatorReach.MEDIUM;
    private int clearance;
    private boolean clearanceStale = true;
    private int charge;

    public BlockEntityLevitator(BlockPos pos, BlockState state) {
        super(TTBlockEntities.LEVITATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityLevitator levitator) {
        levitator.step(level, pos, state);
    }

    public void cycleReach(Player player) {
        reach = reach.next();
        clearance = 0;
        clearanceStale = true;
        if (level != null && !level.isClientSide()) {
            setChangedAndSync();
            player.sendSystemMessage(
                    Component.translatable("gui.thaumaturge.levitator", reach.blocks(), reach.visCost()));
        }
    }

    private void refuel(Level level, BlockPos pos) {
        if (charge >= REFUEL_THRESHOLD) {
            return;
        }
        int gained = (int) (AuraHelper.drainVis(level, pos, VIS_DRAW, false) * CHARGE_PER_VIS);
        if (gained > 0) {
            charge += gained;
            setChangedAndSync();
        }
    }

    private int carry(Level level, BlockPos pos, Direction facing) {
        AABB beam = new AABB(pos)
                .expandTowards(
                        facing.getStepX() * (double) clearance,
                        facing.getStepY() * (double) clearance,
                        facing.getStepZ() * (double) clearance);
        List<Entity> riders = level.getEntitiesOfClass(Entity.class, beam, BlockEntityLevitator::isCarried);
        int carried = 0;
        for (Entity rider : riders) {
            if (charge <= 0) {
                break;
            }
            push(rider, facing);
            charge -= reach.visCost();
            carried++;
            if (level.isClientSide()) {
                LevitatorMist.cling(level, rider);
                LevitatorMist.stream(level, pos, facing, WORKING_MIST_CHANCE);
            }
        }
        if (carried > 0 && !level.isClientSide()) {
            setChanged();
        }
        return carried;
    }

    private static boolean isCarried(Entity entity) {
        if (!entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        return entity instanceof ItemEntity
                || entity instanceof Player
                || entity instanceof AbstractHorse
                || entity.isPushable();
    }

    static void push(Entity rider, Direction facing) {
        Vec3 motion = rider.getDeltaMovement();
        double x = motion.x;
        double y = motion.y;
        double z = motion.z;
        if (facing == Direction.UP && rider.isShiftKeyDown()) {
            y = y < 0.0 ? y * HOVER_DAMPING : y;
        } else {
            x = Math.clamp(x + facing.getStepX() * THRUST, -SPEED_CAP, SPEED_CAP);
            y = Math.clamp(y + facing.getStepY() * THRUST, -SPEED_CAP, SPEED_CAP);
            z = Math.clamp(z + facing.getStepZ() * THRUST, -SPEED_CAP, SPEED_CAP);
            if (facing.getAxis() != Direction.Axis.Y && !rider.onGround()) {
                y = (y < 0.0 ? y * HOVER_DAMPING : y) + LIFT;
            }
        }
        rider.setDeltaMovement(x, y, z);
        rider.resetFallDistance();
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        TTNbt.store(output, REACH_KEY, LevitatorReach.CODEC, registries, reach);
        output.putInt(CHARGE_KEY, charge);
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        reach = TTNbt.read(input, REACH_KEY, LevitatorReach.CODEC, registries)
                .orElseGet(() -> input.contains("range")
                        ? LevitatorReach.values()[
                                Mth.clamp(input.getByte("range"), 0, LevitatorReach.values().length - 1)]
                        : LevitatorReach.MEDIUM);
        charge = (input.contains(CHARGE_KEY) ? input.getInt(CHARGE_KEY) : input.getInt("vis"));
        clearance = Math.min(clearance, reach.blocks());
        clearanceStale = true;
    }

    private void step(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        if (clearanceStale || level.getGameTime() % CLEARANCE_INTERVAL == 0) {
            clearance = measureClearance(level, pos, facing);
            clearanceStale = false;
        }
        if (!level.isClientSide()) {
            refuel(level, pos);
        }
        if (clearance <= 0 || charge <= 0 || !state.getValue(BlockStateProperties.ENABLED)) {
            return;
        }
        int carried = carry(level, pos, facing);
        if (level.isClientSide()) {
            LevitatorMist.stream(level, pos, facing, IDLE_MIST_CHANCE);
        } else if (carried > 0 && level.getGameTime() % RIDER_SYNC_INTERVAL == 0) {
            setChangedAndSync();
        }
    }

    private int measureClearance(Level level, BlockPos pos, Direction facing) {
        int range = reach.blocks();
        for (int distance = 1; distance <= range; distance++) {
            probe.setWithOffset(
                    pos, facing.getStepX() * distance, facing.getStepY() * distance, facing.getStepZ() * distance);
            if (!level.hasChunkAt(probe) || level.getBlockState(probe).isSolidRender(level, probe)) {
                return distance;
            }
        }
        return range;
    }
}
