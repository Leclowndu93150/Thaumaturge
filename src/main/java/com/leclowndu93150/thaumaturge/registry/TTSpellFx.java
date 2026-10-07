package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.spell.fx.SpellFxStyle;
import com.leclowndu93150.thaumaturge.content.spell.fx.SpellFxStyles;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTSpellFx {
    public static final DeferredRegister<SpellFxStyle> STYLES = DeferredRegister.create(SpellFxStyle.REGISTRY_KEY, TTIds.MODID);

    private static final Registry<SpellFxStyle> REGISTRY = STYLES.makeRegistry(builder -> builder.sync(true));

    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> SPARKLE = STYLES.register("sparkle", () -> SpellFxStyles::sparkle);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> MOTE = STYLES.register("mote", () -> SpellFxStyles::mote);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> FLAME = STYLES.register("flame", () -> SpellFxStyles::flame);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> FROST = STYLES.register("frost", () -> SpellFxStyles::frost);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> GUST = STYLES.register("gust", () -> SpellFxStyles::gust);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> PEBBLE = STYLES.register("pebble", () -> SpellFxStyles::pebble);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> FLUX = STYLES.register("flux", () -> SpellFxStyles::flux);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> HEAL = STYLES.register("heal", () -> SpellFxStyles::heal);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> CURSE = STYLES.register("curse", () -> SpellFxStyles::curse);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> CRACK = STYLES.register("crack", () -> SpellFxStyles::crack);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> RIFT = STYLES.register("rift", () -> SpellFxStyles::rift);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> PRIMAL = STYLES.register("primal", () -> SpellFxStyles::primal);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> WARD = STYLES.register("ward", () -> SpellFxStyles::ward);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> BUBBLE = STYLES.register("bubble", () -> SpellFxStyles::bubble);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> SPARK = STYLES.register("spark", () -> SpellFxStyles::spark);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> LEAF = STYLES.register("leaf", () -> SpellFxStyles::leaf);
    public static final DeferredHolder<SpellFxStyle, SpellFxStyle> SMOKE = STYLES.register("smoke", () -> SpellFxStyles::smoke);

    private TTSpellFx() {}

    public static Registry<SpellFxStyle> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        STYLES.register(modBus);
    }
}
