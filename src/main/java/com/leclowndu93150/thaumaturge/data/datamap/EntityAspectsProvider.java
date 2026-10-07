package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.api.aspect.AspectDataMaps;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class EntityAspectsProvider extends DataMapProvider {
    private HolderGetter<IAspect> aspects;

    public EntityAspectsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    public String getName() {
        return "Entity Aspects Data Maps";
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        aspects = provider.lookupOrThrow(IAspect.REGISTRY_KEY);
        Builder<AspectList, EntityType<?>> b = builder(AspectDataMaps.ENTITY_ASPECTS);

        add(b, EntityType.ZOMBIE, list(TTAspects.EXANIMIS, 20, TTAspects.HUMANUS, 10, TTAspects.TERRA, 5));
        add(b, EntityType.HUSK, list(TTAspects.EXANIMIS, 20, TTAspects.HUMANUS, 10, TTAspects.IGNIS, 5));
        add(b, EntityType.GIANT, list(TTAspects.EXANIMIS, 25, TTAspects.HUMANUS, 15, TTAspects.TERRA, 10));
        add(b, EntityType.SKELETON, list(TTAspects.EXANIMIS, 20, TTAspects.HUMANUS, 5, TTAspects.TERRA, 5));
        add(b, EntityType.WITHER_SKELETON, list(TTAspects.EXANIMIS, 25, TTAspects.HUMANUS, 5, TTAspects.PERDITIO, 10));
        add(b, EntityType.CREEPER, list(TTAspects.HERBA, 15, TTAspects.IGNIS, 15));
        add(b, EntityType.HORSE, list(TTAspects.BESTIA, 15, TTAspects.TERRA, 5, TTAspects.AER, 5));
        add(b, EntityType.DONKEY, list(TTAspects.BESTIA, 15, TTAspects.TERRA, 5, TTAspects.AER, 5));
        add(b, EntityType.MULE, list(TTAspects.BESTIA, 15, TTAspects.TERRA, 5, TTAspects.AER, 5));
        add(b, EntityType.SKELETON_HORSE, list(TTAspects.BESTIA, 5, TTAspects.EXANIMIS, 10, TTAspects.TERRA, 5, TTAspects.AER, 5));
        add(b, EntityType.ZOMBIE_HORSE, list(TTAspects.BESTIA, 10, TTAspects.EXANIMIS, 5, TTAspects.TERRA, 5, TTAspects.AER, 5));
        add(b, EntityType.PIG, list(TTAspects.BESTIA, 10, TTAspects.TERRA, 10, TTAspects.DESIDERIUM, 5));
        add(b, EntityType.EXPERIENCE_ORB, list(TTAspects.COGNITIO, 10));
        add(b, EntityType.SHEEP, list(TTAspects.BESTIA, 10, TTAspects.TERRA, 10));
        add(b, EntityType.COW, list(TTAspects.BESTIA, 15, TTAspects.TERRA, 15));
        add(b, EntityType.MOOSHROOM, list(TTAspects.BESTIA, 15, TTAspects.HERBA, 15, TTAspects.TERRA, 15));
        add(b, EntityType.SNOW_GOLEM, list(TTAspects.GELUM, 10, TTAspects.HUMANUS, 5, TTAspects.MACHINA, 5, TTAspects.PRAECANTATIO, 5));
        add(b, EntityType.OCELOT, list(TTAspects.BESTIA, 10, TTAspects.PERDITIO, 10));
        add(b, EntityType.CHICKEN, list(TTAspects.BESTIA, 5, TTAspects.VOLATUS, 5, TTAspects.AER, 5));
        add(b, EntityType.SQUID, list(TTAspects.BESTIA, 5, TTAspects.AQUA, 10));
        add(b, EntityType.WOLF, list(TTAspects.BESTIA, 15, TTAspects.TERRA, 10, TTAspects.AVERSIO, 5));
        add(b, EntityType.BAT, list(TTAspects.BESTIA, 5, TTAspects.VOLATUS, 5, TTAspects.TENEBRAE, 5));
        add(b, EntityType.SPIDER, list(TTAspects.BESTIA, 10, TTAspects.PERDITIO, 10, TTAspects.VINCULUM, 10));
        add(b, EntityType.SLIME, list(TTAspects.VICTUS, 10, TTAspects.AQUA, 10, TTAspects.ALKIMIA, 5));
        add(b, EntityType.GHAST, list(TTAspects.EXANIMIS, 15, TTAspects.IGNIS, 15));
        add(b, EntityType.ZOMBIFIED_PIGLIN, list(TTAspects.EXANIMIS, 15, TTAspects.IGNIS, 15, TTAspects.BESTIA, 10));
        add(b, EntityType.ENDERMAN, list(TTAspects.ALIENIS, 10, TTAspects.MOTUS, 15, TTAspects.DESIDERIUM, 5));
        add(b, EntityType.CAVE_SPIDER, list(TTAspects.BESTIA, 5, TTAspects.MORTUUS, 10, TTAspects.VINCULUM, 10));
        add(b, EntityType.SILVERFISH, list(TTAspects.BESTIA, 5, TTAspects.TERRA, 10));
        add(b, EntityType.BLAZE, list(TTAspects.ALIENIS, 5, TTAspects.IGNIS, 15, TTAspects.VOLATUS, 5));
        add(b, EntityType.MAGMA_CUBE, list(TTAspects.AQUA, 5, TTAspects.IGNIS, 10, TTAspects.ALKIMIA, 5));
        add(b, EntityType.ENDER_DRAGON, list(TTAspects.ALIENIS, 50, TTAspects.BESTIA, 30, TTAspects.PERDITIO, 50, TTAspects.VOLATUS, 10));
        add(b, EntityType.WITHER, list(TTAspects.EXANIMIS, 50, TTAspects.PERDITIO, 25, TTAspects.IGNIS, 25));
        add(b, EntityType.WITCH, list(TTAspects.HUMANUS, 15, TTAspects.PRAECANTATIO, 5, TTAspects.ALKIMIA, 10));
        add(b, EntityType.VILLAGER, list(TTAspects.HUMANUS, 15));
        add(b, EntityType.IRON_GOLEM, list(TTAspects.METALLUM, 15, TTAspects.HUMANUS, 5, TTAspects.MACHINA, 5, TTAspects.PRAECANTATIO, 5));
        add(b, EntityType.END_CRYSTAL, list(TTAspects.ALIENIS, 15, TTAspects.AURAM, 15, TTAspects.VICTUS, 15));
        add(b, EntityType.ITEM_FRAME, list(TTAspects.SENSUS, 5, TTAspects.FABRICO, 5));
        add(b, EntityType.GLOW_ITEM_FRAME, list(TTAspects.SENSUS, 5, TTAspects.FABRICO, 5));
        add(b, EntityType.PAINTING, list(TTAspects.SENSUS, 10, TTAspects.FABRICO, 5));
        add(b, EntityType.GUARDIAN, list(TTAspects.BESTIA, 10, TTAspects.ALIENIS, 10, TTAspects.AQUA, 10));
        add(b, EntityType.ELDER_GUARDIAN, list(TTAspects.BESTIA, 10, TTAspects.ALIENIS, 15, TTAspects.AQUA, 15));
        add(b, EntityType.RABBIT, list(TTAspects.BESTIA, 5, TTAspects.TERRA, 5, TTAspects.MOTUS, 5));
        add(b, EntityType.ENDERMITE, list(TTAspects.BESTIA, 5, TTAspects.ALIENIS, 5, TTAspects.MOTUS, 5));
        add(b, EntityType.POLAR_BEAR, list(TTAspects.BESTIA, 15, TTAspects.GELUM, 10));
        add(b, EntityType.SHULKER, list(TTAspects.ALIENIS, 10, TTAspects.VINCULUM, 5, TTAspects.VOLATUS, 5, TTAspects.PRAEMUNIO, 5));
        add(b, EntityType.EVOKER, list(TTAspects.ALIENIS, 5, TTAspects.PRAECANTATIO, 5, TTAspects.HUMANUS, 10));
        add(b, EntityType.VINDICATOR, list(TTAspects.AVERSIO, 5, TTAspects.PRAECANTATIO, 5, TTAspects.HUMANUS, 10));
        add(b, EntityType.ILLUSIONER, list(TTAspects.SENSUS, 5, TTAspects.PRAECANTATIO, 5, TTAspects.HUMANUS, 10));
        add(b, EntityType.LLAMA, list(TTAspects.BESTIA, 15, TTAspects.AQUA, 5));
        add(b, EntityType.PARROT, list(TTAspects.BESTIA, 5, TTAspects.VOLATUS, 5, TTAspects.SENSUS, 5));
        add(b, EntityType.STRAY, list(TTAspects.EXANIMIS, 20, TTAspects.HUMANUS, 5, TTAspects.VINCULUM, 5));
        add(b, EntityType.VEX, list(TTAspects.ALIENIS, 5, TTAspects.VOLATUS, 5, TTAspects.PRAECANTATIO, 5, TTAspects.HUMANUS, 5));

        add(b, EntityType.DOLPHIN, list(TTAspects.BESTIA, 10, TTAspects.AQUA, 10, TTAspects.MOTUS, 5, TTAspects.COGNITIO, 5));
        add(b, EntityType.DROWNED, list(TTAspects.EXANIMIS, 20, TTAspects.HUMANUS, 10, TTAspects.AQUA, 5));
        add(b, EntityType.TURTLE, list(TTAspects.BESTIA, 10, TTAspects.AQUA, 5, TTAspects.PRAEMUNIO, 5));
        add(b, EntityType.COD, list(TTAspects.BESTIA, 5, TTAspects.AQUA, 5));
        add(b, EntityType.SALMON, list(TTAspects.BESTIA, 5, TTAspects.AQUA, 5));
        add(b, EntityType.TROPICAL_FISH, list(TTAspects.BESTIA, 5, TTAspects.AQUA, 5, TTAspects.SENSUS, 3));
        add(b, EntityType.PUFFERFISH, list(TTAspects.BESTIA, 5, TTAspects.AQUA, 5, TTAspects.AVERSIO, 3));
        add(b, EntityType.PHANTOM, list(TTAspects.EXANIMIS, 15, TTAspects.VOLATUS, 10, TTAspects.TENEBRAE, 5, TTAspects.SPIRITUS, 5));
        add(b, EntityType.CAT, list(TTAspects.BESTIA, 10, TTAspects.SENSUS, 5));
        add(b, EntityType.FOX, list(TTAspects.BESTIA, 10, TTAspects.SENSUS, 5, TTAspects.MOTUS, 3));
        add(b, EntityType.PANDA, list(TTAspects.BESTIA, 15, TTAspects.HERBA, 5));
        add(b, EntityType.PILLAGER, list(TTAspects.HUMANUS, 10, TTAspects.AVERSIO, 10));
        add(b, EntityType.RAVAGER, list(TTAspects.BESTIA, 20, TTAspects.AVERSIO, 15, TTAspects.TERRA, 5));
        add(b, EntityType.TRADER_LLAMA, list(TTAspects.BESTIA, 15, TTAspects.AQUA, 5));
        add(b, EntityType.WANDERING_TRADER, list(TTAspects.HUMANUS, 10, TTAspects.PERMUTATIO, 10, TTAspects.DESIDERIUM, 5));
        add(b, EntityType.BEE, list(TTAspects.BESTIA, 5, TTAspects.VOLATUS, 5, TTAspects.HERBA, 5, TTAspects.FABRICO, 2));
        add(b, EntityType.HOGLIN, list(TTAspects.BESTIA, 15, TTAspects.AVERSIO, 10, TTAspects.IGNIS, 5));
        add(b, EntityType.ZOGLIN, list(TTAspects.EXANIMIS, 15, TTAspects.BESTIA, 10, TTAspects.AVERSIO, 10));
        add(b, EntityType.PIGLIN, list(TTAspects.HUMANUS, 10, TTAspects.BESTIA, 5, TTAspects.DESIDERIUM, 10));
        add(b, EntityType.PIGLIN_BRUTE, list(TTAspects.HUMANUS, 10, TTAspects.BESTIA, 5, TTAspects.AVERSIO, 15));
        add(b, EntityType.STRIDER, list(TTAspects.BESTIA, 10, TTAspects.IGNIS, 10, TTAspects.MOTUS, 5));
        add(b, EntityType.AXOLOTL, list(TTAspects.BESTIA, 10, TTAspects.AQUA, 5, TTAspects.VICTUS, 5));
        add(b, EntityType.GLOW_SQUID, list(TTAspects.BESTIA, 5, TTAspects.AQUA, 5, TTAspects.LUX, 5));
        add(b, EntityType.GOAT, list(TTAspects.BESTIA, 10, TTAspects.TERRA, 5, TTAspects.MOTUS, 5));
        add(b, EntityType.ALLAY, list(TTAspects.SPIRITUS, 10, TTAspects.VOLATUS, 5, TTAspects.SENSUS, 5, TTAspects.DESIDERIUM, 3));
        add(b, EntityType.FROG, list(TTAspects.BESTIA, 8, TTAspects.AQUA, 4, TTAspects.MOTUS, 3));
        add(b, EntityType.TADPOLE, list(TTAspects.BESTIA, 4, TTAspects.AQUA, 4, TTAspects.VICTUS, 2));
        add(b, EntityType.WARDEN, list(TTAspects.TENEBRAE, 20, TTAspects.SENSUS, 15, TTAspects.AVERSIO, 15, TTAspects.TERRA, 10, TTAspects.VITIUM, 5));
        add(b, EntityType.CAMEL, list(TTAspects.BESTIA, 12, TTAspects.TERRA, 5, TTAspects.MOTUS, 3));
        add(b, EntityType.SNIFFER, list(TTAspects.BESTIA, 15, TTAspects.HERBA, 10, TTAspects.SENSUS, 10, TTAspects.TERRA, 5));
        add(b, EntityType.ARMADILLO, list(TTAspects.BESTIA, 8, TTAspects.PRAEMUNIO, 5, TTAspects.TERRA, 3));
        add(b, EntityType.BOGGED, list(TTAspects.EXANIMIS, 20, TTAspects.HUMANUS, 5, TTAspects.HERBA, 5));
        add(b, EntityType.BREEZE, list(TTAspects.AER, 15, TTAspects.MOTUS, 10, TTAspects.POTENTIA, 5, TTAspects.PRAECANTATIO, 3));
        add(b, EntityType.CREAKING, list(TTAspects.HERBA, 15, TTAspects.SPIRITUS, 10, TTAspects.TENEBRAE, 10, TTAspects.AVERSIO, 5));
        add(b, EntityType.HAPPY_GHAST, list(TTAspects.BESTIA, 10, TTAspects.VOLATUS, 10, TTAspects.AER, 5, TTAspects.VICTUS, 3));
        add(b, EntityType.COPPER_GOLEM, list(TTAspects.METALLUM, 10, TTAspects.MACHINA, 10, TTAspects.MOTUS, 5, TTAspects.HUMANUS, 3));
        add(b, EntityType.CAMEL_HUSK, list(TTAspects.EXANIMIS, 15, TTAspects.BESTIA, 8, TTAspects.IGNIS, 3));
        add(b, EntityType.PARCHED, list(TTAspects.EXANIMIS, 15, TTAspects.HUMANUS, 8, TTAspects.TERRA, 3));
        add(b, EntityType.NAUTILUS, list(TTAspects.BESTIA, 8, TTAspects.AQUA, 8, TTAspects.PRAEMUNIO, 3));
        add(b, EntityType.ZOMBIE_NAUTILUS, list(TTAspects.EXANIMIS, 12, TTAspects.BESTIA, 5, TTAspects.AQUA, 5));
        add(b, EntityType.ZOMBIE_VILLAGER, list(TTAspects.EXANIMIS, 20, TTAspects.HUMANUS, 15, TTAspects.TERRA, 5));
        add(b, EntityType.MANNEQUIN, list(TTAspects.HUMANUS, 10, TTAspects.SENSUS, 5));

        add(b, TTEntities.THAUMIC_SLIME.get(), list(TTAspects.VICTUS, 5, TTAspects.AQUA, 5, TTAspects.VITIUM, 5, TTAspects.ALKIMIA, 5));
        add(b, TTEntities.TAINTACLE.get(), list(TTAspects.VITIUM, 15, TTAspects.BESTIA, 10));
        add(b, TTEntities.TAINT_SPORE.get(), list(TTAspects.VITIUM, 2, TTAspects.AER, 2));
        add(b, TTEntities.TAINT_SPORE_SWARMER.get(), list(TTAspects.VITIUM, 2, TTAspects.AER, 2));
        add(b, TTEntities.TAINT_SEED.get(), list(TTAspects.VITIUM, 20, TTAspects.AURAM, 10, TTAspects.HERBA, 5));
        add(b, TTEntities.TAINT_SEED_PRIME.get(), list(TTAspects.VITIUM, 25, TTAspects.AURAM, 15, TTAspects.HERBA, 5));
        add(b, TTEntities.TAINTACLE_SMALL.get(), list(TTAspects.VITIUM, 5, TTAspects.BESTIA, 5));
        add(b, TTEntities.TAINT_SWARM.get(), list(TTAspects.VITIUM, 15, TTAspects.AER, 5));
        add(b, TTEntities.FIRE_BAT.get(), list(TTAspects.BESTIA, 5, TTAspects.VOLATUS, 5, TTAspects.IGNIS, 10));
        add(b, TTEntities.MIND_SPIDER.get(), list(TTAspects.VITIUM, 5, TTAspects.IGNIS, 5));
        add(b, TTEntities.BRAINY_ZOMBIE.get(), list(TTAspects.EXANIMIS, 20, TTAspects.HUMANUS, 10, TTAspects.COGNITIO, 5, TTAspects.AVERSIO, 5));
        add(b, TTEntities.BRAINY_DROWNED.get(), list(TTAspects.EXANIMIS, 20, TTAspects.HUMANUS, 10, TTAspects.COGNITIO, 5, TTAspects.AQUA, 10));
        add(b, TTEntities.BRAINY_HUSK.get(), list(TTAspects.EXANIMIS, 20, TTAspects.HUMANUS, 10, TTAspects.COGNITIO, 5, TTAspects.TERRA, 10));
        add(b, TTEntities.GIANT_BRAINY_ZOMBIE.get(), list(TTAspects.EXANIMIS, 25, TTAspects.HUMANUS, 15, TTAspects.COGNITIO, 5, TTAspects.AVERSIO, 10));
        add(b, TTEntities.PECH.get(), list(TTAspects.HUMANUS, 10, TTAspects.AURAM, 5, TTAspects.PERMUTATIO, 10, TTAspects.DESIDERIUM, 5));
        add(b, TTEntities.ELDRITCH_GUARDIAN.get(), list(TTAspects.ALIENIS, 20, TTAspects.MORTUUS, 20, TTAspects.EXANIMIS, 20));
        add(b, TTEntities.CULTIST_KNIGHT.get(), list(TTAspects.ALIENIS, 5, TTAspects.HUMANUS, 15, TTAspects.AVERSIO, 5));
        add(b, TTEntities.CULTIST_CLERIC.get(), list(TTAspects.ALIENIS, 5, TTAspects.HUMANUS, 15, TTAspects.AVERSIO, 5));
        add(b, TTEntities.ELDRITCH_CRAB.get(), list(TTAspects.ALIENIS, 10, TTAspects.BESTIA, 10, TTAspects.VINCULUM, 10));
        add(b, TTEntities.CULTIST_LEADER.get(), list(TTAspects.ALIENIS, 15, TTAspects.HUMANUS, 25, TTAspects.AVERSIO, 10, TTAspects.PRAECANTATIO, 15, TTAspects.COGNITIO, 10));
        add(b, TTEntities.ELDRITCH_HIEROPHANT.get(), list(TTAspects.ALIENIS, 40, TTAspects.PRAECANTATIO, 30, TTAspects.MORTUUS, 20));
        add(b, TTEntities.ELDRITCH_GOLEM.get(), list(TTAspects.ALIENIS, 25, TTAspects.METALLUM, 20, TTAspects.MOTUS, 15));
        add(b, TTEntities.INHABITED_ZOMBIE.get(), list(TTAspects.EXANIMIS, 20, TTAspects.HUMANUS, 10, TTAspects.ALIENIS, 10, TTAspects.BESTIA, 10));
        add(b, TTEntities.TAINTACLE_GIANT.get(), list(TTAspects.VITIUM, 30, TTAspects.BESTIA, 20));
        add(b, TTEntities.THAUMATURGE_GOLEM.get(), list(TTAspects.MOTUS, 10, TTAspects.MACHINA, 10, TTAspects.PRAECANTATIO, 5));
    }

    private void add(Builder<AspectList, EntityType<?>> b, EntityType<?> type, AspectList value) {
        b.add(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type), value, false);
    }

    private AspectList list(ResourceKey<IAspect> a1, int n1) {
        return AspectList.EMPTY.add(aspects.getOrThrow(a1), n1);
    }

    private AspectList list(ResourceKey<IAspect> a1, int n1, ResourceKey<IAspect> a2, int n2) {
        return list(a1, n1).add(aspects.getOrThrow(a2), n2);
    }

    private AspectList list(ResourceKey<IAspect> a1, int n1, ResourceKey<IAspect> a2, int n2, ResourceKey<IAspect> a3, int n3) {
        return list(a1, n1, a2, n2).add(aspects.getOrThrow(a3), n3);
    }

    private AspectList list(ResourceKey<IAspect> a1, int n1, ResourceKey<IAspect> a2, int n2, ResourceKey<IAspect> a3, int n3, ResourceKey<IAspect> a4, int n4) {
        return list(a1, n1, a2, n2, a3, n3).add(aspects.getOrThrow(a4), n4);
    }

    private AspectList list(ResourceKey<IAspect> a1, int n1, ResourceKey<IAspect> a2, int n2, ResourceKey<IAspect> a3, int n3, ResourceKey<IAspect> a4, int n4, ResourceKey<IAspect> a5, int n5) {
        return list(a1, n1, a2, n2, a3, n3, a4, n4).add(aspects.getOrThrow(a5), n5);
    }
}
