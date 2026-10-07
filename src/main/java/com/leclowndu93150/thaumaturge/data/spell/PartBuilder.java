package com.leclowndu93150.thaumaturge.data.spell;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AffinityReaction;
import com.leclowndu93150.thaumaturge.api.spell.behavior.SpellBehavior;
import com.leclowndu93150.thaumaturge.api.spell.part.AspectInput;
import com.leclowndu93150.thaumaturge.api.spell.part.SettingSpec;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvent;

final class PartBuilder {
    private static final String VALUE = "spell.thaumaturge.value.";

    private final SpellBehavior behavior;
    private final List<SettingSpec> settings = new ArrayList<>();
    private Identifier icon = TTIds.rl("textures/foci/root.png");
    private int color = 0x999999;
    private int complexity;
    private float vis;
    private float power = 1.0F;
    private AspectInput aspect = AspectInput.NONE;
    private Optional<ResearchGate> research = Optional.empty();
    private int maxPerSpell;
    private boolean hidden;
    private Optional<Holder<SoundEvent>> sound = Optional.empty();
    private float visMultiplier = 1.0F;
    private float cooldownMultiplier = 1.0F;
    private Identifier fx = TTIds.rl("sparkle");

    private PartBuilder(SpellBehavior behavior) {
        this.behavior = behavior;
    }

    static PartBuilder of(SpellBehavior behavior) {
        return new PartBuilder(behavior);
    }

    PartBuilder glyph(String name) {
        icon = TTIds.rl("textures/foci/" + name + ".png");
        return this;
    }

    PartBuilder color(int color) {
        this.color = color;
        return this;
    }

    PartBuilder complexity(int complexity) {
        this.complexity = complexity;
        return this;
    }

    PartBuilder vis(float vis) {
        this.vis = vis;
        return this;
    }

    PartBuilder power(float power) {
        this.power = power;
        return this;
    }

    PartBuilder aspect(ResourceKey<IAspect> fixed) {
        aspect = new AspectInput.Fixed(fixed);
        return this;
    }

    PartBuilder chooses(AffinityReaction reaction, ResourceKey<IAspect> fallback) {
        aspect = new AspectInput.FromAffinities(reaction, fallback);
        return this;
    }

    PartBuilder research(String entry) {
        research = Optional.of(new ResearchGate(TTIds.rl(entry), Optional.empty(), false));
        return this;
    }

    PartBuilder research(String entry, int stage) {
        research = Optional.of(new ResearchGate(TTIds.rl(entry), Optional.of(stage), false));
        return this;
    }

    PartBuilder max(int maxPerSpell) {
        this.maxPerSpell = maxPerSpell;
        return this;
    }

    PartBuilder hidden() {
        hidden = true;
        return this;
    }

    PartBuilder sound(Holder<SoundEvent> sound) {
        this.sound = Optional.of(sound);
        return this;
    }

    PartBuilder visMultiplier(float visMultiplier) {
        this.visMultiplier = visMultiplier;
        return this;
    }

    PartBuilder cooldownMultiplier(float cooldownMultiplier) {
        this.cooldownMultiplier = cooldownMultiplier;
        return this;
    }

    PartBuilder fx(String style) {
        fx = TTIds.rl(style);
        return this;
    }

    PartBuilder setting(String key, int min, int max, int step, int defaultValue, float complexityPerStep) {
        settings.add(new SettingSpec(key, min, max, step, defaultValue, complexityPerStep, 0.0F, Optional.empty(), Optional.empty()));
        return this;
    }

    PartBuilder options(String key, float complexityPerStep, String... labels) {
        List<String> keys = new ArrayList<>(labels.length);
        for (String label : labels) {
            keys.add(VALUE + label);
        }
        settings.add(new SettingSpec(key, 0, labels.length - 1, 1, 0, complexityPerStep, 0.0F, Optional.of(keys), Optional.empty()));
        return this;
    }

    void register(BootstrapContext<SpellPart> ctx, ResourceKey<SpellPart> key) {
        ctx.register(key, new SpellPart(behavior, icon, color, complexity, vis, power, aspect, settings, research, maxPerSpell, hidden, sound, visMultiplier, cooldownMultiplier, fx));
    }
}
