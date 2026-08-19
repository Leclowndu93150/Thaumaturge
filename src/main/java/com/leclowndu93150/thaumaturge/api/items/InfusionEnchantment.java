package com.leclowndu93150.thaumaturge.api.items;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import java.util.Set;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * An enchantment applied through infusion crafting rather than the vanilla enchanting table.
 *
 * <p>Each value declares the tool classes it may be applied to and the maximum level it may
 * reach. Levels are stored per stack in the {@code thaumaturge:infusion_enchantments} data
 * component. Applicability is tested against an item's tool classes, weapon status, armor slot,
 * or {@link IRechargable} nature.
 *
 * @since 1.0.0
 */
public enum InfusionEnchantment implements StringRepresentable {
    /** Draws harvested drops and combat drops toward the player. */
    COLLECTOR("collector", Set.of("axe", "pickaxe", "shovel", "weapon"), 1),
    /** Breaks a plane of blocks around the mined block. */
    DESTRUCTIVE("destructive", Set.of("axe", "pickaxe", "shovel"), 1),
    /** Follows a vein of logs or ore, breaking the furthest connected block. */
    BURROWING("burrowing", Set.of("axe", "pickaxe"), 1),
    /** Pings nearby ore on a sneak right-click. */
    SOUNDING("sounding", Set.of("pickaxe"), 4),
    /** Chance to upgrade a drop to a more valuable form, scaling with level. */
    REFINING("refining", Set.of("pickaxe"), 4),
    /** Chains a fraction of the strike to nearby enemies. */
    ARCING("arcing", Set.of("weapon"), 4),
    /** Drops aspect crystals distilled from slain creatures. */
    ESSENCE("essence", Set.of("weapon"), 5),
    /** Increases the vis capacity of a chargeable item. */
    VISBATTERY("visbattery", Set.of("chargable"), 3),
    /** Increases the recharge rate of a chargeable item. */
    VISCHARGE("vischarge", Set.of("chargable"), 1),
    /** Boots movement enchant. */
    SWIFT("swift", Set.of("boots"), 4),
    /** Leggings movement enchant. */
    AGILE("agile", Set.of("legs"), 1),
    /** Chestplate taint enchant. */
    INFESTED("infested", Set.of("chest"), 1),
    /** Places a glimmer light in darkness where a block was mined. */
    LAMPLIGHT("lamplight", Set.of("axe", "pickaxe", "shovel"), 1);

    /** Codec for datapack and component serialization. */
    public static final Codec<InfusionEnchantment> CODEC = StringRepresentable.fromEnum(InfusionEnchantment::values);

    /** Network codec for payload and component sync. */
    public static final StreamCodec<ByteBuf, InfusionEnchantment> STREAM_CODEC = ByteBufCodecs.idMapper(i -> values()[i], InfusionEnchantment::ordinal);

    private final String name;
    private final Set<String> toolClasses;
    private final int maxLevel;

    InfusionEnchantment(String name, Set<String> toolClasses, int maxLevel) {
        this.name = name;
        this.toolClasses = toolClasses;
        this.maxLevel = maxLevel;
    }

    /**
     * Stable lowercase name used as the JSON enum value.
     *
     * @return the lowercase name
     */
    @Override
    public String getSerializedName() {
        return name;
    }

    /**
     * The set of tool-class tokens this enchantment may be applied to.
     *
     * @return an unmodifiable set of tool-class tokens
     */
    public Set<String> toolClasses() {
        return toolClasses;
    }

    /**
     * The highest level this enchantment may reach.
     *
     * @return the maximum level, always positive
     */
    public int maxLevel() {
        return maxLevel;
    }
}
