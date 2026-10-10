package com.leclowndu93150.thaumaturge.content.world.plant;

import com.leclowndu93150.thaumaturge.content.particle.WispyMoteParticleOptions;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;

public final class MagicPlantTraits {
    private static final int VISHROOM_SPORE_COLOR = 0xFFB48CE6;
    private static final int VISHROOM_SPORE_LIFE = 14;
    private static final float VISHROOM_SPORE_GRAVITY = 0.0F;
    private static final float VISHROOM_SPORE_CHANCE = 0.33F;
    private static final float VISHROOM_CAP_HEIGHT = 0.45F;
    private static final float VISHROOM_SPORE_SPREAD = 0.08F;
    private static final float VISHROOM_SPORE_DRIFT = 0.002F;

    private static final float CINDERPEARL_PUFF_CHANCE = 0.5F;
    private static final float CINDERPEARL_BULB_HEIGHT = 0.6F;
    private static final float CINDERPEARL_PUFF_SPREAD = 0.02F;
    private static final float CINDERPEARL_PUFF_DRIFT = 0.0F;

    private static final int SHIMMERLEAF_GLINT_COLOR = 0xFFD2F6FA;
    private static final int SHIMMERLEAF_GLINT_LIFE = 16;
    private static final float SHIMMERLEAF_GLINT_GRAVITY = 0.0F;
    private static final float SHIMMERLEAF_GLINT_CHANCE = 0.33F;
    private static final float SHIMMERLEAF_LEAF_HEIGHT = 0.4F;
    private static final float SHIMMERLEAF_GLINT_SPREAD = 0.15F;
    private static final float SHIMMERLEAF_GLINT_DRIFT = 0.006F;

    public static final PlantAura VISHROOM_SPORES = new PlantAura(
            List.of(new WispyMoteParticleOptions(VISHROOM_SPORE_COLOR, VISHROOM_SPORE_LIFE, VISHROOM_SPORE_GRAVITY, WispyMoteParticleOptions.NO_ENTITY)), VISHROOM_SPORE_CHANCE, VISHROOM_CAP_HEIGHT,
            VISHROOM_SPORE_SPREAD, VISHROOM_SPORE_DRIFT);

    public static final PlantAura CINDERPEARL_EMBERS = new PlantAura(List.of(ParticleTypes.SMOKE, ParticleTypes.FLAME), CINDERPEARL_PUFF_CHANCE, CINDERPEARL_BULB_HEIGHT, CINDERPEARL_PUFF_SPREAD,
            CINDERPEARL_PUFF_DRIFT);

    public static final PlantAura SHIMMERLEAF_GLINTS = new PlantAura(
            List.of(new WispyMoteParticleOptions(SHIMMERLEAF_GLINT_COLOR, SHIMMERLEAF_GLINT_LIFE, SHIMMERLEAF_GLINT_GRAVITY, WispyMoteParticleOptions.NO_ENTITY)), SHIMMERLEAF_GLINT_CHANCE,
            SHIMMERLEAF_LEAF_HEIGHT, SHIMMERLEAF_GLINT_SPREAD, SHIMMERLEAF_GLINT_DRIFT);

    public static final Optional<PlantContactEffect> VISHROOM_NAUSEA = Optional.of(new PlantContactEffect(MobEffects.NAUSEA, 200, 5));

    private MagicPlantTraits() {}
}
