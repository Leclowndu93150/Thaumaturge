package com.leclowndu93150.thaumaturge.content.world.plant;

import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import com.leclowndu93150.thaumaturge.data.worldgen.biome.TTBiomes;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GrassBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockGrassAmbient extends GrassBlock {
    private static final int CAVE_MOTE_ONE_IN = 5;
    private static final int DAYLIGHT_CUTOFF = 5;
    private static final int CHANCE_DENOMINATOR = 7;
    private static final int CHANCE_AT_DARK = 6;
    private static final int SEARCH_RADIUS = 8;
    private static final int PROBE_START_ABOVE = 5;
    private static final int PROBE_MAX_DROP = 10;
    private static final int SURFACE_PROBE_FLOOR = 50;
    private static final float VALUE = 1.0F;
    private static final int OPAQUE_ALPHA = 255;
    private static final float GREEN_HUE = 0.30F;
    private static final float AQUA_HUE = 0.50F;
    private static final float MAX_TINT_SATURATION = 0.35F;
    private static final int MOTE_LIFE_TICKS = 400;
    private static final int MOTE_LIFE_VARIATION = 200;
    private static final float MOTE_BUOYANCY = -0.003F;
    private static final double TOP_FACE_OFFSET = 1.02;

    public BlockGrassAmbient(Properties properties) {
        super(properties);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
        if (level.getBiome(pos).is(TTBiomes.MAGICAL_FOREST_CAVES)) {
            if (random.nextInt(CAVE_MOTE_ONE_IN) == 0) {
                releaseMote(level, pos, random, level.getMinY());
            }
            return;
        }
        int daylight = daylightAbove(level, pos);
        if (daylight >= DAYLIGHT_CUTOFF || random.nextInt(CHANCE_DENOMINATOR) >= CHANCE_AT_DARK - daylight) {
            return;
        }
        releaseMote(level, pos, random, SURFACE_PROBE_FLOOR);
    }

    private static int daylightAbove(Level level, BlockPos pos) {
        BlockPos above = pos.above();
        int sky = level.getEffectiveSkyBrightness(above);
        if (sky <= 0) {
            return 0;
        }
        float sunAngle = level.environmentAttributes().getValue(EnvironmentAttributes.SUN_ANGLE, above) * Mth.DEG_TO_RAD;
        return Math.round(sky * Math.max(0.0F, Mth.cos(sunAngle)));
    }

    private void releaseMote(Level level, BlockPos origin, RandomSource random, int floorY) {
        int x = origin.getX() + random.nextIntBetweenInclusive(-SEARCH_RADIUS, SEARCH_RADIUS);
        int z = origin.getZ() + random.nextIntBetweenInclusive(-SEARCH_RADIUS, SEARCH_RADIUS);
        BlockPos.MutableBlockPos probe = new BlockPos.MutableBlockPos(x, origin.getY() + PROBE_START_ABOVE, z);
        int lowest = Math.max(floorY, probe.getY() - PROBE_MAX_DROP);
        while (probe.getY() >= lowest) {
            BlockState found = level.getBlockState(probe);
            if (found.is(Blocks.GRASS_BLOCK) || found.is(this)) {
                level.addParticle(paleMote(random), probe.getX() + random.nextDouble(), probe.getY() + TOP_FACE_OFFSET, probe.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
                return;
            }
            probe.move(0, -1, 0);
        }
    }

    private static WispyMoteParticleOptions paleMote(RandomSource random) {
        float hue = Mth.lerp(random.nextFloat(), GREEN_HUE, AQUA_HUE);
        float saturation = random.nextFloat() * MAX_TINT_SATURATION;
        int color = Mth.hsvToArgb(hue, saturation, VALUE, OPAQUE_ALPHA);
        int life = MOTE_LIFE_TICKS + random.nextInt(MOTE_LIFE_VARIATION);
        return new WispyMoteParticleOptions(color, life, MOTE_BUOYANCY, WispyMoteParticleOptions.NO_ENTITY);
    }
}
