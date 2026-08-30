package com.leclowndu93150.thaumaturge.content.device.bore;

import com.leclowndu93150.thaumaturge.content.entity.construct.TurretPlacerItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

public final class ArcaneBoreItem extends BlockItem {
    private final TurretPlacerItem.ConstructFactory factory;

    public ArcaneBoreItem(Block block, Properties properties, TurretPlacerItem.ConstructFactory factory) {
        super(block, properties);
        this.factory = factory;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos()).is(BlockTags.RAILS)) {
            return TurretPlacerItem.placeConstruct(context, factory);
        }
        return super.useOn(context);
    }
}
