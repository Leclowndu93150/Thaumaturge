package com.leclowndu93150.thaumaturge.content.aspect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectDataMaps;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;

public record AspectIndexFingerprint(String digest, Map<String, String> sections) {
    private static final int FORMAT_VERSION = 5;
    private static final String ITEMS = "items";
    private static final String BASE_ASPECTS = "base_aspects";
    private static final String RECIPES_PREFIX = "recipes/";
    private static final int MAX_LOGGED_SECTIONS = 12;

    public static AspectIndexFingerprint compute(MinecraftServer server) {
        Map<String, List<String>> lines = new TreeMap<>();
        List<String> items = lines.computeIfAbsent(ITEMS, key -> new ArrayList<>());
        for (ResourceLocation id : BuiltInRegistries.ITEM.keySet()) {
            items.add(id.toString());
        }
        List<String> base = lines.computeIfAbsent(BASE_ASPECTS, key -> new ArrayList<>());
        for (Item item : BuiltInRegistries.ITEM) {
            AspectList declared = item.builtInRegistryHolder().getData(AspectDataMaps.BASE_ASPECTS);
            if (declared == null || declared.isEmpty()) {
                continue;
            }
            List<String> parts = new ArrayList<>(declared.entries().size());
            for (var entry : declared.entries()) {
                parts.add(entry.aspect()
                                .unwrapKey()
                                .map(key -> key.location().toString())
                                .orElse("?") + "=" + entry.amount());
            }
            Collections.sort(parts);
            base.add(BuiltInRegistries.ITEM.getKey(item) + ":" + String.join(",", parts));
        }
        RegistryOps<JsonElement> ops = server.registryAccess().createSerializationContext(JsonOps.INSTANCE);
        int unencodable = 0;
        String firstUnencodable = null;
        RuntimeException firstFailure = null;
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            ResourceLocation id = holder.id();
            String contents;
            try {
                DataResult<JsonElement> encoded = Recipe.CODEC.encodeStart(ops, holder.value());
                contents =
                        encoded.result().map(AspectIndexFingerprint::canonical).orElse(null);
            } catch (RuntimeException e) {
                contents = null;
                if (firstFailure == null) {
                    firstFailure = e;
                }
            }
            if (contents == null) {
                unencodable++;
                if (firstUnencodable == null) {
                    firstUnencodable = id.toString();
                }
                contents = "!" + holder.value().getClass().getName();
            }
            lines.computeIfAbsent(RECIPES_PREFIX + id.getNamespace(), key -> new ArrayList<>())
                    .add(id + ":" + contents);
        }
        if (unencodable > 0) {
            Thaumaturge.LOGGER.warn(
                    "{} recipes could not be encoded for the aspect index fingerprint (first: {}); edits to their contents will not invalidate the cache",
                    unencodable,
                    firstUnencodable,
                    firstFailure);
        }
        Map<String, String> sections = new TreeMap<>();
        for (Map.Entry<String, List<String>> entry : lines.entrySet()) {
            Collections.sort(entry.getValue());
            sections.put(entry.getKey(), sha256(entry.getValue()));
        }
        List<String> summary = new ArrayList<>(sections.size() + 1);
        summary.add("format:" + FORMAT_VERSION);
        for (Map.Entry<String, String> entry : sections.entrySet()) {
            summary.add(entry.getKey() + "=" + entry.getValue());
        }
        return new AspectIndexFingerprint(sha256(summary), Collections.unmodifiableMap(sections));
    }

    public JsonObject sectionsJson() {
        JsonObject json = new JsonObject();
        for (Map.Entry<String, String> entry : sections.entrySet()) {
            json.addProperty(entry.getKey(), entry.getValue());
        }
        return json;
    }

    public String describeChanges(JsonObject saved) {
        List<String> changed = new ArrayList<>();
        for (Map.Entry<String, String> entry : sections.entrySet()) {
            JsonElement previous = saved.get(entry.getKey());
            if (previous == null
                    || !previous.isJsonPrimitive()
                    || !previous.getAsString().equals(entry.getValue())) {
                changed.add(entry.getKey());
            }
        }
        for (String key : saved.keySet()) {
            if (!sections.containsKey(key)) {
                changed.add(key);
            }
        }
        if (changed.isEmpty()) {
            return "format";
        }
        Collections.sort(changed);
        if (changed.size() > MAX_LOGGED_SECTIONS) {
            int rest = changed.size() - MAX_LOGGED_SECTIONS;
            return String.join(", ", changed.subList(0, MAX_LOGGED_SECTIONS)) + " and " + rest + " more";
        }
        return String.join(", ", changed);
    }

    private static String canonical(JsonElement element) {
        StringBuilder out = new StringBuilder();
        appendCanonical(element, out);
        return out.toString();
    }

    private static void appendCanonical(JsonElement element, StringBuilder out) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            List<String> keys = new ArrayList<>(object.keySet());
            Collections.sort(keys);
            out.append('{');
            for (int i = 0; i < keys.size(); i++) {
                if (i > 0) {
                    out.append(',');
                }
                out.append(keys.get(i)).append(':');
                appendCanonical(object.get(keys.get(i)), out);
            }
            out.append('}');
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            out.append('[');
            for (int i = 0; i < array.size(); i++) {
                if (i > 0) {
                    out.append(',');
                }
                appendCanonical(array.get(i), out);
            }
            out.append(']');
        } else {
            out.append(element);
        }
    }

    private static String sha256(List<String> lines) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
        for (String line : lines) {
            digest.update(line.getBytes(StandardCharsets.UTF_8));
            digest.update((byte) '\n');
        }
        return HexFormat.of().formatHex(digest.digest());
    }
}
