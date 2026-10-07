package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehaviorType;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import com.leclowndu93150.thaumaturge.content.spell.delivery.ArchitectDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.AuraDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.BatDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.ChainDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.CloudDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.HierophantDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.MineDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.ProjectileDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.RayDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SelfDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SprayDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SummonDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.WallDelivery;
import com.leclowndu93150.thaumaturge.content.spell.effect.AspectBurstEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.AspectStrikeEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.BlinkEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.BreakEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.EldritchRendEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.ExchangeEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.HarvestEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.HellbatEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.ImbueEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.PrimalEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.RiftEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.WardEffect;
import com.leclowndu93150.thaumaturge.content.spell.flow.BranchFlow;
import com.leclowndu93150.thaumaturge.content.spell.flow.DelayFlow;
import com.leclowndu93150.thaumaturge.content.spell.flow.OriginFlow;
import com.leclowndu93150.thaumaturge.content.spell.flow.RelayFlow;
import com.leclowndu93150.thaumaturge.content.spell.flow.SieveFlow;
import com.leclowndu93150.thaumaturge.content.spell.modifier.EchoModifier;
import com.leclowndu93150.thaumaturge.content.spell.modifier.FanModifier;
import com.leclowndu93150.thaumaturge.content.spell.modifier.ScatterModifier;
import com.leclowndu93150.thaumaturge.content.spell.modifier.StatModifier;
import com.leclowndu93150.thaumaturge.content.spell.script.ScriptedEffect;
import com.leclowndu93150.thaumaturge.content.spell.script.ScriptedNode;
import com.mojang.serialization.MapCodec;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTSpellBehaviors {
    public static final DeferredRegister<SpellBehaviorType<?>> BEHAVIORS = DeferredRegister.create(SpellBehaviorType.REGISTRY_KEY, TTIds.MODID);

    private static final Registry<SpellBehaviorType<?>> REGISTRY = BEHAVIORS.makeRegistry(builder -> builder.sync(false));

    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<OriginFlow>> ORIGIN = flow("origin", OriginFlow.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<BranchFlow>> BRANCH = flow("branch", BranchFlow.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<RelayFlow>> RELAY = flow("relay", RelayFlow.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<DelayFlow>> DELAY = flow("delay", DelayFlow.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<SieveFlow>> SIEVE = flow("sieve", SieveFlow.CODEC);

    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<RayDelivery>> RAY = delivery("ray", RayDelivery.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<ProjectileDelivery>> PROJECTILE = delivery("projectile", ProjectileDelivery.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<SprayDelivery>> SPRAY = delivery("spray", SprayDelivery.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<ChainDelivery>> CHAIN = delivery("chain", ChainDelivery.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<AuraDelivery>> AURA = delivery("aura", AuraDelivery.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<SelfDelivery>> SELF = delivery("self", SelfDelivery.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<CloudDelivery>> CLOUD = delivery("cloud", CloudDelivery.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<MineDelivery>> MINE = delivery("mine", MineDelivery.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<BatDelivery>> SPELLBAT = delivery("spellbat", BatDelivery.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<WallDelivery>> WALL = delivery("wall", WallDelivery.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<SummonDelivery>> SUMMON = delivery("summon", SummonDelivery.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<ArchitectDelivery>> ARCHITECT = delivery("architect", ArchitectDelivery.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<HierophantDelivery>> HIEROPHANT = delivery("hierophant", HierophantDelivery.CODEC);

    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<AspectStrikeEffect>> ASPECT_STRIKE = effect("aspect_strike", AspectStrikeEffect.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<AspectBurstEffect>> ASPECT_BURST = effect("aspect_burst", AspectBurstEffect.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<ImbueEffect>> IMBUE = effect("imbue", ImbueEffect.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<BreakEffect>> BREAK = effect("break", BreakEffect.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<ExchangeEffect>> EXCHANGE = effect("exchange", ExchangeEffect.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<RiftEffect>> RIFT = effect("rift", RiftEffect.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<WardEffect>> WARD = effect("ward", WardEffect.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<PrimalEffect>> PRIMAL = effect("primal", PrimalEffect.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<HellbatEffect>> HELLBAT = effect("hellbat", HellbatEffect.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<BlinkEffect>> BLINK = effect("blink", BlinkEffect.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<HarvestEffect>> HARVEST = effect("harvest", HarvestEffect.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<EldritchRendEffect>> ELDRITCH_REND = effect("eldritch_rend", EldritchRendEffect.CODEC);

    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<StatModifier>> STAT = modifier("stat", StatModifier.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<ScatterModifier>> SCATTER = modifier("scatter", ScatterModifier.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<FanModifier>> FAN = modifier("fan", FanModifier.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<EchoModifier>> ECHO = modifier("echo", EchoModifier.CODEC);

    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<ScriptedEffect>> SCRIPTED_EFFECT = effect("scripted_effect", ScriptedEffect.CODEC);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<ScriptedNode>> SCRIPTED_DELIVERY = scripted("scripted_delivery", SpellPartKind.DELIVERY);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<ScriptedNode>> SCRIPTED_MODIFIER = scripted("scripted_modifier", SpellPartKind.MODIFIER);
    public static final DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<ScriptedNode>> SCRIPTED_FLOW = scripted("scripted_flow", SpellPartKind.FLOW);

    private TTSpellBehaviors() {}

    private static <B extends SpellBehavior> DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<B>> register(String path, SpellPartKind kind, MapCodec<B> codec) {
        return BEHAVIORS.register(path, () -> new SpellBehaviorType<>(kind, codec));
    }

    private static <B extends SpellBehavior> DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<B>> delivery(String path, MapCodec<B> codec) {
        return register(path, SpellPartKind.DELIVERY, codec);
    }

    private static <B extends SpellBehavior> DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<B>> effect(String path, MapCodec<B> codec) {
        return register(path, SpellPartKind.EFFECT, codec);
    }

    private static <B extends SpellBehavior> DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<B>> modifier(String path, MapCodec<B> codec) {
        return register(path, SpellPartKind.MODIFIER, codec);
    }

    private static <B extends SpellBehavior> DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<B>> flow(String path, MapCodec<B> codec) {
        return register(path, SpellPartKind.FLOW, codec);
    }

    private static DeferredHolder<SpellBehaviorType<?>, SpellBehaviorType<ScriptedNode>> scripted(String path, SpellPartKind kind) {
        Supplier<SpellBehaviorType<?>> self = () -> REGISTRY.getValue(TTIds.rl(path));
        return register(path, kind, ScriptedNode.codec(self));
    }

    public static Registry<SpellBehaviorType<?>> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        BEHAVIORS.register(modBus);
    }
}
