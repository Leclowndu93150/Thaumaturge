package com.leclowndu93150.thaumaturge.client.effect;

import com.leclowndu93150.thaumaturge.client.space.ClientSpaces;
import com.leclowndu93150.thaumaturge.content.particle.BlockRunesParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.BubbleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.NitorCoreParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SmokeSpiralParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.SparkleParticleOptions;
import com.leclowndu93150.thaumaturge.content.particle.WispFlameParticleOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class ClientEffects {
    private ClientEffects() {}

    public static void essentiaDrop(Level level, double x, double y, double z, float r, float g, float b, float alpha) {
        RandomSource rand = level.getRandom();
        BubbleParticleOptions options = new BubbleParticleOptions(
                ARGB32.colorFromFloat(1.0F, r, g, b),
                alpha,
                0.4F + rand.nextFloat() * 0.2F,
                20 + rand.nextInt(10),
                0.01F,
                false);
        spawn(options, x, y, z, rand.nextGaussian() * 0.005, rand.nextGaussian() * 0.005, rand.nextGaussian() * 0.005);
    }

    public static void smokeSpiral(
            Level level, double x, double y, double z, float radius, int start, int minY, int color) {
        float r = ARGB32.red(color) / 255.0F;
        float g = ARGB32.green(color) / 255.0F;
        float b = ARGB32.blue(color) / 255.0F;
        level.addParticle(new SmokeSpiralParticleOptions(radius, start, minY, r, g, b), x, y, z, 0.0, 0.0, 0.0);
    }

    public static void followingBubble(Level level, double x, double y, double z) {
        RandomSource rand = level.getRandom();
        BubbleParticleOptions options = new BubbleParticleOptions(
                ARGB32.colorFromFloat(1.0F, 0.33F, 0.33F, 1.0F),
                1.0F,
                rand.nextFloat() * 0.3F + 0.3F,
                15 + rand.nextInt(10),
                -0.001F,
                false);
        spawn(options, x, y, z, 0.0, 0.0, 0.0);
    }

    public static void nitorCore(
            Level level, double x, double y, double z, double vx, double vy, double vz, int color) {
        float r = ARGB32.red(color) / 255.0F;
        float g = ARGB32.green(color) / 255.0F;
        float b = ARGB32.blue(color) / 255.0F;
        spawn(new NitorCoreParticleOptions(r, g, b), x, y, z, vx, vy, vz);
    }

    public static void nitorFlames(
            Level level, double x, double y, double z, double vx, double vy, double vz, int color, int delay) {
        RandomSource rand = level.getRandom();
        WispFlameParticleOptions options =
                new WispFlameParticleOptions(color, 0.66F, 3.0F + rand.nextFloat(), 0.05F, delay);
        spawn(options, x, y, z, vx, vy, vz);
    }

    public static void blockRunes(
            Level level, double x, double y, double z, float r, float g, float b, int duration, float gravity) {
        spawn(
                new BlockRunesParticleOptions(r, g, b, duration, gravity, false),
                x + 0.5,
                y + 0.5,
                z + 0.5,
                0.0,
                0.0,
                0.0);
    }

    public static void scanHighlight(Level level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getShape(level, pos);
        if (shape.isEmpty()) {
            return;
        }
        scanHighlight(level, shape.bounds().move(pos));
    }

    public static void scanHighlight(Entity entity) {
        scanHighlight(entity.level(), entity.getBoundingBox());
    }

    public static void scanHighlight(Level level, AABB box) {
        RandomSource rand = level.getRandom();
        int num = Mth.ceil(box.getSize() * 2.0);
        double ax = (box.minX + box.maxX) / 2.0;
        double ay = (box.minY + box.maxY) / 2.0;
        double az = (box.minZ + box.maxZ) / 2.0;
        for (Direction face : Direction.values()) {
            double mx = 0.5 + face.getStepX() * 0.51;
            double my = 0.5 + face.getStepY() * 0.51;
            double mz = 0.5 + face.getStepZ() * 0.51;
            for (int a = 0; a < num * 2; a++) {
                double x = Mth.clamp(mx + rand.nextGaussian() * (box.maxX - box.minX), box.minX - ax, box.maxX - ax);
                double y = Mth.clamp(my + rand.nextGaussian() * (box.maxY - box.minY), box.minY - ay, box.maxY - ay);
                double z = Mth.clamp(mz + rand.nextGaussian() * (box.maxZ - box.minZ), box.minZ - az, box.maxZ - az);
                float r = Mth.nextInt(rand, 16, 32) / 255.0F;
                float g = Mth.nextInt(rand, 132, 165) / 255.0F;
                float b = Mth.nextInt(rand, 223, 239) / 255.0F;
                drawSimpleSparkle(
                        level,
                        rand,
                        ax + x,
                        ay + y,
                        az + z,
                        0.0,
                        0.0,
                        0.0,
                        0.4F + (float) rand.nextGaussian() * 0.1F,
                        r,
                        g,
                        b,
                        rand.nextInt(10),
                        1.0F,
                        0.0F,
                        4);
            }
        }
    }

    public static void drawSimpleSparkle(
            Level level,
            RandomSource rand,
            double x,
            double y,
            double z,
            double vx,
            double vy,
            double vz,
            float scale,
            float r,
            float g,
            float b,
            int delay,
            float decay,
            float gravity,
            int baseAge) {
        SparkleParticleOptions options = new SparkleParticleOptions(
                ARGB32.colorFromFloat(1.0F, r, g, b), scale, delay, decay, gravity, baseAge, true);
        spawn(options, x, y, z, vx, vy, vz);
    }

    private static void spawn(ParticleOptions options, double x, double y, double z, double vx, double vy, double vz) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;
        Vec3 at = ClientSpaces.renderPosition(mc.level, new Vec3(x, y, z));
        mc.particleEngine.createParticle(options, at.x, at.y, at.z, vx, vy, vz);
    }
}
