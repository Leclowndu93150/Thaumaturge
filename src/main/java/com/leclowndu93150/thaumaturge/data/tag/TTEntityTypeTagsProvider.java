package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.api.entity.ThaumaturgeEntityTypeTags;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTEntityTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

public final class TTEntityTypeTagsProvider extends TagsProvider<EntityType<?>> {
    public TTEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ENTITY_TYPE, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(TTEntityTags.TAINT_CONVERSION_IMMUNE);
        tag(EntityTypeTags.CAN_BREATHE_UNDER_WATER)
                .add(key(TTEntities.CULTIST_LEADER.get()))
                .add(key(TTEntities.CULTIST_PORTAL_GREATER.get()))
                .add(key(TTEntities.ELDRITCH_GOLEM.get()))
                .add(key(TTEntities.ELDRITCH_WARDEN.get()))
                .add(key(TTEntities.ELDRITCH_HIEROPHANT.get()))
                .add(key(TTEntities.TAINTACLE_GIANT.get()));
        tag(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS).add(key(TTEntities.ELDRITCH_CRAB.get()));
        tag(EntityTypeTags.UNDEAD)
                .add(key(TTEntities.ELDRITCH_GUARDIAN.get()))
                .add(key(TTEntities.INHABITED_ZOMBIE.get()))
                .add(key(TTEntities.BRAINY_ZOMBIE.get()))
                .add(key(TTEntities.GIANT_BRAINY_ZOMBIE.get()))
                .add(key(TTEntities.BRAINY_DROWNED.get()))
                .add(key(TTEntities.BRAINY_HUSK.get()));
        tag(EntityTypeTags.SENSITIVE_TO_SMITE)
                .add(key(TTEntities.ELDRITCH_GUARDIAN.get()))
                .add(key(TTEntities.INHABITED_ZOMBIE.get()))
                .add(key(TTEntities.BRAINY_ZOMBIE.get()))
                .add(key(TTEntities.GIANT_BRAINY_ZOMBIE.get()))
                .add(key(TTEntities.BRAINY_DROWNED.get()))
                .add(key(TTEntities.BRAINY_HUSK.get()));
        tag(EntityTypeTags.INVERTED_HEALING_AND_HARM)
                .add(key(TTEntities.ELDRITCH_GUARDIAN.get()))
                .add(key(TTEntities.INHABITED_ZOMBIE.get()))
                .add(key(TTEntities.BRAINY_ZOMBIE.get()))
                .add(key(TTEntities.GIANT_BRAINY_ZOMBIE.get()))
                .add(key(TTEntities.BRAINY_DROWNED.get()))
                .add(key(TTEntities.BRAINY_HUSK.get()));
        tag(ThaumaturgeEntityTypeTags.ELDRITCH)
                .add(key(TTEntities.MIND_SPIDER.get()))
                .add(key(TTEntities.ELDRITCH_CRAB.get()))
                .add(key(TTEntities.INHABITED_ZOMBIE.get()))
                .add(key(TTEntities.ELDRITCH_GUARDIAN.get()))
                .add(key(TTEntities.ELDRITCH_GOLEM.get()))
                .add(key(TTEntities.ELDRITCH_WARDEN.get()))
                .add(key(TTEntities.ELDRITCH_HIEROPHANT.get()))
                .add(key(TTEntities.TAINTACLE_GIANT.get()));
        tag(ThaumaturgeEntityTypeTags.TAINTED)
                .add(key(TTEntities.THAUMIC_SLIME.get()))
                .add(key(TTEntities.TAINT_CRAWLER.get()))
                .add(key(TTEntities.TAINT_SWARM.get()))
                .add(key(TTEntities.TAINT_SEED.get()))
                .add(key(TTEntities.TAINT_SEED_PRIME.get()))
                .add(key(TTEntities.TAINT_SPORE.get()))
                .add(key(TTEntities.TAINT_SPORE_SWARMER.get()))
                .add(key(TTEntities.TAINTACLE.get()))
                .add(key(TTEntities.TAINTACLE_SMALL.get()))
                .add(key(TTEntities.TAINTACLE_GIANT.get()));
        tag(EntityTypeTags.WITHER_FRIENDS).add(key(TTEntities.ELDRITCH_GUARDIAN.get()));
    }

    private static ResourceKey<EntityType<?>> key(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getResourceKey(type).orElseThrow();
    }
}
