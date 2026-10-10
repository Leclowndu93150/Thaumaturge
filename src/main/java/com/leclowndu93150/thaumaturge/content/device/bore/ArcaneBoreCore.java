package com.leclowndu93150.thaumaturge.content.device.bore;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityLampArcane;
import com.leclowndu93150.thaumaturge.content.device.BlockLamp;
import com.leclowndu93150.thaumaturge.content.equipment.RefiningResults;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jspecify.annotations.Nullable;

public final class ArcaneBoreCore {
    public static final float MAX_CHARGE = 10.0F;
    public static final float IDLE_YAW_STEP = 8.0F;
    public static final float IDLE_PITCH_STEP = 6.0F;
    public static final float DIG_PITCH_STEP = 24.0F;

    private static final int LEVEL_EVENT_BLOCK_BREAK = 2001;
    private static final int RECHARGE_INTERVAL = 10;
    private static final float DIG_COST = 0.25F;
    private static final int WEAR_INTERVAL = 50;
    private static final int SOUND_SPACING = 24;
    private static final int SOUND_JITTER_RANGE = 2;
    private static final float SOUND_VOLUME = 0.25F;
    private static final float SOUND_PITCH_MIN = 0.9F;
    private static final float SOUND_PITCH_SPREAD = 0.2F;
    private static final double DROP_RANGE = 1.5;
    private static final float REFINE_CHANCE_PER_STEP = 0.125F;
    private static final int INACTIVE_AIM_DISTANCE = 1;
    private static final int ACTIVE_AIM_DISTANCE = 2;
    private static final double TARGET_CENTER = 0.5;
    private static final int BASE_DELAY = 10;
    private static final int HARDNESS_DELAY_FACTOR = 2;
    private static final int SPEED_DELAY_FACTOR = 2;
    private static final int MIN_DELAY = 1;
    private static final float UNBREAKABLE = -1.0F;
    private static final int MAX_DUPLICATE_SKIPS = 4;
    private static final int FULL_LIGHT = 15;
    private static final int LIGHT_PHASES = 4;
    private static final int LIGHT_PHASE_SPAN = 2;
    private static final int LIGHT_MAX_SPREAD = 3;
    private static final int LIGHT_LOWERING = 2;
    private static final int LIGHT_PHASE_LOWERED = 3;
    private static final int LIGHT_PHASE_RIGHT = 0;
    private static final int LIGHT_PHASE_LEFT = 2;
    private static final Direction[] DIRECTIONS = Direction.values();
    private static final Direction[] SIDES = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

    private float charge;
    private @Nullable BlockPos target;
    private int delay;
    private int ring;
    private int step;
    private @Nullable BlockPos lastCell;
    private int nextSoundTick;
    private int wear;

