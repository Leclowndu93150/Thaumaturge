package com.leclowndu93150.thaumaturge.data.lang;

import java.util.function.BiConsumer;

public final class SpellResearchTextEn {
    private static final String RESEARCH = "research.thaumaturge.";

    private SpellResearchTextEn() {}

    public static void addAll(BiConsumer<String, String> add) {
        deliveries(add);
        effects(add);
        weaves(add);
    }

    private static void entry(BiConsumer<String, String> add, String id, String title, String before, String after) {
        add.accept(RESEARCH + id + ".title", title);
        add.accept(RESEARCH + id + ".stage_0", before);
        add.accept(RESEARCH + id + ".stage_1", after);
    }

    private static void deliveries(BiConsumer<String, String> add) {
        entry(add, "focus_beam", "Focus Delivery: Beam",
                "A bolt discharges all at once and is gone. If I could hold the stream open instead of releasing it, the vis would keep flowing along the same path for as long as I can sustain it.",
                "I can now hold a steady beam of focused vis. It reaches as far as a bolt and is at its best when the focus is set to channel, pulsing the effect into whatever it touches for as long as I keep casting.<BR>"
                        + "Studying the beam also taught me two cruder ways to shape the stream. A §nSpray§0 fans the effect across a short cone in front of me, and a §nChain§0 bolt leaps from one foe to the next, losing a little strength with every jump.");
        entry(add, "focus_lob", "Focus Delivery: Lob",
                "My projectiles fly true but light. A heavier shell, thrown high, should come down hard and spread its payload over everything near where it lands.",
                "The Lob delivery hurls a dense shell in a steep arc. It is slow and takes some practice to aim, but on impact it splashes its effects over a small area.<BR>"
                        + "The same weighting lets me anchor a §nWall§0 of shimmering vis where the spell lands. Anything that crosses the wall suffers its effects until it fades.");
        entry(add, "focus_aura", "Focus Delivery: Aura", "Every delivery I know throws the spell away from me. There are times I would rather keep it close: around me, or even upon me.",
                "The Aura delivery releases the spell in a ring centred on myself. Everything nearby is touched by it, save for me.<BR>"
                        + "Reversing the pattern gives the §nSelf§0 delivery, which turns the spell upon its caster. Healing and protective effects benefit most, but I must be careful what I pair it with.");
        entry(add, "focus_sprite", "Focus Delivery: Sprite", "The spellbats are effective but wild. A smaller, tamer construct that stays at my side would be far easier to direct.",
                "I can now call a sprite: a mote of bound vis that circles me and lashes out at nearby enemies with the spell it carries.<BR>"
                        + "Only a few sprites can be maintained at once. While they are all circling me, casting again calls nothing new.");
    }

    private static void effects(BiConsumer<String, String> add) {
        entry(add, "focus_burst", "Focus Effect: Elemental Burst",
                "The elemental effects strike a single target. With a little more vis it should be possible to release the same energy outward instead.",
                "The Elemental Burst effect detonates the chosen aspect in a sphere around the point of impact. Its strength falls off toward the edge of the blast.<BR>"
                        + "It accepts any aspect I have learned to weave into a spell. Each one bursts in its own way.");
        entry(add, "focus_imbue", "Focus Effect: Imbue", "Victus mends flesh directly. I suspect other aspects carry blessings of their own, if they are offered rather than thrown.",
                "Imbue grants the boon of the chosen aspect to its target instead of its harm. Ignis wards against flame, Aer lightens my step, Terra hardens the skin, and so on.<BR>"
                        + "The effect is gentle and lasts a while. It pairs naturally with the Self and Aura deliveries.");
        entry(add, "focus_blink", "Focus Effect: Blink", "If rifts can shift matter aside for a moment, they should be able to shift me as well. The trick is choosing where I come out.",
                "Blink steps me through the aether to wherever the spell lands. I arrive standing, though a little shaken.<BR>"
                        + "I need room to stand where I arrive. If there is none near the landing point, or it lies too far away, nothing happens.");
        entry(add, "focus_silk", "Focus Modifiers: Silk Touch and Fortune",
                "The Break effect tears blocks apart rather carelessly. The enchanters' arts suggest gentler and richer ways to take blocks from the world.",
                "Two modifiers now shape every block effect that follows them. §nSilk Touch§0 keeps the blocks whole, and §nFortune§0 coaxes more out of them.<BR>"
                        + "Fortune can be strengthened at the cost of complexity. Both draw a little extra vis for every block they touch.");
        entry(add, "focus_harvest", "Focus Effect: Harvest", "Breaking crops one at a time is tedious. Herba should let me tell what is ripe and what is not.",
                "Harvest reaps every ripe crop around the target and replants it from the harvest. Unripe crops are left alone.<BR>" + "The radius can be widened at the cost of complexity.");
    }

    private static void weaves(BiConsumer<String, String> add) {
        entry(add, "focus_amplify", "Focus Modifiers: Amplification",
                "Every part of a spell carries a fixed strength. A modifier placed before the effects should be able to raise or reshape all of them at once.",
                "I have devised a family of modifiers that alter every part that follows them:<LINE>§nAmplify§0 raises their power.<BR>§nExtend§0 lengthens their durations.<BR>"
                        + "§nWiden§0 grows their area, and lets single-target effects spill onto nearby creatures.<BR>§nQuicken§0 speeds up carriers and shortens the cooldown.<BR>"
                        + "§nEfficient§0 lowers the vis cost considerably, at a small loss of power.<BR>Most of them can be raised in level for more complexity.");
        entry(add, "focus_trajectory", "Focus Modifiers: Trajectory", "Scatter splits a trajectory at random. With more care I should be able to control the shape and reach of each path.",
                "Two new modifiers refine how a spell travels.<LINE>§nPierce§0 lets rays and projectiles pass through extra creatures.<BR>"
                        + "§nFan§0 splits the spell into an even arc of trajectories, which is far easier to aim than Scatter.");
        entry(add, "focus_flow", "Focus Flow", "Branching a spell has shown me that it need not run in a straight line. I want finer control over where it goes next and when.",
                "I can now place flow parts in a focus:<LINE>§nRelay§0 re-aims the spell from wherever it struck: onward, reflected off the surface, up, down, back toward me or toward the nearest creature.<BR>"
                        + "§nDelay§0 holds the spell for a moment before it continues.<BR>§nSieve§0 lets only some targets through, such as blocks, enemies, allies or the undead.");
        entry(add, "focus_echo", "Focus Modifier: Echo", "A strong enough weave seems to ring after it is cast. If I can catch that ringing, the spell might repeat itself.",
                "The Echo modifier makes everything after it run again a short while later. Each repeat is weaker than the last.<BR>" + "It is costly, and a focus can only hold so many repeats.");
        add.accept(RESEARCH + "focus_split.title", "Focus Flow: Branch");
        add.accept(RESEARCH + "focus_split.stage_0",
                "While the Scatter modifier has proven quite useful, it splits a trajectory without any regard for what follows. I realise now my original plan was too ambitious.<BR>I should start smaller.");
        add.accept(RESEARCH + "focus_split.stage_1",
                "I have designed the Branch, a flow part that forks a spell into several paths. Every branch receives the same targets, so one cast can apply entirely different effects at once.<BR>"
                        + "Branches can be nested, though each one adds to the complexity of the focus.");
    }
}
