package com.leclowndu93150.thaumaturge.content.wands.assembly;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandCap;
import com.leclowndu93150.thaumaturge.content.wands.ItemWandRod;
import com.leclowndu93150.thaumaturge.content.wands.WandEconomy;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import com.leclowndu93150.thaumaturge.registry.TCWandParts;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

public final class WandAssemblyPackResources implements PackResources {
    private final PackLocationInfo location;
    private @Nullable Map<Identifier, byte[]> entries;

    public WandAssemblyPackResources(PackLocationInfo location) {
        this.location = location;
    }

    private synchronized Map<Identifier, byte[]> entries() {
        if (entries == null) {
            entries = generate();
        }
        return entries;
    }

    private static Map<Identifier, byte[]> generate() {
        Map<Identifier, byte[]> out = new LinkedHashMap<>();
        Map<Identifier, Identifier> capItems = new LinkedHashMap<>();
        Map<Identifier, Identifier> rodItems = new LinkedHashMap<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ItemWandCap capItem && capItem.cap() != null) {
                Identifier partId = TCWandParts.caps().getKey(capItem.cap());
                if (partId != null) {
                    capItems.putIfAbsent(partId, BuiltInRegistries.ITEM.getKey(item));
                }
            } else if (item instanceof ItemWandRod rodItem && rodItem.rod() != null) {
                Identifier partId = TCWandParts.rods().getKey(rodItem.rod());
                if (partId != null) {
                    rodItems.putIfAbsent(partId, BuiltInRegistries.ITEM.getKey(item));
                }
            }
        }
        Identifier woodRod = TCIds.rl("wood");
        if (TCWandParts.rods().containsKey(woodRod)) {
            rodItems.putIfAbsent(woodRod, BuiltInRegistries.ITEM.getKey(Items.STICK));
        }

        Identifier ironCap = TCIds.rl("iron");
        for (Map.Entry<Identifier, Identifier> capEntry : capItems.entrySet()) {
            WandCap cap = TCWandParts.caps().getValue(capEntry.getKey());
            for (Map.Entry<Identifier, Identifier> rodEntry : rodItems.entrySet()) {
                WandRod rod = TCWandParts.rods().getValue(rodEntry.getKey());
                boolean starterCombo = capEntry.getKey().equals(ironCap) && rodEntry.getKey().equals(woodRod);
                if (!starterCombo) {
                    int vis = WandEconomy.PRIMAL_COUNT * cap.craftCost() * rod.craftCost();
                    out.put(recipeFile("assembly", capEntry.getKey(), rodEntry.getKey()), recipe(capEntry, rodEntry, false, vis, assemblyGate(rod)));
                }
                if (!rod.staff()) {
                    int vis = WandEconomy.PRIMAL_COUNT * (int) (cap.craftCost() * rod.craftCost() * WandEconomy.SCEPTRE_CRAFT_COST_FACTOR);
                    out.put(recipeFile("sceptre", capEntry.getKey(), rodEntry.getKey()), recipe(capEntry, rodEntry, true, vis, TCIds.rl("sceptre").toString()));
                }
            }
        }
        Thaumaturge.LOGGER.info("Serving {} wand assembly recipes from {} caps and {} rods", out.size(), capItems.size(), rodItems.size());
        return out;
    }

    private static String assemblyGate(WandRod rod) {
        Identifier gate = rod.assemblyResearch();
        return gate != null ? gate.toString() : TCIds.rl("unlock_auromancy").toString();
    }

    private static Identifier recipeFile(String kind, Identifier capId, Identifier rodId) {
        boolean modPair = capId.getNamespace().equals(TCIds.MODID) && rodId.getNamespace().equals(TCIds.MODID);
        String name = modPair ? capId.getPath() + "_" + rodId.getPath() : capId.getNamespace() + "_" + capId.getPath() + "_" + rodId.getNamespace() + "_" + rodId.getPath();
        return TCIds.rl("recipe/wand/" + kind + "/" + name + ".json");
    }

    private static byte[] recipe(Map.Entry<Identifier, Identifier> cap, Map.Entry<Identifier, Identifier> rod, boolean sceptre, int vis, String gate) {
        JsonObject key = new JsonObject();
        key.addProperty("C", cap.getValue().toString());
        key.addProperty("R", rod.getValue().toString());
        JsonArray pattern = new JsonArray();
        if (sceptre) {
            key.addProperty("P", BuiltInRegistries.ITEM.getKey(TCItems.PRIMAL_CHARM.get()).toString());
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
        components.add(TCIds.rl("wand_parts").toString(), parts);
        JsonObject result = new JsonObject();
        result.add("components", components);
        result.addProperty("id", TCIds.rl("wand").toString());
        JsonObject json = new JsonObject();
        json.addProperty("type", TCIds.rl("arcane_workbench_shaped").toString());
        json.add("crystals", new JsonArray());
        json.add("key", key);
        json.add("pattern", pattern);
        json.add("research", research);
        json.add("result", result);
        json.addProperty("vis", vis);
        return json.toString().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String... path) {
        return null;
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(PackType type, Identifier id) {
        if (type != PackType.SERVER_DATA) {
            return null;
        }
        byte[] data = entries().get(id);
        return data == null ? null : () -> new ByteArrayInputStream(data);
    }

    @Override
    public void listResources(PackType type, String namespace, String directory, ResourceOutput output) {
        if (type != PackType.SERVER_DATA || !namespace.equals(TCIds.MODID)) {
            return;
        }
        for (Map.Entry<Identifier, byte[]> entry : entries().entrySet()) {
            if (entry.getKey().getPath().startsWith(directory)) {
                byte[] data = entry.getValue();
                output.accept(entry.getKey(), () -> new ByteArrayInputStream(data));
            }
        }
    }

    @Override
    public Set<String> getNamespaces(PackType type) {
        return type == PackType.SERVER_DATA ? Set.of(TCIds.MODID) : Set.of();
    }

    @Override
    public <T> @Nullable T getMetadataSection(MetadataSectionType<T> serializer) throws IOException {
        return null;
    }

    @Override
    public PackLocationInfo location() {
        return location;
    }

    @Override
    public void close() {}
}
