package com.leclowndu93150.thaumaturge.compat.jade;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IServerDataProvider;

public enum GolemDataProvider implements IServerDataProvider<EntityAccessor> {
    INSTANCE;

    private static final Identifier UID = TTIds.rl("golem");

    @Override
    public Identifier getUid() {
        return UID;
    }

    @Override
    public void appendServerData(CompoundTag tag, EntityAccessor accessor) {
        if (accessor.getEntity() instanceof EntityThaumaturgeGolem golem) {
            tag.putInt("RankXp", golem.getRankXp());
            int rank = golem.properties().rank();
            tag.putInt("RankXpRequired", (rank + 1) * (rank + 1) * EntityThaumaturgeGolem.XP_PER_RANK_UNIT);
        }
    }
}
