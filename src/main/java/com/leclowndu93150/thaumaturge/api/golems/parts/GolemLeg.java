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
 * A golem leg part.
 *
 * @since 1.0.0
 */
public final class GolemLeg extends GolemPart {
    /** The registry key for golem legs. */
    public static final ResourceKey<Registry<GolemLeg>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("golem_leg"));

    /**
     * @param research   research entries gating these legs; empty means ungated
     * @param icon       the icon drawn in the golem press
     * @param model      the model rendered for these legs
     * @param components the crafting components consumed
     * @param ability    the ability ticked for these legs, or null when they have none
     * @param traits     traits granted by these legs
     */
    public GolemLeg(
            List<ResourceLocation> research,
            ResourceLocation icon,
            @Nullable GolemPartModel model,
            List<GolemComponent> components,
            @Nullable IGolemPartAbility ability,
            List<Holder<GolemTrait>> traits) {
        super(research, icon, components, traits, model, ability);
    }
}
