package com.leclowndu93150.thaumaturge.content.aura.node;

import com.leclowndu93150.thaumaturge.api.aspect.AspectIndexAccess;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.nodes.NodeModifier;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.particle.BoreDebrisParticleOptions;
import com.leclowndu93150.thaumaturge.content.wands.WandChargingEvents;
import com.leclowndu93150.thaumaturge.content.warding.WardHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

final class NodeHunger {
    private static final int PULL_REFRESH_INTERVAL = 10;
    private static final double GLOBAL_PULL_RANGE = 15.0;
    private static final double ITEM_RANGE_PADDING = 0.5;
    private static final double MINIMUM_PULL_DISTANCE = 1.0E-4;
    private static final float DEVOUR_DAMAGE = 1.0F;
    private static final int RANGE_PALE_DIVISOR = 3;
    private static final int RANGE_AVERAGE_NUMERATOR = 2;
    private static final double SIDEWAYS_DRAG = 0.15;
    private static final double UPRIGHT_DRAG = 0.25;
    private static final double MAW_RADIUS = 1.5;
    private static final float GROWTH_CAPACITY_WEIGHT = 2.0F;
    private static final int FRAGMENTS_PER_GLIMPSE = 3;
    private static final double FRAGMENT_KICK = 0.03;

    private final BlockEntityNode node;
    private final List<Entity> pulled = new ArrayList<>();
    private @Nullable ServerLevel cachedLevel;

    NodeHunger(BlockEntityNode node) {
        this.node = node;
    }

    static int eatRange(@Nullable NodeModifier modifier) {
        int fixed = ThaumaturgeCommonConfig.HUNGRY_NODE_BLOCK_EAT_RANGE.get();
        if (!ThaumaturgeCommonConfig.SCALE_HUNGRY_NODE_RANGE_BY_MODIFIER.get()) {
            return fixed;
        }
        int minimum = ThaumaturgeCommonConfig.HUNGRY_NODE_MINIMUM_BLOCK_EAT_RANGE.get();
        int maximum = Math.max(minimum, ThaumaturgeCommonConfig.HUNGRY_NODE_MAXIMUM_BLOCK_EAT_RANGE.get());
        return scaledEatRange(minimum, maximum, modifier);
    }

    static int scaledEatRange(int minimum, int maximum, @Nullable NodeModifier modifier) {
        maximum = Math.max(minimum, maximum);
        int span = maximum - minimum;
        if (modifier == NodeModifier.BRIGHT) {
            return maximum;
        }
        if (modifier == NodeModifier.FADING) {
            return minimum;
        }
        return modifier == NodeModifier.PALE
                ? minimum + span / RANGE_PALE_DIVISOR
                : minimum + span * RANGE_AVERAGE_NUMERATOR / RANGE_PALE_DIVISOR;
    }

    static boolean eatDue(int counter) {
        return counter % ThaumaturgeCommonConfig.HUNGRY_NODE_BLOCK_EAT_INTERVAL.get() == 0;
    }

    private static boolean isPullable(Entity entity) {
        return entity.isAlive() && !(entity instanceof Player player && (player.isCreative() || player.isSpectator()));
    }

    void tick(ServerLevel level, BlockPos pos, int counter) {
        if (cachedLevel != level) {
            pulled.clear();
            cachedLevel = level;
        }
        Vec3 centre = Vec3.atCenterOf(pos);
        double itemReach = eatRange(node.getNodeModifier()) + ITEM_RANGE_PADDING;
        if (counter % PULL_REFRESH_INTERVAL == 0) {
            gather(level, centre, Math.max(itemReach, GLOBAL_PULL_RANGE));
        }
        if (pulled.isEmpty()) {
            return;
        }
        for (Entity entity : pulled) {
            if (!isPullable(entity)
                    || entity.level() != level
                    || !level.isPositionEntityTicking(entity.blockPosition())) {
                continue;
            }
            Vec3 toCentre = centre.subtract(entity.getBoundingBox().getCenter());
            double distance = toCentre.length();
            double reach = entity instanceof ItemEntity ? itemReach : GLOBAL_PULL_RANGE;
            if (distance > MINIMUM_PULL_DISTANCE && distance < reach) {
                drag(entity, toCentre, distance, reach);
            }
            if (shouldSwallow(distance)) {
                swallow(level, entity);
            }
        }
    }

    static boolean shouldSwallow(double distance) {
        return distance <= MAW_RADIUS;
    }

