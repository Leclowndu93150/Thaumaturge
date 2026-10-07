package com.leclowndu93150.thaumaturge.api.spell;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AspectAffinity;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellContinuation;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellTarget;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPartKind;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Static facade of the spell system: reading and writing spells on foci, looking up parts and
 * affinities, analysing costs and casting.
 *
 * @apiNote {@link #bind} is called once by Thaumaturge during mod construction; addons never call it.
 * @since 1.0.0
 */
public final class Spells {
    private static final int WHITE = 0xFFFFFF;

    private static Bindings binding;

    private Spells() {}

    /**
     * Binds the implementation.
     *
     * @param bindings the implementation
     * @throws IllegalStateException when already bound
     */
    public static void bind(Bindings bindings) {
        if (binding != null) {
            throw new IllegalStateException("Spells already bound");
        }
        binding = bindings;
    }

    /**
     * The spell inscribed on a focus.
     *
     * @param focus the focus stack
     * @return the spell, or null when the stack carries none
     */
    public static @Nullable Spell spellOf(ItemStack focus) {
        return impl().spellOf(focus);
    }

    /**
     * Inscribes or clears the spell of a focus.
     *
     * @param focus the focus stack, mutated
     * @param spell the spell, or null to clear
     */
    public static void setSpell(ItemStack focus, @Nullable Spell spell) {
        impl().setSpell(focus, spell);
    }

    /**
     * The limits a focus item imposes.
     *
     * @param focus the focus stack
     * @return the tier, empty when the item is not a focus
     */
    public static Optional<FocusTier> tierOf(ItemStack focus) {
        return Optional.ofNullable(focus.typeHolder().getData(FocusTier.DATA_MAP));
    }

    /**
     * Looks a part up.
     *
     * @param registries the registry lookup
     * @param key        the part key
     * @return the part, empty when no datapack defines it
     */
    public static Optional<SpellPart> part(HolderLookup.Provider registries, ResourceKey<SpellPart> key) {
        return registries.lookup(SpellPart.REGISTRY_KEY).flatMap(lookup -> lookup.get(key)).map(Holder.Reference::value);
    }

    /**
     * Looks the affinity of an aspect up.
     *
     * @param registries the registry lookup
     * @param aspect     the aspect key
     * @return the affinity, empty when the aspect has no spell reactions
     */
    public static Optional<AspectAffinity> affinity(HolderLookup.Provider registries, ResourceKey<IAspect> aspect) {
        return registries.lookup(AspectAffinity.REGISTRY_KEY).flatMap(lookup -> lookup.get(ResourceKey.create(AspectAffinity.REGISTRY_KEY, aspect.identifier()))).map(Holder.Reference::value);
    }

    /**
     * The tint of a node: its aspect's colour when it has an aspect, else its part's colour.
     *
     * @param registries the registry lookup
     * @param node       the node
     * @return the packed {@code 0xRRGGBB} colour, white for unknown parts
     */
    public static int color(HolderLookup.Provider registries, SpellNode node) {
        Optional<SpellPart> part = part(registries, node.part());
        if (part.isEmpty()) {
            return WHITE;
        }
        Optional<ResourceKey<IAspect>> aspect = part.get().aspect().resolve(node.aspect(), registries);
        if (aspect.isPresent() && part.get().aspect().selectable()) {
            Optional<Holder.Reference<IAspect>> holder = registries.lookup(IAspect.REGISTRY_KEY).flatMap(lookup -> lookup.get(aspect.get()));
            if (holder.isPresent()) {
                return holder.get().value().color() & WHITE;
            }
        }
        return part.get().color() & WHITE;
    }

    /**
     * The tint of a whole spell: the average of its effect nodes' colours.
     *
     * @param registries the registry lookup
     * @param spell      the spell
     * @return the packed {@code 0xRRGGBB} colour, white when the spell has no effects
     */
    public static int color(HolderLookup.Provider registries, Spell spell) {
        int r = 0;
        int g = 0;
        int b = 0;
        int count = 0;
        for (SpellNode node : spell.nodes()) {
            Optional<SpellPart> part = part(registries, node.part());
            if (part.isPresent() && part.get().kind() == SpellPartKind.EFFECT) {
                int color = color(registries, node);
                r += (color >> 16) & 0xFF;
                g += (color >> 8) & 0xFF;
                b += color & 0xFF;
                count++;
            }
        }
        if (count == 0) {
            return WHITE;
        }
        return (r / count) << 16 | (g / count) << 8 | b / count;
    }

    /**
     * Computes costs and validates a spell.
     *
     * @param spell      the spell
     * @param tier       the focus limits, or null to skip the budget checks
     * @param registries the registry lookup
     * @param player     the player whose research and discoveries gate parts and aspects, or null to skip those checks
     * @return the summary
     */
    public static SpellSummary analyze(Spell spell, @Nullable FocusTier tier, HolderLookup.Provider registries, @Nullable Player player) {
        return impl().analyze(spell, tier, registries, player);
    }

    /**
     * Casts a spell from a mob or a script, along the caster's look. No vis is paid and no cooldown
     * starts; {@code SpellCastEvent} still fires. Server side only.
     *
     * @param caster the casting entity
     * @param spell  the spell
     * @param power  the starting power multiplier
     */
    public static void cast(LivingEntity caster, Spell spell, float power) {
        impl().cast(caster, ItemStack.EMPTY, spell, List.of(SpellTarget.origin(caster)), power);
    }

    /**
     * Casts a spell from explicit origins. Server side only.
     *
     * @param caster the casting entity
     * @param focus  the focus the spell comes from, empty for mob casts
     * @param spell  the spell
     * @param origin the targets handed to the origin's children
     * @param power  the starting power multiplier
     */
    public static void cast(LivingEntity caster, ItemStack focus, Spell spell, List<SpellTarget> origin, float power) {
        impl().cast(caster, focus, spell, origin, power);
    }

    /**
     * Resumes a spell held by a carrier. Server side only.
     *
     * @param level        the level
     * @param continuation the rest of the spell
     * @param targets      the targets the carrier reached
     */
    public static void resume(ServerLevel level, SpellContinuation continuation, List<SpellTarget> targets) {
        impl().resume(level, continuation, targets);
    }

    private static Bindings impl() {
        if (binding == null) {
            throw new IllegalStateException("Spells used before Thaumaturge bound it");
        }
        return binding;
    }

    /**
     * The implementation behind the facade.
     *
     * @since 1.0.0
     */
    public interface Bindings {
        /**
         * Reads the spell component.
         *
         * @param focus the stack
         * @return the spell or null
         */
        @Nullable
        Spell spellOf(ItemStack focus);

        /**
         * Writes or clears the spell component.
         *
         * @param focus the stack
         * @param spell the spell or null
         */
        void setSpell(ItemStack focus, @Nullable Spell spell);

        /**
         * Analyses a spell.
         *
         * @param spell      the spell
         * @param tier       the limits or null
         * @param registries the registry lookup
         * @param player     the player or null
         * @return the summary
         */
        SpellSummary analyze(Spell spell, @Nullable FocusTier tier, HolderLookup.Provider registries, @Nullable Player player);

        /**
         * Casts a spell.
         *
         * @param caster the caster
         * @param focus  the focus or empty
         * @param spell  the spell
         * @param origin the origin targets
         * @param power  the starting power
         */
        void cast(LivingEntity caster, ItemStack focus, Spell spell, List<SpellTarget> origin, float power);

        /**
         * Resumes a continuation.
         *
         * @param level        the level
         * @param continuation the continuation
         * @param targets      the targets
         */
        void resume(ServerLevel level, SpellContinuation continuation, List<SpellTarget> targets);
    }
}
