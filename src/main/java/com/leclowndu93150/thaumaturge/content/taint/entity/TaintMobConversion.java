package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSheep;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCEntityTags;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Sheep;

/** Single router for specialized legacy replacements and the modern generic tainted fallback. */
public final class TaintMobConversion {
    private static final float LEGACY_REPLACEMENT_PRESSURE = 0.04F;
    private static final float GENERIC_REPLACEMENT_PRESSURE = 0.01F;

    private TaintMobConversion() {}

    public static Result tryConvert(ServerLevel level, LivingEntity source) {
        if (ThaumaturgeCommonConfig.WUSS_MODE.get()
                || level.getDifficulty() == Difficulty.PEACEFUL
                || source instanceof ITaintedMob
                || source instanceof EntityThaumaturgeGolem
                || source.getType().builtInRegistryHolder().is(TCEntityTags.TAINT_CONVERSION_IMMUNE)) {
            return Result.IGNORED;
        }

        if (source.getType().builtInRegistryHolder().is(TCEntityTags.TAINT_LEGACY_CREEPER)) {
            return replace(level, source, () -> TCEntities.TAINT_CREEPER.get().create(level));
        }
        if (source.getType().builtInRegistryHolder().is(TCEntityTags.TAINT_LEGACY_COW)) {
            return replace(level, source, () -> TCEntities.TAINT_COW.get().create(level));
        }
        if (source.getType().builtInRegistryHolder().is(TCEntityTags.TAINT_LEGACY_PIG)) {
            return replace(level, source, () -> TCEntities.TAINT_PIG.get().create(level));
        }
        if (source.getType().builtInRegistryHolder().is(TCEntityTags.TAINT_LEGACY_CHICKEN)) {
            return replace(level, source, () -> TCEntities.TAINT_CHICKEN.get().create(level));
        }
        if (source.getType().builtInRegistryHolder().is(TCEntityTags.TAINT_LEGACY_SHEEP)) {
            return replace(level, source, () -> TCEntities.TAINT_SHEEP.get().create(level));
        }
        if (source.getType().builtInRegistryHolder().is(TCEntityTags.TAINT_LEGACY_VILLAGER)) {
            return replace(level, source, () -> TCEntities.TAINT_VILLAGER.get().create(level));
        }

        ChampionHelper.makeTainted(source);
        TaintEcology.addPressure(level, source.blockPosition(), GENERIC_REPLACEMENT_PRESSURE);
        return Result.CONVERTED_TO_GENERIC_TAINTED;
    }

    private static Result replace(ServerLevel level, LivingEntity source, Supplier<? extends Mob> factory) {
        Mob replacement = factory.get();
        if (replacement == null) {
            return Result.IGNORED;
        }

        replacement.moveTo(source.getX(), source.getY(), source.getZ(), source.getYRot(), source.getXRot());
        replacement.setYHeadRot(source.getYHeadRot());
        replacement.setDeltaMovement(source.getDeltaMovement());
        replacement.setSilent(source.isSilent());
        replacement.setGlowingTag(source.isCurrentlyGlowing());
        replacement.setInvulnerable(source.isInvulnerable());
        if (source.hasCustomName()) {
            replacement.setCustomName(source.getCustomName());
            replacement.setCustomNameVisible(source.isCustomNameVisible());
        }
        if (source instanceof Mob sourceMob) {
            replacement.setNoAi(sourceMob.isNoAi());
            replacement.setLeftHanded(sourceMob.isLeftHanded());
            if (sourceMob.isPersistenceRequired()) {
                replacement.setPersistenceRequired();
            }
        }
        if (source instanceof AgeableMob sourceAgeable && replacement instanceof AgeableMob replacementAgeable) {
            replacementAgeable.setAge(sourceAgeable.getAge());
        }
        if (source instanceof Sheep sourceSheep && replacement instanceof EntityTaintSheep replacementSheep) {
            replacementSheep.setColor(sourceSheep.getColor());
            replacementSheep.setSheared(sourceSheep.isSheared());
        }

        float healthRatio = source.getMaxHealth() <= 0.0F ? 1.0F : source.getHealth() / source.getMaxHealth();
        replacement.setHealth(
                Math.max(1.0F, Math.min(replacement.getMaxHealth(), replacement.getMaxHealth() * healthRatio)));

        source.discard();
        level.addFreshEntity(replacement);
        TaintEcology.addPressure(level, replacement.blockPosition(), LEGACY_REPLACEMENT_PRESSURE);
        return Result.REPLACED_WITH_LEGACY_VARIANT;
    }

    public enum Result {
        REPLACED_WITH_LEGACY_VARIANT,
        CONVERTED_TO_GENERIC_TAINTED,
        IGNORED
    }
}
