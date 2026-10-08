package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityStabilizer;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTInstabilityEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record EjectEffect(
        boolean consume, EjectAftermath aftermath, IntProvider mitigation, IntProvider pollution, float explosionPower)
        implements InstabilityEffect {
    private static final int PEDESTAL_TRIES = 25;
    private static final float FLUX_SOUND_VOLUME = 0.3F;
    public static final IntProvider DEFAULT_MITIGATION = UniformInt.of(5, 10);
    public static final IntProvider DEFAULT_POLLUTION = UniformInt.of(5, 9);
    public static final float DEFAULT_EXPLOSION_POWER = 1.0F;

    public static final MapCodec<EjectEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.BOOL.optionalFieldOf("consume", false).forGetter(EjectEffect::consume),
                    EjectAftermath.CODEC
                            .optionalFieldOf("aftermath", EjectAftermath.NONE)
                            .forGetter(EjectEffect::aftermath),
                    IntProvider.CODEC
                            .optionalFieldOf("mitigation", DEFAULT_MITIGATION)
                            .forGetter(EjectEffect::mitigation),
                    IntProvider.CODEC
                            .optionalFieldOf("pollution", DEFAULT_POLLUTION)
                            .forGetter(EjectEffect::pollution),
                    Codec.FLOAT
                            .optionalFieldOf("explosion_power", DEFAULT_EXPLOSION_POWER)
                            .forGetter(EjectEffect::explosionPower))
            .apply(instance, EjectEffect::new));

    public static EjectEffect of(boolean consume, EjectAftermath aftermath) {
        return new EjectEffect(consume, aftermath, DEFAULT_MITIGATION, DEFAULT_POLLUTION, DEFAULT_EXPLOSION_POWER);
    }

    @Override
    public InstabilityEffectType<?> type() {
        return TTInstabilityEffects.EJECT.get();
    }

    @Override
    public void apply(InstabilityContext context) {
        ServerLevel level = context.level();
        RandomSource random = context.random();
        for (int tries = 0; tries < PEDESTAL_TRIES && !context.pedestals().isEmpty(); tries++) {
            BlockPos pedestalPos =
                    context.pedestals().get(random.nextInt(context.pedestals().size()));
            if (!(level.getBlockEntity(pedestalPos) instanceof BlockEntityPedestal pedestal)
                    || pedestal.getItem().isEmpty()) {
                continue;
            }
            BlockPos mitigatorPos = pedestal.findInstabilityMitigator();
            if (mitigatorPos != null
                    && level.getBlockEntity(mitigatorPos) instanceof BlockEntityStabilizer stabilizer
                    && stabilizer.mitigate(mitigation.sample(random))) {
                return;
            }
            if (!consume) {
                Containers.dropItemStack(
                        level,
                        pedestalPos.getX() + 0.5,
                        pedestalPos.getY() + 1.0,
                        pedestalPos.getZ() + 0.5,
                        pedestal.getItem());
            }
            pedestal.setItem(ItemStack.EMPTY);
            aftermath(level, pedestalPos, random);
            context.arcTo(Vec3.atCenterOf(pedestalPos.above()));
            context.zapSound();
            return;
        }
    }

    private void aftermath(ServerLevel level, BlockPos pedestalPos, RandomSource random) {
        switch (aftermath) {
            case FLUX -> {
                if (random.nextBoolean()) {
                    PhysicalFlux.placeGoo(level, pedestalPos.above(), PhysicalFlux.MAX_QUANTA);
                } else {
                    PhysicalFlux.placeGas(level, pedestalPos.above(), PhysicalFlux.MAX_QUANTA);
                }
                level.playSound(
                        null, pedestalPos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, FLUX_SOUND_VOLUME, 1.0F);
            }
            case POLLUTE -> AuraHelper.polluteAura(level, pedestalPos, pollution.sample(random), true);
            case EXPLODE ->
                level.explode(
                        null,
                        pedestalPos.getX() + 0.5,
                        pedestalPos.getY() + 0.5,
                        pedestalPos.getZ() + 0.5,
                        explosionPower,
                        Level.ExplosionInteraction.NONE);
            case NONE -> {}
        }
    }
}
