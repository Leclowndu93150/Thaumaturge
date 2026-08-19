package com.leclowndu93150.thaumaturge.api.golems.parts;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

/**
 * A golem head part.
 *
 * @since 1.0.0
 */
public final class GolemHead extends GolemPart {
    /** The registry key for golem heads. */
    public static final ResourceKey<Registry<GolemHead>> REGISTRY_KEY = ResourceKey.createRegistryKey(Identifier.fromNamespaceAndPath("thaumaturge", "golem_head"));

    private final IHeadFunction function;

    /**
     * @param research   research entries gating this head; empty means ungated
     * @param icon       the icon drawn in the golem press
     * @param model      the model rendered for this head
     * @param components the crafting components consumed
     * @param function   the behavior ticked for this head, or null when it has none
     * @param traits     traits granted by this head
     */
    public GolemHead(List<Identifier> research, Identifier icon, @Nullable GolemPartModel model, List<GolemComponent> components, @Nullable IHeadFunction function, List<Holder<GolemTrait>> traits) {
        super(research, icon, components, traits, model);
        this.function = function;
    }

    @Override
    public @Nullable IHeadFunction function() {
        return function;
    }

    /**
     * Behavior attached to a golem head.
     *
     * @since 1.0.0
     */
    public interface IHeadFunction extends IGolemFunction {}
}
