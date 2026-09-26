package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TCEntities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.zombie.Husk;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.event.EventHooks;

public class EntityBrainyHusk extends Husk {
    private static final int CONVERSION_EVENT = 1041;

    public EntityBrainyHusk(EntityType<? extends EntityBrainyHusk> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 25.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.ARMOR, 3.0).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    @Override
    protected void doUnderWaterConversion(ServerLevel level) {
        if (!EventHooks.canLivingConvert(this, TCEntities.BRAINY_ZOMBIE.get(), timer -> this.conversionTime = timer)) {
            return;
        }
        this.convertToZombieType(level, TCEntities.BRAINY_ZOMBIE.get());
        if (!this.isSilent()) {
            level.levelEvent(null, CONVERSION_EVENT, this.blockPosition(), 0);
        }
    }
}
