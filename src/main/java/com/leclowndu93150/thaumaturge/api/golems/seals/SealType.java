package com.leclowndu93150.thaumaturge.api.golems.seals;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;
import org.jspecify.annotations.Nullable;

/**
 * A kind of golem seal, assembled from parts: a behaviour, an optional item filter, an optional work area, declared settings, golem
 * trait gates, a placement rule and the item that places it.
 *
 * <p>Seal types live in the {@link #REGISTRY_KEY} registry. Register one with a {@code DeferredRegister} and build it with
 * {@link #builder}. The configuration screen derives its pages from the parts, so a new type needs no screen code.
 *
 * <pre>
 * SealType.builder(MySealBehavior::new)
 *         .filter(9, SealFilterMode.PLAIN)
 *         .area()
 *         .settings(MY_SETTING)
 *         .showSettings()
 *         .requires(SMART_TRAIT)
 *         .placement(MY_PLACEMENT)
 *         .placer(MY_SEAL_ITEM)
 *         .build();
 * </pre>
 *
 * @since 1.0.0
 */
public final class SealType {
    /** The seal type registry. */
    public static final ResourceKey<Registry<SealType>> REGISTRY_KEY = ResourceKey.createRegistryKey(TTIds.rl("seal"));

    private final Supplier<? extends ISealBehavior> behavior;
    private final MapCodec<ISealBehavior> behaviorCodec;
    private final @Nullable SealFilterSpec filter;
    private final boolean area;
    private final List<SealSetting> settings;
    private final boolean settingsShown;
    private final List<Holder<GolemTrait>> required;
    private final List<Holder<GolemTrait>> forbidden;
    private final SealPlacement placement;
    private final Supplier<? extends ItemLike> placer;
    private final @Nullable ResourceLocation icon;
    private final List<SealPanel> panels;

    private SealType(Builder builder) {
        this.behavior = builder.behavior;
        this.behaviorCodec = builder.behaviorCodec;
        this.filter = builder.filter;
        this.area = builder.area;
        this.settings = List.copyOf(builder.settings);
        this.settingsShown = builder.settingsShown;
        this.required = List.copyOf(builder.required);
        this.forbidden = List.copyOf(builder.forbidden);
        this.placement = builder.placement;
        this.placer = builder.placer;
        this.icon = builder.icon;
        this.panels = derivePanels();
    }

    /**
     * Starts a type whose behaviour keeps no state across restarts.
     *
     * @param behavior creates the behaviour of each new placement
     * @return a builder
     */
    public static Builder builder(Supplier<? extends ISealBehavior> behavior) {
        return new Builder(behavior);
    }

    private List<SealPanel> derivePanels() {
        List<SealPanel> out = new ArrayList<>();
        if (area) {
            out.add(SealPanel.AREA);
        }
        if (filter != null) {
            out.add(SealPanel.FILTER);
        }
        if (settingsShown && !settings.isEmpty()) {
            out.add(SealPanel.TOGGLES);
        }
        out.add(SealPanel.PRIORITY);
        out.add(SealPanel.TAGS);
        return List.copyOf(out);
    }

    /**
     * @return a fresh behaviour for a new placement
     */
    public ISealBehavior newBehavior() {
        return behavior.get();
    }

    /**
     * @return the codec of the behaviour's saved state; a no-op codec for stateless behaviours
     */
    public MapCodec<ISealBehavior> behaviorCodec() {
        return behaviorCodec;
    }

    /**
     * @return the filter shape, present when the type has an item filter
     */
    public Optional<SealFilterSpec> filter() {
        return Optional.ofNullable(filter);
    }

    /**
     * @return whether the type has a configurable work area
     */
    public boolean hasArea() {
        return area;
    }

    /**
     * @return the declared settings, in display order
     */
    public List<SealSetting> settings() {
        return settings;
    }

    /**
     * @return whether players can change the settings; hidden settings keep their fallback values
     */
    public boolean showsSettings() {
        return settingsShown;
    }

    /**
     * @return traits a golem must have to work for this seal
     */
    public List<Holder<GolemTrait>> requiredTraits() {
        return required;
    }

    /**
     * @return traits that keep a golem from working for this seal
     */
    public List<Holder<GolemTrait>> forbiddenTraits() {
        return forbidden;
    }

