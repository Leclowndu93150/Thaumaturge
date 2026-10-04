package com.leclowndu93150.thaumaturge.content.infusion.grindstone;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockArcaneGrindstone extends GrindstoneBlock {
    private static final Component TITLE = Component.translatable("gui.thaumaturge.arcane_grindstone.title");

    public BlockArcaneGrindstone(Properties properties) {
        super(properties);
    }

    @Override
    protected MenuProvider getMenuProvider(BlockState state, Level level, BlockPos pos) {
        return new SimpleMenuProvider((containerId, inventory, player) -> new MenuArcaneGrindstone(containerId, inventory, ContainerLevelAccess.create(level, pos)), TITLE);
    }
}
