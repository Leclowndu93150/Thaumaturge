package com.leclowndu93150.thaumaturge.content.eldritch.site.behavior;

import com.leclowndu93150.thaumaturge.api.labyrinth.ObeliskSiteBehavior;
import com.leclowndu93150.thaumaturge.api.labyrinth.ObeliskSiteBehaviorType;
import com.leclowndu93150.thaumaturge.api.labyrinth.SiteContext;
import com.leclowndu93150.thaumaturge.registry.TTObeliskSiteBehaviors;
import com.mojang.serialization.MapCodec;

public record DormantBehavior() implements ObeliskSiteBehavior {
    public static final DormantBehavior INSTANCE = new DormantBehavior();
    public static final MapCodec<DormantBehavior> CODEC = MapCodec.unit(INSTANCE);

    @Override
    public ObeliskSiteBehaviorType<?> type() {
        return TTObeliskSiteBehaviors.DORMANT.get();
    }

    @Override
    public void tick(SiteContext context) {
        context.quell();
    }
}
