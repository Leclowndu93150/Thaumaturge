package com.leclowndu93150.thaumaturge.data.lang;

import java.util.function.BiConsumer;

public final class SpellTextEn {
    private static final String PART = "spell.thaumaturge.";

    private SpellTextEn() {}

    public static void addAll(BiConsumer<String, String> add) {
        parts(add);
        vocabulary(add);
        problems(add);
        screen(add);
    }

    private static void part(BiConsumer<String, String> add, String id, String name, String description) {
        add.accept(PART + id + ".name", name);
        add.accept(PART + id + ".desc", description);
    }

    private static void parts(BiConsumer<String, String> add) {
        part(add, "origin", "Caster", "Where every spell begins: your hand and your gaze.");
        part(add, "touch", "Touch", "Lays the spell on whatever your hand can reach.");
        part(add, "bolt", "Bolt", "An instant crackling ray that strikes the first thing within 16 blocks.");
        part(add, "beam", "Beam", "A steady beam of light. Pairs well with channeling.");
        part(add, "projectile", "Projectile", "Gathers the spell into a slow orb that flies under gravity.");
        part(add, "lob", "Lob", "Hurls a heavy shell in a high arc. It splashes everything near where it lands.");
        part(add, "spray", "Spray", "Sprays the spell over a cone in front of you.");
        part(add, "chain", "Chain", "A bolt that leaps from enemy to enemy, weakening with every jump.");
        part(add, "aura", "Aura", "Washes the spell over everything around you, sparing yourself.");
        part(add, "self", "Self", "Turns the spell upon yourself.");
        part(add, "cloud", "Cloud", "Leaves a lingering cloud that affects anything within it.");
        part(add, "mine", "Arcane Mine", "Sets a trap that springs on the first creature to come near.");
        part(add, "wall", "Wall", "Raises a shimmering barrier. Anything crossing it suffers the spell.");
        part(
                add,
                "spellbat",
                "Summon Spellbat",
                "Conjures a bat that hunts down creatures and carries the spell to them.");
        part(add, "sprite", "Summon Sprite", "Calls a sprite that circles you and zaps nearby enemies with the spell.");
        part(add, "plan", "Plan", "Picks out an area of blocks shaped by the caster's area controls.");
        part(add, "fire", "Fire", "Ignis. Burns the target and sets it alight. Can start fires.");
        part(add, "frost", "Frost", "Gelum. Chills and slows the target. Freezes water.");
        part(add, "air", "Air", "Aer. A blast of wind that throws things back.");
        part(add, "earth", "Earth", "Terra. Earthen shrapnel that hits hard and crumbles weak blocks.");
        part(add, "flux", "Flux", "Vitium. Corrupting energy. Leaves taint in the air.");
        part(add, "curse", "Curse", "Mortuus. Poisons and weakens. Saps the life from the ground.");
        part(add, "heal", "Heal", "Victus. Mends living creatures and sears the undead.");
        part(add, "burst", "Elemental Burst", "Releases the chosen aspect in an explosion around the target.");
        part(add, "imbue", "Imbue", "Grants the boon of the chosen aspect to the target.");
        part(add, "break", "Break", "Disruptive energy that breaks down most materials.");
        part(add, "exchange", "Exchange", "Swaps blocks for the block picked with the caster.");
        part(add, "rift", "Rift", "Shifts matter elsewhere for a while, opening a tunnel.");
        part(add, "ward", "Ward", "Wards a block against tampering, or lifts your own ward.");
        part(add, "primal", "Primal", "A raw discharge of primal energy. Devastating and erratic.");
        part(add, "hellbat", "Nine Hells", "Summons hellbats that harry the target with fire.");
        part(add, "blink", "Blink", "Steps you through the aether to wherever the spell lands.");
        part(add, "harvest", "Harvest", "Reaps ripe crops around the target and replants them.");
        part(add, "amplify", "Amplify", "Strengthens everything that follows.");
        part(add, "extend", "Extend", "Lengthens every duration that follows.");
        part(add, "widen", "Widen", "Grows every area that follows. Single-target effects also hit nearby creatures.");
        part(add, "quicken", "Quicken", "Speeds up carriers and shortens the cooldown.");
        part(add, "efficient", "Efficient", "A frugal weave: costs much less vis, at some loss of power.");
        part(add, "pierce", "Pierce", "Rays and projectiles pass through extra creatures.");
        part(add, "homing", "Homing", "Carriers steer toward nearby enemies.");
        part(add, "bounce", "Bounce", "Projectiles bounce off blocks.");
        part(add, "silk_touch", "Silk Touch", "Block effects that follow keep blocks whole.");
        part(add, "fortune", "Fortune", "Block effects that follow yield more.");
        part(add, "scatter", "Scatter", "Splits the spell into several trajectories in a random cone.");
        part(add, "fan", "Fan", "Splits the spell into an even arc of trajectories.");
        part(add, "echo", "Echo", "Runs what follows again, a little weaker each time.");
        part(add, "branch", "Branch", "Forks the spell. Every branch receives the same targets.");
        part(add, "relay", "Relay", "Re-aims the spell from where it struck.");
        part(add, "delay", "Delay", "Waits before continuing.");
        part(add, "sieve", "Sieve", "Lets only some targets through.");
        part(add, "eldritch_crescent", "Eldritch Crescent", "A sweeping blade of eldritch force.");
        part(add, "eldritch_sigil", "Seal of Unmaking", "A marked seal erupts after a warning.");
        part(add, "eldritch_nova", "Hollow Nova", "A low ring of eldritch force expands from the caster.");
        part(add, "eldritch_hammer", "Halo Hammer", "A relic torn from the halo and hurled at the target.");
        part(add, "eldritch_rend", "Rend", "Tears at the target with eldritch force.");
    }

