package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.api.entity.ThaumaturgeEntityTypeTags;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
import com.leclowndu93150.thaumaturge.registry.TCEntityTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

public final class TCEntityTypeTagsProvider extends KeyTagProvider<EntityType<?>> {
    public TCEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ENTITY_TYPE, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(EntityTypeTags.CAN_BREATHE_UNDER_WATER).add(key(TCEntities.CULTIST_LEADER.get())).add(key(TCEntities.CULTIST_PORTAL_GREATER.get())).add(key(TCEntities.ELDRITCH_GOLEM.get()))
                .add(key(TCEntities.ELDRITCH_WARDEN.get())).add(key(TCEntities.ELDRITCH_HIEROPHANT.get())).add(key(TCEntities.TAINTACLE_GIANT.get()));
        tag(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS).add(key(TCEntities.ELDRITCH_CRAB.get()));
        tag(EntityTypeTags.UNDEAD).add(key(TCEntities.ELDRITCH_GUARDIAN.get())).add(key(TCEntities.INHABITED_ZOMBIE.get())).add(key(TCEntities.BRAINY_ZOMBIE.get()))
                .add(key(TCEntities.GIANT_BRAINY_ZOMBIE.get())).add(key(TCEntities.BRAINY_DROWNED.get())).add(key(TCEntities.BRAINY_HUSK.get()));
        tag(EntityTypeTags.SENSITIVE_TO_SMITE).add(key(TCEntities.ELDRITCH_GUARDIAN.get())).add(key(TCEntities.INHABITED_ZOMBIE.get())).add(key(TCEntities.BRAINY_ZOMBIE.get()))
                .add(key(TCEntities.GIANT_BRAINY_ZOMBIE.get())).add(key(TCEntities.BRAINY_DROWNED.get())).add(key(TCEntities.BRAINY_HUSK.get()));
        tag(EntityTypeTags.INVERTED_HEALING_AND_HARM).add(key(TCEntities.ELDRITCH_GUARDIAN.get())).add(key(TCEntities.INHABITED_ZOMBIE.get())).add(key(TCEntities.BRAINY_ZOMBIE.get()))
                .add(key(TCEntities.GIANT_BRAINY_ZOMBIE.get())).add(key(TCEntities.BRAINY_DROWNED.get())).add(key(TCEntities.BRAINY_HUSK.get()));
        tag(TCEntityTags.TAINT_CONVERSION_IMMUNE).add(key(TCEntities.THAUMATURGE_GOLEM.get()));
        tag(ThaumaturgeEntityTypeTags.ELDRITCH).add(key(TCEntities.MIND_SPIDER.get())).add(key(TCEntities.ELDRITCH_CRAB.get())).add(key(TCEntities.INHABITED_ZOMBIE.get()))
                .add(key(TCEntities.ELDRITCH_GUARDIAN.get())).add(key(TCEntities.ELDRITCH_GOLEM.get())).add(key(TCEntities.ELDRITCH_WARDEN.get())).add(key(TCEntities.ELDRITCH_HIEROPHANT.get()))
                .add(key(TCEntities.TAINTACLE_GIANT.get()));
        tag(ThaumaturgeEntityTypeTags.TAINTED).add(key(TCEntities.THAUMIC_SLIME.get())).add(key(TCEntities.TAINT_CRAWLER.get())).add(key(TCEntities.TAINT_SWARM.get()))
                .add(key(TCEntities.TAINT_SEED.get())).add(key(TCEntities.TAINT_SEED_PRIME.get())).add(key(TCEntities.TAINT_SPORE.get())).add(key(TCEntities.TAINT_SPORE_SWARMER.get()))
                .add(key(TCEntities.TAINTACLE.get())).add(key(TCEntities.TAINTACLE_SMALL.get())).add(key(TCEntities.TAINTACLE_GIANT.get()));
        tag(EntityTypeTags.WITHER_FRIENDS).add(key(TCEntities.ELDRITCH_GUARDIAN.get()));
    }

    private static ResourceKey<EntityType<?>> key(EntityType<?> type) {
        return BuiltInRegistries.ENTITY_TYPE.getResourceKey(type).orElseThrow();
    }
}
