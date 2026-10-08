package com.leclowndu93150.thaumaturge.api.spell.part;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.spell.SpellRegistries;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;

/**
 * A spell node players can place in the focal manipulator: a configured behaviour plus everything
 * needed to balance and draw it. Loaded from {@code data/<ns>/thaumaturge/spell_part/<path>.json}
 * and synced to clients.
 *
 * <p>Costs: a node's complexity is {@link #complexity()} plus each setting's per-step complexity
 * plus its aspect affinity's complexity. A spell's vis per cast is its total complexity times
 * {@code 0.2}, plus every node's {@link #vis()} and per-step setting vis, times every node's
 * {@link #visMultiplier()}.
 *
 * @param behavior           what the node does
 * @param icon               the glyph texture
 * @param color              the packed {@code 0xRRGGBB} backplate tint, used when the node has no aspect
 * @param complexity         the base complexity
 * @param vis                vis per cast added on top of the complexity-derived cost
 * @param power              the factor applied to the power stat when the node runs
 * @param aspect             the aspect the node is bound to
 * @param settings           the node's spinners in display order
 * @param research           the research needed to place the node
 * @param maxPerSpell        how often the part may appear in one spell, 0 for no limit
 * @param hidden             true for parts that never show in the manipulator (origin, boss spells)
 * @param sound              the sound played at the caster when a spell containing the part is cast
 * @param visMultiplier      the factor applied to the whole spell's vis cost
 * @param cooldownMultiplier the factor applied to the whole spell's cooldown
 * @param fx                 the {@code SpellFxStyle} id the node's own visuals use
 * @since 1.0.0
 */
public record SpellPart(
        SpellBehavior behavior,
        ResourceLocation icon,
        int color,
        int complexity,
        float vis,
        float power,
        AspectInput aspect,
        List<SettingSpec> settings,
        Optional<ResearchGate> research,
        int maxPerSpell,
        boolean hidden,
        Optional<Holder<SoundEvent>> sound,
        float visMultiplier,
        float cooldownMultiplier,
        ResourceLocation fx) {
    private static final ResourceLocation DEFAULT_FX = TTIds.rl("sparkle");

    /** The datapack registry key of parts. */
    public static final ResourceKey<Registry<SpellPart>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("spell_part"));

    /** Disk and network codec. */
    public static final Codec<SpellPart> CODEC = RecordCodecBuilder.create(i -> i.group(
                    SpellRegistries.BEHAVIOR_CODEC.fieldOf("behavior").forGetter(SpellPart::behavior),
                    ResourceLocation.CODEC.fieldOf("icon").forGetter(SpellPart::icon),
                    Codec.INT.optionalFieldOf("color", 0x999999).forGetter(SpellPart::color),
                    Codec.INT.optionalFieldOf("complexity", 0).forGetter(SpellPart::complexity),
                    Codec.FLOAT.optionalFieldOf("vis", 0.0F).forGetter(SpellPart::vis),
                    Codec.FLOAT.optionalFieldOf("power", 1.0F).forGetter(SpellPart::power),
                    AspectInput.CODEC
                            .optionalFieldOf("aspect", AspectInput.NONE)
                            .forGetter(SpellPart::aspect),
                    SettingSpec.CODEC
                            .listOf()
                            .optionalFieldOf("settings", List.of())
                            .forGetter(SpellPart::settings),
                    ResearchGate.CODEC.optionalFieldOf("research").forGetter(SpellPart::research),
                    Codec.INT.optionalFieldOf("max_per_spell", 0).forGetter(SpellPart::maxPerSpell),
                    Codec.BOOL.optionalFieldOf("hidden", false).forGetter(SpellPart::hidden),
                    SoundEvent.CODEC.optionalFieldOf("sound").forGetter(SpellPart::sound),
                    Codec.FLOAT.optionalFieldOf("vis_multiplier", 1.0F).forGetter(SpellPart::visMultiplier),
                    Codec.FLOAT.optionalFieldOf("cooldown_multiplier", 1.0F).forGetter(SpellPart::cooldownMultiplier),
                    ResourceLocation.CODEC.optionalFieldOf("fx", DEFAULT_FX).forGetter(SpellPart::fx))
            .apply(i, SpellPart::new));

    /**
     * Canonicalizes the settings into an immutable copy.
     */
    public SpellPart {
        settings = List.copyOf(settings);
    }

    /**
     * The part's role, taken from its behaviour type.
     *
     * @return the kind
     */
    public SpellPartKind kind() {
        return behavior.type().kind();
    }

    /**
     * The spec of a setting.
     *
     * @param key the setting key
     * @return the spec, empty when the part has no such setting
     */
    public Optional<SettingSpec> setting(String key) {
        for (SettingSpec spec : settings) {
            if (spec.key().equals(key)) {
                return Optional.of(spec);
            }
        }
        return Optional.empty();
    }

    /**
     * The translation key of a part's name.
     *
     * @param key the part key
     * @return {@code spell.<namespace>.<path>.name}
     */
    public static String nameKey(ResourceKey<SpellPart> key) {
        return "spell." + key.location().getNamespace() + "." + key.location().getPath() + ".name";
    }

    /**
     * The translation key of a part's description.
     *
     * @param key the part key
     * @return {@code spell.<namespace>.<path>.desc}
     */
    public static String descriptionKey(ResourceKey<SpellPart> key) {
        return "spell." + key.location().getNamespace() + "." + key.location().getPath() + ".desc";
    }
}
