package com.leclowndu93150.thaumaturge.content.aspect;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.aspect.AspectDataMaps;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.mojang.serialization.JsonOps;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.fml.loading.FMLPaths;

public final class AspectIndexFile {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final int FORMAT_VERSION = 3;

    private AspectIndexFile() {}

    public static Path path() {
        return FMLPaths.CONFIGDIR.get().resolve("thaumaturge").resolve("aspect_index.json");
    }

    public static String fingerprint(MinecraftServer server) {
        List<String> lines = new ArrayList<>();
        lines.add("format:" + FORMAT_VERSION);
        for (Identifier id : BuiltInRegistries.ITEM.keySet()) {
            lines.add("item:" + id);
        }
        for (Item item : BuiltInRegistries.ITEM) {
            AspectList declared = item.builtInRegistryHolder().getData(AspectDataMaps.BASE_ASPECTS);
            if (declared == null || declared.isEmpty()) {
                continue;
            }
            List<String> parts = new ArrayList<>(declared.entries().size());
            for (var entry : declared.entries()) {
                parts.add(entry.aspect().unwrapKey().map(key -> key.identifier().toString()).orElse("?") + "=" + entry.amount());
            }
            Collections.sort(parts);
            lines.add("base:" + BuiltInRegistries.ITEM.getKey(item) + ":" + String.join(",", parts));
        }
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            lines.add("recipe:" + holder.id().identifier());
        }
        Collections.sort(lines);
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

    public static Optional<AspectIndex> load(HolderLookup.Provider registries, String liveFingerprint) {
        Path file = path();
        if (!Files.exists(file)) {
            return Optional.empty();
        }
        JsonObject root;
        try {
            root = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        } catch (IOException | RuntimeException e) {
            Thaumaturge.LOGGER.error("Failed to read aspect index cache at {}, rebuilding from recipes", file, e);
            return Optional.empty();
        }
        String savedFingerprint = GsonHelper.getAsString(root, "fingerprint", "");
        if (!savedFingerprint.equals(liveFingerprint)) {
            Thaumaturge.LOGGER.info("Aspect index cache at {} no longer matches the current item, recipe, and base-aspect sets ({} items / {} recipes when written), rebuilding", file,
                    GsonHelper.getAsInt(root, "item_count", -1), GsonHelper.getAsInt(root, "recipe_count", -1));
            return Optional.empty();
        }
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        JsonObject aspects = GsonHelper.getAsJsonObject(root, "aspects", new JsonObject());
        Map<Item, AspectIndex.ItemEntry> map = new HashMap<>(aspects.size());
        int skipped = 0;
        for (Map.Entry<String, JsonElement> entry : aspects.entrySet()) {
            Identifier id = Identifier.tryParse(entry.getKey());
            Item item = id == null ? null : BuiltInRegistries.ITEM.getValue(id);
            if (item == null) {
                skipped++;
                continue;
            }
            AspectIndex.ItemEntry parsed = AspectIndex.ItemEntry.CODEC.parse(ops, entry.getValue()).result().orElse(null);
            if (parsed == null || parsed.isEmpty()) {
                skipped++;
                continue;
            }
            map.put(item, parsed);
        }
        if (skipped > 0) {
            Thaumaturge.LOGGER.warn("Skipped {} unreadable or unresolvable entries in aspect index cache at {}", skipped, file);
        }
        if (map.isEmpty()) {
            Thaumaturge.LOGGER.warn("Aspect index cache at {} contained no usable entries, rebuilding from recipes", file);
            return Optional.empty();
        }
        Thaumaturge.LOGGER.info("Loaded aspect index for {} items from {}", map.size(), file);
        return Optional.of(AspectIndex.ofEntries(map));
    }

    public static void write(AspectIndex index, HolderLookup.Provider registries, String fingerprint, int itemCount, int recipeCount) {
        Path file = path();
        RegistryOps<JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, registries);
        JsonElement aspects = AspectIndex.CODEC.encodeStart(ops, index).result().orElse(null);
        if (aspects == null) {
            Thaumaturge.LOGGER.error("Failed to encode aspect index, not writing cache to {}", file);
            return;
        }
        JsonObject root = new JsonObject();
        root.addProperty("fingerprint", fingerprint);
        root.addProperty("item_count", itemCount);
        root.addProperty("recipe_count", recipeCount);
        root.add("aspects", aspects);
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, GSON.toJson(root));
            Thaumaturge.LOGGER.info("Wrote aspect index for {} items to {}", index.size(), file);
        } catch (IOException e) {
            Thaumaturge.LOGGER.error("Failed to write aspect index cache to {}", file, e);
        }
    }
}
