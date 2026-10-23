package com.leclowndu93150.thaumaturge.content.equipment.shield;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.registry.TTDamageTypeTags;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class ShieldEvents {
    private static final float WARDED_SHIELD_DAMAGE_MULTIPLIER = 2.0F;
    private static final int SAP_TICKS = 100;
    private static final int SAP_AMPLIFIER = 0;

    private ShieldEvents() {}

    @SubscribeEvent
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        LivingEntity blocker = event.getEntity();
        ItemStack shield = ShieldReflection.blockingItem(blocker);
        if (shield.isEmpty()) {
            return;
        }
        DamageSource source = event.getDamageSource();
        if (!event.getOriginalBlock()
                && event.getBlockedDamage() > 0.0F
                && source.is(TTDamageTypeTags.WARDED_BY_SHIELDS)
                && shield.is(TTItemTags.SHIELDS_WARDING)) {
            event.setBlocked(true);
            event.setShieldDamage(event.getBlockedDamage() * WARDED_SHIELD_DAMAGE_MULTIPLIER);
        }
        if (event.getBlocked()
                && shield.is(TTItemTags.SHIELDS_SAPPING)
                && !source.is(DamageTypeTags.IS_PROJECTILE)
                && source.getDirectEntity() instanceof LivingEntity attacker) {
            attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, SAP_TICKS, SAP_AMPLIFIER), blocker);
        }
    }

    @SubscribeEvent
    public static void onProjectileImpact(ProjectileImpactEvent event) {
        if (!(event.getRayTraceResult() instanceof EntityHitResult hit)
                || !(hit.getEntity() instanceof LivingEntity blocker)) {
            return;
        }
        ItemStack shield = ShieldReflection.blockingItem(blocker);
        Projectile projectile = event.getProjectile();
        if (!shield.is(TTItemTags.SHIELDS_REFLECTING) || !ShieldReflection.facing(blocker, projectile.position())) {
            return;
        }
        event.setCanceled(true);
        ShieldReflection.reflect(projectile, blocker, shield);
    }
}
