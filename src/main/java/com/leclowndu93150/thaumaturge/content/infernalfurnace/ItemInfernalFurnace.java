package com.leclowndu93150.thaumaturge.content.infernalfurnace;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.content.recipe.dust.ItemMultiblockPlacer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public final class ItemInfernalFurnace extends ItemMultiblockPlacer {
    private static final ResourceKey<Blueprint> BLUEPRINT = ResourceKey.create(Blueprint.REGISTRY_KEY, TCIds.rl("infernal_furnace"));

    public ItemInfernalFurnace(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected ResourceKey<Blueprint> blueprint() {
        return BLUEPRINT;
    }
}
