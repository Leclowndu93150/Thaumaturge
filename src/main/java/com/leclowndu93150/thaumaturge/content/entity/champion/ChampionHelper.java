package com.leclowndu93150.thaumaturge.content.entity.champion;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Creeper;

public final class ChampionHelper {
    private ChampionHelper() {}

    public static List<Holder<MobTrait>> championTraits() {
        List<Holder<MobTrait>> traits = new ArrayList<>();
        TTMobTraits.registry()
                .holders()
                .filter(trait -> trait.value().isChampion())
                .forEach(traits::add);
        return traits;
    }

    public static boolean rolled(LivingEntity mob) {
        return Boolean.TRUE.equals(mob.getExistingDataOrNull(TTAttachments.CHAMPION_ROLLED));
    }

    public static void markRolled(LivingEntity mob) {
        mob.setData(TTAttachments.CHAMPION_ROLLED, true);
    }

    public static void makeChampion(Mob mob, boolean persist) {
        List<Holder<MobTrait>> traits = championTraits();
        if (!traits.isEmpty()) {
            makeChampion(
                    mob,
                    persist,
                    mob instanceof Creeper
                            ? TTMobTraits.BOLD
                            : traits.get(mob.getRandom().nextInt(traits.size())));
        }
    }

    public static void makeChampion(Mob mob, boolean persist, Holder<MobTrait> trait) {
        if (rolled(mob)) {
            return;
        }
        markRolled(mob);
        MobTraits.add(mob, trait);
        if (persist) {
            mob.setPersistenceRequired();
        }
    }
}
