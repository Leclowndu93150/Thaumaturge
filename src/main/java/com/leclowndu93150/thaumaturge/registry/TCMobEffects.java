package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.taint.effect.FluxTaintEffect;
import com.leclowndu93150.thaumaturge.content.taint.effect.InfectiousVisExhaustEffect;
import com.leclowndu93150.thaumaturge.content.taint.effect.VisExhaustEffect;
import com.leclowndu93150.thaumaturge.content.warp.effect.BlurredVisionEffect;
import com.leclowndu93150.thaumaturge.content.warp.effect.DeathGazeEffect;
import com.leclowndu93150.thaumaturge.content.warp.effect.SunScornedEffect;
import com.leclowndu93150.thaumaturge.content.warp.effect.ThaumarhiaEffect;
import com.leclowndu93150.thaumaturge.content.warp.effect.UnnaturalHungerEffect;
import com.leclowndu93150.thaumaturge.content.warp.effect.WarpWardEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCMobEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, TCIds.MODID);

    public static final Holder<MobEffect> VIS_EXHAUST = MOB_EFFECTS.register("vis_exhaust", VisExhaustEffect::new);

    public static final Holder<MobEffect> INFECTIOUS_VIS_EXHAUST = MOB_EFFECTS.register("infectious_vis_exhaust", InfectiousVisExhaustEffect::new);

    public static final Holder<MobEffect> FLUX_TAINT = MOB_EFFECTS.register("flux_taint", FluxTaintEffect::new);

    public static final Holder<MobEffect> THAUMARHIA = MOB_EFFECTS.register("thaumarhia", ThaumarhiaEffect::new);

    public static final Holder<MobEffect> UNNATURAL_HUNGER = MOB_EFFECTS.register("unnatural_hunger", UnnaturalHungerEffect::new);

    public static final Holder<MobEffect> SUN_SCORNED = MOB_EFFECTS.register("sun_scorned", SunScornedEffect::new);

    public static final Holder<MobEffect> DEATH_GAZE = MOB_EFFECTS.register("death_gaze", DeathGazeEffect::new);

    public static final Holder<MobEffect> BLURRED_VISION = MOB_EFFECTS.register("blurred_vision", BlurredVisionEffect::new);

    public static final Holder<MobEffect> WARP_WARD = MOB_EFFECTS.register("warp_ward", WarpWardEffect::new);

    private TCMobEffects() {}

    public static void register(IEventBus modBus) {
        MOB_EFFECTS.register(modBus);
    }
}
