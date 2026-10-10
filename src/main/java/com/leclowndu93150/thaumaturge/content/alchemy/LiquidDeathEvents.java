package com.leclowndu93150.thaumaturge.content.alchemy;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.damagesource.TTDamageTypes;
import com.leclowndu93150.thaumaturge.content.aspect.EntityAspects;
import com.leclowndu93150.thaumaturge.content.taint.item.EssentiaCrystalFactory;
import java.util.Collection;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class LiquidDeathEvents {
    private static final int POINTS_PER_EXTRA_CRYSTAL = 10;

    private LiquidDeathEvents() {}

    @SubscribeEvent
    public static void onLivingDrops(LivingDropsEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || !event.getSource().is(TTDamageTypes.DISSOLVE)) {
            return;
        }
        AspectList aspects = EntityAspects.of(victim);
        if (aspects.isEmpty()) {
            return;
        }
        RandomSource random = victim.getRandom();
        int extra = aspects.totalAmount() / POINTS_PER_EXTRA_CRYSTAL;
        int crystals = 1 + random.nextInt(extra + 1);
        Collection<ItemEntity> drops = event.getDrops();
        for (int crystal = 0; crystal < crystals; crystal++) {
            Holder<IAspect> aspect = aspects.entries().get(random.nextInt(aspects.size())).aspect();
            ItemEntity drop = new ItemEntity(level, victim.getX(), victim.getEyeY(), victim.getZ(), EssentiaCrystalFactory.of(aspect));
            drop.setDefaultPickUpDelay();
            drops.add(drop);
        }
    }
}
