package com.leclowndu93150.thaumaturge.api.golems.parts;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;

/**
 * Base description shared by every golem part kind: the research gating it, the icon shown
 * in the golem press, the crafting components it consumes, the traits it grants and the
 * model it renders with.
 *
 * @since 1.0.0
 */
public abstract class GolemPart {
    private final List<Identifier> research;
    private final Identifier icon;
    private final List<GolemComponent> components;
    private final List<Holder<GolemTrait>> traits;
    private final GolemPartModel model;

    protected GolemPart(List<Identifier> research, Identifier icon, List<GolemComponent> components, List<Holder<GolemTrait>> traits, @Nullable GolemPartModel model) {
        this.research = List.copyOf(research);
        this.icon = icon;
        this.components = List.copyOf(components);
        this.traits = List.copyOf(traits);
        this.model = model;
    }

    /**
     * @return research entries gating this part; empty means ungated
     */
    public List<Identifier> research() {
        return research;
    }

    /**
     * @return the icon drawn for this part in the golem press
     */
    public Identifier icon() {
        return icon;
    }

    /**
     * @return the crafting components consumed by this part
     */
    public List<GolemComponent> components() {
        return components;
    }

    /**
     * @return traits granted by this part
     */
    public List<Holder<GolemTrait>> traits() {
        return traits;
    }

    /**
     * @return the model rendered for this part, or null when it has no visual
     */
    public @Nullable GolemPartModel model() {
        return model;
    }

    /**
     * @return the behavior ticked for this part, or null when it has none
     */
    public abstract @Nullable IGolemFunction function();

    /**
     * The translation key for a part's display name in golem UIs.
     *
     * @param kind the part kind, one of {@code head}, {@code arm}, {@code leg}, {@code addon}
     * @param id   the part id
     * @return {@code golem.<kind>.<namespace>.<path>}
     */
    public static String nameKey(String kind, Identifier id) {
        return "golem." + kind + "." + id.getNamespace() + "." + id.getPath();
    }

    /**
     * The translation key for a part's descriptive text in golem UIs.
     *
     * @param kind the part kind, one of {@code head}, {@code arm}, {@code leg}, {@code addon}
     * @param id   the part id
     * @return {@code golem.<kind>.text.<namespace>.<path>}
     */
    public static String descriptionKey(String kind, Identifier id) {
        return "golem." + kind + ".text." + id.getNamespace() + "." + id.getPath();
    }
}
