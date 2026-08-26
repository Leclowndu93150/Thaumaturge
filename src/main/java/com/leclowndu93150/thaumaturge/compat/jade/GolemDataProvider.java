package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
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
    public boolean shouldRequestData(EntityAccessor accessor) {
        return accessor.showDetails()
                && accessor.getEntity() instanceof EntityThaumaturgeGolem golem
                && golem.getProperties().hasTrait(TCGolemTraits.SMART.get())
                && golem.getProperties().getRank() < EntityThaumaturgeGolem.MAX_RANK;
    }

    @Override
    public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof EntityThaumaturgeGolem golem) {
            tag.putInt("RankXp", golem.getRankXp());
            int rank = golem.getProperties().getRank();
            tag.putInt("RankXpRequired", (rank + 1) * (rank + 1) * EntityThaumaturgeGolem.XP_PER_RANK_UNIT);
        }
    }
}
