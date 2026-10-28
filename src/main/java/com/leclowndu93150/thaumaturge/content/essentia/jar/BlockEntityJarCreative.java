package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEntityJarCreative extends AbstractBlockEntityJar {
    public BlockEntityJarCreative(BlockPos pos, BlockState state) {
        super(TTBlockEntities.JAR_CREATIVE.get(), pos, state);
    }

    @Override
    protected int acceptable(ResourceKey<IAspect> key, int requested) {
        return requested > 0 && matchesFilter(key) ? requested : 0;
    }

    @Override
    protected int doAddToContainer(ResourceKey<IAspect> key, int requested) {
        if (requested <= 0) {
            return 0;
        }
        if (!matchesFilter(key)) {
            return requested;
        }
        if (!key.equals(aspectKey()) || amount() < capacity()) {
            replaceContents(key, capacity());
        }
        return 0;
    }

    @Override
    protected boolean doTakeFromContainer(ResourceKey<IAspect> key, int requested) {
        return requested > 0 && amount() > 0 && key.equals(aspectKey());
    }

    @Override
    public int extract(Holder<IAspect> taken, int requested, Direction face, boolean simulate) {
        ResourceKey<IAspect> key = taken.unwrapKey().orElse(null);
        return face == Direction.UP && key != null && doTakeFromContainer(key, requested) ? requested : 0;
    }

    @Override
    public void setStorageContents(@Nullable ResourceKey<IAspect> newAspect, int newAmount) {
        if (newAspect != null) {
            super.setStorageContents(newAspect, capacity());
        }
    }

    @Override
    public int storageInsertLimit(int requested) {
        return requested;
    }

    @Override
    public int spillAmount() {
        return 0;
    }

    private boolean matchesFilter(ResourceKey<IAspect> key) {
        ResourceKey<IAspect> filter = aspectFilterKey();
        return filter == null || filter.equals(key);
    }
}
