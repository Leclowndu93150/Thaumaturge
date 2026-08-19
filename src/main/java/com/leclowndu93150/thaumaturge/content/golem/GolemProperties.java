package com.leclowndu93150.thaumaturge.content.golem;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemProperties;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemAddon;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemArm;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemComponent;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemHead;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.registry.TCGolemParts;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

public final class GolemProperties implements IGolemProperties {
    public static final Codec<GolemProperties> CODEC = RecordCodecBuilder
            .create(instance -> instance.group(TCGolemParts.materials().byNameCodec().fieldOf("material").forGetter(GolemProperties::getMaterial),
                    TCGolemParts.heads().byNameCodec().fieldOf("head").forGetter(GolemProperties::getHead), TCGolemParts.arms().byNameCodec().fieldOf("arms").forGetter(GolemProperties::getArms),
                    TCGolemParts.legs().byNameCodec().fieldOf("legs").forGetter(GolemProperties::getLegs), TCGolemParts.addons().byNameCodec().fieldOf("addon").forGetter(GolemProperties::getAddon),
                    Codec.intRange(0, 10).optionalFieldOf("rank", 0).forGetter(GolemProperties::getRank)).apply(instance, GolemProperties::new));

    public static final StreamCodec<ByteBuf, GolemProperties> STREAM_CODEC = StreamCodec.composite(registryStream(TCGolemParts::materials), GolemProperties::getMaterial,
            registryStream(TCGolemParts::heads), GolemProperties::getHead, registryStream(TCGolemParts::arms), GolemProperties::getArms, registryStream(TCGolemParts::legs), GolemProperties::getLegs,
            registryStream(TCGolemParts::addons), GolemProperties::getAddon, ByteBufCodecs.VAR_INT, GolemProperties::getRank, GolemProperties::new);

    private GolemMaterial material;
    private GolemHead head;
    private GolemArm arms;
    private GolemLeg legs;
    private GolemAddon addon;
    private int rank;
    private Set<GolemTrait> traitCache;

    public GolemProperties(GolemMaterial material, GolemHead head, GolemArm arms, GolemLeg legs, GolemAddon addon, int rank) {
        this.material = material;
        this.head = head;
        this.arms = arms;
        this.legs = legs;
        this.addon = addon;
        this.rank = rank;
    }

    public static GolemProperties createDefault() {
        return new GolemProperties(TCGolemParts.WOOD.get(), TCGolemParts.HEAD_BASIC.get(), TCGolemParts.ARMS_BASIC.get(), TCGolemParts.LEGS_WALKER.get(), TCGolemParts.ADDON_NONE.get(), 0);
    }

    public GolemProperties copy() {
        return new GolemProperties(material, head, arms, legs, addon, rank);
    }

    private static <T> StreamCodec<ByteBuf, T> registryStream(Supplier<Registry<T>> registry) {
        return Identifier.STREAM_CODEC.map(id -> registry.get().getValue(id), value -> registry.get().getKey(value));
    }

    @Override
    public Set<GolemTrait> getTraits() {
        if (traitCache == null) {
            traitCache = new LinkedHashSet<>();
            material.traits().forEach(this::addTraitSmart);
            head.traits().forEach(this::addTraitSmart);
            arms.traits().forEach(this::addTraitSmart);
            legs.traits().forEach(this::addTraitSmart);
            addon.traits().forEach(this::addTraitSmart);
        }
        return traitCache;
    }

    private void addTraitSmart(Holder<GolemTrait> trait) {
        GolemTrait value = trait.value();
        GolemTrait opposite = value.opposite() == null ? null : TCGolemTraits.registry().getValue(value.opposite());
        if (opposite != null && traitCache.contains(opposite)) {
            traitCache.remove(opposite);
        } else {
            traitCache.add(value);
        }
    }

    @Override
    public boolean hasTrait(GolemTrait trait) {
        return getTraits().contains(trait);
    }

    @Override
    public List<ItemStack> generateComponents() {
        List<ItemStack> components = new ArrayList<>();
        addToList(components, material.base(), 2);
        addToList(components, material.mechanism(), 1);
        addPartComponents(components, arms.components());
        addPartComponents(components, legs.components());
        addPartComponents(components, head.components());
        addPartComponents(components, addon.components());
        return components;
    }

    private void addPartComponents(List<ItemStack> components, List<GolemComponent> parts) {
        for (GolemComponent component : parts) {
            addToList(components, component.resolve(material), 1);
        }
    }

    private static void addToList(List<ItemStack> components, ItemStack newItem, int multiplier) {
        for (ItemStack stack : components) {
            if (ItemStack.isSameItemSameComponents(stack, newItem)) {
                stack.grow(newItem.getCount() * multiplier);
                return;
            }
        }
        ItemStack copy = newItem.copy();
        copy.setCount(copy.getCount() * multiplier);
        components.add(copy);
    }

    @Override
    public void setMaterial(GolemMaterial material) {
        this.material = material;
        traitCache = null;
    }

    @Override
    public GolemMaterial getMaterial() {
        return material;
    }

    @Override
    public void setHead(GolemHead head) {
        this.head = head;
        traitCache = null;
    }

    @Override
    public GolemHead getHead() {
        return head;
    }

    @Override
    public void setArms(GolemArm arms) {
        this.arms = arms;
        traitCache = null;
    }

    @Override
    public GolemArm getArms() {
        return arms;
    }

    @Override
    public void setLegs(GolemLeg legs) {
        this.legs = legs;
        traitCache = null;
    }

    @Override
    public GolemLeg getLegs() {
        return legs;
    }

    @Override
    public void setAddon(GolemAddon addon) {
        this.addon = addon;
        traitCache = null;
    }

    @Override
    public GolemAddon getAddon() {
        return addon;
    }

    @Override
    public void setRank(int rank) {
        this.rank = rank;
    }

    @Override
    public int getRank() {
        return rank;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GolemProperties props && props.material == material && props.head == head && props.arms == arms && props.legs == legs && props.addon == addon && props.rank == rank;
    }

    @Override
    public int hashCode() {
        int hash = material.hashCode();
        hash = 31 * hash + head.hashCode();
        hash = 31 * hash + arms.hashCode();
        hash = 31 * hash + legs.hashCode();
        hash = 31 * hash + addon.hashCode();
        return 31 * hash + rank;
    }
}
