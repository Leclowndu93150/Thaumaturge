package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.registry.TCEffectTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class TCMobEffectTagsProvider extends TagsProvider<MobEffect> {
    public TCMobEffectTagsProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            ExistingFileHelper existingFileHelper) {
        super(output, Registries.MOB_EFFECT, lookupProvider, TCIds.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(TCEffectTags.MANA_BEAN_EFFECTS)
                .add(key(MobEffects.MOVEMENT_SPEED))
                .add(key(MobEffects.MOVEMENT_SLOWDOWN))
                .add(key(MobEffects.DIG_SPEED))
                .add(key(MobEffects.DIG_SLOWDOWN))
                .add(key(MobEffects.DAMAGE_BOOST))
                .add(key(MobEffects.HEAL))
                .add(key(MobEffects.HARM))
                .add(key(MobEffects.JUMP))
                .add(key(MobEffects.CONFUSION))
                .add(key(MobEffects.REGENERATION))
                .add(key(MobEffects.DAMAGE_RESISTANCE))
                .add(key(MobEffects.FIRE_RESISTANCE))
                .add(key(MobEffects.WATER_BREATHING))
                .add(key(MobEffects.INVISIBILITY))
                .add(key(MobEffects.BLINDNESS))
                .add(key(MobEffects.NIGHT_VISION))
                .add(key(MobEffects.HUNGER))
                .add(key(MobEffects.WEAKNESS))
                .add(key(MobEffects.POISON))
                .add(key(MobEffects.WITHER))
                .add(key(MobEffects.HEALTH_BOOST))
                .add(key(MobEffects.ABSORPTION))
                .add(key(MobEffects.SATURATION));
    }

    private static ResourceKey<MobEffect> key(Holder<MobEffect> effect) {
        return effect.unwrapKey().orElseThrow();
    }
}