    private static void vocabulary(BiConsumer<String, String> add) {
        add.accept(PART + "kind.delivery", "Delivery");
        add.accept(PART + "kind.effect", "Effect");
        add.accept(PART + "kind.modifier", "Modifier");
        add.accept(PART + "kind.flow", "Flow");
        add.accept(PART + "kind_aspect", "%s - %s");
        add.accept(PART + "style.instant", "Instant");
        add.accept(PART + "style.instant.desc", "Casts on use, then cools down.");
        add.accept(PART + "style.charged", "Charged");
        add.accept(PART + "style.charged.desc", "Hold to charge, release to cast. A full charge hits harder.");
        add.accept(PART + "style.channeled", "Channeled");
        add.accept(PART + "style.channeled.desc", "Pulses while held, draining vis with every pulse.");
        String[][] settings = {
            {"power", "Power"},
            {"duration", "Duration"},
            {"radius", "Radius"},
            {"speed", "Speed"},
            {"range", "Range"},
            {"jumps", "Jumps"},
            {"width", "Width"},
            {"target", "Targets"},
            {"method", "Method"},
            {"depth", "Depth"},
            {"bats", "Bats"},
            {"potency", "Potency"},
            {"level", "Level"},
            {"forks", "Forks"},
            {"cone", "Cone"},
            {"count", "Count"},
            {"arc", "Arc"},
            {"repeats", "Repeats"},
            {"direction", "Direction"},
            {"ticks", "Ticks"},
            {"keep", "Keep"},
            {"left", "Left"}
        };
        for (String[] setting : settings) {
            add.accept(PART + "setting." + setting[0], setting[1]);
        }
        String[][] values = {
            {"enemy", "Enemies"},
            {"ally", "Allies"},
            {"full", "Full"},
            {"surface", "Surface"},
            {"forward", "Forward"},
            {"reflect", "Reflect"},
            {"up", "Up"},
            {"down", "Down"},
            {"caster", "Caster"},
            {"nearest", "Nearest"},
            {"entities", "Entities"},
            {"blocks", "Blocks"},
            {"enemies", "Enemies"},
            {"allies", "Allies"},
            {"undead", "Undead"},
            {"living", "Living"}
        };
        for (String[] value : values) {
            add.accept(PART + "value." + value[0], value[1]);
        }
        add.accept(PART + "fizzle", "The spell fizzles");
        add.accept(PART + "not_enough_vis", "Not enough vis");
        add.accept(PART + "max", "Max");
    }

