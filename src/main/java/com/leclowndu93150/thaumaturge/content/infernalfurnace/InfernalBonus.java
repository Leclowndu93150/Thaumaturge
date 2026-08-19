package com.leclowndu93150.thaumaturge.content.infernalfurnace;

import com.leclowndu93150.thaumaturge.TCIds;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.IntProvider;
import net.minecraft.util.valueproviders.IntProviders;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.AdvancedDataMapType;
import net.neoforged.neoforge.registries.datamaps.DataMapValueRemover;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public record InfernalBonus(HolderSet<Item> items, IntProvider count, float chance) {

    private static final Codec<InfernalBonus> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(ExtraCodecs.nonEmptyHolderSet(Ingredient.NON_AIR_HOLDER_SET_CODEC).fieldOf("items").forGetter(InfernalBonus::items),
                    IntProviders.codec(1, 64).optionalFieldOf("count", new ConstantInt(1)).forGetter(InfernalBonus::count),
                    Codec.floatRange(0, 1).optionalFieldOf("chance", 1.0f).forGetter(InfernalBonus::chance)).apply(instance, InfernalBonus::new));

    public static final AdvancedDataMapType<Item, List<InfernalBonus>, Remover> DATA_MAP = AdvancedDataMapType
            .builder(Identifier.fromNamespaceAndPath(TCIds.MODID, "infernal_bonus"), Registries.ITEM, CODEC.listOf(1, 64)).merger((_, _, fv, _, sv) -> Stream.concat(fv.stream(), sv.stream()).toList())
            .remover(Remover.CODEC).synced(CODEC.listOf(1, 64), false).build();

    @SubscribeEvent
    public static void onRegister(RegisterDataMapTypesEvent event) {
        event.register(DATA_MAP);
    }

    public static Builder builder(ItemLike item) {
        return new Builder(HolderSet.direct(item.asItem().builtInRegistryHolder()));
    }

    public static Builder builder(HolderLookup<Item> getter, TagKey<Item> tag) {
        return new Builder(getter.getOrThrow(tag));
    }

    public static final class Builder {

        private final HolderSet<Item> items;
        private IntProvider count = new ConstantInt(1);
        private float chance = 1.0F;

        private Builder(HolderSet<Item> items) {
            this.items = items;
        }

        public Builder count(IntProvider count) {
            this.count = count;
            return this;
        }

        public Builder count(int count) {
            this.count = new ConstantInt(count);
            return this;
        }

        public Builder chance(float chance) {
            this.chance = chance;
            return this;
        }

        public InfernalBonus build() {
            return new InfernalBonus(items, count, chance);
        }
    }

    public record Remover(Item key) implements DataMapValueRemover<Item, List<InfernalBonus>> {
        public static final Codec<Remover> CODEC = BuiltInRegistries.ITEM.byNameCodec().xmap(Remover::new, Remover::key);

        @Override
        public Optional<List<InfernalBonus>> remove(List<InfernalBonus> value, Registry<Item> registry, Either<TagKey<Item>, ResourceKey<Item>> source, Item object) {
            List<InfernalBonus> result = new ArrayList<>(value);
            result.removeIf(bonus -> bonus.items().contains(key.builtInRegistryHolder()));
            return result.isEmpty() ? Optional.empty() : Optional.of(result);
        }
    }
}
