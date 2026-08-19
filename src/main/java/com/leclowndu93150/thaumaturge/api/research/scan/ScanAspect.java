package com.leclowndu93150.thaumaturge.api.research.scan;

import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeType;
import com.leclowndu93150.thaumaturge.api.research.TCResearchCategories;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Scannable subject matching anything whose aspect composition contains a given aspect. Used to
 * let players discover each aspect by scanning something that carries it. A successful scan also
 * awards a point of observation knowledge to the basics, auromancy, and alchemy categories.
 *
 * @since 1.0.0
 */
public class ScanAspect implements IScanThing {
    private static final int DISCOVERY_OBSERVATION_POINTS = 1;

    private final Identifier research;
    private final Holder<IAspect> aspect;

    /**
     * Creates the subject.
     *
     * @param research the research key granted on scan
     * @param aspect the aspect the target must carry
     */
    public ScanAspect(Identifier research, Holder<IAspect> aspect) {
        this.research = research;
        this.aspect = aspect;
    }

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        if (target == null) {
            return false;
        }
        AspectList aspects;
        if (target instanceof Entity entity && !(target instanceof ItemEntity)) {
            aspects = ScanningManager.entityAspects(entity);
        } else {
            ItemStack stack = ScanningManager.getItemFromParms(player, target);
            if (stack.isEmpty()) {
                return false;
            }
            aspects = ScanningManager.itemAspects(stack);
        }
        return aspects.amountOf(aspect) > 0;
    }

    @Override
    public void onSuccess(Player player, @Nullable Object target) {
        ScanningManager.addKnowledge(player, KnowledgeType.OBSERVATION, TCResearchCategories.AUROMANCY.identifier(), DISCOVERY_OBSERVATION_POINTS);
        ScanningManager.addKnowledge(player, KnowledgeType.OBSERVATION, TCResearchCategories.BASICS.identifier(), DISCOVERY_OBSERVATION_POINTS);
        ScanningManager.addKnowledge(player, KnowledgeType.OBSERVATION, TCResearchCategories.ALCHEMY.identifier(), DISCOVERY_OBSERVATION_POINTS);
    }

    @Override
    public Identifier getResearchKey(Player player, @Nullable Object target) {
        return research;
    }
}
