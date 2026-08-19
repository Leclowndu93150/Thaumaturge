package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import net.minecraft.resources.Identifier;

public class SealPickupAdvanced extends SealPickup implements ISealConfigToggles {
    @Override
    public Identifier getKey() {
        return TCIds.rl("pickup_advanced");
    }

    @Override
    public int getFilterSize() {
        return 9;
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_pickup_advanced.png");
    }

    @Override
    public int[] getGuiCategories() {
        return new int[]{CAT_AREA, CAT_FILTER, CAT_TOGGLES, CAT_PRIORITY, CAT_TAGS};
    }

    @Override
    public GolemTrait[] getRequiredTags() {
        return new GolemTrait[]{TCGolemTraits.SMART.get()};
    }

    @Override
    public ISealConfigToggles.SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int index, boolean value) {
        props[index].setValue(value);
    }
}
