package com.leclowndu93150.thaumaturge.content.device.bore;

import com.leclowndu93150.thaumaturge.content.entity.construct.ConstructDeployment;
import com.leclowndu93150.thaumaturge.content.entity.construct.EntityArcaneBore;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;

public final class ArcaneBoreItem extends BlockItem {
    private static final ConstructDeployment<EntityArcaneBore> ON_RAILS =
            new ConstructDeployment<>(TTEntities.ARCANE_BORE, ArcaneBoreItem::faceAwayFromPlacer);

    public ArcaneBoreItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().getBlockState(context.getClickedPos()).is(BlockTags.RAILS)) {
            return ON_RAILS.deploy(context);
        }
        return super.useOn(context);
    }

    private static void faceAwayFromPlacer(EntityArcaneBore bore, Player placer) {
        bore.setFacing(placer.getDirection());
    }
}
