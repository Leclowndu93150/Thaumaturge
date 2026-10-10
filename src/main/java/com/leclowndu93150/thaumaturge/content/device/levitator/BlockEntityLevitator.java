package com.leclowndu93150.thaumaturge.content.device.levitator;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.blockentity.AbstractSyncedBlockEntity;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BlockEntityLevitator extends AbstractSyncedBlockEntity {
    private static final String REACH_KEY = "reach";
    private static final String CHARGE_KEY = "charge";
    private static final int REFUEL_THRESHOLD = 10;
    private static final float REFUEL_DRAW = 1.0F;
    private static final int CHARGE_PER_VIS = 1200;
    private static final int CLEARANCE_INTERVAL = 10;
    private static final int SYNC_INTERVAL = 20;
    private static final double PUSH = 0.1;
    private static final double SPEED_CAP = 0.35;
    private static final double DAMPING = 0.7;
    private static final double LIFT = 1.0;
    private static final float IDLE_STREAM_CHANCE = 0.1F;
    private static final float STREAM_CHANCE = 0.6F;

    private LevitatorReach reach = LevitatorReach.MEDIUM;
    private int charge;
    private int clearance;
    private boolean clearanceStale = true;

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
            player.sendSystemMessage(Component.translatable("gui.thaumaturge.levitator", reach.blocks(), reach.visCost()));
        }
    }

    void markClearanceStale() {
        clearanceStale = true;
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store(REACH_KEY, LevitatorReach.CODEC, reach);
        output.putInt(CHARGE_KEY, charge);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        reach = input.read(REACH_KEY, LevitatorReach.CODEC).orElse(LevitatorReach.MEDIUM);
        charge = input.getIntOr(CHARGE_KEY, 0);
        clearance = Math.min(clearance, reach.blocks());
        clearanceStale = true;
    }

    private void step(Level level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(BlockLevitator.FACING);
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
            LevitatorMist.stream(level, pos, facing, carried > 0 ? STREAM_CHANCE : IDLE_STREAM_CHANCE);
        } else if (carried > 0 && level.getGameTime() % SYNC_INTERVAL == 0) {
            setChangedAndSync();
        }
    }

    private void refuel(Level level, BlockPos pos) {
        if (charge >= Math.max(REFUEL_THRESHOLD, reach.visCost())) {
            return;
        }
        float received = AuraHelper.drainVis(level, pos, REFUEL_DRAW, false);
        int gained = Math.round(received * CHARGE_PER_VIS);
        if (gained > 0) {
            charge += gained;
            setChanged();
        }
    }

    private int carry(Level level, BlockPos pos, Direction facing) {
        AABB beam = new AABB(pos.relative(facing)).expandTowards(facing.getStepX() * (clearance - 1.0), facing.getStepY() * (clearance - 1.0), facing.getStepZ() * (clearance - 1.0));
        List<Entity> riders = level.getEntities((Entity) null, beam, BlockEntityLevitator::canCarry);
        int cost = reach.visCost();
        int carried = 0;
        for (Entity rider : riders) {
            if (charge < cost) {
                break;
            }
            charge -= cost;
            carried++;
            rider.resetFallDistance();
            if (rider.isLocalInstanceAuthoritative()) {
                push(rider, facing);
            }
            if (level.isClientSide()) {
                LevitatorMist.cling(level, rider);
            }
        }
        if (carried > 0 && !level.isClientSide()) {
            setChanged();
        }
        return carried;
    }

    private static boolean canCarry(Entity entity) {
        if (!entity.isAlive() || entity.isSpectator()) {
            return false;
        }
        return entity instanceof ItemEntity || entity instanceof Player || entity instanceof AbstractHorse || entity.isPushable();
    }

    private static void push(Entity rider, Direction facing) {
        Vec3 motion = rider.getDeltaMovement();
        if (facing == Direction.UP && rider.isShiftKeyDown() && rider instanceof Player) {
            if (motion.y < 0.0) {
                rider.setDeltaMovement(motion.x, motion.y * DAMPING, motion.z);
            }
            return;
        }
        double x = accelerate(motion.x, facing.getStepX());
        double y = accelerate(motion.y, facing.getStepY());
        double z = accelerate(motion.z, facing.getStepZ());
        if (facing.getAxis() != Direction.Axis.Y && !rider.onGround()) {
            if (y < 0.0) {
                y *= DAMPING;
            }
            y += rider.getGravity() * LIFT;
        }
        rider.setDeltaMovement(x, y, z);
    }

    private static double accelerate(double speed, int step) {
        if (step == 0) {
            return speed;
        }
        double along = speed * step;
        if (along < SPEED_CAP) {
            along = Math.min(SPEED_CAP, along + PUSH);
        }
        return along * step;
    }

    private int measureClearance(Level level, BlockPos pos, Direction facing) {
        int range = reach.blocks();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int distance = 1; distance <= range; distance++) {
            cursor.setWithOffset(pos, facing.getStepX() * distance, facing.getStepY() * distance, facing.getStepZ() * distance);
            if (!level.hasChunkAt(cursor) || level.getBlockState(cursor).isSolidRender()) {
                return distance;
            }
        }
        return range;
    }

}
