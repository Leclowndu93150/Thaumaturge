package com.leclowndu93150.thaumaturge.content.taint.entity;

import com.leclowndu93150.thaumaturge.api.entity.ITaintedMob;
import com.leclowndu93150.thaumaturge.config.ThaumaturgeCommonConfig;
import com.leclowndu93150.thaumaturge.content.entity.EntityTaintSheep;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.golem.EntityThaumaturgeGolem;
import com.leclowndu93150.thaumaturge.content.taint.ecology.TaintEcology;
import com.leclowndu93150.thaumaturge.registry.TCDataMaps;
import com.leclowndu93150.thaumaturge.registry.TCEntityTags;
import java.util.function.Supplier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.npc.Villager;
import org.jspecify.annotations.Nullable;

/** Single router for specialized replacements and the generic tainted fallback. */
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

        TaintConversion conversion = conversionFor(source.getType());
        if (conversion != null) {
            return replace(level, source, () -> conversion.into().create(level) instanceof Mob mob ? mob : null);
        }

        ChampionHelper.makeTainted(source);
        TaintEcology.addPressure(level, source.blockPosition(), GENERIC_REPLACEMENT_PRESSURE);
        return Result.CONVERTED_TO_GENERIC_TAINTED;
    }

    public static @Nullable TaintConversion conversionFor(EntityType<?> type) {
        return type.builtInRegistryHolder().getData(TCDataMaps.TAINT_CONVERSION);
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
        if (source instanceof Villager sourceVillager && replacement instanceof Villager replacementVillager) {
            replacementVillager.setVillagerData(sourceVillager.getVillagerData());
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
