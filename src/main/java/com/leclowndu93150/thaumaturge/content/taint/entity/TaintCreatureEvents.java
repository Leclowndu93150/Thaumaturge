package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintCreeper;
import com.leclowndu93150.thaumaturge.content.taint.block.BlockTaintFibre;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintBiomeManager;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.ExplosionEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TaintCreatureEvents {
    private static final int TC4_TAINT_SPLosion_ATTEMPTS = 10;

    private TaintCreatureEvents() {}

    @SubscribeEvent
    public static void onDetonate(ExplosionEvent.Detonate event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(event.getExplosion().getDirectSourceEntity() instanceof EntityTaintCreeper creeper)) {
            return;
        }
        for (net.minecraft.world.entity.Entity entity : event.getAffectedEntities()) {
            if (entity instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(TCMobEffects.FLUX_TAINT, 600, 0, false, true, false));
            }
        }
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()) {
            return;
        }

        BlockPos center = creeper.blockPosition();
        RandomSource random = level.getRandom();
        for (int i = 0; i < TC4_TAINT_SPLosion_ATTEMPTS; i++) {
            int x = center.getX() + (int) ((random.nextFloat() - random.nextFloat()) * 6.0F);
            int z = center.getZ() + (int) ((random.nextFloat() - random.nextFloat()) * 6.0F);
            if (!random.nextBoolean()) {
                continue;
            }
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            BlockPos column = new BlockPos(x, y, z);
            if (!level.hasChunkAt(column) || !TaintBiomeManager.taintColumn(level, column)) {
                continue;
            }
            if (level.getBlockState(column).canBeReplaced() && BlockTaintFibre.hasSolidAttachment(level, column)) {
                level.setBlock(column, TCBlocks.TAINT_FIBRE.get().defaultBlockState(), Block.UPDATE_ALL);
            }
            TaintEcology.addPressure(level, column, 0.01F);
        }
    }
}