    private void gather(ServerLevel level, Vec3 centre, double reach) {
        pulled.clear();
        AABB area = AABB.ofSize(centre, reach * 2.0, reach * 2.0, reach * 2.0);
        double reachSquared = reach * reach;
        for (Entity entity : level.getEntitiesOfClass(Entity.class, area, NodeHunger::isPullable)) {
            if (entity.getBoundingBox().getCenter().distanceToSqr(centre) <= reachSquared) {
                pulled.add(entity);
            }
        }
    }

    private static void drag(Entity entity, Vec3 toCentre, double distance, double reach) {
        double closeness = 1.0 - distance / reach;
        double strength = closeness * closeness / distance;
        entity.push(
                toCentre.x * SIDEWAYS_DRAG * strength,
                toCentre.y * UPRIGHT_DRAG * strength,
                toCentre.z * SIDEWAYS_DRAG * strength);
        if (entity instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
    }

    private void swallow(ServerLevel level, Entity entity) {
        AspectList essence = essenceOf(entity);
        boolean wasPresent = entity.isAlive();
        entity.hurt(level.damageSources().fellOutOfWorld(), DEVOUR_DAMAGE);
        if (wasPresent && !entity.isAlive() && !essence.isEmpty()) {
            feed(level, essence, level.getRandom());
        }
    }

    private static AspectList essenceOf(Entity entity) {
        if (entity instanceof ItemEntity item) {
            return AspectIndexAccess.of(item.getItem().copyWithCount(1));
        }
        if (entity instanceof LivingEntity && !(entity instanceof Player)) {
            return EntityAspects.of(entity);
        }
        return AspectList.EMPTY;
    }

    private void feed(ServerLevel level, AspectList essence, RandomSource random) {
        List<Map.Entry<ResourceKey<IAspect>, Integer>> primals =
                new ArrayList<>(WandChargingEvents.reduceToPrimals(essence).entrySet());
        if (primals.isEmpty()) {
            return;
        }
        Map.Entry<ResourceKey<IAspect>, Integer> chosen = primals.get(random.nextInt(primals.size()));
        Holder<IAspect> primal = Aspects.resolve(level, chosen.getKey());
        if (primal == null) {
            return;
        }
        int capacity = node.aspectsBase.amountOf(primal);
        if (node.aspects.amountOf(primal) < capacity) {
            node.aspects = node.aspects.add(primal, 1);
        } else if (random.nextFloat()
                < Math.min(1.0F, chosen.getValue() / (1.0F + GROWTH_CAPACITY_WEIGHT * capacity))) {
            node.aspectsBase = node.aspectsBase.add(primal, 1);
        } else {
            return;
        }
        node.nodeChange();
    }

    static @Nullable BlockPos findTarget(
            Level level, BlockPos pos, @Nullable NodeModifier modifier, RandomSource random) {
        int reach = eatRange(modifier);
        BlockPos aim = NodeScatter.around(pos, reach, random);
        if (aim.equals(pos)
                || !level.hasChunkAt(aim)
                || aim.getY() >= level.getHeight(Heightmap.Types.WORLD_SURFACE, aim.getX(), aim.getZ())) {
            return null;
        }
        BlockHitResult hit = level.clip(new SourceIgnoringClipContext(
                pos,
                Vec3.atCenterOf(pos),
                Vec3.atCenterOf(aim),
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.SOURCE_ONLY));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return null;
        }
        BlockPos struck = hit.getBlockPos();
        if (struck.distSqr(pos) > (double) reach * reach || WardHandler.isWarded(level, struck)) {
            return null;
        }
        BlockState state = level.getBlockState(struck);
        float hardness = state.getDestroySpeed(level, struck);
        if (state.isAir() || hardness < 0.0F || hardness >= ThaumaturgeCommonConfig.HUNGRY_NODE_BLOCK_HARDNESS.get()) {
            return null;
        }
        return struck;
    }

    static void predict(Level level, BlockPos pos, @Nullable NodeModifier modifier) {
        RandomSource random = level.getRandom();
        BlockPos target = findTarget(level, pos, modifier, random);
        if (target == null) {
            return;
        }
        Vec3 centre = Vec3.atCenterOf(pos);
        BoreDebrisParticleOptions shard = new BoreDebrisParticleOptions(
                level.getBlockState(target), centre.x, centre.y, centre.z, kick(random), kick(random), kick(random));
        for (int fragment = 0; fragment < FRAGMENTS_PER_GLIMPSE; fragment++) {
            level.addParticle(
                    shard,
                    target.getX() + random.nextDouble(),
                    target.getY() + random.nextDouble(),
                    target.getZ() + random.nextDouble(),
                    0.0,
                    0.0,
                    0.0);
        }
    }

    private static double kick(RandomSource random) {
        return (random.nextDouble() * 2.0 - 1.0) * FRAGMENT_KICK;
    }
}
