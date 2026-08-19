package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealConfigToggles;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public class SealEmptyAdvanced extends SealEmpty implements ISealConfigToggles {
    @Override
    public Identifier getKey() {
        return TCIds.rl("empty_advanced");
    }

    @Override
    public int getFilterSize() {
        return 9;
    }

    @Override
    public Identifier getSealIcon() {
        return TCIds.rl("textures/item/seal_empty_advanced.png");
    }

    @Override
    protected List<ItemStack> getInv(int cycle) {
        if (getToggles()[4].getValue() && !isBlacklist()) {
            List<ItemStack> nonEmpty = new ArrayList<>();
            for (ItemStack stack : getInv()) {
                if (!stack.isEmpty()) {
                    nonEmpty.add(stack);
                }
            }
            if (!nonEmpty.isEmpty()) {
                return List.of(nonEmpty.get(Math.abs(cycle % nonEmpty.size())));
            }
        }
        return getInv();
    }

    @Override
    public ISealConfigToggles.SealToggle[] getToggles() {
        return props;
    }

    @Override
    public int[] getGuiCategories() {
        return new int[]{CAT_FILTER, CAT_TOGGLES, CAT_PRIORITY, CAT_TAGS};
    }

    @Override
    public void setToggle(int index, boolean value) {
        props[index].setValue(value);
    }

    @Override
    public GolemTrait[] getRequiredTags() {
        return new GolemTrait[]{TCGolemTraits.SMART.get()};
    }
}
