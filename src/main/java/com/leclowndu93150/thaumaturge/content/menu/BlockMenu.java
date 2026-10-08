package com.leclowndu93150.thaumaturge.content.menu;

import net.minecraft.world.level.block.entity.BlockEntity;
import org.jspecify.annotations.Nullable;

public interface BlockMenu<B extends BlockEntity> {
    @Nullable
    B blockEntity();
}
