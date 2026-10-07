package com.leclowndu93150.thaumaturge.api.golems.parts;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import org.jspecify.annotations.Nullable;

/**
 * Base description shared by every golem part kind: the research gating it, the icon shown
 * in the golem press, the crafting components it consumes, the traits it grants and the
 * models it renders with.
 *
 * @since 1.0.0
 */
public abstract class GolemPart {
    private final List<ResourceLocation> research;
    private final ResourceLocation icon;
    private final List<GolemComponent> components;
    private final List<Holder<GolemTrait>> traits;
    private final List<GolemPartModel> models;

    protected GolemPart(
            List<ResourceLocation> research,
            ResourceLocation icon,
            List<GolemComponent> components,
            List<Holder<GolemTrait>> traits,
            @Nullable GolemPartModel model) {
        this(research, icon, components, traits, model == null ? List.of() : List.of(model));
    }

    /**
     * @param research   research entries gating this part; empty means ungated
     * @param icon       the icon drawn for this part in the golem press
     * @param components the crafting components consumed by this part
     * @param traits     traits granted by this part
     * @param models     the models rendered for this part, each at its own attach point; empty when it has no visual
     * @since 1.0.0
     */
    protected GolemPart(
            List<ResourceLocation> research,
            ResourceLocation icon,
            List<GolemComponent> components,
            List<Holder<GolemTrait>> traits,
            List<GolemPartModel> models) {
        this.research = List.copyOf(research);
        this.icon = icon;
        this.components = List.copyOf(components);
        this.traits = List.copyOf(traits);
        this.models = List.copyOf(models);
    }

    /**
     * @return research entries gating this part; empty means ungated
     */
    public List<ResourceLocation> research() {
        return research;
    }

    /**
     * @return the icon drawn for this part in the golem press
     */
    public ResourceLocation icon() {
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
     * @return the first model rendered for this part, or null when it has no visual
     */
    public @Nullable GolemPartModel model() {
        return models.isEmpty() ? null : models.getFirst();
    }

    /**
     * Every model rendered for this part. A part can place models at several attach points,
     * for example a body plate together with fittings on both arms.
     *
     * @return the models in render order, never null; empty when the part has no visual
     * @since 1.0.0
     */
    public List<GolemPartModel> models() {
        return models;
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
    public static String nameKey(String kind, ResourceLocation id) {
        return "golem." + kind + "." + id.getNamespace() + "." + id.getPath();
    }

    /**
     * The translation key for a part's descriptive text in golem UIs.
     *
     * @param kind the part kind, one of {@code head}, {@code arm}, {@code leg}, {@code addon}
     * @param id   the part id
     * @return {@code golem.<kind>.text.<namespace>.<path>}
     */
    public static String descriptionKey(String kind, ResourceLocation id) {
        return "golem." + kind + ".text." + id.getNamespace() + "." + id.getPath();
    }
}
