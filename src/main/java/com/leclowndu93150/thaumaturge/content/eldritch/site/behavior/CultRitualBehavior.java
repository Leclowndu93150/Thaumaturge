package com.leclowndu93150.thaumaturge.content.eldritch.site.behavior;

import com.leclowndu93150.thaumaturge.api.labyrinth.ObeliskSiteBehavior;
import com.leclowndu93150.thaumaturge.api.labyrinth.ObeliskSiteBehaviorType;
import com.leclowndu93150.thaumaturge.api.labyrinth.SiteContext;
import com.leclowndu93150.thaumaturge.content.entity.EntityCultistCleric;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTObeliskSiteBehaviors;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;

public record CultRitualBehavior(
        int ritualists,
        SimpleWeightedRandomList<EntityType<?>> guards,
        int maxGuards,
        int reinforcements,
        int activationRange)
        implements ObeliskSiteBehavior {
    private static final int RITUAL_RING_MIN = 2;
    private static final int RITUAL_RING_MAX = 3;
    private static final int GUARD_RING_MIN = 4;
    private static final int GUARD_RING_MAX = 8;
    public static final MapCodec<CultRitualBehavior> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    ExtraCodecs.POSITIVE_INT.fieldOf("ritualists").forGetter(CultRitualBehavior::ritualists),
                    SimpleWeightedRandomList.wrappedCodec(BuiltInRegistries.ENTITY_TYPE.byNameCodec())
                            .fieldOf("guards")
                            .forGetter(CultRitualBehavior::guards),
                    ExtraCodecs.NON_NEGATIVE_INT.fieldOf("max_guards").forGetter(CultRitualBehavior::maxGuards),
                    ExtraCodecs.NON_NEGATIVE_INT
                            .fieldOf("reinforcements")
                            .forGetter(CultRitualBehavior::reinforcements),
                    ExtraCodecs.POSITIVE_INT.fieldOf("activation_range").forGetter(CultRitualBehavior::activationRange))
            .apply(instance, CultRitualBehavior::new));

    @Override
    public ObeliskSiteBehaviorType<?> type() {
        return TTObeliskSiteBehaviors.CULT_RITUAL.get();
    }

    @Override
    public void tick(SiteContext context) {
        if (!context.activated()) {
            if (context.playerWithin(activationRange)) {
                int spawned = 0;
                for (int i = 0; i < ritualists; i++) {
                    Optional<Mob> ritualist =
                            context.spawnMember(TTEntities.CULTIST_CLERIC.get(), RITUAL_RING_MIN, RITUAL_RING_MAX);
                    ritualist.ifPresent(CultRitualBehavior::beginRitual);
                    spawned += ritualist.isPresent() ? 1 : 0;
                }
                if (spawned > 0) {
                    context.activate(reinforcements);
                }
            }
            return;
        }
        List<Mob> members = context.members(activationRange * 2.0);
        boolean ritualAlive = members.stream().anyMatch(CultRitualBehavior::isRitualist);
        if (!ritualAlive) {
            context.quell();
            return;
        }
        long guardsAlive =
                members.stream().filter(member -> !isRitualist(member)).count();
        if (guardsAlive < maxGuards && context.budget() > 0) {
            guards.getRandomValue(context.random())
                    .flatMap(type -> context.spawnMember(type, GUARD_RING_MIN, GUARD_RING_MAX))
                    .ifPresent(guard -> context.consumeBudget());
        }
    }

    private static void beginRitual(Mob mob) {
        if (mob instanceof EntityCultistCleric cleric) {
            cleric.setRitualist(true);
        }
    }

    private static boolean isRitualist(Mob mob) {
        return mob instanceof EntityCultistCleric cleric && cleric.isRitualist();
    }
}
