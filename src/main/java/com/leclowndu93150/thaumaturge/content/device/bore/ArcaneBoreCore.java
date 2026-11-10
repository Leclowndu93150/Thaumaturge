package com.leclowndu93150.thaumaturge.content.device.bore;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.casters.BlockBreakerEngine;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityLampArcane;
import com.leclowndu93150.thaumaturge.content.equipment.RefiningResults;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.content.world.space.BlockSightline;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jspecify.annotations.Nullable;

public final class ArcaneBoreCore {
    public static final float MAX_CHARGE = 10.0F;
    public static final float IDLE_YAW_STEP = 8.0F;
    public static final float IDLE_PITCH_STEP = 6.0F;
    public static final float DIG_PITCH_STEP = 24.0F;

    private static final int RECHARGE_INTERVAL = 10;
    private static final double SIGHT_LIMIT = 64.0;
    private static final float DIG_COST = 0.25F;
    private static final int DURABILITY_PER_BREAKS = 50;
    private static final long SOUND_DELAY_TICKS = 24;
    private static final double DROP_COLLECT_RANGE = 1.5;
    private static final float REFINING_CHANCE_STEP = 0.125F;
    private static final int MIN_DIG_DELAY = 1;
    private static final int BASE_DIG_DELAY = 10;
    private static final float HARDNESS_TO_DELAY = 2.0F;
    private static final float SPIRAL_BASE_STEP = 3.0F;
    private static final float SPIRAL_INNER_BOOST = 10.0F;
    private static final int FULL_TURN = 360;
    private static final int TUNNEL_LIGHT_SIDEWAYS = 3;
    private static final int TUNNEL_LIGHT_DROP = 2;
    private static final int MAX_TUNNEL_LIGHT = 15;
    private static final int ACTIVE_AIM_DISTANCE = 2;
    private static final int INACTIVE_AIM_DISTANCE = 1;
    private static final double TARGET_CENTER = 0.5;
    private static final Direction[] SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private @Nullable BlockPos digTarget;
    private @Nullable BlockPos digTargetPrev;
    private long soundDelay;
    private int breakCounter;
    private int digDelay;
    private int digDelayMax;
    private float radInc;
    private int spiral;
    private float currentRadius;
    private float charge;

    public @Nullable BlockPos digTarget() {
        return digTarget;
    }

    public float charge() {
        return charge;
    }

    public void setCharge(float charge) {
        this.charge = charge;
    }

    public void serverTick(ArcaneBoreHost host, ServerLevel level, int tickCount) {
        if (tickCount % RECHARGE_INTERVAL == 0) {
            recharge(host, level);
        }
        ItemStack tool = host.boreTool();
        if (!host.boreActive() || !host.boreValid() || !ArcaneBoreTool.valid(tool)) {
            digTarget = null;
            digDelay = 0;
            host.setBoreDigging(false);
            aim(host, null);
            return;
        }
        if (digTarget == null) {
            if (charge >= DIG_COST) {
                findNextBlockToDig(host, level);
            }
        } else if (--digDelay <= 0) {
            dig(host, level);
        }
        host.setBoreDigging(digTarget != null);
        aim(host, digTarget);
    }

    public static void aim(ArcaneBoreHost host, @Nullable BlockPos target) {
        if (target != null) {
            host.aimBore(
                    target.getX() + TARGET_CENTER,
                    target.getY() + TARGET_CENTER,
                    target.getZ() + TARGET_CENTER,
                    IDLE_YAW_STEP,
                    DIG_PITCH_STEP);
            return;
        }
        Direction facing = host.boreFacing();
        int distance = host.boreActive() ? ACTIVE_AIM_DISTANCE : INACTIVE_AIM_DISTANCE;
        Vec3 point = host.boreEye()
                .add(
                        facing.getStepX() * (double) distance,
                        facing.getStepY() * (double) distance,
                        facing.getStepZ() * (double) distance);
        host.aimBore(point.x, point.y, point.z, IDLE_YAW_STEP, IDLE_PITCH_STEP);
    }

    private void recharge(ArcaneBoreHost host, ServerLevel level) {
        float missing = MAX_CHARGE - charge;
        if (missing > 0.0F) {
            charge += AuraHelper.drainVis(level, host.borePos(), missing, false);
        }
    }

    private void dig(ArcaneBoreHost host, ServerLevel level) {
        BlockPos target = digTarget;
        digTarget = null;
        if (target == null || !LoadedChunks.contains(level, target)) {
            return;
        }
        BlockState state = level.getBlockState(target);
        if (state.isAir()) {
            return;
        }
        Set<ItemEntity> lying = new HashSet<>(itemsAround(level, target));
        if (!breakAsFakePlayer(host, level, target)) {
            return;
        }
        collectAndEjectDrops(host, level, target, state, lying);
        charge = Math.max(0.0F, charge - DIG_COST);
        damageTool(host);
        lightTunnel(host, level);
        if (soundDelay < level.getGameTime()) {
            soundDelay =
                    level.getGameTime() + SOUND_DELAY_TICKS + host.boreRandom().nextInt(2);
            host.playBoreSound(
                    TTSounds.RUMBLE.get(), 0.25F, 0.9F + host.boreRandom().nextFloat() * 0.2F);
        }
    }

