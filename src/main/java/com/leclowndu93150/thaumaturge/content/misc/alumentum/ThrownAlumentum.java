package com.leclowndu93150.thaumaturge.content.misc.alumentum;

import com.leclowndu93150.thaumaturge.content.entity.projectile.AbstractThrownCharge;
import com.leclowndu93150.thaumaturge.content.entity.projectile.ChargeTrail;
import com.leclowndu93150.thaumaturge.content.item.ThrowProfile;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ThrownAlumentum extends AbstractThrownCharge {
    public static final ThrowProfile THROW = new ThrowProfile(0.75F, 2.0F, 0.0F, 0.5F);

    private static final float BLAST_POWER = 1.1F;
    private static final ChargeTrail SMOULDER = new ChargeTrail(0x3A2A24, 0.6F, 0.45F, 3.5F);

    public ThrownAlumentum(EntityType<? extends ThrownAlumentum> type, Level level) {
        super(type, level);
    }

    public ThrownAlumentum(Level level, LivingEntity thrower, ItemStack stack) {
        super(TTEntities.ALUMENTUM.get(), thrower, level, stack);
    }

    @Override
    protected Item getDefaultItem() {
        return TTItems.ALUMENTUM.get();
    }

    @Override
    protected ChargeTrail trail() {
        return SMOULDER;
    }

    @Override
    protected void detonate(ServerLevel level) {
        level.explode(this, getX(), getY(), getZ(), BLAST_POWER, Level.ExplosionInteraction.TNT);
    }
}
