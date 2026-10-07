package com.leclowndu93150.thaumaturge.data.damagetype;

import com.leclowndu93150.thaumaturge.api.damagesource.TTDamageTypes;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.damagesource.DamageEffects;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

public final class TTDamageTypeBootstrap {
    private TTDamageTypeBootstrap() {}

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(
                TTDamageTypes.TAINT,
                new DamageType(
                        "thaumaturge.taint", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.0F, DamageEffects.HURT));
        context.register(
                TTDamageTypes.TENTACLE,
                new DamageType(
                        "thaumaturge.tentacle",
                        DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                        0.1F,
                        DamageEffects.HURT));
        context.register(
                TTDamageTypes.SWARM,
                new DamageType(
                        "thaumaturge.swarm", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1F, DamageEffects.HURT));
        context.register(
                TTDamageTypes.DISSOLVE,
                new DamageType(
                        "thaumaturge.dissolve",
                        DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                        0.0F,
                        DamageEffects.HURT));
        context.register(
                TTDamageTypes.FOCUS_FIRE,
                new DamageType(
                        "thaumaturge.focus_fire",
                        DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER,
                        0.1F,
                        DamageEffects.BURNING));
    }
}