    private boolean breakAsFakePlayer(ArcaneBoreHost host, ServerLevel level, BlockPos target) {
        FakePlayer digger = host.boreDigger(level);
        digger.setItemInHand(InteractionHand.MAIN_HAND, host.boreTool().copy());
        try {
            BlockBreakerEngine.harvestBlock(
                    level,
                    digger,
                    target,
                    ArcaneBoreTool.silkTouch(level, host.boreTool()),
                    ArcaneBoreTool.fortune(level, host.boreTool()));
            return level.getBlockState(target).isAir();
        } finally {
            digger.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    private static List<ItemEntity> itemsAround(ServerLevel level, BlockPos target) {
        return level.getEntitiesOfClass(
                ItemEntity.class, LevelSpaces.worldBox(level, target, new AABB(target).inflate(DROP_COLLECT_RANGE)));
    }

    private void collectAndEjectDrops(
            ArcaneBoreHost host, ServerLevel level, BlockPos target, BlockState state, Set<ItemEntity> lying) {
        int refining = ArcaneBoreTool.refining(host.boreTool());
        boolean silk = ArcaneBoreTool.silkTouch(level, host.boreTool());
        for (ItemEntity item : itemsAround(level, target)) {
            if (lying.contains(item)) {
                continue;
            }
            ItemStack drop = item.getItem().copy();
            item.discard();
            ItemStack ejected = drop;
            if (!silk && refining > 0 && host.boreRandom().nextFloat() < (refining + 1) * REFINING_CHANCE_STEP) {
                Item cluster = RefiningResults.clusterFor(state);
                if (cluster != null) {
                    ejected = new ItemStack(cluster, drop.getCount());
                }
            }
            ejectStack(host, level, ejected);
        }
    }

    private void damageTool(ArcaneBoreHost host) {
        ItemStack held = host.boreTool();
        breakCounter++;
        if (held.isEmpty()) {
            breakCounter = 0;
            return;
        }
        if (breakCounter >= DURABILITY_PER_BREAKS) {
            breakCounter -= DURABILITY_PER_BREAKS;
            host.hurtBoreTool();
        }
    }

    private void ejectStack(ArcaneBoreHost host, ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        for (Direction face : Direction.values()) {
            BlockPos side = host.borePos().relative(face);
            if (InvHelper.handlerAt(level, side, face.getOpposite()) != null) {
                ItemStack remainder = InvHelper.insertAt(level, side, face.getOpposite(), stack, false);
                if (remainder.isEmpty()) {
                    return;
                }
                stack = remainder;
            }
        }
        host.dropBoreOutput(level, stack);
    }

    private void lightTunnel(ArcaneBoreHost host, ServerLevel level) {
        if (!hasAdjacentLamp(host, level)) {
            return;
        }
        Direction facing = host.boreFacing();
        ItemStack tool = host.boreTool();
        int distance = host.boreRandom().nextInt(ArcaneBoreTool.digDepth(tool) / 2 + 1) * 2;
        int phase = distance / 2 % 4;
        int spread = Math.min(TUNNEL_LIGHT_SIDEWAYS, ArcaneBoreTool.digRadius(tool));
        int sideways = phase == 0 ? spread : phase == 2 ? -spread : 0;
        BlockPos origin = host.borePos().relative(facing, 1 + distance);
        int x = origin.getX() + (facing.getStepX() != 0 ? 0 : sideways);
        int y = origin.getY() - (phase == 3 && facing.getStepY() == 0 ? TUNNEL_LIGHT_DROP : 0);
        int z = origin.getZ() + (facing.getStepX() != 0 ? sideways : 0);
        BlockPos target = LevelSpaces.worldBlock(level, new BlockPos(x, y, z));
        if (LoadedChunks.contains(level, target)
                && level.getBlockState(target).isAir()
                && level.getBrightness(LightLayer.BLOCK, target) < MAX_TUNNEL_LIGHT) {
            level.setBlock(target, TTBlocks.EFFECT_GLIMMER.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static boolean hasAdjacentLamp(ArcaneBoreHost host, ServerLevel level) {
        BlockPos pos = host.borePos();
        for (Direction side : SIDES) {
            BlockPos lamp = pos.relative(side);
            if (!LoadedChunks.contains(level, lamp)) {
                continue;
            }
            BlockState state = level.getBlockState(lamp);
            if (state.hasProperty(BlockStateProperties.ENABLED)
                    && state.getValue(BlockStateProperties.ENABLED)
                    && level.getBlockEntity(lamp) instanceof BlockEntityLampArcane) {
                return true;
            }
        }
        return false;
    }

    private void findNextBlockToDig(ArcaneBoreHost host, ServerLevel level) {
        int digRadius = ArcaneBoreTool.digRadius(host.boreTool());
        if (digTargetPrev == null
                || digTargetPrev.distToCenterSqr(host.borePosition()) > (digRadius + 1) * (digRadius + 1)) {
            digTargetPrev = host.borePos();
        }
        if (radInc == 0.0F) {
            radInc = 1.0F;
        }
        if (acquireTargetThrough(host, level, digTargetPrev)) {
            BlockPos target = digTarget;
            if (target != null) {
                host.showBoreDig(level, target, digDelayMax);
            }
            return;
        }
        digTargetPrev = advanceSpiral(host, digRadius);
    }

    private boolean acquireTargetThrough(ArcaneBoreHost host, ServerLevel level, BlockPos scanPoint) {
        Direction facing = host.boreFacing();
        ItemStack tool = host.boreTool();
        BlockPos end = scanPoint.relative(facing, ArcaneBoreTool.digDepth(tool));
        BlockHitResult hit = level.clip(new ClipContext(
                LevelSpaces.toWorld(level, Vec3.atCenterOf(scanPoint)),
                LevelSpaces.toWorld(level, Vec3.atCenterOf(end)),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                host.boreCollisionContext()));
        if (hit.getType() == HitResult.Type.MISS) {
            return false;
        }
        Vec3 digger =
                LevelSpaces.toWorld(level, host.boreEye()).add(LevelSpaces.faceNormal(level, host.borePos(), facing));
        hit = level.clip(new ClipContext(
                digger,
                LevelSpaces.worldCenter(level, hit.getBlockPos()),
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                host.boreCollisionContext()));
        if (hit.getType() == HitResult.Type.MISS) {
            return false;
        }
        BlockPos target = hit.getBlockPos();
        BlockState state = level.getBlockState(target);
        if (state.getDestroySpeed(level, target) <= -1.0F
                || state.getCollisionShape(level, target).isEmpty()) {
            return false;
        }
        int speed = ArcaneBoreTool.digSpeed(level, tool, state);
        digDelay = Math.max(
                MIN_DIG_DELAY,
                Math.max(
                        BASE_DIG_DELAY - speed,
                        Mth.floor(state.getDestroySpeed(level, target) * HARDNESS_TO_DELAY) - speed * 2));
        digDelayMax = digDelay;
        if (target.equals(host.borePos()) || target.equals(host.borePos().below())) {
            return false;
        }
        if (!canSee(host, level, target)) {
            return false;
        }
        digTarget = target;
        return true;
    }

    static boolean canSee(ArcaneBoreHost host, ServerLevel level, BlockPos target) {
        return BlockSightline.isClear(
                level,
                host.boreEye(),
                Vec3.atCenterOf(target),
                host.borePos(),
                target,
                SIGHT_LIMIT,
                host.boreCollisionContext());
    }

    private BlockPos advanceSpiral(ArcaneBoreHost host, int digRadius) {
        Direction facing = host.boreFacing();
        int x = digTargetPrev.getX();
        int y = digTargetPrev.getY();
        int z = digTargetPrev.getZ();
        while (x == digTargetPrev.getX() && z == digTargetPrev.getZ() && y == digTargetPrev.getY()) {
            if (Math.abs(currentRadius) > digRadius) {
                currentRadius = digRadius;
            }
            spiral = (int) (spiral
                    + (SPIRAL_BASE_STEP + Math.max(0.0F, (SPIRAL_INNER_BOOST - Math.abs(currentRadius)) * 2.0F)));
            if (spiral >= FULL_TURN) {
                spiral -= FULL_TURN;
                currentRadius += radInc;
                if (currentRadius > digRadius || currentRadius < -digRadius) {
                    currentRadius = 0.0F;
                }
            }
            Vec3 scan = spiralScanPoint(host, facing);
            x = Mth.floor(scan.x);
            y = Mth.floor(scan.y);
            z = Mth.floor(scan.z);
        }
        return new BlockPos(x, y, z);
    }

    private Vec3 spiralScanPoint(ArcaneBoreHost host, Direction facing) {
        BlockPos pos = host.borePos();
        Vec3 position = host.borePosition();
        Vec3 source = new Vec3(
                pos.getX() + 0.5 + facing.getStepX(),
                position.y + facing.getStepY() + host.boreEyeHeight(),
                pos.getZ() + 0.5 + facing.getStepZ());
        Vec3 offset = new Vec3(0.0, currentRadius, 0.0);
        offset = rotateAroundZ(offset, spiral / 180.0F * (float) Math.PI);
        offset = rotateAroundY(offset, (float) (Math.PI / 2) * facing.getStepX());
        offset = rotateAroundX(offset, (float) (Math.PI / 2) * facing.getStepY());
        return source.add(offset);
    }

    private static Vec3 rotateAroundX(Vec3 vec, float angle) {
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        return new Vec3(vec.x, vec.y * cos - vec.z * sin, vec.y * sin + vec.z * cos);
    }

    private static Vec3 rotateAroundY(Vec3 vec, float angle) {
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        return new Vec3(vec.x * cos + vec.z * sin, vec.y, vec.z * cos - vec.x * sin);
    }

    private static Vec3 rotateAroundZ(Vec3 vec, float angle) {
        float cos = Mth.cos(angle);
        float sin = Mth.sin(angle);
        return new Vec3(vec.x * cos - vec.y * sin, vec.x * sin + vec.y * cos, vec.z);
    }
}
