package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import net.minecraft.resources.ResourceLocation;

public class SealBreakerAdvanced extends SealBreaker {
    public SealBreakerAdvanced() {
        props = new ISealConfigToggles.SealToggle[] {
            new ISealConfigToggles.SealToggle(true, "pmeta", "golem.prop.meta"),
            new ISealConfigToggles.SealToggle(false, "psilk", "golem.prop.silk")
        };
    }

    @Override
    public ResourceLocation getKey() {
        return TTIds.rl("breaker_advanced");
    }

    @Override
    public int getFilterSize() {
        return 9;
    }

    @Override
    public ResourceLocation getSealIcon() {
        return TTIds.rl("textures/item/seal_breaker_advanced.png");
    }

    @Override
    public GolemTrait[] getRequiredTags() {
        return new GolemTrait[] {TTGolemTraits.BREAKER.get(), TTGolemTraits.SMART.get()};
    }
}
