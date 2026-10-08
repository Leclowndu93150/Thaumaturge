package com.leclowndu93150.thaumaturge.content.world.plant;

import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockGrassAmbient extends GrassBlock {
    private static final int CAVE_GLOW_ONE_IN = 5;
    private static final int DARKNESS_THRESHOLD = 5;
    private static final int WISP_REACH = 8;
    private static final int WISP_SCAN_TOP = 5;
    private static final int WISP_SCAN_STEPS = 10;
    private static final int WISP_FLOOR_Y = 50;
    private static final int WISP_LIFETIME = 320;
    private static final int WISP_LIFETIME_SPREAD = 100;
    private static final float WISP_BUOYANCY = -0.008F;
    private static final float WISP_HUE_MIN = 0.28F;
    private static final float WISP_HUE_SPAN = 0.22F;
    private static final float WISP_SATURATION_MIN = 0.25F;
    private static final float WISP_SATURATION_SPAN = 0.3F;

    public BlockGrassAmbient(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockPos above = pos.above();
        if (level.getBiome(pos).is(TTBiomes.MAGICAL_FOREST_CAVES)) {
            if (random.nextInt(CAVE_GLOW_ONE_IN) == 0 && level.isEmptyBlock(above)) {
                releaseWisp(level, above, random, true);
            }
            return;
        }
        int daylight =
                level.isNight() ? 0 : Math.max(0, level.getBrightness(LightLayer.SKY, above) - level.getSkyDarken());
        if (daylight < DARKNESS_THRESHOLD && random.nextInt(DARKNESS_THRESHOLD + 2) > daylight) {
            seedNearbyGrass(level, pos, random);
        }
    }

    private static void seedNearbyGrass(Level level, BlockPos origin, RandomSource random) {
        BlockPos.MutableBlockPos probe = origin.mutable()
                .move(
                        Mth.nextInt(random, -WISP_REACH, WISP_REACH),
                        WISP_SCAN_TOP,
                        Mth.nextInt(random, -WISP_REACH, WISP_REACH));
        for (int step = 0; step < WISP_SCAN_STEPS && probe.getY() > WISP_FLOOR_Y; step++) {
            if (level.getBlockState(probe).is(Blocks.GRASS_BLOCK)) {
                releaseWisp(level, probe.above(), random, false);
                return;
            }
            probe.move(Direction.DOWN);
        }
    }

    private static void releaseWisp(Level level, BlockPos at, RandomSource random, boolean glowing) {
        float hue = WISP_HUE_MIN + random.nextFloat() * WISP_HUE_SPAN;
        float saturation = WISP_SATURATION_MIN + random.nextFloat() * WISP_SATURATION_SPAN;
        int color = (0xFF000000 | Mth.hsvToRgb(hue, saturation, 1.0F));
        int lifetime = WISP_LIFETIME + random.nextInt(WISP_LIFETIME_SPREAD);
        level.addParticle(
                new WispyMoteParticleOptions(
                        color, lifetime, WISP_BUOYANCY, WispyMoteParticleOptions.NO_ENTITY, glowing),
                at.getX() + random.nextDouble(),
                at.getY(),
                at.getZ() + random.nextDouble(),
                0.0,
                0.0,
                0.0);
    }
}
