package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.api.items.ChargeProfile;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import net.minecraft.core.component.DataComponentType;

public final class RechargeBindings implements RechargeAccess.Bindings {
    @Override
    public DataComponentType<Integer> charge() {
        return TCDataComponents.CHARGE.get();
    }

    @Override
    public DataComponentType<ChargeProfile> profile() {
        return TCDataComponents.RECHARGEABLE.get();
    }
}
