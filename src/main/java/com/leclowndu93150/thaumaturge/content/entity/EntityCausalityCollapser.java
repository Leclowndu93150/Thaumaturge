package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.content.entity.projectile.AbstractThrownCharge;
import com.leclowndu93150.thaumaturge.content.entity.projectile.ChargeTrail;
import com.leclowndu93150.thaumaturge.content.item.ThrowProfile;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class EntityCausalityCollapser extends AbstractThrownCharge {
    public static final ThrowProfile THROW = new ThrowProfile(0.8F, 2.0F, 0.0F, 0.3F);

    private static final float EXPLOSION_STRENGTH = 2.0F;
    private static final double RIFT_COLLAPSE_RANGE = 3.0;
    private static final ChargeTrail EMBERS = new ChargeTrail(0xF07A1E, 0.3F, 0.55F, 3.0F);

    public EntityCausalityCollapser(EntityType<? extends EntityCausalityCollapser> type, Level level) {
        super(type, level);
    }

    public EntityCausalityCollapser(Level level, LivingEntity shooter, ItemStack stack) {
        super(TTEntities.CAUSALITY_COLLAPSER.get(), shooter, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return TTItems.CAUSALITY_COLLAPSER.get();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        return entity instanceof EntityFluxRift || super.canHitEntity(entity);
    }

    @Override
    protected ChargeTrail trail() {
        return EMBERS;
    }

    @Override
    protected void detonate(ServerLevel level) {
        level.explode(this, getX(), getY(), getZ(), EXPLOSION_STRENGTH, Level.ExplosionInteraction.MOB);
        for (EntityFluxRift rift :
                level.getEntitiesOfClass(EntityFluxRift.class, getBoundingBox().inflate(RIFT_COLLAPSE_RANGE))) {
            rift.setCollapse(true);
        }
    }
}
