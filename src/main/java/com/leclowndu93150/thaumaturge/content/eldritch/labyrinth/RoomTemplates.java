package com.leclowndu93150.thaumaturge.content.eldritch.labyrinth;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.CompiledRoom;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.RoomCompiler;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.RoomShapeRules;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.random.WeightedEntry.Wrapper;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public final class RoomTemplates {
    private final Map<ResourceLocation, CompiledRoom> rooms = new ConcurrentHashMap<>();

    public Optional<CompiledRoom> get(
            StructureTemplateManager manager, HolderLookup.Provider registries, ResourceLocation id) {
        Optional<StructureTemplate> template = manager.get(id);
        if (template.isEmpty()) {
            return Optional.empty();
        }
        CompiledRoom cached = rooms.get(id);
        if (cached != null && cached.template() == template.get()) {
            return Optional.of(cached);
        }
        CompiledRoom compiled = RoomCompiler.compile(id, template.get(), registries);
        rooms.put(id, compiled);
        return Optional.of(compiled);
    }

    public boolean usable(StructureTemplateManager manager, HolderLookup.Provider registries, Holder<RoomType> room) {
        String roomId = RoomType.id(room);
        for (Wrapper<ResourceLocation> entry : room.value().templates().unwrap()) {
            Optional<CompiledRoom> compiled = get(manager, registries, entry.data());
            if (compiled.isEmpty()) {
                Thaumaturge.LOGGER.error("Labyrinth room {} names missing template {}", roomId, entry.data());
                return false;
            }
            CompiledRoom value = compiled.get();
            boolean valid = value.validFor(roomId, template -> validate(roomId, room.value(), template));
            if (!valid) {
                return false;
            }
        }
        return true;
    }

    private static boolean validate(String roomId, RoomType room, CompiledRoom compiled) {
        List<String> problems = RoomShapeRules.check(compiled, room.width(), room.depth(), room.sockets());
        if (!problems.isEmpty()) {
            Thaumaturge.LOGGER.error(
                    "Labyrinth room {} template {} is not usable: {}",
                    roomId,
                    compiled.id(),
                    String.join("; ", problems));
        }
        return problems.isEmpty();
    }
}
