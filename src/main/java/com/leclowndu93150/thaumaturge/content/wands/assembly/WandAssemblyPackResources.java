package com.leclowndu93150.thaumaturge.content.wands.assembly;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.api.wands.WandAssemblyPart;
import com.leclowndu93150.thaumaturge.api.wands.WandAssemblyPart.Variant;
import com.leclowndu93150.thaumaturge.api.wands.RegisterWandAssemblyEvent;
import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.mojang.serialization.JsonOps;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionType;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.NeoForge;
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
        RegisterWandAssemblyEvent registration = new RegisterWandAssemblyEvent();
        for (Item item : BuiltInRegistries.ITEM) {
            if (item instanceof ItemWandCap capItem && capItem.cap() != null) {
                Identifier partId = TTWandParts.caps().getKey(capItem.cap());
                if (partId != null) {
                    registration.registerCap(new WandAssemblyPart<>(ResourceKey.create(WandCap.REGISTRY_KEY, partId), Ingredient.of(item), List.of(), Set.of(Variant.values()), true));
                }
            } else if (item instanceof ItemWandRod rodItem && rodItem.rod() != null) {
                Identifier partId = TTWandParts.rods().getKey(rodItem.rod());
                if (partId != null) {
                    registration.registerRod(new WandAssemblyPart<>(ResourceKey.create(WandRod.REGISTRY_KEY, partId), Ingredient.of(item), List.of(),
                            rodItem.rod().staff() ? Set.of(Variant.STAFF) : Set.of(Variant.WAND, Variant.SCEPTRE), true));
                }
            }
        }
        Identifier woodRod = TTIds.rl("wood");
        if (TTWandParts.rods().containsKey(woodRod)) {
            registration.registerRod(new WandAssemblyPart<>(ResourceKey.create(WandRod.REGISTRY_KEY, woodRod), Ingredient.of(Items.STICK), List.of(), Set.of(Variant.WAND, Variant.SCEPTRE), true));
        }
        NeoForge.EVENT_BUS.post(registration);
        registration.close();
        Map<Identifier, WandAssemblyPart<WandCap>> capItems = registration.caps();
        Map<Identifier, WandAssemblyPart<WandRod>> rodItems = registration.rods();
        capItems.keySet().forEach(id -> {
            if (!TTWandParts.caps().containsKey(id)) {
                throw new IllegalArgumentException("Unknown wand assembly cap: " + id);
            }
        });
        rodItems.keySet().forEach(id -> {
            if (!TTWandParts.rods().containsKey(id)) {
                throw new IllegalArgumentException("Unknown wand assembly rod: " + id);
            }
        });

        Identifier ironCap = TTIds.rl("iron");
        for (var capEntry : capItems.entrySet()) {
            WandCap cap = TTWandParts.caps().getValue(capEntry.getKey());
            for (var rodEntry : rodItems.entrySet()) {
                WandRod rod = TTWandParts.rods().getValue(rodEntry.getKey());
                if (!capEntry.getValue().enabled() || !rodEntry.getValue().enabled()) {
                    continue;
                }
                boolean starterCombo = capEntry.getKey().equals(ironCap) && rodEntry.getKey().equals(woodRod);
                Variant variant = rod.staff() ? Variant.STAFF : Variant.WAND;
                if (!starterCombo && capEntry.getValue().variants().contains(variant) && rodEntry.getValue().variants().contains(variant)) {
                    int vis = WandEconomy.PRIMAL_COUNT * cap.craftCost() * rod.craftCost();
                    out.put(recipeFile("assembly", capEntry.getKey(), rodEntry.getKey()), recipe(capEntry, rodEntry, false, vis, assemblyGate(rod)));
                }
                if (!rod.staff() && capEntry.getValue().variants().contains(Variant.SCEPTRE) && rodEntry.getValue().variants().contains(Variant.SCEPTRE)) {
                    int vis = WandEconomy.PRIMAL_COUNT * (int) (cap.craftCost() * rod.craftCost() * WandEconomy.SCEPTRE_CRAFT_COST_FACTOR);
                    out.put(recipeFile("sceptre", capEntry.getKey(), rodEntry.getKey()), recipe(capEntry, rodEntry, true, vis, assemblyGate(rod)));
                }
            }
        }
        Thaumaturge.LOGGER.info("Serving {} wand assembly recipes from {} caps and {} rods", out.size(), capItems.size(), rodItems.size());
        return out;
    }

    private static String assemblyGate(WandRod rod) {
        Identifier gate = rod.assemblyResearch();
        return gate != null ? gate.toString() : TTIds.rl("unlock_auromancy").toString();
    }

    private static Identifier recipeFile(String kind, Identifier capId, Identifier rodId) {
        boolean modPair = capId.getNamespace().equals(TTIds.MODID) && rodId.getNamespace().equals(TTIds.MODID);
        String name = modPair ? capId.getPath() + "_" + rodId.getPath() : "external/" + encode(capId) + "/" + encode(rodId);
        return TTIds.rl("recipe/wand/" + kind + "/" + name + ".json");
    }

    private static String encode(Identifier id) {
        return java.util.HexFormat.of().formatHex(id.toString().getBytes(StandardCharsets.UTF_8));
    }

    private static byte[] recipe(Map.Entry<Identifier, WandAssemblyPart<WandCap>> cap, Map.Entry<Identifier, WandAssemblyPart<WandRod>> rod, boolean sceptre, int vis, String gate) {
        JsonObject key = new JsonObject();
        var ops = RegistryOps.create(JsonOps.INSTANCE, RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY));
        key.add("C", Ingredient.CODEC.encodeStart(ops, cap.getValue().ingredient()).getOrThrow());
        key.add("R", Ingredient.CODEC.encodeStart(ops, rod.getValue().ingredient()).getOrThrow());
        JsonArray pattern = new JsonArray();
        if (sceptre) {
            key.addProperty("P", BuiltInRegistries.ITEM.getKey(TTItems.PRIMAL_CHARM.get()).toString());
            pattern.add(" CP");
            pattern.add(" RC");
            pattern.add("C  ");
        } else {
            pattern.add("  C");
            pattern.add(" R ");
            pattern.add("C  ");
        }
        List<ResearchGate> gates = new ArrayList<>();
        gates.add(new ResearchGate(Identifier.parse(gate), Optional.empty(), false));
        gates.addAll(cap.getValue().research());
        gates.addAll(rod.getValue().research());
        if (sceptre) {
            gates.add(new ResearchGate(TTIds.rl("sceptre"), Optional.empty(), false));
        }
        JsonObject research = new JsonObject();
        JsonArray requirements = new JsonArray();
        gates.stream().distinct().forEach(requirement -> requirements.add(ResearchGate.CODEC.encodeStart(JsonOps.INSTANCE, requirement).getOrThrow()));
        research.add("all_of", requirements);
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
        if (type != PackType.SERVER_DATA || !namespace.equals(TTIds.MODID)) {
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
        return type == PackType.SERVER_DATA ? Set.of(TTIds.MODID) : Set.of();
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
