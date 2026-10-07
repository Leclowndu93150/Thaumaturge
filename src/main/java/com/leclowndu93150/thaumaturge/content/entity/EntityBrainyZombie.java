package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.neoforged.neoforge.event.EventHooks;

public class EntityBrainyZombie extends Zombie {
    private static final int CONVERSION_EVENT = 1040;

    public EntityBrainyZombie(EntityType<? extends EntityBrainyZombie> type, Level level) {
        super(type, level);
    }

    public static boolean checkBrainyZombieSpawnRules(EntityType<? extends Monster> type, ServerLevelAccessor level, EntitySpawnReason reason, BlockPos pos, RandomSource random) {
        return !level.getBiome(pos).is(TTBiomeTags.IS_TAINTED) && Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Zombie.createAttributes().add(Attributes.MAX_HEALTH, 25.0).add(Attributes.ATTACK_DAMAGE, 5.0).add(Attributes.ARMOR, 3.0).add(Attributes.SPAWN_REINFORCEMENTS_CHANCE, 0.0);
    }

    @Override
    protected boolean convertsInWater() {
        return true;
    }

    @Override
    protected void doUnderWaterConversion(ServerLevel level) {
        if (!EventHooks.canLivingConvert(this, TTEntities.BRAINY_DROWNED.get(), timer -> this.conversionTime = timer)) {
            return;
        }
        this.convertToZombieType(level, TTEntities.BRAINY_DROWNED.get());
        if (!this.isSilent()) {
            level.levelEvent(null, CONVERSION_EVENT, this.blockPosition(), 0);
        }
    }
}
