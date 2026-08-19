package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCGolemTraits {
    public static final DeferredRegister<GolemTrait> TRAITS = DeferredRegister.create(GolemTrait.REGISTRY_KEY, TCIds.MODID);

    private static final Registry<GolemTrait> REGISTRY = TRAITS.makeRegistry(builder -> builder.sync(true));

    private static final ResourceKey<GolemTrait> DEFT_KEY = key("deft");
    private static final ResourceKey<GolemTrait> CLUMSY_KEY = key("clumsy");
    private static final ResourceKey<GolemTrait> HEAVY_KEY = key("heavy");
    private static final ResourceKey<GolemTrait> LIGHT_KEY = key("light");
    private static final ResourceKey<GolemTrait> FRAGILE_KEY = key("fragile");
    private static final ResourceKey<GolemTrait> ARMORED_KEY = key("armored");

    public static final DeferredHolder<GolemTrait, GolemTrait> SMART = simple("smart");
    public static final DeferredHolder<GolemTrait, GolemTrait> DEFT = opposed("deft", CLUMSY_KEY);
    public static final DeferredHolder<GolemTrait, GolemTrait> CLUMSY = opposed("clumsy", DEFT_KEY);
    public static final DeferredHolder<GolemTrait, GolemTrait> FIGHTER = simple("fighter");
    public static final DeferredHolder<GolemTrait, GolemTrait> WHEELED = simple("wheeled");
    public static final DeferredHolder<GolemTrait, GolemTrait> FLYER = simple("flyer");
    public static final DeferredHolder<GolemTrait, GolemTrait> CLIMBER = simple("climber");
    public static final DeferredHolder<GolemTrait, GolemTrait> HEAVY = opposed("heavy", LIGHT_KEY);
    public static final DeferredHolder<GolemTrait, GolemTrait> LIGHT = opposed("light", HEAVY_KEY);
    public static final DeferredHolder<GolemTrait, GolemTrait> FRAGILE = opposed("fragile", ARMORED_KEY);
    public static final DeferredHolder<GolemTrait, GolemTrait> REPAIR = simple("repair");
    public static final DeferredHolder<GolemTrait, GolemTrait> SCOUT = simple("scout");
    public static final DeferredHolder<GolemTrait, GolemTrait> ARMORED = opposed("armored", FRAGILE_KEY);
    public static final DeferredHolder<GolemTrait, GolemTrait> BRUTAL = simple("brutal");
    public static final DeferredHolder<GolemTrait, GolemTrait> FIREPROOF = simple("fireproof");
    public static final DeferredHolder<GolemTrait, GolemTrait> BREAKER = simple("breaker");
    public static final DeferredHolder<GolemTrait, GolemTrait> HAULER = simple("hauler");
    public static final DeferredHolder<GolemTrait, GolemTrait> RANGED = simple("ranged");
    public static final DeferredHolder<GolemTrait, GolemTrait> BLASTPROOF = simple("blastproof");

    private TCGolemTraits() {}

    private static ResourceKey<GolemTrait> key(String name) {
        return ResourceKey.create(GolemTrait.REGISTRY_KEY, TCIds.rl(name));
    }

    private static DeferredHolder<GolemTrait, GolemTrait> simple(String name) {
        return TRAITS.register(name, id -> GolemTrait.ofId(id, null));
    }

    private static DeferredHolder<GolemTrait, GolemTrait> opposed(String name, ResourceKey<GolemTrait> opposite) {
        return TRAITS.register(name, id -> GolemTrait.ofId(id, opposite));
    }

    public static Registry<GolemTrait> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        TRAITS.register(modBus);
    }
}
