package com.leclowndu93150.thaumaturge.data.labyrinth;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;

public final class SparseTemplateProvider implements DataProvider {
    private final PackOutput.PathProvider path;

    public SparseTemplateProvider(PackOutput output) {
        this.path = TemplateNbtWriter.structures(output);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        Map<ResourceLocation, SiteCanvas> templates = new HashMap<>(ObeliskSiteTemplates.all());
        templates.putAll(ArenaTemplates.all());
        return CompletableFuture.allOf(templates.entrySet().stream()
                .map(entry ->
                        TemplateNbtWriter.save(output, path, entry.getKey(), TemplateNbtWriter.write(entry.getValue())))
                .toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Thaumaturge sparse templates";
    }
}
