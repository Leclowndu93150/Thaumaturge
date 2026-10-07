package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.ArmoredChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.EffectChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.FieryChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.InfestedChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.ModifierChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.SpinedChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.UndyingChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.VampiricChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.WardedChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.champion.trait.WarpChampionTrait;
import com.leclowndu93150.thaumaturge.content.entity.trait.LeapingTrait;
import com.leclowndu93150.thaumaturge.content.entity.trait.MarkerTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintBroodTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintGrazingTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintedTrait;
import net.minecraft.core.Registry;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTMobTraits {
    private static final double BOLD_SPEED = 0.3;
    private static final double MIGHTY_DAMAGE = 2.0;
    private static final int GRIM_WITHER_TICKS = 200;
    private static final int SICKLY_HUNGER_TICKS = 500;
    private static final int VENOMOUS_POISON_TICKS = 100;

    public static final DeferredRegister<MobTrait> TRAITS = DeferredRegister.create(MobTrait.REGISTRY_KEY, TTIds.MODID);

    private static final Registry<MobTrait> REGISTRY = TRAITS.makeRegistry(builder -> builder.sync(true));

    public static final DeferredHolder<MobTrait, ModifierChampionTrait> BOLD = TRAITS.register(
            "bold",
            () -> new ModifierChampionTrait(
                    Attributes.MOVEMENT_SPEED, BOLD_SPEED, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
    public static final DeferredHolder<MobTrait, SpinedChampionTrait> SPINED =
            TRAITS.register("spine", SpinedChampionTrait::new);
    public static final DeferredHolder<MobTrait, ArmoredChampionTrait> ARMORED =
            TRAITS.register("armor", ArmoredChampionTrait::new);
    public static final DeferredHolder<MobTrait, ModifierChampionTrait> MIGHTY = TRAITS.register(
            "mighty",
            () -> new ModifierChampionTrait(
                    Attributes.ATTACK_DAMAGE, MIGHTY_DAMAGE, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    public static final DeferredHolder<MobTrait, EffectChampionTrait> GRIM =
            TRAITS.register("grim", () -> new EffectChampionTrait(MobEffects.WITHER, GRIM_WITHER_TICKS));
    public static final DeferredHolder<MobTrait, WardedChampionTrait> WARDED =
            TRAITS.register("warded", WardedChampionTrait::new);
    public static final DeferredHolder<MobTrait, WarpChampionTrait> WARP =
            TRAITS.register("warp", WarpChampionTrait::new);
    public static final DeferredHolder<MobTrait, UndyingChampionTrait> UNDYING =
            TRAITS.register("undying", UndyingChampionTrait::new);
    public static final DeferredHolder<MobTrait, FieryChampionTrait> FIERY =
            TRAITS.register("fiery", FieryChampionTrait::new);
    public static final DeferredHolder<MobTrait, EffectChampionTrait> SICKLY =
            TRAITS.register("sickly", () -> new EffectChampionTrait(MobEffects.HUNGER, SICKLY_HUNGER_TICKS));
    public static final DeferredHolder<MobTrait, EffectChampionTrait> VENOMOUS =
            TRAITS.register("venomous", () -> new EffectChampionTrait(MobEffects.POISON, VENOMOUS_POISON_TICKS));
    public static final DeferredHolder<MobTrait, VampiricChampionTrait> VAMPIRIC =
            TRAITS.register("vampiric", VampiricChampionTrait::new);
    public static final DeferredHolder<MobTrait, InfestedChampionTrait> INFESTED =
            TRAITS.register("infested", InfestedChampionTrait::new);

    public static final DeferredHolder<MobTrait, TaintedTrait> TAINTED = TRAITS.register("tainted", TaintedTrait::new);
    public static final DeferredHolder<MobTrait, TaintGrazingTrait> TAINT_GRAZING =
            TRAITS.register("taint_grazing", TaintGrazingTrait::new);
    public static final DeferredHolder<MobTrait, MarkerTrait> TAINT_BLAST =
            TRAITS.register("taint_blast", MarkerTrait::new);
    public static final DeferredHolder<MobTrait, TaintBroodTrait> TAINT_BROOD =
            TRAITS.register("taint_brood", TaintBroodTrait::new);
    public static final DeferredHolder<MobTrait, LeapingTrait> LEAPING = TRAITS.register("leaping", LeapingTrait::new);

    private TTMobTraits() {}

    public static Registry<MobTrait> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        TRAITS.register(modBus);
    }
}
