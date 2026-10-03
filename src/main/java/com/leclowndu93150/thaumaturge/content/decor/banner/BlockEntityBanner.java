package com.leclowndu93150.thaumaturge.content.decor.banner;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

public final class BlockEntityBanner extends BlockEntity {
    private @Nullable ResourceKey<IAspect> aspect;

    public BlockEntityBanner(BlockPos pos, BlockState state) {
        super(TCBlockEntities.BANNER.get(), pos, state);
    }

    public @Nullable ResourceKey<IAspect> aspect() {
        return aspect;
    }

    public void setAspect(@Nullable ResourceKey<IAspect> aspect) {
        this.aspect = aspect;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag input, HolderLookup.Provider registries) {
        super.loadAdditional(input, registries);
        String id = input.getString("aspect");
        ResourceLocation parsed = id.isEmpty() ? null : ResourceLocation.tryParse(id);
        this.aspect = parsed == null ? null : ResourceKey.create(IAspect.REGISTRY_KEY, parsed);
    }

    @Override
    protected void saveAdditional(CompoundTag output, HolderLookup.Provider registries) {
        super.saveAdditional(output, registries);
        output.putString("aspect", aspect == null ? "" : aspect.location().toString());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        {
            CompoundTag output = new CompoundTag();
            saveAdditional(output, registries);
            nbt.merge(output);
        }
        return nbt;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void collectImplicitComponents(DataComponentMap.Builder builder) {
        super.collectImplicitComponents(builder);
        if (aspect != null) {
            builder.set(TCDataComponents.ASPECT_FILTER.get(), aspect);
        }
    }

    @Override
    public void applyImplicitComponents(DataComponentInput components) {
        super.applyImplicitComponents(components);
        this.aspect = components.get(TCDataComponents.ASPECT_FILTER.get());
    }

    @Override
    public void saveToItem(ItemStack stack, HolderLookup.Provider registries) {
        CompoundTag output = saveCustomOnly(registries);
        output.remove("aspect");
        BlockItem.setBlockEntityData(stack, getType(), output);
        stack.applyComponents(collectComponents());
    }
}
