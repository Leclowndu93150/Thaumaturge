package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.casters.FocusElement;
import com.leclowndu93150.thaumaturge.api.casters.FocusElementType;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectAir;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectBreak;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectCurse;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectEarth;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectExchange;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectFire;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectFlux;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectFrost;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectHeal;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectHellbat;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectPrimal;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectRift;
import com.leclowndu93150.thaumaturge.content.focus.effect.FocusEffectWard;
import com.leclowndu93150.thaumaturge.content.focus.medium.FocusMediumBolt;
import com.leclowndu93150.thaumaturge.content.focus.medium.FocusMediumCloud;
import com.leclowndu93150.thaumaturge.content.focus.medium.FocusMediumMine;
import com.leclowndu93150.thaumaturge.content.focus.medium.FocusMediumPlan;
import com.leclowndu93150.thaumaturge.content.focus.medium.FocusMediumProjectile;
import com.leclowndu93150.thaumaturge.content.focus.medium.FocusMediumRoot;
import com.leclowndu93150.thaumaturge.content.focus.medium.FocusMediumSpellBat;
import com.leclowndu93150.thaumaturge.content.focus.medium.FocusMediumTouch;
import com.leclowndu93150.thaumaturge.content.focus.mod.FocusModScatter;
import com.leclowndu93150.thaumaturge.content.focus.mod.FocusModSplitTarget;
import com.leclowndu93150.thaumaturge.content.focus.mod.FocusModSplitTrajectory;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCFocusElements {
    public static final DeferredRegister<FocusElementType> ELEMENTS =
            DeferredRegister.create(FocusElementType.REGISTRY_KEY, TCIds.MODID);

    private static final Registry<FocusElementType> REGISTRY = ELEMENTS.makeRegistry(builder -> builder.sync(false));

    public static final DeferredHolder<FocusElementType, FocusElementType> ROOT =
            element("root", new FocusMediumRoot(), 10066329);

    public static final DeferredHolder<FocusElementType, FocusElementType> TOUCH =
            element("touch", new FocusMediumTouch(), 11371909);
    public static final DeferredHolder<FocusElementType, FocusElementType> BOLT =
            element("bolt", new FocusMediumBolt(), 11377029);
    public static final DeferredHolder<FocusElementType, FocusElementType> PROJECTILE =
            element("projectile", new FocusMediumProjectile(), 11382149);
    public static final DeferredHolder<FocusElementType, FocusElementType> CLOUD =
            element("cloud", new FocusMediumCloud(), 10071429);
    public static final DeferredHolder<FocusElementType, FocusElementType> MINE =
            element("mine", new FocusMediumMine(), 8760709);
    public static final DeferredHolder<FocusElementType, FocusElementType> PLAN =
            element("plan", new FocusMediumPlan(), 8760728);
    public static final DeferredHolder<FocusElementType, FocusElementType> SPELLBAT =
            element("spellbat", new FocusMediumSpellBat(), 8760748);

    public static final DeferredHolder<FocusElementType, FocusElementType> HELLBAT =
            element("hellbat", new FocusEffectHellbat(), 14431746);

    public static final DeferredHolder<FocusElementType, FocusElementType> PRIMAL =
            element("primal", new FocusEffectPrimal(), 10854849);

    public static final DeferredHolder<FocusElementType, FocusElementType> FIRE =
            element("fire", new FocusEffectFire(), 16734721);
    public static final DeferredHolder<FocusElementType, FocusElementType> FROST =
            element("frost", new FocusEffectFrost(), 14811135);
    public static final DeferredHolder<FocusElementType, FocusElementType> AIR =
            element("air", new FocusEffectAir(), 16777086);
    public static final DeferredHolder<FocusElementType, FocusElementType> EARTH =
            element("earth", new FocusEffectEarth(), 5685248);
    public static final DeferredHolder<FocusElementType, FocusElementType> FLUX =
            element("flux", new FocusEffectFlux(), 8388736);
    public static final DeferredHolder<FocusElementType, FocusElementType> BREAK =
            element("break", new FocusEffectBreak(), 9063176);
    public static final DeferredHolder<FocusElementType, FocusElementType> RIFT =
            element("rift", new FocusEffectRift(), 3084645);
    public static final DeferredHolder<FocusElementType, FocusElementType> EXCHANGE =
            element("exchange", new FocusEffectExchange(), 5735255);
    public static final DeferredHolder<FocusElementType, FocusElementType> CURSE =
            element("curse", new FocusEffectCurse(), 6946821);
    public static final DeferredHolder<FocusElementType, FocusElementType> HEAL =
            element("heal", new FocusEffectHeal(), 14548997);
    public static final DeferredHolder<FocusElementType, FocusElementType> WARD =
            element("ward", new FocusEffectWard(), 16771535);

    public static final DeferredHolder<FocusElementType, FocusElementType> SCATTER =
            element("scatter", new FocusModScatter(), 10066329);
    public static final DeferredHolder<FocusElementType, FocusElementType> SPLIT_TARGET =
            element("split_target", new FocusModSplitTarget(), 10066329);
    public static final DeferredHolder<FocusElementType, FocusElementType> SPLIT_TRAJECTORY =
            element("split_trajectory", new FocusModSplitTrajectory(), 10066329);

    private TCFocusElements() {}

    private static DeferredHolder<FocusElementType, FocusElementType> element(
            String path, FocusElement element, int color) {
        return ELEMENTS.register(
                path, () -> new FocusElementType(element, TCIds.rl("textures/foci/" + path + ".png"), color));
    }

    public static Registry<FocusElementType> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        ELEMENTS.register(modBus);
    }
}
