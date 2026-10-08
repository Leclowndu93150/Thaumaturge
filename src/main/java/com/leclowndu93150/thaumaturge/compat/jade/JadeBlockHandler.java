package com.leclowndu93150.thaumaturge.compat.jade;

import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;

record JadeBlockHandler<T extends BlockEntity>(
        Class<T> type, ResourceLocation option, BiConsumer<T, JadeDetailBuilder> reader) {
    JadeBlockDetails read(BlockEntity entity, Player player) {
        JadeDetailBuilder builder = new JadeDetailBuilder(option, player);
        reader.accept(type.cast(entity), builder);
        return builder.build();
    }
}
