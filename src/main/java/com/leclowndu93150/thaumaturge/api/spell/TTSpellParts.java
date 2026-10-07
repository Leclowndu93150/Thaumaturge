package com.leclowndu93150.thaumaturge.api.spell;

import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;

/**
 * Keys of the spell parts Thaumaturge ships. Datapacks may override or remove any of them; code
 * building spells from these keys must cope with a missing part.
 *
 * @since 1.0.0
 */
public final class TTSpellParts {
    /** The hidden root of every spell. */
    public static final ResourceKey<SpellPart> ORIGIN = Spell.ORIGIN;

    /** Delivery: what the caster's hand can reach. */
    public static final ResourceKey<SpellPart> TOUCH = key("touch");
    /** Delivery: an instant crackling ray. */
    public static final ResourceKey<SpellPart> BOLT = key("bolt");
    /** Delivery: a straight beam with adjustable range. */
    public static final ResourceKey<SpellPart> BEAM = key("beam");
    /** Delivery: a flying orb. */
    public static final ResourceKey<SpellPart> PROJECTILE = key("projectile");
    /** Delivery: an arcing shell that splashes on impact. */
    public static final ResourceKey<SpellPart> LOB = key("lob");
    /** Delivery: a cone in front of the caster. */
    public static final ResourceKey<SpellPart> SPRAY = key("spray");
    /** Delivery: an arc that jumps between enemies. */
    public static final ResourceKey<SpellPart> CHAIN = key("chain");
    /** Delivery: everything around the caster. */
    public static final ResourceKey<SpellPart> AURA = key("aura");
    /** Delivery: the caster. */
    public static final ResourceKey<SpellPart> SELF = key("self");
    /** Delivery: a lingering cloud. */
    public static final ResourceKey<SpellPart> CLOUD = key("cloud");
    /** Delivery: a trap. */
    public static final ResourceKey<SpellPart> MINE = key("mine");
    /** Delivery: a barrier line. */
    public static final ResourceKey<SpellPart> WALL = key("wall");
    /** Delivery: a bat that hunts and strikes. */
    public static final ResourceKey<SpellPart> SPELLBAT = key("spellbat");
    /** Delivery: an orbiting sprite that zaps enemies. */
    public static final ResourceKey<SpellPart> SPRITE = key("sprite");
    /** Delivery: an area of blocks shaped by the caster's area controls. */
    public static final ResourceKey<SpellPart> PLAN = key("plan");

    /** Effect: ignis strike. */
    public static final ResourceKey<SpellPart> FIRE = key("fire");
    /** Effect: gelum strike. */
    public static final ResourceKey<SpellPart> FROST = key("frost");
    /** Effect: aer strike. */
    public static final ResourceKey<SpellPart> AIR = key("air");
    /** Effect: terra strike. */
    public static final ResourceKey<SpellPart> EARTH = key("earth");
    /** Effect: vitium strike. */
    public static final ResourceKey<SpellPart> FLUX = key("flux");
    /** Effect: mortuus strike. */
    public static final ResourceKey<SpellPart> CURSE = key("curse");
    /** Effect: victus strike. */
    public static final ResourceKey<SpellPart> HEAL = key("heal");
    /** Effect: an explosion of any chosen aspect. */
    public static final ResourceKey<SpellPart> BURST = key("burst");
    /** Effect: the boon of any chosen aspect. */
    public static final ResourceKey<SpellPart> IMBUE = key("imbue");
    /** Effect: breaks blocks. */
    public static final ResourceKey<SpellPart> BREAK = key("break");
    /** Effect: swaps blocks for the picked block. */
    public static final ResourceKey<SpellPart> EXCHANGE = key("exchange");
    /** Effect: opens a temporary tunnel. */
    public static final ResourceKey<SpellPart> RIFT = key("rift");
    /** Effect: wards or unwards blocks. */
    public static final ResourceKey<SpellPart> WARD = key("ward");
    /** Effect: a raw, unstable discharge. */
    public static final ResourceKey<SpellPart> PRIMAL = key("primal");
    /** Effect: summons fire bats. */
    public static final ResourceKey<SpellPart> HELLBAT = key("hellbat");
    /** Effect: teleports the caster to the target. */
    public static final ResourceKey<SpellPart> BLINK = key("blink");
    /** Effect: reaps and replants ripe crops. */
    public static final ResourceKey<SpellPart> HARVEST = key("harvest");

    /** Modifier: more power. */
    public static final ResourceKey<SpellPart> AMPLIFY = key("amplify");
    /** Modifier: longer durations. */
    public static final ResourceKey<SpellPart> EXTEND = key("extend");
    /** Modifier: larger areas. */
    public static final ResourceKey<SpellPart> WIDEN = key("widen");
    /** Modifier: faster carriers and shorter cooldowns. */
    public static final ResourceKey<SpellPart> QUICKEN = key("quicken");
    /** Modifier: cheaper and weaker. */
    public static final ResourceKey<SpellPart> EFFICIENT = key("efficient");
    /** Modifier: passes through entities. */
    public static final ResourceKey<SpellPart> PIERCE = key("pierce");
    /** Modifier: carriers steer toward enemies. */
    public static final ResourceKey<SpellPart> HOMING = key("homing");
    /** Modifier: projectiles bounce. */
    public static final ResourceKey<SpellPart> BOUNCE = key("bounce");
    /** Modifier: silk touch for block effects. */
    public static final ResourceKey<SpellPart> SILK_TOUCH = key("silk_touch");
    /** Modifier: fortune for block effects. */
    public static final ResourceKey<SpellPart> FORTUNE = key("fortune");
    /** Modifier: random spread of trajectories. */
    public static final ResourceKey<SpellPart> SCATTER = key("scatter");
    /** Modifier: a flat arc of trajectories. */
    public static final ResourceKey<SpellPart> FAN = key("fan");
    /** Modifier: repeats what follows. */
    public static final ResourceKey<SpellPart> ECHO = key("echo");

    /** Flow: forks the spell. */
    public static final ResourceKey<SpellPart> BRANCH = key("branch");
    /** Flow: re-aims the spell. */
    public static final ResourceKey<SpellPart> RELAY = key("relay");
    /** Flow: waits before continuing. */
    public static final ResourceKey<SpellPart> DELAY = key("delay");
    /** Flow: keeps only some targets. */
    public static final ResourceKey<SpellPart> SIEVE = key("sieve");

    private TTSpellParts() {}

    private static ResourceKey<SpellPart> key(String path) {
        return ResourceKey.create(SpellPart.REGISTRY_KEY, Identifier.fromNamespaceAndPath("thaumaturge", path));
    }
}
