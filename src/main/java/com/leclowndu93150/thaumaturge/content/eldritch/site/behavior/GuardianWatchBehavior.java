package com.leclowndu93150.thaumaturge.content.eldritch.site.behavior;

import com.leclowndu93150.thaumaturge.api.labyrinth.ObeliskSiteBehavior;
import com.leclowndu93150.thaumaturge.api.labyrinth.ObeliskSiteBehaviorType;
import com.leclowndu93150.thaumaturge.api.labyrinth.SiteContext;
import com.leclowndu93150.thaumaturge.registry.TTObeliskSiteBehaviors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.entity.EntityType;

public record GuardianWatchBehavior(
        EntityType<?> guardian, int budget, int maxAlive, int activationRange, int spawnRadius)
        implements ObeliskSiteBehavior {
    private static final int MIN_SPAWN_RADIUS = 3;
    public static final MapCodec<GuardianWatchBehavior> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    BuiltInRegistries.ENTITY_TYPE
                            .byNameCodec()
                            .fieldOf("guardian")
                            .forGetter(GuardianWatchBehavior::guardian),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("budget").forGetter(GuardianWatchBehavior::budget),
                    ExtraCodecs.POSITIVE_INT.optionalFieldOf("max_alive", 1).forGetter(GuardianWatchBehavior::maxAlive),
                    ExtraCodecs.POSITIVE_INT
                            .fieldOf("activation_range")
                            .forGetter(GuardianWatchBehavior::activationRange),
                    ExtraCodecs.POSITIVE_INT
                            .optionalFieldOf("spawn_radius", MIN_SPAWN_RADIUS * 2)
                            .forGetter(GuardianWatchBehavior::spawnRadius))
            .apply(instance, GuardianWatchBehavior::new));

    @Override
    public ObeliskSiteBehaviorType<?> type() {
        return TTObeliskSiteBehaviors.GUARDIAN_WATCH.get();
    }

    @Override
    public void tick(SiteContext context) {
        if (!context.activated()) {
            if (context.playerWithin(activationRange)) {
                context.activate(budget);
            }
            return;
        }
        int alive = context.members(activationRange * 2.0).size();
        if (alive < maxAlive && context.budget() > 0) {
            context.spawnMember(guardian, MIN_SPAWN_RADIUS, spawnRadius).ifPresent(guard -> context.consumeBudget());
        } else if (alive == 0 && context.budget() == 0) {
            context.quell();
        }
    }
}
