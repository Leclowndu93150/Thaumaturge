package com.leclowndu93150.thaumaturge.content.decor.banner;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.registry.TCBlockEntities;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
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
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String id = input.getStringOr("aspect", "");
        Identifier parsed = id.isEmpty() ? null : Identifier.tryParse(id);
        this.aspect = parsed == null ? null : ResourceKey.create(IAspect.REGISTRY_KEY, parsed);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("aspect", aspect == null ? "" : aspect.identifier().toString());
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag nbt = super.getUpdateTag(registries);
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), Thaumaturge.LOGGER)) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            saveAdditional(output);
            nbt.merge(output.buildResult());
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
    public void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        this.aspect = components.get(TCDataComponents.ASPECT_FILTER.get());
    }

    @Override
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard("aspect");
    }
}
