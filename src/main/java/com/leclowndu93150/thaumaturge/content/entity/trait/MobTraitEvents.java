package com.leclowndu93150.thaumaturge.content.entity.trait;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class MobTraitEvents {
    private MobTraitEvents() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()
                && event.getEntity() instanceof LivingEntity mob
                && !MobTraitEngine.traits(mob).isEmpty()) {
            MobTraitEngine.refresh(mob);
        }
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof LivingEntity mob)
                || !mob.isAlive()) {
            return;
        }
        for (Holder<MobTrait> trait : MobTraitEngine.traits(mob)) {
            trait.value().tick(mob);
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        LivingEntity attacker = event.getSource().getEntity() instanceof LivingEntity living ? living : null;
        for (Holder<MobTrait> trait : MobTraitEngine.traits(victim)) {
            event.setAmount(trait.value().onHurt(victim, attacker, event.getSource(), event.getAmount()));
        }
        if (attacker == null || event.getAmount() <= 0.0F) {
            return;
        }
        List<Holder<MobTrait>> attackerTraits = MobTraitEngine.traits(attacker);
        for (Holder<MobTrait> trait : attackerTraits) {
            event.setAmount(trait.value().onAttack(attacker, victim, event.getSource(), event.getAmount()));
        }
    }
}
