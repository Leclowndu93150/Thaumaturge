package com.leclowndu93150.thaumaturge.content.essentia.jar;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.registry.TTBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

public final class BlockEntityJarVoid extends AbstractBlockEntityJar {
    private static final int LEAK_ODDS = 250;
    private static final float LEAK_FLUX = 1.0F;
    private static final int FILTERED_SUCTION = 48;
    private static final int UNFILTERED_SUCTION = 32;
    private static final int FILTERED_MINIMUM_SUCTION = 48;
    private static final int UNFILTERED_MINIMUM_SUCTION = 32;

    public BlockEntityJarVoid(BlockPos pos, BlockState state) {
        super(TTBlockEntities.JAR_VOID.get(), pos, state);
    }

    @Override
    protected int doAddToContainer(ResourceKey<IAspect> key, int requested) {
        if (requested <= 0) {
            return 0;
        }
        if (refuses(key)) {
            return requested;
        }
        if (super.doAddToContainer(key, requested) > 0) {
            rollLeak();
        }
        return 0;
    }

    @Override
    public int storageInsertLimit(int requested) {
        return requested;
    }

    @Override
    public void onStorageVoided(int voided) {
        rollLeak();
    }

    @Override
    public int fill(Holder<IAspect> aspect, int amount) {
        ResourceKey<IAspect> key = aspect.unwrapKey().orElse(null);
        return key == null ? amount : doAddToContainer(key, amount);
    }

    @Override
    protected boolean shouldFillFromAbove() {
        return true;
    }

    @Override
    protected int acceptable(ResourceKey<IAspect> key, int requested) {
        return requested <= 0 || refuses(key) ? 0 : requested;
    }

    @Override
    protected int pullStrength() {
        return aspectFilterKey() != null && amount() < CAPACITY ? FILTERED_SUCTION : UNFILTERED_SUCTION;
    }

    @Override
    public int pullThreshold() {
        return aspectFilterKey() != null ? FILTERED_MINIMUM_SUCTION : UNFILTERED_MINIMUM_SUCTION;
    }

    private void rollLeak() {
        if (level instanceof ServerLevel server && server.getRandom().nextInt(LEAK_ODDS) == 0) {
            AuraHelper.addFlux(server, worldPosition, LEAK_FLUX);
        }
    }
}
