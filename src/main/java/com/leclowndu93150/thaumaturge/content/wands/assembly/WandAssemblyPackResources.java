package com.leclowndu93150.thaumaturge.content.wands.assembly;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandCap;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandRod;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import com.leclowndu93150.thaumaturge.registry.TTWandParts;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public final class WandAssemblyPackResources implements PackResources {
    private final PackLocationInfo location;
    private @Nullable Map<ResourceLocation, byte[]> entries;

    public WandAssemblyPackResources(PackLocationInfo location) {
        this.location = location;
    }

    private synchronized Map<ResourceLocation, byte[]> entries() {
        if (entries == null) {
            entries = generate();
        }
        return entries;
    }

    private static Map<ResourceLocation, byte[]> generate() {
        Map<ResourceLocation, byte[]> out = new LinkedHashMap<>();
        Map<ResourceLocation, ResourceLocation> capItems = new LinkedHashMap<>();
        Map<ResourceLocation, ResourceLocation> rodItems = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ItemWandCap capItem && capItem.cap() != null) {
                ResourceLocation partId = TTWandParts.caps().getKey(capItem.cap());
                if (partId != null) {
                    capItems.putIfAbsent(partId, BuiltInRegistries.ITEM.getKey(item));
                }
            } else if (item instanceof ItemWandRod rodItem && rodItem.rod() != null) {
                ResourceLocation partId = TTWandParts.rods().getKey(rodItem.rod());
                if (partId != null) {
                    rodItems.putIfAbsent(partId, BuiltInRegistries.ITEM.getKey(item));
                }
            }
        }
        ResourceLocation woodRod = TTIds.rl("wood");
        if (TTWandParts.rods().containsKey(woodRod)) {
            rodItems.putIfAbsent(woodRod, BuiltInRegistries.ITEM.getKey(Items.STICK));
        }

        ResourceLocation ironCap = TTIds.rl("iron");
        for (Map.Entry<ResourceLocation, ResourceLocation> capEntry : capItems.entrySet()) {
            WandCap cap = TTWandParts.caps().get(capEntry.getKey());
            for (Map.Entry<ResourceLocation, ResourceLocation> rodEntry : rodItems.entrySet()) {
                WandRod rod = TTWandParts.rods().get(rodEntry.getKey());
                boolean starterCombo =
                        capEntry.getKey().equals(ironCap) && rodEntry.getKey().equals(woodRod);
                if (!starterCombo) {
                    int vis = WandEconomy.PRIMAL_COUNT * cap.craftCost() * rod.craftCost();
                    out.put(
                            recipeFile("assembly", capEntry.getKey(), rodEntry.getKey()),
                            recipe(capEntry, rodEntry, false, vis, assemblyGate(rod)));
                }
                if (!rod.staff()) {
                    int vis = WandEconomy.PRIMAL_COUNT
                            * (int) (cap.craftCost() * rod.craftCost() * WandEconomy.SCEPTRE_CRAFT_COST_FACTOR);
                    out.put(
                            recipeFile("sceptre", capEntry.getKey(), rodEntry.getKey()),
                            recipe(
                                    capEntry,
                                    rodEntry,
                                    true,
                                    vis,
                                    TTIds.rl("sceptre").toString()));
                }
            }
        }
        Thaumaturge.LOGGER.info(
                "Serving {} wand assembly recipes from {} caps and {} rods",
                out.size(),
                capItems.size(),
                rodItems.size());
        return out;
    }

    private static String assemblyGate(WandRod rod) {
        ResourceLocation gate = rod.assemblyResearch();
        return gate != null ? gate.toString() : TTIds.rl("unlock_auromancy").toString();
    }

    private static ResourceLocation recipeFile(String kind, ResourceLocation capId, ResourceLocation rodId) {
        boolean modPair =
                capId.getNamespace().equals(TTIds.MODID) && rodId.getNamespace().equals(TTIds.MODID);
        String name = modPair
                ? capId.getPath() + "_" + rodId.getPath()
                : capId.getNamespace() + "_" + capId.getPath() + "_" + rodId.getNamespace() + "_" + rodId.getPath();
        return TTIds.rl("recipe/wand/" + kind + "/" + name + ".json");
    }

    private static byte[] recipe(
            Map.Entry<ResourceLocation, ResourceLocation> cap,
            Map.Entry<ResourceLocation, ResourceLocation> rod,
            boolean sceptre,
            int vis,
            String gate) {
        JsonObject key = new JsonObject();
        key.add("C", ingredient(cap.getValue()));
        key.add("R", ingredient(rod.getValue()));
        JsonArray pattern = new JsonArray();
        if (sceptre) {
            key.add("P", ingredient(BuiltInRegistries.ITEM.getKey(TTItems.PRIMAL_CHARM.get())));
            pattern.add(" CP");
            pattern.add(" RC");
            pattern.add("C  ");
        } else {
            pattern.add("  C");
            pattern.add(" R ");
            pattern.add("C  ");
        }
        JsonObject research = new JsonObject();
        research.addProperty("entry", gate);
        JsonObject parts = new JsonObject();
        parts.addProperty("cap", cap.getKey().toString());
        parts.addProperty("rod", rod.getKey().toString());
        if (sceptre) {
            parts.addProperty("sceptre", true);
        }
        JsonObject components = new JsonObject();
        components.add(TTIds.rl("wand_parts").toString(), parts);
        JsonObject result = new JsonObject();
        result.add("components", components);
        result.addProperty("id", TTIds.rl("wand").toString());
        JsonObject json = new JsonObject();
        json.addProperty("type", TTIds.rl("arcane_workbench_shaped").toString());
        json.add("crystals", new JsonArray());
        json.add("key", key);
        json.add("pattern", pattern);
        json.add("research", research);
        json.add("result", result);
        json.addProperty("vis", vis);
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

    private static JsonObject ingredient(ResourceLocation item) {
        JsonObject json = new JsonObject();
        json.addProperty("item", item.toString());
        return json;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... path) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, ResourceLocation id) {
        if (type != PackType.SERVER_DATA) {
            return null;
        }
        byte[] data = entries().get(id);
        return data == null ? null : () -> new ByteArrayInputStream(data);
    }

    @Override
    public void listResources(PackType type, String namespace, String directory, ResourceOutput output) {
        if (type != PackType.SERVER_DATA || !namespace.equals(TTIds.MODID)) {
            return;
        }
        for (Map.Entry<ResourceLocation, byte[]> entry : entries().entrySet()) {
            if (entry.getKey().getPath().startsWith(directory)) {
                byte[] data = entry.getValue();
                output.accept(entry.getKey(), () -> new ByteArrayInputStream(data));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.SERVER_DATA ? Set.of(TTIds.MODID) : Set.of();
    }

    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionSerializer<T> serializer) throws IOException {
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return location;
    }

    @Override
    public void close() {}
}
