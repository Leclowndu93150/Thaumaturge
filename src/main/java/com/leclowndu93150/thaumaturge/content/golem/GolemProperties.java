package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemProperties;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemAddon;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemArm;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemComponent;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemHead;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPart;
import com.leclowndu93150.thaumaturge.registry.TTGolemParts;
import com.leclowndu93150.thaumaturge.registry.TTGolemTraits;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class GolemProperties implements IGolemProperties {
    private static final int MAX_RANK = 10;
    private static final int BASE_SHARE = 2;

    public static final Codec<GolemProperties> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    TTGolemParts.materials().byNameCodec().fieldOf("material").forGetter(GolemProperties::material),
                    TTGolemParts.heads().byNameCodec().fieldOf("head").forGetter(GolemProperties::head),
                    TTGolemParts.arms().byNameCodec().fieldOf("arms").forGetter(GolemProperties::arms),
                    TTGolemParts.legs().byNameCodec().fieldOf("legs").forGetter(GolemProperties::legs),
                    TTGolemParts.addons().byNameCodec().fieldOf("addon").forGetter(GolemProperties::addon),
                    Codec.intRange(0, MAX_RANK).optionalFieldOf("rank", 0).forGetter(GolemProperties::rank))
            .apply(instance, GolemProperties::new));
    public static final StreamCodec<ByteBuf, GolemProperties> STREAM_CODEC = StreamCodec.composite(
            byId(TTGolemParts::materials),
            GolemProperties::material,
            byId(TTGolemParts::heads),
            GolemProperties::head,
            byId(TTGolemParts::arms),
            GolemProperties::arms,
            byId(TTGolemParts::legs),
            GolemProperties::legs,
            byId(TTGolemParts::addons),
            GolemProperties::addon,
            ByteBufCodecs.VAR_INT.map(rank -> Mth.clamp(rank, 0, MAX_RANK), rank -> rank),
            GolemProperties::rank,
            GolemProperties::new);

    private final GolemMaterial material;
    private final GolemHead head;
    private final GolemArm arms;
    private final GolemLeg legs;
    private final GolemAddon addon;
    private final int rank;
    private @Nullable Set<GolemTrait> traits;

    public GolemProperties(
            GolemMaterial material, GolemHead head, GolemArm arms, GolemLeg legs, GolemAddon addon, int rank) {
        this.material = material;
        this.head = head;
        this.arms = arms;
        this.legs = legs;
        this.addon = addon;
        this.rank = rank;
    }

    public static GolemProperties createDefault() {
        return new GolemProperties(
                TTGolemParts.WOOD.get(),
                TTGolemParts.HEAD_BASIC.get(),
                TTGolemParts.ARMS_BASIC.get(),
                TTGolemParts.LEGS_WALKER.get(),
                TTGolemParts.ADDON_NONE.get(),
                0);
    }

    public static GolemProperties of(IGolemProperties build) {
        return build instanceof GolemProperties own
                ? own
                : new GolemProperties(
                        build.material(), build.head(), build.arms(), build.legs(), build.addon(), build.rank());
    }

    private static <T> StreamCodec<ByteBuf, T> byId(Supplier<Registry<T>> registry) {
        return ResourceLocation.STREAM_CODEC.map(
                id -> {
                    T value = registry.get().get(id);
                    if (value == null) {
                        throw new DecoderException("Unknown golem part " + id);
                    }
                    return value;
                },
                value -> registry.get().getKey(value));
    }

    public boolean isKnownBy(IPlayerKnowledge knowledge) {
        return Stream.of(material.research(), head.research(), arms.research(), legs.research(), addon.research())
                .flatMap(List::stream)
                .allMatch(knowledge::isResearchComplete);
    }

    @Override
    public Set<GolemTrait> traits() {
        if (traits == null) {
            traits = Collections.unmodifiableSet(resolveTraits());
        }
        return traits;
    }

    private Set<GolemTrait> resolveTraits() {
        Set<GolemTrait> resolved = new LinkedHashSet<>();
        Stream.of(material.traits(), head.traits(), arms.traits(), legs.traits(), addon.traits())
                .flatMap(List::stream)
                .map(Holder::value)
                .forEach(trait -> {
                    GolemTrait opposite = trait.opposite() == null
                            ? null
                            : TTGolemTraits.registry().get(trait.opposite());
                    if (opposite != null && resolved.remove(opposite)) {
                        return;
                    }
                    resolved.add(trait);
                });
        return resolved;
    }

    @Override
    public List<ItemStack> components() {
        List<ItemStack> bill = new ArrayList<>();
        merge(bill, material.base(), BASE_SHARE);
        merge(bill, material.mechanism(), 1);
        for (GolemPart part : List.of(arms, legs, head, addon)) {
            for (GolemComponent component : part.components()) {
                merge(bill, component.resolve(material), 1);
            }
        }
        return bill;
    }

    private static void merge(List<ItemStack> bill, ItemStack item, int times) {
        for (ItemStack line : bill) {
            if (ItemStack.isSameItemSameComponents(line, item)) {
                line.grow(item.getCount() * times);
                return;
            }
        }
        bill.add(item.copyWithCount(item.getCount() * times));
    }

    @Override
    public GolemMaterial material() {
        return material;
    }

    @Override
    public GolemHead head() {
        return head;
    }

    @Override
    public GolemArm arms() {
        return arms;
    }

    @Override
    public GolemLeg legs() {
        return legs;
    }

    @Override
    public GolemAddon addon() {
        return addon;
    }

    @Override
    public int rank() {
        return rank;
    }

    @Override
    public GolemProperties withMaterial(GolemMaterial material) {
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    @Override
    public GolemProperties withHead(GolemHead head) {
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    @Override
    public GolemProperties withArms(GolemArm arms) {
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    @Override
    public GolemProperties withLegs(GolemLeg legs) {
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    @Override
    public GolemProperties withAddon(GolemAddon addon) {
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    @Override
    public GolemProperties withRank(int rank) {
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GolemProperties build
                && build.material == material
                && build.head == head
                && build.arms == arms
                && build.legs == legs
                && build.addon == addon
                && build.rank == rank;
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, head, arms, legs, addon, rank);
    }
}