    /**
     * @return where the seal may sit
     */
    public SealPlacement placement() {
        return placement;
    }

    /**
     * @return the item that places this seal and that it drops
     */
    public ItemLike placer() {
        return placer.get();
    }

    /**
     * @return the icon texture drawn on placed seals, when the type overrides the default {@code <namespace>:textures/item/seal_<path>.png}
     */
    public Optional<ResourceLocation> icon() {
        return Optional.ofNullable(icon);
    }

    /**
     * @return the configuration screen pages, in order
     */
    public List<SealPanel> panels() {
        return panels;
    }

    /**
     * Assembles a {@link SealType}. Every part is optional except the placer.
     *
     * @since 1.0.0
     */
    public static final class Builder {
        private final Supplier<? extends ISealBehavior> behavior;
        private MapCodec<ISealBehavior> behaviorCodec;
        private @Nullable SealFilterSpec filter;
        private boolean area;
        private final List<SealSetting> settings = new ArrayList<>();
        private boolean settingsShown;
        private final List<Holder<GolemTrait>> required = new ArrayList<>();
        private final List<Holder<GolemTrait>> forbidden = new ArrayList<>();
        private SealPlacement placement =
                (level, pos, face) -> !level.getBlockState(pos).isAir();
        private @Nullable Supplier<? extends ItemLike> placer;
        private @Nullable ResourceLocation icon;

        private Builder(Supplier<? extends ISealBehavior> behavior) {
            this.behavior = behavior;
            this.behaviorCodec = MapCodec.unit(() -> (ISealBehavior) behavior.get());
        }

        /**
         * Saves behaviour state through a codec. The codec also creates the behaviour when a seal loads.
         *
         * @param codec the state codec
         * @param kind  the behaviour class the codec handles
         * @param <B>   the behaviour type
         * @return this builder
         */
        public <B extends ISealBehavior> Builder persistent(MapCodec<B> codec, Class<B> kind) {
            this.behaviorCodec = codec.xmap(stored -> (ISealBehavior) stored, kind::cast);
            return this;
        }

        /**
         * Adds an item filter.
         *
         * @param slots the number of ghost slots, from 1 to 9
         * @param mode  how the slots are interpreted
         * @return this builder
         */
        public Builder filter(int slots, SealFilterMode mode) {
            this.filter = new SealFilterSpec(slots, mode);
            return this;
        }

        /**
         * Adds a configurable work area. New seals start 3 wide on the axes the seal does not face.
         *
         * @return this builder
         */
        public Builder area() {
            this.area = true;
            return this;
        }

        /**
         * Declares settings, appended in display order.
         *
         * @param declared the settings
         * @return this builder
         */
        public Builder settings(SealSetting... declared) {
            settings.addAll(List.of(declared));
            return this;
        }

        /**
         * Lets players change the declared settings on a Toggles page.
         *
         * @return this builder
         */
        public Builder showSettings() {
            this.settingsShown = true;
            return this;
        }

        /**
         * @param traits traits a golem must have
         * @return this builder
         */
        @SafeVarargs
        public final Builder requires(Holder<GolemTrait>... traits) {
            required.addAll(List.of(traits));
            return this;
        }

        /**
         * @param traits traits that disqualify a golem
         * @return this builder
         */
        @SafeVarargs
        public final Builder forbids(Holder<GolemTrait>... traits) {
            forbidden.addAll(List.of(traits));
            return this;
        }

        /**
         * Replaces the default rule, which accepts any non-air block.
         *
         * @param rule where the seal may sit
         * @return this builder
         */
        public Builder placement(SealPlacement rule) {
            this.placement = rule;
            return this;
        }

        /**
         * @param item the item that places this seal and that it drops
         * @return this builder
         */
        public Builder placer(Supplier<? extends ItemLike> item) {
            this.placer = item;
            return this;
        }

        /**
         * @param texture the icon texture drawn on placed seals
         * @return this builder
         */
        public Builder icon(ResourceLocation texture) {
            this.icon = texture;
            return this;
        }

        /**
         * @return the seal type
         * @throws IllegalStateException when no placer item was given
         */
        public SealType build() {
            if (placer == null) {
                throw new IllegalStateException("A seal type needs a placer item");
            }
            return new SealType(this);
        }
    }
}
