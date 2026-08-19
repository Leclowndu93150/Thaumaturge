package com.leclowndu93150.thaumaturge.api.wands;

import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

/**
 * A wand rod (or staff core) type. Rods determine how much primal vis a wand can store, and
 * optionally self-charge through an {@link IWandRodOnUpdate} callback. Staff cores are rods
 * flagged as staves: they hold more vis and cast, but cannot be used for arcane crafting.
 *
 * @since 1.0.0
 */
public final class WandRod {
    /** The registry key for wand rods. */
    public static final ResourceKey<Registry<WandRod>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "wand_rod"));

    private final int capacity;
    private final int craftCost;
    private final Identifier texture;
    private final @Nullable IWandRodOnUpdate onUpdate;
    private final boolean glow;
    private final boolean staff;
    private final boolean runes;
    private final @Nullable Identifier assemblyResearch;

    /**
     * @param capacity  vis stored per primal pool, in whole vis
     * @param craftCost the crafting cost factor of this rod, multiplied with the cap's factor
     *                  to price wand assembly
     * @param texture   the texture rendered on wand models built with this rod
     * @param onUpdate  inventory tick callback, or null for none
     * @param glow      whether wand models built with this rod render fullbright
     * @param staff     whether this rod is a staff core
     * @param runes     whether this rod bears runes, granting a free potency level to any
     *                  socketed focus
     */
    public WandRod(int capacity, int craftCost, Identifier texture, @Nullable IWandRodOnUpdate onUpdate, boolean glow, boolean staff, boolean runes) {
        this(capacity, craftCost, texture, onUpdate, glow, staff, runes, null);
    }

    /**
     * @param capacity         vis stored per primal pool, in whole vis
     * @param craftCost        the crafting cost factor of this rod
     * @param texture          the texture rendered on wand models built with this rod
     * @param onUpdate         inventory tick callback, or null for none
     * @param glow             whether wand models built with this rod render fullbright
     * @param staff            whether this rod is a staff core
     * @param runes            whether this rod bears runes
     * @param assemblyResearch the research entry gating wand assembly recipes built around
     *                         this rod, or null for the base auromancy gate
     */
    public WandRod(int capacity, int craftCost, Identifier texture, @Nullable IWandRodOnUpdate onUpdate, boolean glow, boolean staff, boolean runes, @Nullable Identifier assemblyResearch) {
        this.capacity = capacity;
        this.craftCost = craftCost;
        this.texture = texture;
        this.onUpdate = onUpdate;
        this.glow = glow;
        this.staff = staff;
        this.runes = runes;
        this.assemblyResearch = assemblyResearch;
    }

    /**
     * The research entry gating assembly recipes that use this rod.
     *
     * @return the research entry id, or null to gate on base auromancy
     */
    public @Nullable Identifier assemblyResearch() {
        return assemblyResearch;
    }

    /**
     * @return vis stored per primal pool, in whole vis
     */
    public int capacity() {
        return capacity;
    }

    /**
     * @return the crafting cost factor used to price wand assembly recipes
     */
    public int craftCost() {
        return craftCost;
    }

    /**
     * @return the texture rendered on wand models built with this rod
     */
    public Identifier texture() {
        return texture;
    }

    /**
     * @return the inventory tick callback, or null when this rod does not self-charge
     */
    public @Nullable IWandRodOnUpdate onUpdate() {
        return onUpdate;
    }

    /**
     * @return whether wand models built with this rod render fullbright
     */
    public boolean glow() {
        return glow;
    }

    /**
     * @return whether this rod is a staff core
     */
    public boolean staff() {
        return staff;
    }

    /**
     * @return whether this rod bears runes
     */
    public boolean runes() {
        return runes;
    }
}
