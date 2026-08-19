package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
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

public final class TCSeals {
    public static final DeferredRegister<SealType> SEALS = DeferredRegister.create(SealType.REGISTRY_KEY, TCIds.MODID);

    private static final Registry<SealType> REGISTRY = SEALS.makeRegistry(builder -> builder.sync(false));

    public static final DeferredHolder<SealType, SealType> PICKUP = seal("pickup", SealPickup::new, () -> TCItems.SEAL_PICKUP);
    public static final DeferredHolder<SealType, SealType> PICKUP_ADVANCED = seal("pickup_advanced", SealPickupAdvanced::new, () -> TCItems.SEAL_PICKUP_ADVANCED);
    public static final DeferredHolder<SealType, SealType> FILL = seal("fill", SealFill::new, () -> TCItems.SEAL_FILL);
    public static final DeferredHolder<SealType, SealType> FILL_ADVANCED = seal("fill_advanced", SealFillAdvanced::new, () -> TCItems.SEAL_FILL_ADVANCED);
    public static final DeferredHolder<SealType, SealType> EMPTY = seal("empty", SealEmpty::new, () -> TCItems.SEAL_EMPTY);
    public static final DeferredHolder<SealType, SealType> EMPTY_ADVANCED = seal("empty_advanced", SealEmptyAdvanced::new, () -> TCItems.SEAL_EMPTY_ADVANCED);
    public static final DeferredHolder<SealType, SealType> HARVEST = seal("harvest", SealHarvest::new, () -> TCItems.SEAL_HARVEST);
    public static final DeferredHolder<SealType, SealType> BUTCHER = seal("butcher", SealButcher::new, () -> TCItems.SEAL_BUTCHER);
    public static final DeferredHolder<SealType, SealType> GUARD = seal("guard", SealGuard::new, () -> TCItems.SEAL_GUARD);
    public static final DeferredHolder<SealType, SealType> GUARD_ADVANCED = seal("guard_advanced", SealGuardAdvanced::new, () -> TCItems.SEAL_GUARD_ADVANCED);
    public static final DeferredHolder<SealType, SealType> LUMBER = seal("lumber", SealLumber::new, () -> TCItems.SEAL_LUMBER);
    public static final DeferredHolder<SealType, SealType> BREAKER = seal("breaker", SealBreaker::new, () -> TCItems.SEAL_BREAKER);
    public static final DeferredHolder<SealType, SealType> USE = seal("use", SealUse::new, () -> TCItems.SEAL_USE);
    public static final DeferredHolder<SealType, SealType> PROVIDER = seal("provider", SealProvide::new, () -> TCItems.SEAL_PROVIDER);
    public static final DeferredHolder<SealType, SealType> STOCK = seal("stock", SealStock::new, () -> TCItems.SEAL_STOCK);
    public static final DeferredHolder<SealType, SealType> BREAKER_ADVANCED = seal("breaker_advanced", SealBreakerAdvanced::new, () -> TCItems.SEAL_BREAKER_ADVANCED);

    private TCSeals() {}

    private static DeferredHolder<SealType, SealType> seal(String path, Supplier<? extends ISeal> factory, Supplier<Supplier<? extends ItemLike>> item) {
        return SEALS.register(path, () -> new SealType(factory, () -> item.get().get()));
    }

    public static Registry<SealType> registry() {
        return REGISTRY;
    }

    public static void register(IEventBus modBus) {
        SEALS.register(modBus);
    }
}
