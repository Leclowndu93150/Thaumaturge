package com.leclowndu93150.thaumaturge.content.research.book;

import com.leclowndu93150.thaumaturge.content.research.ResearchProgressionEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

public class ThaumonomiconItem extends Item {
    public ThaumonomiconItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ResearchProgressionEvents.bookOpened(player);
        if (level.isClientSide()) {
            ThaumonomiconClientOpener.open();
        }
        return InteractionResult.SUCCESS;
    }
}
