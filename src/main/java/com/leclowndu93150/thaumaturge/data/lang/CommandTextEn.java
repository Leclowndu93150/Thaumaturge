package com.leclowndu93150.thaumaturge.data.lang;

import java.util.function.BiConsumer;

public final class CommandTextEn {
    private CommandTextEn() {}

    public static void addAll(BiConsumer<String, String> add) {
        add.accept("commands.thaumaturge.targets", "%s players");
        add.accept("commands.thaumaturge.separator", ", ");

        add.accept("commands.thaumaturge.research.unknown_entry", "Unknown research entry: %s");
        add.accept("commands.thaumaturge.research.unknown_category", "Unknown research category: %s");
        add.accept("commands.thaumaturge.research.reset", "Reset research and knowledge for %s");
        add.accept("commands.thaumaturge.research.everything", "Completed all research for %s (%s entries)");
        add.accept("commands.thaumaturge.research.stage", "Set %s to the %s stage (%s research completed)");
        add.accept("commands.thaumaturge.research.category", "Completed the %s category for %s (%s research completed)");
        add.accept("commands.thaumaturge.research.grant", "Completed %s and its prerequisites for %s (%s research completed)");
        add.accept("commands.thaumaturge.research.ready", "%s is ready to research for %s (%s prerequisites completed)");
        add.accept("commands.thaumaturge.research.revoke", "Revoked %s and %s research that depends on it from %s");
        add.accept("commands.thaumaturge.research.query", "%s has completed %s of %s research entries");
        add.accept("commands.thaumaturge.research.query.category", "%s: %s/%s");

        add.accept("commands.thaumaturge.knowledge.add", "Added %1$s %2$s knowledge in %3$s for %4$s");
        add.accept("commands.thaumaturge.knowledge.remove", "Removed %1$s %2$s knowledge in %3$s from %4$s");
        add.accept("commands.thaumaturge.knowledge.set", "Set %2$s knowledge in %3$s to %1$s for %4$s");
        add.accept("commands.thaumaturge.knowledge.all_categories", "every category");
        add.accept("commands.thaumaturge.knowledge.query", "Knowledge of %s:");
        add.accept("commands.thaumaturge.knowledge.query.category", "%s: %s");
        add.accept("commands.thaumaturge.knowledge.query.entry", "%s %s");

        add.accept("commands.thaumaturge.aspects.unknown", "Unknown aspect: %s");
        add.accept("commands.thaumaturge.aspects.every", "every aspect");
        add.accept("commands.thaumaturge.aspects.give", "Gave %s points of %s to %s");
        add.accept("commands.thaumaturge.aspects.take", "Took %s points of %s from %s");
        add.accept("commands.thaumaturge.aspects.set", "Set %s to %s points for %s");
        add.accept("commands.thaumaturge.aspects.discover", "Discovered %s for %s");
        add.accept("commands.thaumaturge.aspects.reset", "Reset the aspect pool for %s");
        add.accept("commands.thaumaturge.aspects.query", "%s has discovered %s aspects: %s");
        add.accept("commands.thaumaturge.aspects.query.none", "%s has not discovered any aspects");
        add.accept("commands.thaumaturge.aspects.query.entry", "%s %s");

        add.accept("commands.thaumaturge.showcase.built", "Built a showcase of %s blocks and %s items from %s, %s, %s");
        add.accept("commands.thaumaturge.showcase.too_tall", "The showcase does not fit inside the build height here");
        add.accept("commands.thaumaturge.showcase.unloaded", "Part of the showcase area is not loaded");
    }
}
