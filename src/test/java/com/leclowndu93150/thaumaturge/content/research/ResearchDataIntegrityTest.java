package com.leclowndu93150.thaumaturge.content.research;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.research.ResearchParent;
import com.leclowndu93150.thaumaturge.api.research.TTResearchCategories;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

final class ResearchDataIntegrityTest {
    private static final Set<ResourceLocation> INTENTIONAL_HIDDEN_PARENTS = Set.of(
            id("gotthaumonomicon"),
            id("bathsalts"),
            id("f_toomuchflux"),
            id("scanned/totemundying"),
            id("scanned/orecrystal"),
            id("scanned/fluxrift"),
            id("scanned/dragonbreath"),
            id("scanned/pechwand"),
            id("scanned/orblock1"),
            id("scanned/orblock2"),
            id("instability"),
            id("eldritchmajor"));

    @Test
    void builtInResearchHasValidCategoriesAndPrerequisiteLinks() throws IOException {
        Path resourceRoot = Path.of(System.getProperty("thaumaturge.testResourceRoot", "src/main/resources"));
        Path sourceRoot = resourceRoot.getParent().getParent();
        Path generatedRoot = sourceRoot.resolve("generated/resources/data/thaumaturge/thaumaturge");
        Path researchRoot = resourceRoot.resolve("data/thaumaturge/thaumaturge/research_entry");
        assertTrue(Files.isDirectory(researchRoot), "Research resource directory is missing: " + researchRoot);

        Map<ResourceLocation, JsonObject> entries = new TreeMap<>();
        Map<ResourceLocation, Path> sources = new HashMap<>();
        List<String> problems = new ArrayList<>();
        try (Stream<Path> files = Files.walk(researchRoot)) {
            for (Path file : files.filter(path -> path.toString().endsWith(".json"))
                    .sorted()
                    .toList()) {
                ResourceLocation id = resourceId(researchRoot.relativize(file));
                try {
                    JsonElement parsed = JsonParser.parseString(Files.readString(file, StandardCharsets.UTF_8));
                    if (!parsed.isJsonObject()) {
                        problems.add(file + ": expected a JSON object");
                        continue;
                    }
                    if (entries.putIfAbsent(id, parsed.getAsJsonObject()) != null) {
                        problems.add(file + ": duplicate research id " + id + " (also " + sources.get(id) + ")");
                    } else {
                        sources.put(id, file);
                    }
                } catch (RuntimeException e) {
                    problems.add(file + ": invalid JSON: " + e.getMessage());
                }
            }
        }

        Set<ResourceLocation> categories = readResourceIds(generatedRoot.resolve("research_category"));
        if (categories.isEmpty()) {
            categories = Set.of(
                    TTResearchCategories.BASICS.location(),
                    TTResearchCategories.AUROMANCY.location(),
                    TTResearchCategories.ALCHEMY.location(),
                    TTResearchCategories.ARTIFICE.location(),
                    TTResearchCategories.INFUSION.location(),
                    TTResearchCategories.GOLEMANCY.location(),
                    TTResearchCategories.ELDRITCH.location());
        }
        Set<ResourceLocation> knownProgress = new HashSet<>(entries.keySet());
        for (Path scanFile : jsonFiles(generatedRoot.resolve("scan"))) {
            JsonObject scan = JsonParser.parseString(Files.readString(scanFile, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            ResourceLocation key = ResourceLocation.tryParse(scan.get("key").getAsString());
            if (key != null) {
                knownProgress.add(key);
            }
        }
        for (ResourceLocation aspect : readResourceIds(generatedRoot.resolve("aspect"))) {
            knownProgress.add(ResourceLocation.fromNamespaceAndPath(
                    TTIds.MODID, "scanned/aspect/" + aspect.getNamespace() + "/" + aspect.getPath()));
        }
        knownProgress.addAll(INTENTIONAL_HIDDEN_PARENTS);
        knownProgress.addAll(Set.of(
                id("f_onfire"),
                id("m_deepdown"),
                id("m_uphigh"),
                id("m_hellandback"),
                id("m_endoftheworld"),
                id("m_walker"),
                id("m_runner"),
                id("m_jumper"),
                id("m_swimmer")));
        Map<ResourceLocation, Set<ResourceLocation>> prerequisites = new TreeMap<>();
        Map<ResourceLocation, ResourceLocation> categoryGates = new HashMap<>();
        for (Path categoryFile : jsonFiles(generatedRoot.resolve("research_category"))) {
            JsonObject category = JsonParser.parseString(Files.readString(categoryFile, StandardCharsets.UTF_8))
                    .getAsJsonObject();
            JsonElement gateJson = category.get("required_research");
            if (gateJson == null) {
                continue;
            }
            ResourceLocation gate =
                    gateJson.isJsonPrimitive() && gateJson.getAsJsonPrimitive().isString()
                            ? ResourceLocation.tryParse(gateJson.getAsString())
                            : null;
            if (gate == null) {
                problems.add(categoryFile + ": invalid required_research category gate");
            } else {
                ResourceLocation categoryId =
                        resourceId(generatedRoot.resolve("research_category").relativize(categoryFile));
                categoryGates.put(categoryId, gate);
                checkLink(categoryId, categoryFile.toString(), gate, 0, entries, knownProgress, problems);
            }
        }

        for (Map.Entry<ResourceLocation, JsonObject> entry : entries.entrySet()) {
            ResourceLocation id = entry.getKey();
            JsonObject data = entry.getValue();
            String source = sources.get(id).toString();
            checkLocalReference(data, "category", id, source, categories, "research category", problems);

            JsonArray stages = array(data, "stages", id, source, problems);
            if (stages == null || stages.isEmpty()) {
                problems.add(source + ": research entry must have at least one stage");
                continue;
            }

            Set<ResourceLocation> links = new HashSet<>();
            JsonElement categoryJson = data.get("category");
            ResourceLocation categoryId = categoryJson != null
                            && categoryJson.isJsonPrimitive()
                            && categoryJson.getAsJsonPrimitive().isString()
                    ? ResourceLocation.tryParse(categoryJson.getAsString())
                    : null;
            ResourceLocation categoryGate = categoryId == null ? null : categoryGates.get(categoryId);
            if (categoryGate != null) {
                links.add(categoryGate);
            }
            JsonArray parents = array(data, "parents", id, source, problems);
            if (parents != null) {
                for (JsonElement parentJson : parents) {
                    if (!parentJson.isJsonPrimitive()
                            || !parentJson.getAsJsonPrimitive().isString()) {
                        problems.add(source + ": parent must be a string");
                        continue;
                    }
                    String value = parentJson.getAsString();
                    ResearchParent parent = ResearchParent.CODEC
                            .parse(JsonOps.INSTANCE, new JsonPrimitive(value))
                            .resultOrPartial(
                                    error -> problems.add(source + ": invalid parent '" + value + "': " + error))
                            .orElse(null);
                    if (parent == null) {
                        continue;
                    }
                    checkParent(id, source, parent.id(), parent.stage(), entries, knownProgress, problems);
                    if (isLocal(parent.id())) {
                        links.add(parent.id());
                    }
                }
            }

            for (int index = 0; index < stages.size(); index++) {
                JsonElement stageJson = stages.get(index);
                if (!stageJson.isJsonObject()) {
                    problems.add(source + ": stage " + index + " must be an object");
                    continue;
                }
                JsonObject stageData = stageJson.getAsJsonObject();
                if (!stageData.has("text")
                        || !stageData.get("text").isJsonPrimitive()
                        || !stageData.getAsJsonPrimitive("text").isString()) {
                    problems.add(source + ": stage " + index + " is missing its text translation key");
                }
                JsonArray required = array(stageData, "required_research", id, source, problems);
                if (required == null) {
                    continue;
                }
                for (JsonElement requiredJson : required) {
                    if (!requiredJson.isJsonPrimitive()
                            || !requiredJson.getAsJsonPrimitive().isString()) {
                        problems.add(source + ": required_research entry in stage " + index + " must be a string");
                        continue;
                    }
                    ResourceLocation requiredId = ResourceLocation.tryParse(requiredJson.getAsString());
                    if (requiredId == null) {
                        problems.add(source + ": invalid required_research id '" + requiredJson.getAsString() + "'");
                        continue;
                    }
                    checkLink(id, source, requiredId, 0, entries, knownProgress, problems);
                    if (isLocal(requiredId)) {
                        links.add(requiredId);
                    }
                }
            }
            prerequisites.put(id, links);
        }

        findCycles(prerequisites, problems);
        assertTrue(problems.isEmpty(), () -> "Research data integrity errors:\n - " + String.join("\n - ", problems));
    }

    @Test
    void missingProgressReferenceIsReported() {
        List<String> problems = new ArrayList<>();
        checkLink(id("child"), "child.json", id("missing"), 0, Map.of(), Set.of(), problems);
        assertFalse(problems.isEmpty(), "The integrity checker must reject an unresolved prerequisite");
    }

    @Test
    void scanProgressKeyCannotSilentlyReplaceAVisibleParentEntry() {
        List<String> problems = new ArrayList<>();
        ResourceLocation scanKey = id("f_bat");
        checkParent(id("child"), "child.json", scanKey, 0, Map.of(), Set.of(scanKey), problems);
        assertFalse(problems.isEmpty(), "A progress key without a research entry cannot serve as a visible parent");
    }

    private static JsonArray array(
            JsonObject object, String key, ResourceLocation id, String source, List<String> problems) {
        JsonElement value = object.get(key);
        if (value == null) {
            return null;
        }
        if (!value.isJsonArray()) {
            problems.add(source + ": " + key + " on " + id + " must be an array");
            return null;
        }
        return value.getAsJsonArray();
    }

    private static void checkLocalReference(
            JsonObject object,
            String field,
            ResourceLocation owner,
            String source,
            Set<ResourceLocation> known,
            String kind,
            List<String> problems) {
        JsonElement value = object.get(field);
        if (value == null
                || !value.isJsonPrimitive()
                || !value.getAsJsonPrimitive().isString()) {
            problems.add(source + ": missing or invalid " + kind + " on " + owner);
            return;
        }
        ResourceLocation reference = ResourceLocation.tryParse(value.getAsString());
        if (reference == null || (isLocal(reference) && !known.contains(reference))) {
            problems.add(source + ": unknown " + kind + " '" + value.getAsString() + "' on " + owner);
        }
    }

    private static void checkLink(
            ResourceLocation owner,
            String source,
            ResourceLocation target,
            int stage,
            Map<ResourceLocation, JsonObject> entries,
            Set<ResourceLocation> knownProgress,
            List<String> problems) {
        if (!isLocal(target)) {
            return;
        }
        JsonObject targetData = entries.get(target);
        if (targetData == null) {
            if (!knownProgress.contains(target)) {
                problems.add(source + ": " + owner + " references unknown research/progress key " + target);
            }
            return;
        }
        if (stage < 0
                || stage > 0
                        && (targetData.get("stages") == null
                                || !targetData.get("stages").isJsonArray()
                                || stage > targetData.getAsJsonArray("stages").size())) {
            problems.add(source + ": " + owner + " requires unavailable stage @" + stage + " of " + target);
        }
    }

    private static void checkParent(
            ResourceLocation owner,
            String source,
            ResourceLocation target,
            int stage,
            Map<ResourceLocation, JsonObject> entries,
            Set<ResourceLocation> knownProgress,
            List<String> problems) {
        if (isLocal(target) && !entries.containsKey(target) && !INTENTIONAL_HIDDEN_PARENTS.contains(target)) {
            problems.add(source + ": parent " + target
                    + " is not a visible research entry; add an entry or explicitly mark an intentional hidden parent");
        }
        checkLink(owner, source, target, stage, entries, knownProgress, problems);
    }

    private static List<Path> jsonFiles(Path root) throws IOException {
        if (!Files.isDirectory(root)) {
            return List.of();
        }
        try (Stream<Path> files = Files.walk(root)) {
            return files.filter(path -> path.toString().endsWith(".json"))
                    .sorted()
                    .toList();
        }
    }

    private static Set<ResourceLocation> readResourceIds(Path root) throws IOException {
        Set<ResourceLocation> ids = new HashSet<>();
        for (Path file : jsonFiles(root)) {
            ids.add(resourceId(root.relativize(file)));
        }
        return ids;
    }

    private static void findCycles(Map<ResourceLocation, Set<ResourceLocation>> graph, List<String> problems) {
        Map<ResourceLocation, Integer> states = new HashMap<>();
        Deque<ResourceLocation> path = new ArrayDeque<>();
        for (ResourceLocation id : graph.keySet()) {
            visit(id, graph, states, path, problems);
        }
    }

    private static void visit(
            ResourceLocation id,
            Map<ResourceLocation, Set<ResourceLocation>> graph,
            Map<ResourceLocation, Integer> states,
            Deque<ResourceLocation> path,
            List<String> problems) {
        int state = states.getOrDefault(id, 0);
        if (state == 2) {
            return;
        }
        if (state == 1) {
            problems.add("cyclic research prerequisites: " + path + " -> " + id);
            return;
        }
        states.put(id, 1);
        path.addLast(id);
        for (ResourceLocation prerequisite : graph.getOrDefault(id, Set.of())) {
            if (graph.containsKey(prerequisite)) {
                visit(prerequisite, graph, states, path, problems);
            }
        }
        path.removeLast();
        states.put(id, 2);
    }

    private static ResourceLocation resourceId(Path relativePath) {
        String path = relativePath.toString().replace('\\', '/');
        return ResourceLocation.fromNamespaceAndPath(TTIds.MODID, path.substring(0, path.length() - ".json".length()));
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(TTIds.MODID, path);
    }

    private static boolean isLocal(ResourceLocation id) {
        return TTIds.MODID.equals(id.getNamespace());
    }
}
