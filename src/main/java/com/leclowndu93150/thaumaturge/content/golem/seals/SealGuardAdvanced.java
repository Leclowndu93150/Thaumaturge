package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import net.minecraft.resources.Identifier;

public class SealGuardAdvanced extends SealGuard implements ISealConfigToggles {
    @Override
    public Identifier getKey() {
        return TCIds.rl("guard_advanced");
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_guard_advanced.png");
    }

    @Override
    public ISealConfigToggles.SealToggle[] getToggles() {
        return props;
    }

    @Override
    public void setToggle(int index, boolean value) {
        props[index].setValue(value);
    }

    @Override
    public int[] getGuiCategories() {
        return new int[]{CAT_AREA, CAT_TOGGLES, CAT_PRIORITY, CAT_TAGS};
    }

    @Override
    public GolemTrait[] getRequiredTags() {
        return new GolemTrait[]{TCGolemTraits.FIGHTER.get(), TCGolemTraits.SMART.get()};
    }
}
