package com.leclowndu93150.thaumaturge.content.essentia.tube;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.essentia.IAspectQuery;
import com.leclowndu93150.thaumaturge.api.items.IGogglesDisplayExtended;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaTransportHelper;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.serialization.TCNbt;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEntityTubeFilter extends BlockEntityTube implements IAspectQuery, IGogglesDisplayExtended {
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
        if (aspectFilter == null || level == null) return AspectList.EMPTY;
        return AspectList.EMPTY.add(EssentiaTransportHelper.resolve(level, aspectFilter), 1);
    }

    @Override
    public Component[] getIGogglesText() {
        if (aspectFilter == null || level == null) return new Component[0];
        Holder<IAspect> aspect = EssentiaTransportHelper.resolve(level, aspectFilter);
        return aspect == null ? new Component[0] : new Component[] {AspectComponents.name(aspect)};
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        aspectFilter =
                TCNbt.read(input, "AspectFilter", ASPECT_KEY_CODEC, registries).orElse(null);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        if (aspectFilter != null) TCNbt.store(output, "AspectFilter", ASPECT_KEY_CODEC, registries, aspectFilter);
    }
}
