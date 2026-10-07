package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import net.minecraft.resources.ResourceLocation;

public class SealFillAdvanced extends SealFill implements ISealConfigToggles {
    @Override
    public ResourceLocation getKey() {
        return TTIds.rl("fill_advanced");
    }

    @Override
    public int getFilterSize() {
        return 9;
    }

    @Override
    public ResourceLocation getSealIcon() {
        return TTIds.rl("textures/item/seal_fill_advanced.png");
    }

    @Override
    public int[] getGuiCategories() {
        return new int[] {CAT_FILTER, CAT_TOGGLES, CAT_PRIORITY, CAT_TAGS};
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
    public GolemTrait[] getRequiredTags() {
        return new GolemTrait[] {TTGolemTraits.SMART.get()};
    }
}
