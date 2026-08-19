package com.leclowndu93150.thaumaturge.api.golems.parts;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;

/**
 * A material a golem can be assembled from. Materials contribute health, armor and melee
 * damage, the golem's body texture, and the base and mechanism items consumed when parts
 * declare material placeholders.
 *
 * @since 1.0.0
 */
public final class GolemMaterial {
    /** The registry key for golem materials. */
    public static final ResourceKey<Registry<GolemMaterial>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "golem_material"));

    private final List<Identifier> research;
    private final Identifier texture;
    private final int itemColor;
    private final int healthMod;
    private final int armor;
    private final int damage;
    private final Supplier<ItemStack> componentBase;
    private final Supplier<ItemStack> componentMechanism;
    private final List<Holder<GolemTrait>> traits;

    /**
     * @param research           research entries gating this material in the golem press;
     *                           empty means always available
     * @param texture            the body texture rendered on golems of this material
     * @param itemColor          the {@code 0xRRGGBB} tint applied to golem placer items
     * @param healthMod          health added to the golem's base of 10
     * @param armor              the golem's armor rating
     * @param damage             the golem's base melee damage when it can fight
     * @param componentBase      supplies the material's base crafting item
     * @param componentMechanism supplies the material's mechanism crafting item
     * @param traits             traits granted by the material
     */
    public GolemMaterial(List<Identifier> research, Identifier texture, int itemColor, int healthMod, int armor, int damage, Supplier<ItemStack> componentBase, Supplier<ItemStack> componentMechanism, List<Holder<GolemTrait>> traits) {
        this.research = List.copyOf(research);
        this.texture = texture;
        this.itemColor = itemColor;
        this.healthMod = healthMod;
        this.armor = armor;
        this.damage = damage;
        this.componentBase = componentBase;
        this.componentMechanism = componentMechanism;
        this.traits = List.copyOf(traits);
    }

    /**
     * @return research entries gating this material; empty means ungated
     */
    public List<Identifier> research() {
        return research;
    }

    /**
     * @return the body texture for golems of this material
     */
    public Identifier texture() {
        return texture;
    }

    /**
     * @return the {@code 0xRRGGBB} item tint
     */
    public int itemColor() {
        return itemColor;
    }

    /**
     * @return health added to the golem's base of 10
     */
    public int healthMod() {
        return healthMod;
    }

    /**
     * @return the golem's armor rating
     */
    public int armor() {
        return armor;
    }

    /**
     * @return the golem's base melee damage
     */
    public int damage() {
        return damage;
    }

    /**
     * @return a fresh copy of the material's base crafting item
     */
    public ItemStack base() {
        return componentBase.get().copy();
    }

    /**
     * @return a fresh copy of the material's mechanism crafting item
     */
    public ItemStack mechanism() {
        return componentMechanism.get().copy();
    }

    /**
     * @return traits granted by the material
     */
    public List<Holder<GolemTrait>> traits() {
        return traits;
    }

    /**
     * The translation key for a material's display name in golem UIs.
     *
     * @param id the material id
     * @return {@code golem.material.<namespace>.<path>}
     */
    public static String nameKey(Identifier id) {
        return "golem.material." + id.getNamespace() + "." + id.getPath();
    }

    /**
     * The translation key for a material's descriptive text in golem UIs.
     *
     * @param id the material id
     * @return {@code golem.material.text.<namespace>.<path>}
     */
    public static String descriptionKey(Identifier id) {
        return "golem.material.text." + id.getNamespace() + "." + id.getPath();
    }
}
