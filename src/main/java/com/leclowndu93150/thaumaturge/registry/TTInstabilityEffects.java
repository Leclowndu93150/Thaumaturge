package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.infusion.instability.EjectEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.ExplodeEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.HarmEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.InstabilityEffectType;
import com.leclowndu93150.thaumaturge.content.infusion.instability.WarpEffect;
import com.leclowndu93150.thaumaturge.content.infusion.instability.ZapEffect;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTInstabilityEffects {
    public static final DeferredRegister<InstabilityEffectType<?>> EFFECTS =
            DeferredRegister.create(InstabilityEffectType.REGISTRY_KEY, TTIds.MODID);
    private static final Registry<InstabilityEffectType<?>> REGISTRY =
            EFFECTS.makeRegistry(builder -> builder.sync(false));

    public static final DeferredHolder<InstabilityEffectType<?>, InstabilityEffectType<EjectEffect>> EJECT =
            EFFECTS.register("eject", () -> new InstabilityEffectType<>(EjectEffect.CODEC));
    public static final DeferredHolder<InstabilityEffectType<?>, InstabilityEffectType<ZapEffect>> ZAP =
            EFFECTS.register("zap", () -> new InstabilityEffectType<>(ZapEffect.CODEC));
    public static final DeferredHolder<InstabilityEffectType<?>, InstabilityEffectType<HarmEffect>> HARM =
            EFFECTS.register("harm", () -> new InstabilityEffectType<>(HarmEffect.CODEC));
    public static final DeferredHolder<InstabilityEffectType<?>, InstabilityEffectType<WarpEffect>> WARP =
            EFFECTS.register("warp", () -> new InstabilityEffectType<>(WarpEffect.CODEC));
    public static final DeferredHolder<InstabilityEffectType<?>, InstabilityEffectType<ExplodeEffect>> EXPLODE =
            EFFECTS.register("explode", () -> new InstabilityEffectType<>(ExplodeEffect.CODEC));

    private TTInstabilityEffects() {}

    public static Registry<InstabilityEffectType<?>> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        EFFECTS.register(modBus);
    }
}
