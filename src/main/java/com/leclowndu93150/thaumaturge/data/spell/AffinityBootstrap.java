package com.leclowndu93150.thaumaturge.data.spell;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.damagesource.TTDamageTypes;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.content.spell.action.DamageAction;
import com.leclowndu93150.thaumaturge.content.spell.action.DispelAction;
import com.leclowndu93150.thaumaturge.content.spell.action.ErodeAction;
import com.leclowndu93150.thaumaturge.content.spell.action.ExtinguishAction;
import com.leclowndu93150.thaumaturge.content.spell.action.FreezeAction;
import com.leclowndu93150.thaumaturge.content.spell.action.FreezeWaterAction;
import com.leclowndu93150.thaumaturge.content.spell.action.GrowAction;
import com.leclowndu93150.thaumaturge.content.spell.action.HealAction;
import com.leclowndu93150.thaumaturge.content.spell.action.IgniteAction;
import com.leclowndu93150.thaumaturge.content.spell.action.KnockbackAction;
import com.leclowndu93150.thaumaturge.content.spell.action.MobEffectAction;
import com.leclowndu93150.thaumaturge.content.spell.action.PlaceBlockAction;
import com.leclowndu93150.thaumaturge.content.spell.action.PolluteAction;
import com.leclowndu93150.thaumaturge.content.spell.action.SapAction;
import com.leclowndu93150.thaumaturge.content.spell.action.TeleportAction;
import com.leclowndu93150.thaumaturge.content.spell.action.WindBurstAction;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import com.leclowndu93150.thaumaturge.registry.TTMobEffects;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.block.Blocks;

public final class AffinityBootstrap {
    private static final int SECOND = 20;

    private AffinityBootstrap() {}

