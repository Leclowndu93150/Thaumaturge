package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISeal;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealBreaker;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealBreakerAdvanced;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealButcher;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEmpty;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealEmptyAdvanced;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealFill;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealFillAdvanced;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealGuard;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealGuardAdvanced;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealHarvest;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealLumber;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealPickup;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealPickupAdvanced;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealProvide;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealStock;
import com.leclowndu93150.thaumaturge.content.golem.seals.SealUse;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTSeals {
    public static final DeferredRegister<SealType> SEALS = DeferredRegister.create(SealType.REGISTRY_KEY, TTIds.MODID);

    private static final Registry<SealType> REGISTRY = SEALS.makeRegistry(builder -> builder.sync(false));

    public static final DeferredHolder<SealType, SealType> PICKUP =
            seal("pickup", SealPickup::new, () -> TTItems.SEAL_PICKUP);
    public static final DeferredHolder<SealType, SealType> PICKUP_ADVANCED =
            seal("pickup_advanced", SealPickupAdvanced::new, () -> TTItems.SEAL_PICKUP_ADVANCED);
    public static final DeferredHolder<SealType, SealType> FILL = seal("fill", SealFill::new, () -> TTItems.SEAL_FILL);
    public static final DeferredHolder<SealType, SealType> FILL_ADVANCED =
            seal("fill_advanced", SealFillAdvanced::new, () -> TTItems.SEAL_FILL_ADVANCED);
    public static final DeferredHolder<SealType, SealType> EMPTY =
            seal("empty", SealEmpty::new, () -> TTItems.SEAL_EMPTY);
    public static final DeferredHolder<SealType, SealType> EMPTY_ADVANCED =
            seal("empty_advanced", SealEmptyAdvanced::new, () -> TTItems.SEAL_EMPTY_ADVANCED);
    public static final DeferredHolder<SealType, SealType> HARVEST =
            seal("harvest", SealHarvest::new, () -> TTItems.SEAL_HARVEST);
    public static final DeferredHolder<SealType, SealType> BUTCHER =
            seal("butcher", SealButcher::new, () -> TTItems.SEAL_BUTCHER);
    public static final DeferredHolder<SealType, SealType> GUARD =
            seal("guard", SealGuard::new, () -> TTItems.SEAL_GUARD);
    public static final DeferredHolder<SealType, SealType> GUARD_ADVANCED =
            seal("guard_advanced", SealGuardAdvanced::new, () -> TTItems.SEAL_GUARD_ADVANCED);
    public static final DeferredHolder<SealType, SealType> LUMBER =
            seal("lumber", SealLumber::new, () -> TTItems.SEAL_LUMBER);
    public static final DeferredHolder<SealType, SealType> BREAKER =
            seal("breaker", SealBreaker::new, () -> TTItems.SEAL_BREAKER);
    public static final DeferredHolder<SealType, SealType> USE = seal("use", SealUse::new, () -> TTItems.SEAL_USE);
    public static final DeferredHolder<SealType, SealType> PROVIDER =
            seal("provider", SealProvide::new, () -> TTItems.SEAL_PROVIDER);
    public static final DeferredHolder<SealType, SealType> STOCK =
            seal("stock", SealStock::new, () -> TTItems.SEAL_STOCK);
    public static final DeferredHolder<SealType, SealType> BREAKER_ADVANCED =
            seal("breaker_advanced", SealBreakerAdvanced::new, () -> TTItems.SEAL_BREAKER_ADVANCED);

    private TTSeals() {}

    private static DeferredHolder<SealType, SealType> seal(
            String path, Supplier<? extends ISeal> factory, Supplier<Supplier<? extends ItemLike>> item) {
        return SEALS.register(path, () -> new SealType(factory, () -> item.get().get()));
    }

    public static Registry<SealType> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        SEALS.register(modBus);
    }
}
