package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.labyrinth.ObeliskSiteBehaviorType;
import com.leclowndu93150.thaumaturge.content.eldritch.site.behavior.CultRitualBehavior;
import com.leclowndu93150.thaumaturge.content.eldritch.site.behavior.DormantBehavior;
import com.leclowndu93150.thaumaturge.content.eldritch.site.behavior.GuardianWatchBehavior;
import net.minecraft.core.Registry;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTObeliskSiteBehaviors {
    public static final DeferredRegister<ObeliskSiteBehaviorType<?>> BEHAVIORS =
            DeferredRegister.create(ObeliskSiteBehaviorType.REGISTRY_KEY, TTIds.MODID);
    private static final Registry<ObeliskSiteBehaviorType<?>> REGISTRY =
            BEHAVIORS.makeRegistry(builder -> builder.sync(false));

    public static final DeferredHolder<ObeliskSiteBehaviorType<?>, ObeliskSiteBehaviorType<CultRitualBehavior>>
            CULT_RITUAL =
                    BEHAVIORS.register("cult_ritual", () -> new ObeliskSiteBehaviorType<>(CultRitualBehavior.CODEC));
    public static final DeferredHolder<ObeliskSiteBehaviorType<?>, ObeliskSiteBehaviorType<GuardianWatchBehavior>>
            GUARDIAN_WATCH = BEHAVIORS.register(
                    "guardian_watch", () -> new ObeliskSiteBehaviorType<>(GuardianWatchBehavior.CODEC));
    public static final DeferredHolder<ObeliskSiteBehaviorType<?>, ObeliskSiteBehaviorType<DormantBehavior>> DORMANT =
            BEHAVIORS.register("dormant", () -> new ObeliskSiteBehaviorType<>(DormantBehavior.CODEC));

    private TTObeliskSiteBehaviors() {}

    public static Registry<ObeliskSiteBehaviorType<?>> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        BEHAVIORS.register(modBus);
    }
}
