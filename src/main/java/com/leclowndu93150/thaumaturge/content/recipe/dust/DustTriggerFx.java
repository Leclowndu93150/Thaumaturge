package com.leclowndu93150.thaumaturge.content.recipe.dust;

import com.leclowndu93150.thaumaturge.api.recipe.DustTrigger;
import com.leclowndu93150.thaumaturge.api.recipe.DustTriggerPlacement;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class DustTriggerFx {
    public static final double SPARKLE_TICKS_PER_BLOCK = 16.0;

    private static final int BURST_COUNT = 50;
    private static final float BURST_BASE_AGE = 16.0F;

    private DustTriggerFx() {}

    public static Vec3 posToHand(ServerPlayer player, InteractionHand hand) {
        double px = player.getX();
        double py = player.getBoundingBox().minY + player.getBbHeight() / 2.0F + 0.25;
        double pz = player.getZ();
        float m = hand == InteractionHand.MAIN_HAND ? 0.0F : 180.0F;
        float yaw = player.getYRot();
        px += -Mth.cos((yaw + m) / 180.0F * 3.141593F) * 0.3F;
        pz += -Mth.sin((yaw + m) / 180.0F * 3.141593F) * 0.3F;
        Vec3 look = player.getLookAngle();
        px += look.x * 0.3;
        py += look.y * 0.3;
        pz += look.z * 0.3;
        return new Vec3(px, py, pz);
    }

    public static void emitUseBurst(ServerLevel level, ServerPlayer player, InteractionHand hand, BlockPos pos) {
        RandomSource rand = level.getRandom();
        Vec3 v1 = posToHand(player, hand);
        Vec3 v2 = Vec3.atCenterOf(pos).subtract(v1);
        for (int a = 0; a < BURST_COUNT; a++) {
            boolean floaty = a < BURST_COUNT / 3;
            float r = 1.0F;
            float g = (189 + rand.nextInt(67)) / 255.0F;
            float b = (64 + rand.nextInt(192)) / 255.0F;
            double vx = v2.x / 6.0 + rand.nextGaussian() * 0.05;
            double vy = v2.y / 6.0 + rand.nextGaussian() * 0.05 + (floaty ? 0.05 : 0.15);
            double vz = v2.z / 6.0 + rand.nextGaussian() * 0.05;
            int delay = rand.nextInt(5);
            float decay = floaty ? 0.3F + rand.nextFloat() * 0.5F : 0.85F;
            float grav = floaty ? 0.2F : 0.5F;
            Effects.simpleSparkle(level, v1).motion(vx, vy, vz).scale(0.5F).color(r, g, b).delay(delay).decay(decay).gravity(grav).baseAge((int) BURST_BASE_AGE).send();
        }
    }

    public static void emitTriggerSparkles(ServerLevel level, ServerPlayer player, BlockPos triggerPos, DustTrigger trigger, Vec3 hitStart, @Nullable DustTriggerPlacement placement) {
        List<BlockPos> sparkles = trigger.sparkle(level, player, triggerPos, placement == null ? DustTriggerPlacement.origin() : placement);
        if (sparkles == null || sparkles.isEmpty()) {
            return;
        }
        for (BlockPos p : sparkles) {
            emitBlockSparkles(level, p, hitStart);
        }
    }

    public static void emitBlockSparkles(ServerLevel level, BlockPos p, Vec3 start) {
        BlockState st = level.getBlockState(p);
        VoxelShape shape = st.getShape(level, p);
        AABB box;
        if (shape.isEmpty()) {
            box = new AABB(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);
        } else {
            box = shape.bounds();
        }
        box = box.inflate(0.1);
        double avgEdge = (box.getXsize() + box.getYsize() + box.getZsize()) / 3.0;
        int num = (int) (avgEdge * 20.0);
        if (num < 1)
            num = 1;
        RandomSource rand = level.getRandom();
        for (Direction face : Direction.values()) {
            BlockPos neighbor = p.relative(face);
            BlockState neighborState = level.getBlockState(neighbor);
            if (neighborState.isSolidRender() && neighborState.isFaceSturdy(level, neighbor, face.getOpposite())) {
                continue;
            }
            boolean rx = face.getStepX() == 0;
            boolean ry = face.getStepY() == 0;
            boolean rz = face.getStepZ() == 0;
            double mx = 0.5 + face.getStepX() * 0.51;
            double my = 0.5 + face.getStepY() * 0.51;
            double mz = 0.5 + face.getStepZ() * 0.51;
            for (int a = 0; a < num * 2; a++) {
                double x = mx;
                double y = my;
                double z = mz;
                if (rx)
                    x = mx + rand.nextGaussian() * 0.6;
                if (ry)
                    y = my + rand.nextGaussian() * 0.6;
                if (rz)
                    z = mz + rand.nextGaussian() * 0.6;
                x = Mth.clamp(x, box.minX, box.maxX);
                y = Mth.clamp(y, box.minY, box.maxY);
                z = Mth.clamp(z, box.minZ, box.maxZ);
                float r = 1.0F;
                float g = (189 + rand.nextInt(67)) / 255.0F;
                float b = (64 + rand.nextInt(192)) / 255.0F;
                double wx = p.getX() + x;
                double wy = p.getY() + y;
                double wz = p.getZ() + z;
                double dx = wx - start.x;
                double dy = wy - start.y;
                double dz = wz - start.z;
                double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
                int delay = rand.nextInt(5) + (int) (dist * SPARKLE_TICKS_PER_BLOCK);
                float scale = 0.4F + (float) rand.nextGaussian() * 0.1F;
                Effects.simpleSparkle(level, new Vec3(wx, wy, wz)).motion(0.0, 0.0025, 0.0).scale(scale).color(r, g, b).delay(delay).decay(1.0F).gravity(0.01F).baseAge(16).send();
            }
        }
    }
}