    public static void bootstrap(BootstrapContext<AspectAffinity> ctx) {
        register(ctx, TTAspects.IGNIS, "flame", 0, "base_auromancy", sound(SoundEvents.FIRECHARGE_USE),
                List.of(new DamageAction(TTDamageTypes.FOCUS_FIRE, 3.0F, 1.0F, 1.0F, false), new IgniteAction(1.0F, 2.0F)), List.of(new PlaceBlockAction(Blocks.FIRE.defaultBlockState(), 0.5F, true)),
                List.of(effect(MobEffects.FIRE_RESISTANCE, 5, 5, 0.0F, 0.0F, 0)));
        register(ctx, TTAspects.GELUM, "frost", 0, "focus_elemental", sound(SoundEvents.POWDER_SNOW_BREAK),
                List.of(new DamageAction(DamageTypes.FREEZE, 3.0F, 1.0F, 1.0F, false), new FreezeAction(0.0F, SECOND), effect(MobEffects.SLOWNESS, 0, 0.5F, 0.0F, 0.3F, 2)),
                List.of(new FreezeWaterAction(0.0F, 1.5F, 8.0F)), List.of());
        register(ctx, TTAspects.AER, "gust", 0, "focus_elemental", TTSounds.WIND, List.of(new KnockbackAction(0.6F, 0.25F, 0.35F, false)), List.of(new WindBurstAction(1.0F, 0.4F, 3.0F)),
                List.of(effect(MobEffects.SLOW_FALLING, 5, 5, 0.0F, 0.0F, 0), effect(MobEffects.JUMP_BOOST, 5, 5, 0.0F, 0.5F, 2)));
        register(ctx, TTAspects.TERRA, "pebble", 0, "focus_elemental", sound(SoundEvents.DRAGON_FIREBALL_EXPLODE), List.of(new DamageAction(DamageTypes.THROWN, 0.0F, 2.0F, 1.0F, false)),
                List.of(new ErodeAction(0.0F, 0.08F, 0.1F)), List.of(effect(MobEffects.RESISTANCE, 5, 5, 0.0F, 0.5F, 2)));
        register(ctx, TTAspects.AQUA, "bubble", 0, "focus_elemental", sound(SoundEvents.GENERIC_SPLASH),
                List.of(new ExtinguishAction(1.0F), new DamageAction(DamageTypes.DROWN, 2.0F, 1.5F, 1.0F, true), new KnockbackAction(0.3F, 0.1F, 0.1F, false)), List.of(new ExtinguishAction(1.0F)),
                List.of(effect(MobEffects.WATER_BREATHING, 10, 10, 0.0F, 0.0F, 0), effect(MobEffects.DOLPHINS_GRACE, 3, 3, 0.0F, 0.0F, 0)));
        register(ctx, TTAspects.ORDO, "ward", 1, "focus_ward", sound(SoundEvents.BEACON_POWER_SELECT), List.of(new DispelAction(1.0F, 0.5F)), List.of(),
                List.of(effect(MobEffects.ABSORPTION, 10, 6, 0.0F, 0.5F, 3)));
        register(ctx, TTAspects.PERDITIO, "crack", 1, "focus_break", sound(SoundEvents.END_GATEWAY_SPAWN),
                List.of(new DamageAction(DamageTypes.MAGIC, 1.0F, 1.2F, 1.0F, false), effect(MobEffects.WITHER, 1, 1, 0.0F, 0.25F, 1)), List.of(new ErodeAction(0.3F, 0.15F, 0.15F)),
                List.of(effect(MobEffects.HASTE, 10, 10, 0.0F, 0.5F, 2)));
        register(ctx, TTAspects.VICTUS, "heal", 0, "focus_heal", sound(SoundEvents.CHORUS_FLOWER_GROW), List.of(new HealAction(1.0F, 1.5F, 1.5F)), List.of(new GrowAction(0.0F, 0.5F)),
                List.of(effect(MobEffects.REGENERATION, 3, 3, 0.0F, 0.5F, 2)));
        register(ctx, TTAspects.MORTUUS, "curse", 1, "focus_curse", sound(SoundEvents.ELDER_GUARDIAN_CURSE), curse(), List.of(new SapAction(0.0F, 0.75F, 5.0F)), List.of());
        register(ctx, TTAspects.VITIUM, "flux", 1, "focus_flux", sound(SoundEvents.CHORUS_FLOWER_GROW),
                List.of(new DamageAction(DamageTypes.INDIRECT_MAGIC, 3.0F, 1.0F, 1.0F, false), new MobEffectAction(TTMobEffects.FLUX_TAINT, 5 * SECOND, 0.0F, 0.0F, 0.0F, 0, 0.1F)),
                List.of(new PolluteAction(0.0F, 0.2F, 0.25F)), List.of());
        register(ctx, TTAspects.ALIENIS, "rift", 2, "focus_rift", sound(SoundEvents.CHORUS_FRUIT_TELEPORT), List.of(new TeleportAction(4.0F, 2.0F)), List.of(), List.of());
        register(ctx, TTAspects.VINCULUM, "ward", 2, "focus_mine", sound(SoundEvents.CHAIN_PLACE), List.of(effect(MobEffects.SLOWNESS, 0, 1, 4.0F, 0.0F, 6)), List.of(), List.of());
        register(ctx, TTAspects.HERBA, "leaf", 0, null, sound(SoundEvents.GRASS_PLACE), List.of(effect(MobEffects.POISON, 2, 1, 0.0F, 0.0F, 1)), List.of(new GrowAction(1.0F, 0.5F)),
                List.of(new MobEffectAction(MobEffects.SATURATION, 1.0F, 0.0F, 0.0F, 1.0F, 4, 1.0F)));
        register(ctx, TTAspects.POTENTIA, "spark", 2, "focus_bolt", TTSounds.ZAP, List.of(new DamageAction(DamageTypes.LIGHTNING_BOLT, 4.0F, 1.8F, 1.0F, false)),
                List.of(new PlaceBlockAction(TTBlocks.EFFECT_SHOCK.get().defaultBlockState(), 0.6F, true)), List.of(effect(MobEffects.STRENGTH, 5, 5, 0.0F, 0.4F, 1)));
        register(ctx, TTAspects.LUX, "sparkle", 0, null, sound(SoundEvents.AMETHYST_BLOCK_CHIME),
                List.of(effect(MobEffects.GLOWING, 5, 5, 0.0F, 0.0F, 0), new DamageAction(DamageTypes.MAGIC, 0.0F, 0.5F, 4.0F, false)), List.of(),
                List.of(effect(MobEffects.NIGHT_VISION, 20, 20, 0.0F, 0.0F, 0)));
        register(ctx, TTAspects.TENEBRAE, "smoke", 1, null, sound(SoundEvents.WITHER_SHOOT),
                List.of(effect(MobEffects.BLINDNESS, 2, 1, 0.0F, 0.0F, 0), effect(MobEffects.DARKNESS, 2, 1, 0.0F, 0.0F, 0)), List.of(), List.of(effect(MobEffects.INVISIBILITY, 5, 5, 0.0F, 0.0F, 0)));
        register(ctx, TTAspects.MOTUS, "mote", 0, "focus_projectile", SoundEvents.BREEZE_WIND_CHARGE_BURST, List.of(new KnockbackAction(0.4F, 0.3F, 0.1F, false)), List.of(),
                List.of(effect(MobEffects.SPEED, 10, 10, 0.0F, 0.5F, 2)));
        register(ctx, TTAspects.VACUOS, "rift", 1, "focus_rift", sound(SoundEvents.ENDER_EYE_DEATH), List.of(new KnockbackAction(0.6F, 0.25F, 0.1F, true)), List.of(), List.of());
        register(ctx, TTAspects.VOLATUS, "gust", 1, "focus_elemental", TTSounds.WIND, List.of(effect(MobEffects.LEVITATION, 1, 1, 0.0F, 0.4F, 2)), List.of(),
                List.of(effect(MobEffects.SLOW_FALLING, 10, 10, 0.0F, 0.0F, 0)));
    }

