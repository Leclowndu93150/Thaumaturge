package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.serialization.TTNbt;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum BlockDetailsDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final ResourceLocation UID = TTIds.rl("block_details");
    public static final String DATA = "ThaumaturgeDetails";

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        BlockEntity entity = JadeBlockProviderHolder.resolve(accessor);
        if (entity == null) return;
        for (JadeBlockHandler<?> handler : JadeBlockProviderHolder.HANDLERS) {
            if (handler.type().isInstance(entity)) {
                TTNbt.store(tag, DATA, JadeBlockDetails.CODEC, handler.read(entity, accessor.getPlayer()));
                return;
            }
        }
    }
}
