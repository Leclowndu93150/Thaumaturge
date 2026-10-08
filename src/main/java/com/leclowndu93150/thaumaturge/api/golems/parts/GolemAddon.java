package com.leclowndu93150.thaumaturge.api.golems.parts;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * An optional golem addon part.
 *
 * @since 1.0.0
 */
public final class GolemAddon extends GolemPart {
    /** The registry key for golem addons. */
    public static final ResourceKey<Registry<GolemAddon>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("golem_addon"));

    /**
     * @param research   research entries gating this addon; empty means ungated
     * @param icon       the icon drawn in the golem press
     * @param model      the model rendered for this addon, or null when it has no visual
     * @param components the crafting components consumed
     * @param ability    the ability ticked for this addon, or null when it has none
     * @param traits     traits granted by this addon
     */
    public GolemAddon(
            List<ResourceLocation> research,
            ResourceLocation icon,
            @Nullable GolemPartModel model,
            List<GolemComponent> components,
            @Nullable IGolemPartAbility ability,
            List<Holder<GolemTrait>> traits) {
        super(research, icon, components, traits, model, ability);
    }

    private GolemAddon(
            List<ResourceLocation> research,
            ResourceLocation icon,
            List<GolemPartModel> models,
            List<GolemComponent> components,
            @Nullable IGolemPartAbility ability,
            List<Holder<GolemTrait>> traits) {
        super(research, icon, components, traits, models, ability);
    }

    /**
     * Creates an addon that renders several models, each at its own attach point, for example a
     * body plate together with fittings drawn on both arms.
     *
     * @param research   research entries gating this addon; empty means ungated
     * @param icon       the icon drawn in the golem press
     * @param models     the models rendered for this addon in render order; empty when it has no visual
     * @param components the crafting components consumed
     * @param ability    the ability ticked for this addon, or null when it has none
     * @param traits     traits granted by this addon
     * @return the new addon
     * @since 1.0.0
     */
    public static GolemAddon withModels(
            List<ResourceLocation> research,
            ResourceLocation icon,
            List<GolemPartModel> models,
            List<GolemComponent> components,
            @Nullable IGolemPartAbility ability,
            List<Holder<GolemTrait>> traits) {
        return new GolemAddon(research, icon, models, components, ability, traits);
    }
}