    private static List<SpellAction> curse() {
        return List.of(new DamageAction(DamageTypes.INDIRECT_MAGIC, 1.0F, 1.0F, 1.0F, false), new MobEffectAction(MobEffects.POISON, 0.0F, SECOND, 0.0F, 0.5F, 3, 1.0F),
                new MobEffectAction(MobEffects.SLOWNESS, 0.0F, SECOND, 0.0F, 0.5F, 3, 0.85F), new MobEffectAction(MobEffects.WEAKNESS, 0.0F, SECOND, 0.0F, 0.5F, 3, 0.7F),
                new MobEffectAction(MobEffects.MINING_FATIGUE, 0.0F, SECOND * 2, 0.0F, 0.5F, 3, 0.55F), new MobEffectAction(MobEffects.HUNGER, 0.0F, SECOND * 3, 0.0F, 0.5F, 3, 0.4F),
                new MobEffectAction(MobEffects.UNLUCK, 0.0F, SECOND * 3, 0.0F, 0.5F, 3, 0.25F));
    }

    private static MobEffectAction effect(Holder<MobEffect> effect, float seconds, float secondsPerDuration, float amplifier, float perPower, int maxAmplifier) {
        return new MobEffectAction(effect, seconds * SECOND, secondsPerDuration * SECOND, amplifier, perPower, maxAmplifier, 1.0F);
    }

    private static Holder<SoundEvent> sound(SoundEvent event) {
        return BuiltInRegistries.SOUND_EVENT.wrapAsHolder(event);
    }

    private static void register(BootstrapContext<AspectAffinity> ctx, ResourceKey<IAspect> aspect, String fx, int complexity, String research, Holder<SoundEvent> sound, List<SpellAction> entity, List<SpellAction> block, List<SpellAction> imbue) {
        Optional<ResearchGate> gate = research == null ? Optional.empty() : Optional.of(new ResearchGate(TTIds.rl(research), Optional.empty(), false));
        ctx.register(ResourceKey.create(AspectAffinity.REGISTRY_KEY, aspect.identifier()),
                new AspectAffinity(TTIds.rl(fx), complexity, complexity * 0.1F, gate, Optional.of(sound), entity, block, imbue));
    }
}
