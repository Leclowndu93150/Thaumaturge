package com.leclowndu93150.thaumaturge.network;

import com.leclowndu93150.thaumaturge.content.menu.BlockMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jspecify.annotations.Nullable;

public final class BlockMenuGuard {
    private BlockMenuGuard() {}

    public static <B extends BlockEntity> @Nullable B target(
            IPayloadContext context, BlockPos pos, Class<? extends BlockMenu<B>> menuType) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return null;
        }
        AbstractContainerMenu open = player.containerMenu;
        if (!menuType.isInstance(open) || !open.stillValid(player)) {
            return null;
        }
        B blockEntity = menuType.cast(open).blockEntity();
        return blockEntity != null
                        && !blockEntity.isRemoved()
                        && blockEntity.getBlockPos().equals(pos)
                ? blockEntity
                : null;
    }
}
