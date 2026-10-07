package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TTIds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.entity.BlockEntity;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IServerDataProvider;

public enum BlockDetailsDataProvider implements IServerDataProvider<BlockAccessor> {
    INSTANCE;

    public static final Identifier UID = TTIds.rl("block_details");
    public static final String DATA = "ThaumaturgeDetails";

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        BlockEntity entity = JadeBlockProviderHolder.resolve(accessor);
        if (entity == null)
            return;
        for (JadeBlockHandler<?> handler : JadeBlockProviderHolder.HANDLERS) {
            if (handler.type().isInstance(entity)) {
                tag.store(DATA, JadeBlockDetails.CODEC, handler.read(entity, accessor.getPlayer()));
                return;
            }
        }
    }
}
