package com.leclowndu93150.thaumaturge.data.infusion;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.infusion.instability.EjectAftermath;
import com.leclowndu93150.thaumaturge.content.infusion.instability.EjectEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.ExplodeEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.HarmEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.InstabilityEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.InstabilityOutcome;
import com.leclowndu93150.thaumaturge.content.infusion.instability.WarpEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.ZapEffect;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import java.util.List;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.UniformFloat;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.effect.MobEffectInstance;

public final class InstabilityOutcomeBootstrap {
    private static final IntProvider ZAP_DAMAGE = UniformInt.of(4, 7);
    private static final int FLUX_TAINT_TICKS = 120;
    private static final int VIS_EXHAUST_TICKS = 2400;
    private static final float PERMANENT_WARP_CHANCE = 0.25F;
    private static final IntProvider PERMANENT_WARP = ConstantInt.of(1);
    private static final IntProvider TEMPORARY_WARP = UniformInt.of(2, 5);
    private static final float EXPLOSION_MIN = 1.5F;
    private static final float EXPLOSION_MAX = 2.5F;

    private InstabilityOutcomeBootstrap() {}

    public static void bootstrap(BootstrapContext<InstabilityOutcome> context) {
        register(context, "drop_item", 4, EjectEffect.of(false, EjectAftermath.NONE));
        register(context, "warp", 3, new WarpEffect(PERMANENT_WARP_CHANCE, PERMANENT_WARP, TEMPORARY_WARP));
        register(context, "zap", 3, new ZapEffect(false, ZAP_DAMAGE));
        register(context, "zap_everyone", 2, new ZapEffect(true, ZAP_DAMAGE));
        register(context, "drop_item_flux", 2, EjectEffect.of(false, EjectAftermath.FLUX));
        register(context, "drop_item_pollute", 2, EjectEffect.of(false, EjectAftermath.POLLUTE));
        register(context, "consume_item_flux", 1, EjectEffect.of(true, EjectAftermath.FLUX));
        register(context, "consume_item_pollute", 1, EjectEffect.of(true, EjectAftermath.POLLUTE));
        register(context, "harm", 2, new HarmEffect(false, harmEffects()));
        register(context, "drop_item_explode", 2, EjectEffect.of(false, EjectAftermath.EXPLODE));
        register(context, "harm_everyone", 1, new HarmEffect(true, harmEffects()));
        register(context, "explosion", 1, new ExplodeEffect(UniformFloat.of(EXPLOSION_MIN, EXPLOSION_MAX)));
    }

    private static List<MobEffectInstance> harmEffects() {
        return List.of(
                new MobEffectInstance(TTMobEffects.FLUX_TAINT, FLUX_TAINT_TICKS, 0, false, true),
                new MobEffectInstance(TTMobEffects.VIS_EXHAUST, VIS_EXHAUST_TICKS, 0, true, true));
    }

    private static void register(
            BootstrapContext<InstabilityOutcome> context, String name, int weight, InstabilityEffect effect) {
        context.register(
                ResourceKey.create(InstabilityOutcome.REGISTRY_KEY, TTIds.rl(name)),
                new InstabilityOutcome(weight, effect));
    }
}
