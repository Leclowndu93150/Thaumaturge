package com.leclowndu93150.thaumaturge.content.spell.action;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

final class ActionTargets {
    private static final float MAX_RADIUS = 8.0F;
    private static final double EDGE_TOLERANCE = 0.5;

    private ActionTargets() {}

    static Optional<LivingEntity> living(ActionContext ctx) {
        Optional<Entity> entity = ctx.target().entity();
        return entity.isPresent() && entity.get() instanceof LivingEntity living && living.isAlive()
                ? Optional.of(living)
                : Optional.empty();
    }

    static Optional<Entity> entity(ActionContext ctx) {
        return ctx.target().entity().filter(Entity::isAlive);
    }

    static Optional<BlockPos> struckBlock(ActionContext ctx) {
        return ctx.target().block().map(BlockHitResult::getBlockPos);
    }

    static BlockPos facing(ActionContext ctx) {
        Optional<BlockHitResult> block = ctx.target().block();
        return block.map(hit -> hit.getBlockPos().relative(hit.getDirection()))
                .orElse(ctx.target().blockPos());
    }

    static Direction face(ActionContext ctx) {
        return ctx.target().block().map(BlockHitResult::getDirection).orElse(Direction.UP);
    }

    static List<BlockPos> around(ActionContext ctx, BlockPos centre, float radius) {
        float clamped = Math.min(MAX_RADIUS, radius);
        int reach = (int) Math.ceil(clamped);
        List<BlockPos> out = new ArrayList<>();
        Vec3 middle = Vec3.atCenterOf(centre);
        for (BlockPos pos :
                BlockPos.betweenClosed(centre.offset(-reach, -reach, -reach), centre.offset(reach, reach, reach))) {
            if (pos.distToCenterSqr(middle) <= clamped * clamped + EDGE_TOLERANCE
                    && ctx.cast().budget().claim(pos)) {
                out.add(pos.immutable());
            }
        }
        return out;
    }

    static float magnitude(float base, float perPower, ActionContext ctx) {
        return base + perPower * ctx.power();
    }

    static float lasting(float base, float perDuration, ActionContext ctx) {
        return base + perDuration * ctx.duration();
    }
}
