package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.registry.TCEffectTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

public final class TCMobEffectTagsProvider extends KeyTagProvider<MobEffect> {
    public TCMobEffectTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.MOB_EFFECT, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(TCEffectTags.MANA_BEAN_EFFECTS).add(MobEffects.SPEED.getKey()).add(MobEffects.SLOWNESS.getKey()).add(MobEffects.HASTE.getKey()).add(MobEffects.MINING_FATIGUE.getKey())
                .add(MobEffects.STRENGTH.getKey()).add(MobEffects.INSTANT_HEALTH.getKey()).add(MobEffects.INSTANT_DAMAGE.getKey()).add(MobEffects.JUMP_BOOST.getKey()).add(MobEffects.NAUSEA.getKey())
                .add(MobEffects.REGENERATION.getKey()).add(MobEffects.RESISTANCE.getKey()).add(MobEffects.FIRE_RESISTANCE.getKey()).add(MobEffects.WATER_BREATHING.getKey())
                .add(MobEffects.INVISIBILITY.getKey()).add(MobEffects.BLINDNESS.getKey()).add(MobEffects.NIGHT_VISION.getKey()).add(MobEffects.HUNGER.getKey()).add(MobEffects.WEAKNESS.getKey())
                .add(MobEffects.POISON.getKey()).add(MobEffects.WITHER.getKey()).add(MobEffects.HEALTH_BOOST.getKey()).add(MobEffects.ABSORPTION.getKey()).add(MobEffects.SATURATION.getKey());
    }
}
