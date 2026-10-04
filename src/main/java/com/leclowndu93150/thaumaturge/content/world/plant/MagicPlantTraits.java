package com.leclowndu93150.thaumaturge.content.world.plant;

import java.util.List;
import java.util.Optional;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffects;

public final class MagicPlantTraits {
    public static final PlantAura CINDERPEARL_EMBERS = new PlantAura(List.of(ParticleTypes.SMOKE, ParticleTypes.FLAME), 0.4F, 0.65F, 0.06F, 0.0F);
    public static final PlantAura VISHROOM_SPORES = new PlantAura(List.of(ParticleTypes.WITCH), 0.3F, 0.3F, 0.35F, 0.0F);
    public static final PlantAura SHIMMERLEAF_GLINTS = new PlantAura(List.of(ParticleTypes.END_ROD), 0.3F, 0.45F, 0.12F, 0.012F);

    public static final Optional<PlantContactEffect> VISHROOM_NAUSEA = Optional.of(new PlantContactEffect(MobEffects.NAUSEA, 200, 5));

    private MagicPlantTraits() {}
}
