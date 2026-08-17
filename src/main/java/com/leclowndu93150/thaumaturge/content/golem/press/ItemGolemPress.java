package com.leclowndu93150.thaumaturge.content.golem.press;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.content.recipe.dust.ItemMultiblockPlacer;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;

public final class ItemGolemPress extends ItemMultiblockPlacer {
    private static final ResourceKey<Blueprint> BLUEPRINT =
            ResourceKey.create(Blueprint.REGISTRY_KEY, TCIds.rl("golem_press"));

    public ItemGolemPress(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    protected ResourceKey<Blueprint> blueprint() {
        return BLUEPRINT;
    }
}
