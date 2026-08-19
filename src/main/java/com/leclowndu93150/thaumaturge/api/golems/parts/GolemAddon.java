package com.leclowndu93150.thaumaturge.api.golems.parts;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

/**
 * An optional golem addon part.
 *
 * @since 1.0.0
 */
public final class GolemAddon extends GolemPart {
    /** The registry key for golem addons. */
    public static final ResourceKey<Registry<GolemAddon>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "golem_addon"));

    private final IAddonFunction function;

    /**
     * @param research   research entries gating this addon; empty means ungated
     * @param icon       the icon drawn in the golem press
     * @param model      the model rendered for this addon, or null when it has no visual
     * @param components the crafting components consumed
     * @param function   the behavior ticked for this addon, or null when it has none
     * @param traits     traits granted by this addon
     */
    public GolemAddon(List<Identifier> research, Identifier icon, @Nullable GolemPartModel model, List<GolemComponent> components, @Nullable IAddonFunction function, List<Holder<GolemTrait>> traits) {
        super(research, icon, components, traits, model);
        this.function = function;
    }

    @Override
    public @Nullable IAddonFunction function() {
        return function;
    }

    /**
     * Behavior attached to a golem addon.
     *
     * @since 1.0.0
     */
    public interface IAddonFunction extends IGolemFunction {}
}
