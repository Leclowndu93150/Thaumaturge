package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IAspectQuery;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

public final class BlockEntityTubeFilter extends BlockEntityTube implements IAspectQuery {
    private @Nullable ResourceKey<IAspect> aspectFilter;

    public BlockEntityTubeFilter(BlockPos pos, BlockState state) {
        super(TCBlockEntities.TUBE_FILTER.get(), pos, state);
    }

    public @Nullable ResourceKey<IAspect> aspectFilter() {
        return aspectFilter;
    }

    public void setAspectFilter(@Nullable ResourceKey<IAspect> filter) {
        this.aspectFilter = filter;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected @Nullable ResourceKey<IAspect> suctionFilter() {
        return aspectFilter;
    }

    @Override
    public Holder<IAspect> getSuctionType(Direction face) {
        if (aspectFilter != null) {
            return EssentiaTransportHelper.resolve(level, aspectFilter);
        }
        return super.getSuctionType(face);
    }

    @Override
    public AspectList queryAspects() {
        if (aspectFilter == null || level == null)
            return AspectList.EMPTY;
        return AspectList.EMPTY.add(EssentiaTransportHelper.resolve(level, aspectFilter), 1);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        aspectFilter = input.read("AspectFilter", ASPECT_KEY_CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (aspectFilter != null)
            output.store("AspectFilter", ASPECT_KEY_CODEC, aspectFilter);
    }
}
