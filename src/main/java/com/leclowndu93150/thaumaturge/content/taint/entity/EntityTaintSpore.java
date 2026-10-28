package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTraits;
import com.leclowndu93150.thaumaturge.content.taint.TaintHelper;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.level.Level;

public final class EntityTaintSpore extends AbstractTaintSpore {
    private static final double MAX_HEALTH = 10.0;
    private static final int SATELLITE_MIN_SIZE = 8;
    private static final float SATELLITE_MIN_PRESSURE = 0.85F;
    private static final int SATELLITE_CHANCE = 4;
    private static final int MAX_SPIDERS = 6;
    private static final double BROOD_SPREAD = 0.5;
    private static final float FULL_TURN = 360.0F;

    public EntityTaintSpore(EntityType<? extends EntityTaintSpore> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.MOVEMENT_SPEED, 0.0);
    }

    @Override
    protected boolean requiresStalkSupport() {
        return true;
    }

    @Override
    protected void onBurst(ServerLevel level) {
        int size = getSporeSize();
        if (size >= SATELLITE_MIN_SIZE
                && TaintEcology.getSaturation(level, blockPosition()) >= SATELLITE_MIN_PRESSURE
                && random.nextInt(SATELLITE_CHANCE) == 0) {
            TaintHelper.trySpawnSatelliteSeed(level, blockPosition().below(), random);
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        int count = Math.min(MAX_SPIDERS, random.nextInt(size + 1));
        for (int i = 0; i < count; i++) {
            Spider spider = EntityType.SPIDER.create(level);
            if (spider != null) {
                spider.moveTo(
                        getX() + random.nextDouble() - BROOD_SPREAD,
                        getY(),
                        getZ() + random.nextDouble() - BROOD_SPREAD,
                        random.nextFloat() * FULL_TURN,
                        0.0F);
                MobTraits.add(spider, TTMobTraits.TAINT_BROOD);
                level.addFreshEntity(spider);
            }
        }
    }
}
