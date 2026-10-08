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
 * A golem arm part.
 *
 * @since 1.0.0
 */
public final class GolemArm extends GolemPart {
    /** The registry key for golem arms. */
    public static final ResourceKey<Registry<GolemArm>> REGISTRY_KEY =
            ResourceKey.createRegistryKey(TTIds.rl("golem_arm"));

    private final IGolemArmAbility ability;

    /**
     * @param research   research entries gating these arms; empty means ungated
     * @param icon       the icon drawn in the golem press
     * @param model      the model rendered for these arms
     * @param components the crafting components consumed
     * @param ability    the combat ability of these arms, or null when they have none
     * @param traits     traits granted by these arms
     */
    public GolemArm(
            List<ResourceLocation> research,
            ResourceLocation icon,
            @Nullable GolemPartModel model,
            List<GolemComponent> components,
            @Nullable IGolemArmAbility ability,
            List<Holder<GolemTrait>> traits) {
        super(research, icon, components, traits, model, ability);
        this.ability = ability;
    }

    @Override
    public @Nullable IGolemArmAbility ability() {
        return ability;
    }
}