    public @Nullable BlockPos digTarget() {
        return target;
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
            target = null;
            delay = 0;
            host.setBoreDigging(false);
            aim(host, null);
            return;
        }
        if (target == null) {
            if (charge >= DIG_COST) {
                findTarget(host, level, tool);
            }
        } else if (--delay <= 0) {
            dig(host, level, tool, tickCount);
        }
        host.setBoreDigging(target != null);
        aim(host, target);
    }

    public static void aim(ArcaneBoreHost host, @Nullable BlockPos digTarget) {
        if (digTarget != null) {
            host.aimBore(digTarget.getX() + TARGET_CENTER, digTarget.getY() + TARGET_CENTER, digTarget.getZ() + TARGET_CENTER, IDLE_YAW_STEP, DIG_PITCH_STEP);
            return;
        }
        aimIdle(host);
    }

    private static void aimIdle(ArcaneBoreHost host) {
        Direction facing = host.boreFacing();
        int distance = host.boreActive() ? ACTIVE_AIM_DISTANCE : INACTIVE_AIM_DISTANCE;
        Vec3 point = host.boreEye().add(facing.getStepX() * (double) distance, facing.getStepY() * (double) distance, facing.getStepZ() * (double) distance);
        host.aimBore(point.x, point.y, point.z, IDLE_YAW_STEP, IDLE_PITCH_STEP);
    }

    private void recharge(ArcaneBoreHost host, ServerLevel level) {
        float missing = MAX_CHARGE - charge;
        if (missing <= 0.0F) {
            return;
        }
        charge += AuraHelper.drainVis(level, host.borePos(), missing, false);
    }

    private void findTarget(ArcaneBoreHost host, ServerLevel level, ItemStack tool) {
        BlockPos candidate = probe(host, level, tool);
        if (candidate == null || !canSee(host, level, candidate)) {
            return;
        }
        BlockState state = level.getBlockState(candidate);
        int speed = ArcaneBoreTool.digSpeed(level, tool, state);
        float hardness = state.getDestroySpeed(level, candidate);
        delay = Math.max(MIN_DELAY, Math.max(BASE_DELAY - speed, Mth.floor(HARDNESS_DELAY_FACTOR * hardness) - SPEED_DELAY_FACTOR * speed));
        target = candidate;
        host.showBoreDig(level, candidate, delay);
    }

    private static boolean canSee(ArcaneBoreHost host, ServerLevel level, BlockPos candidate) {
        Vec3 eye = host.boreEye();
        Vec3 aim = Vec3.atCenterOf(candidate);
        BlockPos own = host.borePos();
        CollisionContext context = host.boreCollisionContext();
        Boolean visible = BlockGetter.traverseBlocks(eye, aim, level, (getter, cell) -> obstructs(getter, cell, own, candidate, eye, aim, context) ? Boolean.FALSE : null, getter -> Boolean.TRUE);
        return visible;
    }

    private static boolean obstructs(BlockGetter getter, BlockPos cell, BlockPos own, BlockPos candidate, Vec3 from, Vec3 to, CollisionContext context) {
        if (cell.equals(own) || cell.equals(candidate)) {
            return false;
        }
        VoxelShape shape = getter.getBlockState(cell).getCollisionShape(getter, cell, context);
        return !shape.isEmpty() && shape.clip(from, to, cell) != null;
    }

    private void dig(ArcaneBoreHost host, ServerLevel level, ItemStack tool, int tickCount) {
        BlockPos pos = target;
        target = null;
        if (pos == null || !level.hasChunkAt(pos)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return;
        }
        AABB area = dropArea(pos);
        Set<ItemEntity> preexisting = new HashSet<>(level.getEntitiesOfClass(ItemEntity.class, area));
        if (!breakAsPlayer(host, level, tool, pos)) {
            return;
        }
        List<ItemStack> drops = collectDrops(level, area, preexisting);
        refine(drops, state, tool, level, host.boreRandom());
        drops.forEach(drop -> deliver(host, level, drop));
        charge = Math.max(0.0F, charge - DIG_COST);
        wear(host);
        playSound(host, tickCount);
        lightTunnel(host, level, tool);
    }

    private void wear(ArcaneBoreHost host) {
        if (++wear >= WEAR_INTERVAL) {
            wear = 0;
            host.hurtBoreTool();
        }
    }

    private void playSound(ArcaneBoreHost host, int tickCount) {
        if (tickCount < nextSoundTick) {
            return;
        }
        RandomSource random = host.boreRandom();
        host.playBoreSound(TTSounds.RUMBLE.get(), SOUND_VOLUME, SOUND_PITCH_MIN + random.nextFloat() * SOUND_PITCH_SPREAD);
        nextSoundTick = tickCount + SOUND_SPACING + random.nextInt(SOUND_JITTER_RANGE);
    }

    private static void lightTunnel(ArcaneBoreHost host, ServerLevel level, ItemStack tool) {
        if (!hasLitArcaneLamp(level, host.borePos())) {
            return;
        }
        BlockPos spot = pickGlimmerSpot(host, tool);
        if (level.hasChunkAt(spot) && level.getBlockState(spot).isAir() && level.getBrightness(LightLayer.BLOCK, spot) < FULL_LIGHT) {
            level.setBlock(spot, TTBlocks.EFFECT_GLIMMER.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    private static BlockPos pickGlimmerSpot(ArcaneBoreHost host, ItemStack tool) {
        Direction facing = host.boreFacing();
        int depth = ArcaneBoreTool.digDepth(tool);
        int distance = LIGHT_PHASE_SPAN * host.boreRandom().nextInt(depth / LIGHT_PHASE_SPAN + 1);
        BlockPos spot = host.borePos().relative(facing, 1 + distance);
        int shift = Math.min(LIGHT_MAX_SPREAD, ArcaneBoreTool.digRadius(tool));
        Direction side = facing.getAxis() == Direction.Axis.Y ? Direction.EAST : facing.getClockWise();
        int phase = distance / LIGHT_PHASE_SPAN % LIGHT_PHASES;
        if (phase == LIGHT_PHASE_RIGHT) {
            return spot.relative(side, shift);
        }
        if (phase == LIGHT_PHASE_LEFT) {
            return spot.relative(side.getOpposite(), shift);
        }
        if (phase == LIGHT_PHASE_LOWERED && facing.getAxis() != Direction.Axis.Y) {
            return spot.below(LIGHT_LOWERING);
        }
        return spot;
    }

    private @Nullable BlockPos probe(ArcaneBoreHost host, ServerLevel level, ItemStack tool) {
        int radius = ArcaneBoreTool.digRadius(tool);
        if (ring > radius || step >= ringSamples(ring)) {
            ring = 0;
            step = 0;
        }
        Direction facing = host.boreFacing();
        Vec3 start = samplePoint(host.borePos(), facing, ring, step);
        BlockPos cell = BlockPos.containing(start);
        for (int skips = 0; skips < MAX_DUPLICATE_SKIPS && cell.equals(lastCell); skips++) {
            advance(radius);
            start = samplePoint(host.borePos(), facing, ring, step);
            cell = BlockPos.containing(start);
        }
        lastCell = cell;
        advance(radius);
        int depth = ArcaneBoreTool.digDepth(tool);
        Vec3 end = start.add(facing.getStepX() * (double) depth, facing.getStepY() * (double) depth, facing.getStepZ() * (double) depth);
        if (!level.hasChunkAt(cell) || !level.hasChunkAt(BlockPos.containing(end))) {
            return null;
        }
        BlockHitResult hit = level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, host.boreCollisionContext()));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos hitPos = hit.getBlockPos();
        if (hitPos.equals(host.borePos()) || hitPos.equals(host.borePos().below())) {
            return null;
        }
        BlockState state = level.getBlockState(hitPos);
        if (state.isAir() || state.getCollisionShape(level, hitPos).isEmpty() || state.getDestroySpeed(level, hitPos) <= UNBREAKABLE) {
            return null;
        }
        return hitPos;
    }

    private void advance(int radius) {
        step++;
        if (step >= ringSamples(ring)) {
            step = 0;
            ring = ring >= radius ? 0 : ring + 1;
        }
    }

    private static int ringSamples(int ringIndex) {
        return ringIndex == 0 ? 1 : Math.max(1, (int) Math.ceil(2.0 * Math.PI * ringIndex));
    }

    private static Vec3 samplePoint(BlockPos origin, Direction facing, int ringIndex, int stepIndex) {
        double angle = 2.0 * Math.PI * stepIndex / ringSamples(ringIndex);
        double first = ringIndex * Math.cos(angle);
        double second = ringIndex * Math.sin(angle);
        Vec3 front = Vec3.atCenterOf(origin).add(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        return switch (facing.getAxis()) {
            case X -> front.add(0.0, first, second);
            case Y -> front.add(first, 0.0, second);
            case Z -> front.add(first, second, 0.0);
        };
    }

    private boolean breakAsPlayer(ArcaneBoreHost host, ServerLevel level, ItemStack tool, BlockPos pos) {
        FakePlayer digger = host.boreDigger(level);
        ItemStack held = tool.copy();
        int fortune = ArcaneBoreTool.fortune(level, tool);
        if (fortune > 0) {
            Holder<Enchantment> holder = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
            if (fortune > EnchantmentHelper.getItemEnchantmentLevel(holder, held)) {
                EnchantmentHelper.updateEnchantments(held, enchantments -> enchantments.set(holder, fortune));
            }
        }
        digger.setItemInHand(InteractionHand.MAIN_HAND, held);
        BlockState state = level.getBlockState(pos);
        boolean removed = false;
        if (!CommonHooks.fireBlockBreak(level, digger.gameMode.getGameModeForPlayer(), digger, pos, state).isCanceled()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            level.levelEvent(null, LEVEL_EVENT_BLOCK_BREAK, pos, Block.getId(state));
            removed = level.removeBlock(pos, false);
            if (removed) {
                Block.dropResources(state, level, pos, blockEntity, digger, held);
            }
        }
        digger.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return removed;
    }

    private static AABB dropArea(BlockPos pos) {
        Vec3 lower = Vec3.atLowerCornerOf(pos);
        return new AABB(lower.subtract(DROP_RANGE, DROP_RANGE, DROP_RANGE), lower.add(1.0 + DROP_RANGE, 1.0 + DROP_RANGE, 1.0 + DROP_RANGE));
    }

    private static List<ItemStack> collectDrops(ServerLevel level, AABB area, Set<ItemEntity> preexisting) {
        List<ItemEntity> fresh = level.getEntitiesOfClass(ItemEntity.class, area, candidate -> !preexisting.contains(candidate));
        List<ItemStack> drops = fresh.stream().map(entity -> entity.getItem().copy()).collect(Collectors.toCollection(ArrayList::new));
        fresh.forEach(ItemEntity::discard);
        return drops;
    }

    private static void refine(List<ItemStack> drops, BlockState broken, ItemStack tool, ServerLevel level, RandomSource random) {
        int refining = ArcaneBoreTool.refining(tool);
        if (refining <= 0 || ArcaneBoreTool.silkTouch(level, tool)) {
            return;
        }
        Item cluster = RefiningResults.clusterFor(broken);
        if (cluster == null) {
            return;
        }
        for (int i = 0; i < drops.size(); i++) {
            if (random.nextFloat() < (refining + 1) * REFINE_CHANCE_PER_STEP) {
                drops.set(i, new ItemStack(cluster, drops.get(i).getCount()));
            }
        }
    }

    private static void deliver(ArcaneBoreHost host, ServerLevel level, ItemStack drop) {
        ItemStack rest = drop;
        for (Direction direction : DIRECTIONS) {
            if (rest.isEmpty()) {
                return;
            }
            BlockPos neighbor = host.borePos().relative(direction);
            if (level.hasChunkAt(neighbor)) {
                rest = InvHelper.insertStackAt(level, neighbor, direction.getOpposite(), rest, false);
            }
        }
        if (!rest.isEmpty()) {
            host.dropBoreOutput(level, rest);
        }
    }

    private static boolean hasLitArcaneLamp(ServerLevel level, BlockPos origin) {
        return Arrays.stream(SIDES).map(origin::relative).filter(level::hasChunkAt).anyMatch(pos -> isLitArcaneLamp(level, pos));
    }

    private static boolean isLitArcaneLamp(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof BlockLamp && state.getValue(BlockStateProperties.ENABLED) && level.getBlockEntity(pos) instanceof BlockEntityLampArcane;
    }
}
