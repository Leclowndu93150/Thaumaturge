package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityStabilizer;
import com.leclowndu93150.thaumaturge.content.effect.EffectDispatch;
import com.leclowndu93150.thaumaturge.content.effect.Effects;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.content.particle.ShieldSparkParticleOptions;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.content.world.LoadedChunks;
import com.leclowndu93150.thaumaturge.content.world.space.LevelSpaces;
import com.leclowndu93150.thaumaturge.registry.TTInstabilityEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public record EjectEffect(
        boolean consume,
        EjectAftermath aftermath,
        IntProvider mitigation,
        IntProvider pollution,
        float explosionPower,
        Optional<Float> consumeAtOrBelow)
        implements InstabilityEffect {
    private static final double PEDESTAL_TOP = 1.0;
    private static final float FLUX_SOUND_VOLUME = 0.3F;
    private static final float PUFF_RED = 0.3F;
    private static final float PUFF_GREEN = 0.0F;
    private static final float PUFF_BLUE = 0.35F;
    private static final int WARD_FLASH_COUNT = 6;
    private static final double WARD_FLASH_SPREAD = 0.35;
    private static final double WARD_FLASH_LIFT = 0.6;
    private static final int WARD_FLASH_COLOR = ARGB32.colorFromFloat(1.0F, 0.7F, 0.6F, 1.0F);
    private static final float WARD_FLASH_ALPHA = 0.8F;
    private static final float WARD_FLASH_SCALE = 0.5F;
    private static final int WARD_FLASH_AGE = 10;
    private static final int WARD_FLASH_DELAY = 0;
    private static final int WARD_ARC_COLOR = 0x6A7CFF;
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
                            .forGetter(EjectEffect::explosionPower),
                    Codec.FLOAT.optionalFieldOf("consume_at_or_below").forGetter(EjectEffect::consumeAtOrBelow))
            .apply(instance, EjectEffect::new));

    public EjectEffect(
            boolean consume,
            EjectAftermath aftermath,
            IntProvider mitigation,
            IntProvider pollution,
            float explosionPower) {
        this(consume, aftermath, mitigation, pollution, explosionPower, Optional.empty());
    }

    public static EjectEffect of(boolean consume, EjectAftermath aftermath) {
        return new EjectEffect(consume, aftermath, DEFAULT_MITIGATION, DEFAULT_POLLUTION, DEFAULT_EXPLOSION_POWER);
    }

    public EjectEffect consumingAtOrBelow(float stability) {
        return new EjectEffect(consume, aftermath, mitigation, pollution, explosionPower, Optional.of(stability));
    }

    boolean consumesAt(float stability) {
        return consume || consumeAtOrBelow.map(limit -> stability <= limit).orElse(false);
    }

    @Override
    public InstabilityEffectType<?> type() {
        return TTInstabilityEffects.EJECT.get();
    }

    @Override
    public void apply(InstabilityContext context) {
        ServerLevel level = context.level();
        RandomSource random = context.random();
        List<BlockEntityPedestal> stocked = new ArrayList<>();
        for (BlockPos pos : context.pedestals()) {
            if (LoadedChunks.contains(level, pos)
                    && level.getBlockEntity(pos) instanceof BlockEntityPedestal pedestal
                    && !pedestal.getItem().isEmpty()) {
                stocked.add(pedestal);
            }
        }
        if (stocked.isEmpty()) {
            return;
        }

        BlockEntityPedestal pedestal = stocked.get(random.nextInt(stocked.size()));
        BlockPos target = pedestal.getBlockPos();
        BlockPos stabilizerPos = pedestal.findInstabilityMitigator();
        if (stabilizerPos != null
                && LoadedChunks.contains(level, stabilizerPos)
                && level.getBlockEntity(stabilizerPos) instanceof BlockEntityStabilizer stabilizer
                && stabilizer.spendForShield(mitigation.sample(random))) {
            flashWard(context, target);
            Vec3 top = topOf(target);
            context.arcTo(top);
            EffectDispatch.spawnArc(level, top, Vec3.atCenterOf(stabilizerPos), WARD_ARC_COLOR, 0.0F);
            return;
        }

        if (!consumesAt(context.stability())) {
            level.addFreshEntity(new ItemEntity(
                    level, target.getX() + 0.5, target.getY() + 1.0, target.getZ() + 0.5, pedestal.getItem()));
        }
        pedestal.setItem(ItemStack.EMPTY);
        aftermath(level, target, random);
        Effects.bamf(level, topOf(target))
                .color(PUFF_RED, PUFF_GREEN, PUFF_BLUE)
                .send();
        context.arcTo(topOf(target));
    }

    private static void flashWard(InstabilityContext context, BlockPos target) {
        RandomSource random = context.random();
        Vec3 top = topOf(target);
        ShieldSparkParticleOptions flash = new ShieldSparkParticleOptions(
                WARD_FLASH_COLOR, WARD_FLASH_ALPHA, WARD_FLASH_SCALE, WARD_FLASH_AGE, WARD_FLASH_DELAY, true);
        for (int i = 0; i < WARD_FLASH_COUNT; i++) {
            double x = top.x + random.triangle(0.0, WARD_FLASH_SPREAD);
            double y = top.y + random.nextDouble() * WARD_FLASH_LIFT;
            double z = top.z + random.triangle(0.0, WARD_FLASH_SPREAD);
            Effects.spawn(context.level(), flash, x, y, z);
        }
    }

    private static Vec3 topOf(BlockPos target) {
        return new Vec3(target.getX() + 0.5, target.getY() + PEDESTAL_TOP, target.getZ() + 0.5);
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
            case EXPLODE -> {
                Vec3 centre = LevelSpaces.worldCenter(level, pedestalPos);
                level.explode(null, centre.x, centre.y, centre.z, explosionPower, Level.ExplosionInteraction.NONE);
            }
            case NONE -> {}
        }
    }
}
