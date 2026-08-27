package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.api.aspect.AspectDataMaps;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.registry.TCEntities;
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

        add(b, EntityType.ZOMBIE, list(TCAspects.EXANIMIS, 20, TCAspects.HUMANUS, 10, TCAspects.TERRA, 5));
        add(b, EntityType.HUSK, list(TCAspects.EXANIMIS, 20, TCAspects.HUMANUS, 10, TCAspects.IGNIS, 5));
        add(b, EntityType.GIANT, list(TCAspects.EXANIMIS, 25, TCAspects.HUMANUS, 15, TCAspects.TERRA, 10));
        add(b, EntityType.SKELETON, list(TCAspects.EXANIMIS, 20, TCAspects.HUMANUS, 5, TCAspects.TERRA, 5));
        add(b, EntityType.WITHER_SKELETON, list(TCAspects.EXANIMIS, 25, TCAspects.HUMANUS, 5, TCAspects.PERDITIO, 10));
        add(b, EntityType.CREEPER, list(TCAspects.HERBA, 15, TCAspects.IGNIS, 15));
        add(b, EntityType.HORSE, list(TCAspects.BESTIA, 15, TCAspects.TERRA, 5, TCAspects.AER, 5));
        add(b, EntityType.DONKEY, list(TCAspects.BESTIA, 15, TCAspects.TERRA, 5, TCAspects.AER, 5));
        add(b, EntityType.MULE, list(TCAspects.BESTIA, 15, TCAspects.TERRA, 5, TCAspects.AER, 5));
        add(
                b,
                EntityType.SKELETON_HORSE,
                list(TCAspects.BESTIA, 5, TCAspects.EXANIMIS, 10, TCAspects.TERRA, 5, TCAspects.AER, 5));
        add(
                b,
                EntityType.ZOMBIE_HORSE,
                list(TCAspects.BESTIA, 10, TCAspects.EXANIMIS, 5, TCAspects.TERRA, 5, TCAspects.AER, 5));
        add(b, EntityType.PIG, list(TCAspects.BESTIA, 10, TCAspects.TERRA, 10, TCAspects.DESIDERIUM, 5));
        add(b, EntityType.EXPERIENCE_ORB, list(TCAspects.COGNITIO, 10));
        add(b, EntityType.SHEEP, list(TCAspects.BESTIA, 10, TCAspects.TERRA, 10));
        add(b, EntityType.COW, list(TCAspects.BESTIA, 15, TCAspects.TERRA, 15));
        add(b, EntityType.MOOSHROOM, list(TCAspects.BESTIA, 15, TCAspects.HERBA, 15, TCAspects.TERRA, 15));
        add(
                b,
                EntityType.SNOW_GOLEM,
                list(TCAspects.GELUM, 10, TCAspects.HUMANUS, 5, TCAspects.MACHINA, 5, TCAspects.PRAECANTATIO, 5));
        add(b, EntityType.OCELOT, list(TCAspects.BESTIA, 10, TCAspects.PERDITIO, 10));
        add(b, EntityType.CHICKEN, list(TCAspects.BESTIA, 5, TCAspects.VOLATUS, 5, TCAspects.AER, 5));
        add(b, EntityType.SQUID, list(TCAspects.BESTIA, 5, TCAspects.AQUA, 10));
        add(b, EntityType.WOLF, list(TCAspects.BESTIA, 15, TCAspects.TERRA, 10, TCAspects.AVERSIO, 5));
        add(b, EntityType.BAT, list(TCAspects.BESTIA, 5, TCAspects.VOLATUS, 5, TCAspects.TENEBRAE, 5));
        add(b, EntityType.SPIDER, list(TCAspects.BESTIA, 10, TCAspects.PERDITIO, 10, TCAspects.VINCULUM, 10));
        add(b, EntityType.SLIME, list(TCAspects.VICTUS, 10, TCAspects.AQUA, 10, TCAspects.ALKIMIA, 5));
        add(b, EntityType.GHAST, list(TCAspects.EXANIMIS, 15, TCAspects.IGNIS, 15));
        add(b, EntityType.ZOMBIFIED_PIGLIN, list(TCAspects.EXANIMIS, 15, TCAspects.IGNIS, 15, TCAspects.BESTIA, 10));
        add(b, EntityType.ENDERMAN, list(TCAspects.ALIENIS, 10, TCAspects.MOTUS, 15, TCAspects.DESIDERIUM, 5));
        add(b, EntityType.CAVE_SPIDER, list(TCAspects.BESTIA, 5, TCAspects.MORTUUS, 10, TCAspects.VINCULUM, 10));
        add(b, EntityType.SILVERFISH, list(TCAspects.BESTIA, 5, TCAspects.TERRA, 10));
        add(b, EntityType.BLAZE, list(TCAspects.ALIENIS, 5, TCAspects.IGNIS, 15, TCAspects.VOLATUS, 5));
        add(b, EntityType.MAGMA_CUBE, list(TCAspects.AQUA, 5, TCAspects.IGNIS, 10, TCAspects.ALKIMIA, 5));
        add(
                b,
                EntityType.ENDER_DRAGON,
                list(TCAspects.ALIENIS, 50, TCAspects.BESTIA, 30, TCAspects.PERDITIO, 50, TCAspects.VOLATUS, 10));
        add(b, EntityType.WITHER, list(TCAspects.EXANIMIS, 50, TCAspects.PERDITIO, 25, TCAspects.IGNIS, 25));
        add(b, EntityType.WITCH, list(TCAspects.HUMANUS, 15, TCAspects.PRAECANTATIO, 5, TCAspects.ALKIMIA, 10));
        add(b, EntityType.VILLAGER, list(TCAspects.HUMANUS, 15));
        add(
                b,
                EntityType.IRON_GOLEM,
                list(TCAspects.METALLUM, 15, TCAspects.HUMANUS, 5, TCAspects.MACHINA, 5, TCAspects.PRAECANTATIO, 5));
        add(b, EntityType.END_CRYSTAL, list(TCAspects.ALIENIS, 15, TCAspects.AURAM, 15, TCAspects.VICTUS, 15));
        add(b, EntityType.ITEM_FRAME, list(TCAspects.SENSUS, 5, TCAspects.FABRICO, 5));
        add(b, EntityType.GLOW_ITEM_FRAME, list(TCAspects.SENSUS, 5, TCAspects.FABRICO, 5));
        add(b, EntityType.PAINTING, list(TCAspects.SENSUS, 10, TCAspects.FABRICO, 5));
        add(b, EntityType.GUARDIAN, list(TCAspects.BESTIA, 10, TCAspects.ALIENIS, 10, TCAspects.AQUA, 10));
        add(b, EntityType.ELDER_GUARDIAN, list(TCAspects.BESTIA, 10, TCAspects.ALIENIS, 15, TCAspects.AQUA, 15));
        add(b, EntityType.RABBIT, list(TCAspects.BESTIA, 5, TCAspects.TERRA, 5, TCAspects.MOTUS, 5));
        add(b, EntityType.ENDERMITE, list(TCAspects.BESTIA, 5, TCAspects.ALIENIS, 5, TCAspects.MOTUS, 5));
        add(b, EntityType.POLAR_BEAR, list(TCAspects.BESTIA, 15, TCAspects.GELUM, 10));
        add(
                b,
                EntityType.SHULKER,
                list(TCAspects.ALIENIS, 10, TCAspects.VINCULUM, 5, TCAspects.VOLATUS, 5, TCAspects.PRAEMUNIO, 5));
        add(b, EntityType.EVOKER, list(TCAspects.ALIENIS, 5, TCAspects.PRAECANTATIO, 5, TCAspects.HUMANUS, 10));
        add(b, EntityType.VINDICATOR, list(TCAspects.AVERSIO, 5, TCAspects.PRAECANTATIO, 5, TCAspects.HUMANUS, 10));
        add(b, EntityType.ILLUSIONER, list(TCAspects.SENSUS, 5, TCAspects.PRAECANTATIO, 5, TCAspects.HUMANUS, 10));
        add(b, EntityType.LLAMA, list(TCAspects.BESTIA, 15, TCAspects.AQUA, 5));
        add(b, EntityType.PARROT, list(TCAspects.BESTIA, 5, TCAspects.VOLATUS, 5, TCAspects.SENSUS, 5));
        add(b, EntityType.STRAY, list(TCAspects.EXANIMIS, 20, TCAspects.HUMANUS, 5, TCAspects.VINCULUM, 5));
        add(
                b,
                EntityType.VEX,
                list(TCAspects.ALIENIS, 5, TCAspects.VOLATUS, 5, TCAspects.PRAECANTATIO, 5, TCAspects.HUMANUS, 5));

        add(
                b,
                EntityType.DOLPHIN,
                list(TCAspects.BESTIA, 10, TCAspects.AQUA, 10, TCAspects.MOTUS, 5, TCAspects.COGNITIO, 5));
        add(b, EntityType.DROWNED, list(TCAspects.EXANIMIS, 20, TCAspects.HUMANUS, 10, TCAspects.AQUA, 5));
        add(b, EntityType.TURTLE, list(TCAspects.BESTIA, 10, TCAspects.AQUA, 5, TCAspects.PRAEMUNIO, 5));
        add(b, EntityType.COD, list(TCAspects.BESTIA, 5, TCAspects.AQUA, 5));
        add(b, EntityType.SALMON, list(TCAspects.BESTIA, 5, TCAspects.AQUA, 5));
        add(b, EntityType.TROPICAL_FISH, list(TCAspects.BESTIA, 5, TCAspects.AQUA, 5, TCAspects.SENSUS, 3));
        add(b, EntityType.PUFFERFISH, list(TCAspects.BESTIA, 5, TCAspects.AQUA, 5, TCAspects.AVERSIO, 3));
        add(
                b,
                EntityType.PHANTOM,
                list(TCAspects.EXANIMIS, 15, TCAspects.VOLATUS, 10, TCAspects.TENEBRAE, 5, TCAspects.SPIRITUS, 5));
        add(b, EntityType.CAT, list(TCAspects.BESTIA, 10, TCAspects.SENSUS, 5));
        add(b, EntityType.FOX, list(TCAspects.BESTIA, 10, TCAspects.SENSUS, 5, TCAspects.MOTUS, 3));
        add(b, EntityType.PANDA, list(TCAspects.BESTIA, 15, TCAspects.HERBA, 5));
        add(b, EntityType.PILLAGER, list(TCAspects.HUMANUS, 10, TCAspects.AVERSIO, 10));
        add(b, EntityType.RAVAGER, list(TCAspects.BESTIA, 20, TCAspects.AVERSIO, 15, TCAspects.TERRA, 5));
        add(b, EntityType.TRADER_LLAMA, list(TCAspects.BESTIA, 15, TCAspects.AQUA, 5));
        add(
                b,
                EntityType.WANDERING_TRADER,
                list(TCAspects.HUMANUS, 10, TCAspects.PERMUTATIO, 10, TCAspects.DESIDERIUM, 5));
        add(
                b,
                EntityType.BEE,
                list(TCAspects.BESTIA, 5, TCAspects.VOLATUS, 5, TCAspects.HERBA, 5, TCAspects.FABRICO, 2));
        add(b, EntityType.HOGLIN, list(TCAspects.BESTIA, 15, TCAspects.AVERSIO, 10, TCAspects.IGNIS, 5));
        add(b, EntityType.ZOGLIN, list(TCAspects.EXANIMIS, 15, TCAspects.BESTIA, 10, TCAspects.AVERSIO, 10));
        add(b, EntityType.PIGLIN, list(TCAspects.HUMANUS, 10, TCAspects.BESTIA, 5, TCAspects.DESIDERIUM, 10));
        add(b, EntityType.PIGLIN_BRUTE, list(TCAspects.HUMANUS, 10, TCAspects.BESTIA, 5, TCAspects.AVERSIO, 15));
        add(b, EntityType.STRIDER, list(TCAspects.BESTIA, 10, TCAspects.IGNIS, 10, TCAspects.MOTUS, 5));
        add(b, EntityType.AXOLOTL, list(TCAspects.BESTIA, 10, TCAspects.AQUA, 5, TCAspects.VICTUS, 5));
        add(b, EntityType.GLOW_SQUID, list(TCAspects.BESTIA, 5, TCAspects.AQUA, 5, TCAspects.LUX, 5));
        add(b, EntityType.GOAT, list(TCAspects.BESTIA, 10, TCAspects.TERRA, 5, TCAspects.MOTUS, 5));
        add(
                b,
                EntityType.ALLAY,
                list(TCAspects.SPIRITUS, 10, TCAspects.VOLATUS, 5, TCAspects.SENSUS, 5, TCAspects.DESIDERIUM, 3));
        add(b, EntityType.FROG, list(TCAspects.BESTIA, 8, TCAspects.AQUA, 4, TCAspects.MOTUS, 3));
        add(b, EntityType.TADPOLE, list(TCAspects.BESTIA, 4, TCAspects.AQUA, 4, TCAspects.VICTUS, 2));
        add(
                b,
                EntityType.WARDEN,
                list(
                        TCAspects.TENEBRAE,
                        20,
                        TCAspects.SENSUS,
                        15,
                        TCAspects.AVERSIO,
                        15,
                        TCAspects.TERRA,
                        10,
                        TCAspects.VITIUM,
                        5));
        add(b, EntityType.CAMEL, list(TCAspects.BESTIA, 12, TCAspects.TERRA, 5, TCAspects.MOTUS, 3));
        add(
                b,
                EntityType.SNIFFER,
                list(TCAspects.BESTIA, 15, TCAspects.HERBA, 10, TCAspects.SENSUS, 10, TCAspects.TERRA, 5));
        add(b, EntityType.ARMADILLO, list(TCAspects.BESTIA, 8, TCAspects.PRAEMUNIO, 5, TCAspects.TERRA, 3));
        add(b, EntityType.BOGGED, list(TCAspects.EXANIMIS, 20, TCAspects.HUMANUS, 5, TCAspects.HERBA, 5));
        add(
                b,
                EntityType.BREEZE,
                list(TCAspects.AER, 15, TCAspects.MOTUS, 10, TCAspects.POTENTIA, 5, TCAspects.PRAECANTATIO, 3));
        add(b, EntityType.ZOMBIE_VILLAGER, list(TCAspects.EXANIMIS, 20, TCAspects.HUMANUS, 15, TCAspects.TERRA, 5));

        add(
                b,
                TCEntities.THAUMIC_SLIME.get(),
                list(TCAspects.VICTUS, 5, TCAspects.AQUA, 5, TCAspects.VITIUM, 5, TCAspects.ALKIMIA, 5));
        add(b, TCEntities.TAINTACLE.get(), list(TCAspects.VITIUM, 15, TCAspects.BESTIA, 10));
        add(b, TCEntities.TAINT_SEED.get(), list(TCAspects.VITIUM, 20, TCAspects.AURAM, 10, TCAspects.HERBA, 5));
        add(b, TCEntities.TAINT_SEED_PRIME.get(), list(TCAspects.VITIUM, 25, TCAspects.AURAM, 15, TCAspects.HERBA, 5));
        add(b, TCEntities.TAINTACLE_SMALL.get(), list(TCAspects.VITIUM, 5, TCAspects.BESTIA, 5));
        add(b, TCEntities.TAINT_SWARM.get(), list(TCAspects.VITIUM, 15, TCAspects.AER, 5));
        add(b, TCEntities.FIRE_BAT.get(), list(TCAspects.BESTIA, 5, TCAspects.VOLATUS, 5, TCAspects.IGNIS, 10));
        add(b, TCEntities.MIND_SPIDER.get(), list(TCAspects.VITIUM, 5, TCAspects.IGNIS, 5));
        add(
                b,
                TCEntities.BRAINY_ZOMBIE.get(),
                list(TCAspects.EXANIMIS, 20, TCAspects.HUMANUS, 10, TCAspects.COGNITIO, 5, TCAspects.AVERSIO, 5));
        add(
                b,
                TCEntities.BRAINY_DROWNED.get(),
                list(TCAspects.EXANIMIS, 20, TCAspects.HUMANUS, 10, TCAspects.COGNITIO, 5, TCAspects.AQUA, 10));
        add(
                b,
                TCEntities.BRAINY_HUSK.get(),
                list(TCAspects.EXANIMIS, 20, TCAspects.HUMANUS, 10, TCAspects.COGNITIO, 5, TCAspects.TERRA, 10));
        add(
                b,
                TCEntities.GIANT_BRAINY_ZOMBIE.get(),
                list(TCAspects.EXANIMIS, 25, TCAspects.HUMANUS, 15, TCAspects.COGNITIO, 5, TCAspects.AVERSIO, 10));
        add(
                b,
                TCEntities.PECH.get(),
                list(TCAspects.HUMANUS, 10, TCAspects.AURAM, 5, TCAspects.PERMUTATIO, 10, TCAspects.DESIDERIUM, 5));
        add(
                b,
                TCEntities.ELDRITCH_GUARDIAN.get(),
                list(TCAspects.ALIENIS, 20, TCAspects.MORTUUS, 20, TCAspects.EXANIMIS, 20));
        add(
                b,
                TCEntities.CULTIST_KNIGHT.get(),
                list(TCAspects.ALIENIS, 5, TCAspects.HUMANUS, 15, TCAspects.AVERSIO, 5));
        add(
                b,
                TCEntities.CULTIST_CLERIC.get(),
                list(TCAspects.ALIENIS, 5, TCAspects.HUMANUS, 15, TCAspects.AVERSIO, 5));
        add(
                b,
                TCEntities.ELDRITCH_CRAB.get(),
                list(TCAspects.ALIENIS, 10, TCAspects.BESTIA, 10, TCAspects.VINCULUM, 10));
        add(
                b,
                TCEntities.CULTIST_LEADER.get(),
                list(
                        TCAspects.ALIENIS,
                        15,
                        TCAspects.HUMANUS,
                        25,
                        TCAspects.AVERSIO,
                        10,
                        TCAspects.PRAECANTATIO,
                        15,
                        TCAspects.COGNITIO,
                        10));
        add(
                b,
                TCEntities.ELDRITCH_GOLEM.get(),
                list(TCAspects.ALIENIS, 25, TCAspects.METALLUM, 20, TCAspects.MOTUS, 15));
        add(
                b,
                TCEntities.INHABITED_ZOMBIE.get(),
                list(TCAspects.EXANIMIS, 20, TCAspects.HUMANUS, 10, TCAspects.ALIENIS, 10, TCAspects.BESTIA, 10));
        add(b, TCEntities.TAINTACLE_GIANT.get(), list(TCAspects.VITIUM, 30, TCAspects.BESTIA, 20));
        add(
                b,
                TCEntities.THAUMATURGE_GOLEM.get(),
                list(TCAspects.MOTUS, 10, TCAspects.MACHINA, 10, TCAspects.PRAECANTATIO, 5));
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

    private AspectList list(
            ResourceKey<IAspect> a1, int n1, ResourceKey<IAspect> a2, int n2, ResourceKey<IAspect> a3, int n3) {
        return list(a1, n1, a2, n2).add(aspects.getOrThrow(a3), n3);
    }

    private AspectList list(
            ResourceKey<IAspect> a1,
            int n1,
            ResourceKey<IAspect> a2,
            int n2,
            ResourceKey<IAspect> a3,
            int n3,
            ResourceKey<IAspect> a4,
            int n4) {
        return list(a1, n1, a2, n2, a3, n3).add(aspects.getOrThrow(a4), n4);
    }

    private AspectList list(
            ResourceKey<IAspect> a1,
            int n1,
            ResourceKey<IAspect> a2,
            int n2,
            ResourceKey<IAspect> a3,
            int n3,
            ResourceKey<IAspect> a4,
            int n4,
            ResourceKey<IAspect> a5,
            int n5) {
        return list(a1, n1, a2, n2, a3, n3, a4, n4).add(aspects.getOrThrow(a5), n5);
    }
}
