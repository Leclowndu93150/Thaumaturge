package com.leclowndu93150.thaumaturge.content.infusion.instability;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.device.BlockEntityStabilizer;
import com.leclowndu93150.thaumaturge.content.infusion.BlockEntityPedestal;
import com.leclowndu93150.thaumaturge.content.taint.flux.PhysicalFlux;
import com.leclowndu93150.thaumaturge.registry.TTInstabilityEffects;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

public record EjectEffect(boolean consume, EjectAftermath aftermath, IntProvider mitigation, IntProvider pollution, float explosionPower) implements InstabilityEffect {
    private static final int MITIGATION_MIN = 5;
    private static final int MITIGATION_MAX = 10;
    private static final int POLLUTION_MIN = 5;
    private static final int POLLUTION_MAX = 9;
    private static final int MAX_DRAWS = 25;
    private static final double CENTER_OFFSET = 0.5;
    private static final double DROP_HEIGHT = 1.0;
    private static final float FLUX_GOO_CHANCE = 0.5F;
    private static final float BOTTLE_VOLUME = 0.3F;
    private static final float BOTTLE_PITCH = 1.0F;

    public static final IntProvider DEFAULT_MITIGATION = UniformInt.of(MITIGATION_MIN, MITIGATION_MAX);
    public static final IntProvider DEFAULT_POLLUTION = UniformInt.of(POLLUTION_MIN, POLLUTION_MAX);
    public static final float DEFAULT_EXPLOSION_POWER = 1.0F;

    public static final MapCodec<EjectEffect> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(Codec.BOOL.optionalFieldOf("consume", false).forGetter(EjectEffect::consume),
            EjectAftermath.CODEC.optionalFieldOf("aftermath", EjectAftermath.NONE).forGetter(EjectEffect::aftermath),
            IntProviders.CODEC.optionalFieldOf("mitigation", DEFAULT_MITIGATION).forGetter(EjectEffect::mitigation),
            IntProviders.CODEC.optionalFieldOf("pollution", DEFAULT_POLLUTION).forGetter(EjectEffect::pollution),
            Codec.FLOAT.optionalFieldOf("explosion_power", DEFAULT_EXPLOSION_POWER).forGetter(EjectEffect::explosionPower)).apply(instance, EjectEffect::new));

    public static EjectEffect of(boolean consume, EjectAftermath aftermath) {
        return new EjectEffect(consume, aftermath, DEFAULT_MITIGATION, DEFAULT_POLLUTION, DEFAULT_EXPLOSION_POWER);
    }

    @Override
    public InstabilityEffectType<?> type() {
        return TTInstabilityEffects.EJECT.get();
    }

    @Override
    public void apply(InstabilityContext context) {
        BlockEntityPedestal victim = drawStockedPedestal(context);
        if (victim != null) {
            strike(context, victim);
        }
    }

    private static BlockEntityPedestal drawStockedPedestal(InstabilityContext context) {
        List<BlockPos> candidates = context.pedestals();
        if (candidates.isEmpty()) {
            return null;
        }
        int remaining = MAX_DRAWS;
        while (remaining-- > 0) {
            BlockPos drawn = candidates.get(context.random().nextInt(candidates.size()));
            BlockEntity found = context.level().getBlockEntity(drawn);
            if (found instanceof BlockEntityPedestal candidate && !candidate.getItem().isEmpty()) {
                return candidate;
            }
        }
        return null;
    }

    private boolean absorbedByStabilizer(InstabilityContext context, BlockEntityPedestal pedestal) {
        BlockPos source = pedestal.findInstabilityMitigator();
        BlockEntity found = source == null ? null : context.level().getBlockEntity(source);
        if (!(found instanceof BlockEntityStabilizer stabilizer)) {
            return false;
        }
        return stabilizer.spendForShield(mitigation.sample(context.random()));
    }

    private void strike(InstabilityContext context, BlockEntityPedestal pedestal) {
        if (absorbedByStabilizer(context, pedestal)) {
            return;
        }
        ServerLevel level = context.level();
        BlockPos origin = pedestal.getBlockPos();
        ItemStack held = pedestal.getItem();
        pedestal.setItem(ItemStack.EMPTY);
        if (!consume) {
            spill(level, origin, held);
        }
        runAftermath(level, context.random(), origin);
        context.arcTo(Vec3.atCenterOf(origin.above()));
        context.zapSound();
    }

    private static void spill(ServerLevel level, BlockPos origin, ItemStack held) {
        double x = origin.getX() + CENTER_OFFSET;
        double y = origin.getY() + DROP_HEIGHT;
        double z = origin.getZ() + CENTER_OFFSET;
        level.addFreshEntity(new ItemEntity(level, x, y, z, held));
    }

    private void runAftermath(ServerLevel level, RandomSource random, BlockPos origin) {
        if (aftermath == EjectAftermath.FLUX) {
            leakFlux(level, random, origin);
        } else if (aftermath == EjectAftermath.POLLUTE) {
            AuraHelper.polluteAura(level, origin, pollution.sample(random), true);
        } else if (aftermath == EjectAftermath.EXPLODE) {
            detonate(level, origin);
        }
    }

    private static void leakFlux(ServerLevel level, RandomSource random, BlockPos origin) {
        boolean goo = random.nextFloat() < FLUX_GOO_CHANCE;
        placeFlux(level, origin.above(), goo);
        level.playSound(null, origin, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, BOTTLE_VOLUME, BOTTLE_PITCH);
    }

    private static void placeFlux(ServerLevel level, BlockPos target, boolean goo) {
        if (goo) {
            PhysicalFlux.placeGoo(level, target, PhysicalFlux.MAX_QUANTA);
            return;
        }
        PhysicalFlux.placeGas(level, target, PhysicalFlux.MAX_QUANTA);
    }

    private void detonate(ServerLevel level, BlockPos origin) {
        Vec3 center = Vec3.atLowerCornerWithOffset(origin, CENTER_OFFSET, CENTER_OFFSET, CENTER_OFFSET);
        level.explode(null, center.x, center.y, center.z, explosionPower, Level.ExplosionInteraction.NONE);
    }
}
