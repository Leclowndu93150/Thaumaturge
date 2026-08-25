package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public enum GolemDataProvider implements IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private static final ResourceLocation UID = TCIds.rl("golem");

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof EntityThaumaturgeGolem golem) {
            tag.putInt("RankXp", golem.getRankXp());
        }
    }
}
