package com.leclowndu93150.thaumaturge.content.entity.champion;

import com.leclowndu93150.thaumaturge.TCIds;
import java.util.List;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import org.jspecify.annotations.Nullable;

public final class ChampionModifier {
    public enum Trigger {
        PASSIVE, TICK, ON_ATTACK, WHEN_HURT
    }

    @FunctionalInterface
    public interface Effect {
        float perform(LivingEntity mob, @Nullable LivingEntity target, @Nullable DamageSource source, float amount);
    }

    public static final int BOLD = 0;
    public static final int SPINED = 1;
    public static final int ARMORED = 2;
    public static final int MIGHTY = 3;
    public static final int GRIM = 4;
    public static final int WARDED = 5;
    public static final int WARP = 6;
    public static final int UNDYING = 7;
    public static final int FIERY = 8;
    public static final int SICKLY = 9;
    public static final int VENOMOUS = 10;
    public static final int VAMPIRIC = 11;
    public static final int INFESTED = 12;
    public static final int TAINTED = 13;

    private static final int MARKER_OFFSET = 2;

    public static final List<ChampionModifier> MODS = List.of(new ChampionModifier(BOLD, "bold", Trigger.PASSIVE, ChampionEffects::none),
            new ChampionModifier(SPINED, "spine", Trigger.WHEN_HURT, ChampionEffects::spined), new ChampionModifier(ARMORED, "armor", Trigger.WHEN_HURT, ChampionEffects::armored),
            new ChampionModifier(MIGHTY, "mighty", Trigger.PASSIVE, ChampionEffects::none), new ChampionModifier(GRIM, "grim", Trigger.ON_ATTACK, ChampionEffects::grim),
            new ChampionModifier(WARDED, "warded", Trigger.TICK, ChampionEffects::warded), new ChampionModifier(WARP, "warp", Trigger.ON_ATTACK, ChampionEffects::warp),
            new ChampionModifier(UNDYING, "undying", Trigger.TICK, ChampionEffects::undying), new ChampionModifier(FIERY, "fiery", Trigger.ON_ATTACK, ChampionEffects::fiery),
            new ChampionModifier(SICKLY, "sickly", Trigger.ON_ATTACK, ChampionEffects::sickly), new ChampionModifier(VENOMOUS, "venomous", Trigger.ON_ATTACK, ChampionEffects::venomous),
            new ChampionModifier(VAMPIRIC, "vampiric", Trigger.ON_ATTACK, ChampionEffects::vampiric), new ChampionModifier(INFESTED, "infested", Trigger.WHEN_HURT, ChampionEffects::infested),
            new ChampionModifier(TAINTED, "tainted", Trigger.TICK, ChampionEffects::tainted));

    public static final Identifier NONE_MARKER_ID = TCIds.rl("champion/none");
    public static final AttributeModifier NONE_MARKER = new AttributeModifier(NONE_MARKER_ID, 1.0, AttributeModifier.Operation.ADD_VALUE);
    public static final Identifier MINUS_ONE_ID = TCIds.rl("champion/minus_one");
    public static final AttributeModifier MINUS_ONE = new AttributeModifier(MINUS_ONE_ID, -1.0, AttributeModifier.Operation.ADD_VALUE);

    private final int id;
    private final String name;
    private final Trigger trigger;
    private final Effect effect;
    private final AttributeModifier attributeMod;

    private ChampionModifier(int id, String name, Trigger trigger, Effect effect) {
        this.id = id;
        this.name = name;
        this.trigger = trigger;
        this.effect = effect;
        this.attributeMod = new AttributeModifier(TCIds.rl("champion/" + name), id + MARKER_OFFSET, AttributeModifier.Operation.ADD_VALUE);
    }

    public int id() {
        return id;
    }

    public String name() {
        return name;
    }

    public Trigger trigger() {
        return trigger;
    }

    public Effect effect() {
        return effect;
    }

    public AttributeModifier attributeMod() {
        return attributeMod;
    }
}
