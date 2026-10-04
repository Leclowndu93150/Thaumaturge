package com.leclowndu93150.thaumaturge.content.entity.champion.trait;

import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCrawler;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class InfestedChampionTrait extends AbstractChampionTrait {
    private static final float PROC_CHANCE = 0.4F;
    private static final float FULL_TURN = 360.0F;
    private static final float GORE_VOLUME = 0.5F;

    @Override
    public float onHurt(LivingEntity mob, @Nullable LivingEntity attacker, DamageSource source, float amount) {
        if (attacker != null
                && mob.getRandom().nextFloat() < PROC_CHANCE
                && mob.level() instanceof ServerLevel server) {
            EntityTaintCrawler crawler = TCEntities.TAINT_CRAWLER.get().create(server);
            if (crawler != null) {
                crawler.moveTo(
                        mob.getX(),
                        mob.getY() + mob.getBbHeight() / 2.0F,
                        mob.getZ(),
                        mob.getRandom().nextFloat() * FULL_TURN,
                        0.0F);
                server.addFreshEntity(crawler);
                mob.playSound(TCSounds.GORE.get(), GORE_VOLUME, 1.0F);
            }
        }
        return amount;
    }
}
