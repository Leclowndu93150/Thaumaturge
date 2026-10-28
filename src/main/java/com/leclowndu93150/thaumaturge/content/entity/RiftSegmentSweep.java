package com.leclowndu93150.thaumaturge.content.entity;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

final class RiftSegmentSweep {
    private static final double EAT_CUBE_HALF = 0.5;
    private static final float EAT_DAMAGE = 2.0F;
    private static final int UNSET_CURSOR = -1;

    private int cursor = UNSET_CURSOR;

    void tick(ServerLevel level, EntityFluxRift rift) {
        List<Vec3> outline = rift.outline;
        int segments = outline.size() - 1;
        if (segments < 1) {
            return;
        }
        if (this.cursor == UNSET_CURSOR) {
            this.cursor = rift.getRandom().nextInt(segments);
        }
        int index = this.cursor % segments;
        this.cursor = index + 1;
        Vec3 origin = rift.position();
        Vec3 start = origin.add(outline.get(index));
        Vec3 end = origin.add(outline.get(index + 1));
        boolean loaded = level.hasChunkAt(BlockPos.containing(start)) && level.hasChunkAt(BlockPos.containing(end));
        if (!loaded) {
            return;
        }
        eraseBlockAlong(level, rift, start, end);
        punishEntitiesAround(level, rift, start);
    }

    private void eraseBlockAlong(ServerLevel level, EntityFluxRift rift, Vec3 start, Vec3 end) {
        BlockHitResult hit =
                level.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, rift));
        if (hit.getType() != HitResult.Type.BLOCK) {
            return;
        }
        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        boolean breakable = !state.isAir() && state.getDestroySpeed(level, pos) >= 0.0F;
        if (breakable) {
            level.destroyBlock(pos, false);
        }
    }

    private void punishEntitiesAround(ServerLevel level, EntityFluxRift rift, Vec3 center) {
        AABB cube = new AABB(center, center).inflate(EAT_CUBE_HALF);
        DamageSource source = level.damageSources().fellOutOfWorld();
        for (Entity victim : level.getEntities(rift, cube)) {
            if (victim.isRemoved()) {
                continue;
            }
            if (victim instanceof Player player && player.isCreative()) {
                continue;
            }
            victim.hurt(source, EAT_DAMAGE);
            if (victim instanceof ItemEntity) {
                victim.discard();
            }
        }
    }
}
