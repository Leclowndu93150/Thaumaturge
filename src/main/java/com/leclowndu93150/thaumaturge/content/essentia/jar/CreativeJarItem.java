package com.leclowndu93150.thaumaturge.content.essentia.jar;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public final class CreativeJarItem extends JarItem {
    public CreativeJarItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
