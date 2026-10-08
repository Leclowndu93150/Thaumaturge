package com.leclowndu93150.thaumaturge.content.entity;

import com.leclowndu93150.thaumaturge.registry.TTBiomeTags;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public abstract class AbstractTaintacle extends AbstractRootedTaint {
    private static final double SPAWN_SPACING_HORIZONTAL = 24.0;
    private static final double SPAWN_SPACING_VERTICAL = 8.0;
    private static final int SUBSTRATE_CHECK_INTERVAL = 20;
    private static final float STARVE_DAMAGE = 1.0F;
    private static final float FLAIL_MAX = 3.0F;
    private static final float FLAIL_DECAY = 0.01F;
    private static final double FOLLOW_RANGE = 12.0;

    public float flailIntensity = 1.0F;

    protected AbstractTaintacle(EntityType<? extends AbstractTaintacle> type, Level level) {
        super(type, level);
    }

    public static boolean checkTaintacleSpawnRules(
            EntityType<? extends AbstractTaintacle> type,
            ServerLevelAccessor level,
            MobSpawnType reason,
            BlockPos pos,
            RandomSource random) {
        if (!level.getBiome(pos).is(TTBiomeTags.IS_TAINTED)
                || !onTaint(level.getBlockState(pos)) && !onTaint(level.getBlockState(pos.below()))) {
            return false;
        }
        if (!level.getEntitiesOfClass(
                        EntityTaintacle.class,
                        new AABB(pos)
                                .inflate(SPAWN_SPACING_HORIZONTAL, SPAWN_SPACING_VERTICAL, SPAWN_SPACING_HORIZONTAL))
                .isEmpty()) {
            return false;
        }
        return Monster.checkMonsterSpawnRules(type, level, reason, pos, random);
    }

    private static boolean onTaint(BlockState state) {
        return state.is(TTBlocks.TAINT_FIBRE) || state.is(TTBlocks.TAINT_SOIL);
    }

    public static AttributeSupplier.Builder createTaintacleAttributes(double maxHealth, double attackDamage) {
        return createRootedAttributes(maxHealth, attackDamage, FOLLOW_RANGE);
    }

    @Override
    protected void startStrikeAnimation() {
        this.flailIntensity = FLAIL_MAX;
    }

    @Override
    protected void tickStrikeAnimation() {
        if (this.flailIntensity > 1.0F) {
            this.flailIntensity -= FLAIL_DECAY;
        }
    }

    @Override
    protected void rootedServerStep(ServerLevel server) {
        if (this.tickCount % SUBSTRATE_CHECK_INTERVAL == 0
                && !server.getBiome(this.blockPosition()).is(TTBiomeTags.IS_TAINTED)) {
            this.hurt(server.damageSources().starve(), STARVE_DAMAGE);
        }
    }

    public float enrage() {
        return 0.0F;
    }
}
