package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityJarVoid extends BlockEntityJar {
    public BlockEntityJarVoid(BlockPos pos, BlockState state) {
        super(TCBlockEntities.JAR_VOID.get(), pos, state);
    }

    @Override
    protected int doAddToContainer(ResourceKey<IAspect> incoming, int requested) {
        if (requested == 0) return 0;
        ResourceKey<IAspect> filter = aspectFilterKey();
        if (filter != null && !filter.equals(incoming)) return requested;
        ResourceKey<IAspect> currentAspect = aspectKey();
        int currentAmount = amount();
        if (incoming.equals(currentAspect) || currentAmount == 0) {
            int newTotal = currentAmount + requested;
            int capped = Math.min(newTotal, CAPACITY);
            super.doAddToContainer(incoming, capped - currentAmount);
            int overflow = newTotal - CAPACITY;
            if (overflow > 0 && level != null && !level.isClientSide()) {
                if (level.getRandom().nextInt(250) == 0) {
                    AuraHelper.addFlux(level, getBlockPos(), 1.0F);
                }
            }
            return 0;
        }
        return requested;
    }

    @Override
    public int addToContainer(Holder<IAspect> aspect, int amount) {
        ResourceKey<IAspect> key = aspect.unwrapKey().orElse(null);
        return key == null ? amount : doAddToContainer(key, amount);
    }

    @Override
    protected boolean shouldFillFromAbove() {
        return true;
    }

    @Override
    public int getSuctionAmount(Direction face) {
        return aspectFilterKey() != null && amount() < CAPACITY ? 48 : 32;
    }

    @Override
    public int getMinimumSuction() {
        return aspectFilterKey() != null ? 48 : 32;
    }
}
