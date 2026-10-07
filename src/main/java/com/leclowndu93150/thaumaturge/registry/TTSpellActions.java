package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellAction;
import com.leclowndu93150.thaumaturge.api.spell.affinity.SpellActionType;
import com.leclowndu93150.thaumaturge.content.spell.action.DamageAction;
import com.leclowndu93150.thaumaturge.content.spell.action.DispelAction;
import com.leclowndu93150.thaumaturge.content.spell.action.ErodeAction;
import com.leclowndu93150.thaumaturge.content.spell.action.ExplodeAction;
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
import com.mojang.serialization.MapCodec;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTSpellActions {
    public static final DeferredRegister<SpellActionType<?>> ACTIONS = DeferredRegister.create(SpellActionType.REGISTRY_KEY, TTIds.MODID);

    private static final Registry<SpellActionType<?>> REGISTRY = ACTIONS.makeRegistry(builder -> builder.sync(false));

    public static final DeferredHolder<SpellActionType<?>, SpellActionType<DamageAction>> DAMAGE = register("damage", DamageAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<IgniteAction>> IGNITE = register("ignite", IgniteAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<FreezeAction>> FREEZE = register("freeze", FreezeAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<MobEffectAction>> MOB_EFFECT = register("mob_effect", MobEffectAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<KnockbackAction>> KNOCKBACK = register("knockback", KnockbackAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<HealAction>> HEAL = register("heal", HealAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ExtinguishAction>> EXTINGUISH = register("extinguish", ExtinguishAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<TeleportAction>> TELEPORT = register("teleport", TeleportAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<WindBurstAction>> WIND_BURST = register("wind_burst", WindBurstAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ExplodeAction>> EXPLODE = register("explode", ExplodeAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PlaceBlockAction>> PLACE_BLOCK = register("place_block", PlaceBlockAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<FreezeWaterAction>> FREEZE_WATER = register("freeze_water", FreezeWaterAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<GrowAction>> GROW = register("grow", GrowAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<ErodeAction>> ERODE = register("erode", ErodeAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<SapAction>> SAP = register("sap", SapAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<PolluteAction>> POLLUTE = register("pollute", PolluteAction.CODEC);
    public static final DeferredHolder<SpellActionType<?>, SpellActionType<DispelAction>> DISPEL = register("dispel", DispelAction.CODEC);

    private TTSpellActions() {}

    private static <A extends SpellAction> DeferredHolder<SpellActionType<?>, SpellActionType<A>> register(String path, MapCodec<A> codec) {
        return ACTIONS.register(path, () -> new SpellActionType<>(codec));
    }

    public static Registry<SpellActionType<?>> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        ACTIONS.register(modBus);
    }
}
