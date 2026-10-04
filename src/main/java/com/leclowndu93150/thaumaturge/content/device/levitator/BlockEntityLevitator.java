package com.leclowndu93150.thaumaturge.content.device.levitator;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class BlockEntityLevitator extends BlockEntity {
    private static final String REACH_KEY = "reach";
    private static final String CHARGE_KEY = "charge";
    private static final int REFUEL_BELOW = 10;
    private static final float VIS_DRAW = 1.0F;
    private static final float CHARGE_PER_VIS = 1200.0F;
    private static final double THRUST = 0.1;
    private static final double SPEED_LIMIT = 0.35;
    private static final double HOVER_ASSIST = 0.08;
    private static final double FALL_DAMPING = 0.9;
    private static final int RIDER_SYNC_INTERVAL = 20;
    private static final float IDLE_MIST_CHANCE = 0.1F;
    private static final float WORKING_MIST_CHANCE = 0.6F;

    private final BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos();
    private LevitatorReach reach = LevitatorReach.MEDIUM;
    private int clearance;
    private int scanDepth;
    private int charge;

    public BlockEntityLevitator(BlockPos pos, BlockState state) {
        super(TCBlockEntities.LEVITATOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BlockEntityLevitator levitator) {
        Direction facing = state.getValue(BlockStateProperties.FACING);
        levitator.measureClearance(level, pos, facing);
        if (!level.isClientSide()) {
            levitator.refuel(level, pos);
        }
        if (levitator.clearance > 0 && levitator.charge > 0 && state.getValue(BlockStateProperties.ENABLED)) {
            levitator.carry(level, pos, facing);
        }
    }

    public void cycleReach(Player player) {
        clearance = 0;
        if (level == null || level.isClientSide()) {
            return;
        }
        reach = reach.next();
        publish();
        player.sendSystemMessage(Component.translatable("gui.thaumaturge.levitator", reach.blocks(), reach.visCost()));
    }

    private void measureClearance(Level level, BlockPos pos, Direction facing) {
        int limit = reach.blocks();
        clearance = Math.min(clearance, limit);
        scanDepth = scanDepth % limit + 1;
        if (level.getBlockState(probe.set(pos).move(facing, scanDepth)).isSolidRender()) {
            clearance = Math.min(clearance, scanDepth);
            scanDepth = 0;
        } else {
            clearance = Math.max(clearance, scanDepth);
        }
    }

    private void refuel(Level level, BlockPos pos) {
        if (charge >= REFUEL_BELOW) {
            return;
        }
        int gained = (int) (AuraHelper.drainVis(level, pos, VIS_DRAW, false) * CHARGE_PER_VIS);
        if (gained > 0) {
            charge += gained;
            publish();
        }
    }

    private void carry(Level level, BlockPos pos, Direction facing) {
        Vec3 push = facing.getUnitVec3();
        AABB column = new AABB(pos).expandTowards(push.scale(clearance));
        boolean carrying = false;
        for (Entity rider : level.getEntities((Entity) null, column, BlockEntityLevitator::isCarried)) {
            carrying = true;
            steer(rider, facing, push);
            rider.resetFallDistance();
            charge -= reach.visCost();
            if (level.isClientSide()) {
                LevitatorMist.cling(level, rider);
                LevitatorMist.stream(level, pos, facing, WORKING_MIST_CHANCE);
            }
            if (charge <= 0) {
                break;
            }
        }
        if (level.isClientSide()) {
            LevitatorMist.stream(level, pos, facing, IDLE_MIST_CHANCE);
        } else if (carrying && level.getGameTime() % RIDER_SYNC_INTERVAL == 0) {
            publish();
        }
    }

    private static boolean isCarried(Entity entity) {
        return entity instanceof ItemEntity || entity instanceof AbstractHorse || entity.isPushable();
    }

    private static void steer(Entity rider, Direction facing, Vec3 push) {
        Vec3 motion = rider.getDeltaMovement();
        if (facing == Direction.UP && rider.isShiftKeyDown()) {
            if (motion.y < 0.0) {
                rider.setDeltaMovement(motion.multiply(1.0, FALL_DAMPING, 1.0));
            }
            return;
        }
        Vec3 boosted = motion.add(push.scale(THRUST));
        if (facing.getAxis().isHorizontal() && !rider.onGround()) {
            double vertical = boosted.y < 0.0 ? boosted.y * FALL_DAMPING : boosted.y;
            boosted = new Vec3(boosted.x, vertical + HOVER_ASSIST, boosted.z);
        }
        rider.setDeltaMovement(Mth.clamp(boosted.x, -SPEED_LIMIT, SPEED_LIMIT), Mth.clamp(boosted.y, -SPEED_LIMIT, SPEED_LIMIT), Mth.clamp(boosted.z, -SPEED_LIMIT, SPEED_LIMIT));
    }

    private void publish() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
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
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
