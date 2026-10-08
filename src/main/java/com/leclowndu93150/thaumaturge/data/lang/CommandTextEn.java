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
        add.accept(
                "commands.thaumaturge.research.category", "Completed the %s category for %s (%s research completed)");
        add.accept(
                "commands.thaumaturge.research.grant",
                "Completed %s and its prerequisites for %s (%s research completed)");
        add.accept(
                "commands.thaumaturge.research.ready", "%s is ready to research for %s (%s prerequisites completed)");
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

        add.accept("commands.thaumaturge.locate.node.invalid_type", "Unknown node type: %s");
        add.accept(
                "commands.thaumaturge.locate.node.not_found",
                "No indexed %s node found beyond 10 blocks in this dimension. Nodes are indexed as chunks load.");
        add.accept("commands.thaumaturge.locate.node.copy", "Click to copy coordinates");
        add.accept("commands.thaumaturge.locate.node.found", "Nearest indexed %s node is at %s (%s blocks away)");

        add.accept(
                "commands.thaumaturge.build.infusion_altar.height",
                "The infusion altar does not fit inside the build height here");
        add.accept(
                "commands.thaumaturge.build.infusion_altar.unloaded", "Part of the infusion altar area is not loaded");
        add.accept(
                "commands.thaumaturge.build.infusion_altar.success", "Built an infusion altar centered at %s, %s, %s");

        add.accept("commands.thaumaturge.showcase.built", "Built a showcase of %s blocks and %s items from %s, %s, %s");
        add.accept("commands.thaumaturge.showcase.too_tall", "The showcase does not fit inside the build height here");
        add.accept("commands.thaumaturge.showcase.unloaded", "Part of the showcase area is not loaded");

        add.accept("commands.thaumaturge.tools.failed", "Command %s failed, see the server log");
        add.accept("commands.thaumaturge.tools.unknown_trait", "Unknown mob trait: %s");
        add.accept("commands.thaumaturge.tools.not_living", "Only living entities can be tainted: %s");
        add.accept("commands.thaumaturge.tools.unknown_research", "Unknown research entry: %s");
        add.accept("commands.thaumaturge.tools.unknown_aspect", "Unknown aspect: %s");
        add.accept("commands.thaumaturge.tools.spawn_failed", "Could not create %s");
        add.accept("commands.thaumaturge.tools.spawned", "Spawned %s");
        add.accept("commands.thaumaturge.tools.research.reset", "Reset %s research entries and all knowledge");
        add.accept(
                "commands.thaumaturge.tools.research.all",
                "Granted %s research entries and all aspect research points");
        add.accept("commands.thaumaturge.tools.research.not_known", "Research %s is not known");
        add.accept("commands.thaumaturge.tools.research.revoke_failed", "Could not revoke research %s");
        add.accept("commands.thaumaturge.tools.research.revoked", "Revoked research %s");
        add.accept("commands.thaumaturge.tools.research.already_complete", "Research %s is already complete");
        add.accept("commands.thaumaturge.tools.research.grant_failed", "Could not grant research %s");
        add.accept("commands.thaumaturge.tools.research.granted", "Unlocked research %s");
        add.accept("commands.thaumaturge.tools.aspect.all", "Granted %s research points to all %s aspects");
        add.accept("commands.thaumaturge.tools.aspect.one", "Granted %s research points of %s");
        add.accept("commands.thaumaturge.tools.warp.info", "Warp: permanent %s, normal %s, temporary %s, counter %s");
        add.accept("commands.thaumaturge.tools.warp.unknown_type", "Unknown warp type: %s");
        add.accept("commands.thaumaturge.tools.warp.added", "Added %s %s warp");
        add.accept("commands.thaumaturge.tools.warp.removed", "Removed %s %s warp");
        add.accept("commands.thaumaturge.tools.warp.cleared", "Warp cleared");
        add.accept("commands.thaumaturge.tools.warp.event", "Warp event check rolled");
        add.accept("commands.thaumaturge.tools.aura.info", "Aura at %s: vis %s, flux %s, base %s");
        add.accept("commands.thaumaturge.tools.aura.vis_drained", "Drained %s vis");
        add.accept("commands.thaumaturge.tools.aura.vis_added", "Added %s vis");
        add.accept("commands.thaumaturge.tools.aura.flux_drained", "Drained %s flux");
        add.accept("commands.thaumaturge.tools.aura.flux_added", "Added %s flux");
        add.accept("commands.thaumaturge.tools.feature.placed", "Placed %s");
        add.accept(
                "commands.thaumaturge.tools.feature.refused",
                "The feature refused to place here (bad soil or no clearance)");
        add.accept("commands.thaumaturge.tools.taint.spread", "Forced taint spread at %s");
        add.accept("commands.thaumaturge.tools.flux.goo", "Placed flux goo at level %s");
        add.accept("commands.thaumaturge.tools.flux.gas", "Placed flux gas at level %s");
        add.accept("commands.thaumaturge.tools.flux_event.unknown", "Unknown flux event: %s");
        add.accept(
                "commands.thaumaturge.tools.flux_event.blocked",
                "Flux event %s could not trigger here (it needs %s local flux, a valid target, and flux pressure events enabled)");
        add.accept("commands.thaumaturge.tools.flux_event.triggered", "Triggered flux event %s for %s flux");
        add.accept("commands.thaumaturge.tools.streampath.direct", "Direct line of sight, no waypoints needed");
        add.accept("commands.thaumaturge.tools.streampath.none", "No path found (%s nodes searched)");
        add.accept(
                "commands.thaumaturge.tools.streampath.route",
                "Route with %s waypoints (%s nodes searched), all segments clear: %s");
        add.accept(
                "commands.thaumaturge.tools.streampath.route_blocked",
                "Route with %s waypoints (%s nodes searched), segment %s is blocked: %s");
        add.accept("commands.thaumaturge.tools.rift", "Spawned a flux rift of size %s");
        add.accept("commands.thaumaturge.tools.champion.unknown", "Unknown champion modifier: %s");
        add.accept("commands.thaumaturge.tools.champion.not_mob", "Champion modifiers only apply to mobs: %s");
        add.accept("commands.thaumaturge.tools.champion.spawned", "Spawned %s champion %s");
        add.accept("commands.thaumaturge.tools.tainted", "Summoned tainted %s");
        add.accept("commands.thaumaturge.tools.trait.added", "Added %s to %s entities");
        add.accept("commands.thaumaturge.tools.trait.removed", "Removed %s from %s entities");
        add.accept("commands.thaumaturge.tools.trait.none", "%s has no traits");
        add.accept("commands.thaumaturge.tools.trait.list", "%s: %s");
        add.accept(
                "commands.thaumaturge.tools.focus.socketed",
                "Socketed a focus (complexity %s/%s) into the held caster");
        add.accept("commands.thaumaturge.tools.focus.given", "Gave a focus (complexity %s/%s)");
        add.accept("commands.thaumaturge.tools.crystal", "Gave a crystal of %s");
        add.accept("commands.thaumaturge.tools.particle.header", "Thaumaturge particle demos");
        add.accept(
                "commands.thaumaturge.tools.particle.usage",
                "Use /%s particle <name> to spawn one 3 blocks in front of you");
        add.accept("commands.thaumaturge.tools.particle.entry", "%s: %s");
        add.accept("commands.thaumaturge.tools.particle.total", "%s demos in total");
        add.accept("commands.thaumaturge.tools.particle.unknown", "Unknown demo: %s, try /%s particle list");
        add.accept("commands.thaumaturge.tools.particle.spawned", "Spawned demo: %s");
        add.accept("commands.thaumaturge.tools.node.created", "Node created");
        add.accept("commands.thaumaturge.tools.node.failed", "Could not place a node here");
        add.accept("commands.thaumaturge.tools.node.unknown_type", "Unknown node type: %s");
        add.accept(
                "commands.thaumaturge.tools.node.pairs",
                "Aspects must come in pairs: <aspect> <amount> [<aspect> <amount> ...]");
        add.accept("commands.thaumaturge.tools.node.bad_amount", "Amounts must be positive whole numbers: %s");
        add.accept("commands.thaumaturge.tools.link.removed", "Removed %s research links");
        add.accept("commands.thaumaturge.tools.unknown_spell_part", "Unknown spell part: %s");
    }
}
