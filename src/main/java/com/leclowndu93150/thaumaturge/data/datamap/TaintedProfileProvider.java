package com.leclowndu93150.thaumaturge.data.datamap;

import com.leclowndu93150.thaumaturge.api.entity.trait.MobTrait;
import com.leclowndu93150.thaumaturge.content.taint.entity.TaintedProfile;
import com.leclowndu93150.thaumaturge.registry.TTDataMaps;
import com.leclowndu93150.thaumaturge.registry.TTLootTables;
import com.leclowndu93150.thaumaturge.registry.TTMobTraits;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.neoforged.neoforge.common.data.DataMapProvider;

public final class TaintedProfileProvider extends DataMapProvider {
    private static final double FOLLOW_RANGE = 24.0;
    private static final double LIVESTOCK_ARMOR = 2.0;

    public TaintedProfileProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    public String getName() {
        return "Tainted Profile Data Maps";
    }

    @Override
    protected void gather(HolderLookup.Provider provider) {
        Builder<TaintedProfile, EntityType<?>> b = builder(TTDataMaps.TAINTED_PROFILE);
        add(
                b,
                EntityType.COW,
                new TaintedProfile(
                        stats(40.0, 6.0, 0.27, 0.0),
                        true,
                        true,
                        List.<Holder<MobTrait>>of(TTMobTraits.FLUX_MILK),
                        true,
                        Optional.of(TTLootTables.TAINTED_COW)));
        add(
                b,
                EntityType.PIG,
                new TaintedProfile(
                        stats(20.0, 4.0, 0.275, LIVESTOCK_ARMOR),
                        true,
                        true,
                        List.<Holder<MobTrait>>of(TTMobTraits.SWARM_BURST),
                        true,
                        Optional.of(TTLootTables.TAINTED_PIG)));
        add(
                b,
                EntityType.CHICKEN,
                new TaintedProfile(
                        stats(8.0, 3.0, 0.4, LIVESTOCK_ARMOR),
                        true,
                        true,
                        List.<Holder<MobTrait>>of(TTMobTraits.LEAPING, TTMobTraits.TAINT_LAYING),
                        true,
                        Optional.of(TTLootTables.TAINTED_CHICKEN)));
        add(
                b,
                EntityType.SHEEP,
                new TaintedProfile(
                        stats(20.0, 3.0, 0.25, LIVESTOCK_ARMOR),
                        true,
                        false,
                        List.<Holder<MobTrait>>of(TTMobTraits.TAINT_GRAZING, TTMobTraits.TAINT_SHEARING),
                        true,
                        Optional.of(TTLootTables.TAINTED_SHEEP)));
        add(
                b,
                EntityType.VILLAGER,
                new TaintedProfile(
                        stats(30.0, 4.0, 0.3, 0.0),
                        false,
                        false,
                        List.of(),
                        true,
                        Optional.of(TTLootTables.TAINTED_VILLAGER)));
        Map<Holder<Attribute>, Double> creeper = new LinkedHashMap<>();
        creeper.put(Attributes.MAX_HEALTH, 24.0);
        creeper.put(Attributes.MOVEMENT_SPEED, 0.28);
        creeper.put(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
        add(
                b,
                EntityType.CREEPER,
                new TaintedProfile(
                        creeper,
                        false,
                        false,
                        List.<Holder<MobTrait>>of(TTMobTraits.TAINT_BLAST),
                        false,
                        Optional.of(TTLootTables.TAINTED_CREEPER)));
        add(b, EntityType.ZOMBIE, hostile(TTMobTraits.WARPING_STRIKE, true));
        add(b, EntityType.SKELETON, hostile(TTMobTraits.TAINT_ARROWS, true));
        add(b, EntityType.PILLAGER, hostile(TTMobTraits.TAINT_ARROWS, false));
        add(b, EntityType.WITCH, hostile(TTMobTraits.TAINT_BREWING, true));
        add(b, EntityType.PHANTOM, hostile(TTMobTraits.LIFTING_STRIKE, true));
        add(b, EntityType.ENDERMAN, hostile(TTMobTraits.TAINTACLE_LURE, true));
    }

    private static TaintedProfile hostile(Holder<MobTrait> trait, boolean naturalSpawns) {
        return new TaintedProfile(Map.of(), false, false, List.of(trait), naturalSpawns, Optional.empty());
    }

    private static Map<Holder<Attribute>, Double> stats(double health, double attack, double speed, double armor) {
        Map<Holder<Attribute>, Double> stats = new LinkedHashMap<>();
        stats.put(Attributes.MAX_HEALTH, health);
        stats.put(Attributes.ATTACK_DAMAGE, attack);
        stats.put(Attributes.MOVEMENT_SPEED, speed);
        if (armor > 0.0) {
            stats.put(Attributes.ARMOR, armor);
        }
        stats.put(Attributes.FOLLOW_RANGE, FOLLOW_RANGE);
        return stats;
    }

    private static void add(
            Builder<TaintedProfile, EntityType<?>> builder, EntityType<?> type, TaintedProfile profile) {
        builder.add(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type), profile, false);
    }
}