    private static void problems(BiConsumer<String, String> add) {
        String problem = PART + "problem.";
        add.accept(problem + "bad_root", "The spell has no origin");
        add.accept(problem + "empty", "The spell is empty");
        add.accept(problem + "no_effect", "The spell has no effect");
        add.accept(problem + "too_complex", "Too complex (%s/%s)");
        add.accept(problem + "too_deep", "Too long (%s/%s)");
        add.accept(problem + "too_many_branches", "Too many branches (%s/%s)");
        add.accept(problem + "too_many_repeats", "Too many repeats (%s/%s)");
        add.accept(problem + "unknown_part", "Unknown part %s");
        add.accept(problem + "misplaced_origin", "The caster can only be the root");
        add.accept(problem + "hidden_part", "%s cannot be inscribed");
        add.accept(problem + "repeated", "%s at most %s times");
        add.accept(problem + "part_locked", "%s needs %s");
        add.accept(problem + "too_many_children", "%s holds at most %s");
        add.accept(problem + "empty_branch", "A branch is empty");
        add.accept(problem + "leads_nowhere", "%s leads nowhere");
        add.accept(problem + "aspect_not_allowed", "%s cannot channel %s");
        add.accept(problem + "aspect_unknown", "%s is undiscovered");
        add.accept(problem + "aspect_locked", "%s needs %s");
    }

    private static void screen(BiConsumer<String, String> add) {
        String tooltip = "tooltip.thaumaturge.spell.";
        add.accept(tooltip + "vis_cost", "%s vis per cast");
        add.accept(tooltip + "style", "Cast: %s");
        add.accept(tooltip + "complexity", "Complexity %s of %s");
        add.accept(tooltip + "node_complexity", "Complexity %s");
        add.accept(tooltip + "invalid", "Unstable spell");
        add.accept(tooltip + "blank", "No spell inscribed");
        add.accept(tooltip + "empty_slot", "Empty slot");
        add.accept(tooltip + "remove_hint", "Right-click to remove");
        String gui = "gui.thaumaturge.focal_manipulator.";
        add.accept(gui + "selected", "SELECTED");
        add.accept(gui + "aspect", "ASPECT");
        add.accept(gui + "spell", "SPELL");
        add.accept(gui + "cast", "Cast");
        add.accept(gui + "node_cost", "Node cost");
        add.accept(gui + "per_cast", "per cast");
        add.accept(gui + "name_hint", "Spell name");
        add.accept(gui + "search_hint", "Search parts...");
        add.accept(gui + "needs_focus", "Place a focus in the socket");
        add.accept(gui + "inscribing", "Inscribing... %s%%");
        add.accept(gui + "vis_help", "%s vis per cast, drawn from your wand");
        add.accept(gui + "inscribe_cost", "Inscribing costs");
        add.accept(gui + "inscribe_xp", "%s experience levels");
        add.accept(gui + "inscribe_crystal", "%sx %s crystal");
        add.accept(gui + "inscribe_xp_short", "%s experience levels (%s short)");
        add.accept(gui + "inscribe_crystal_short", "%sx %s crystal (%s short)");
        add.accept(gui + "fix_problems", "Fix the spell's problems first");
        add.accept(gui + "busy", "Already inscribing");
        add.accept("entity.thaumaturge.spell_wall", "Spell Wall");
        add.accept("entity.thaumaturge.spell_sprite", "Spell Sprite");
    }
}
