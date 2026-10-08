package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.room.RoomShapeRules;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

public final class LabyrinthRoomProvider implements DataProvider {
    private final PackOutput.PathProvider path;

    public LabyrinthRoomProvider(PackOutput output) {
        this.path = TemplateNbtWriter.structures(output);
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        List<CompletableFuture<?>> writes = new ArrayList<>();
        List<String> failures = new ArrayList<>();
        for (RoomRecipe recipe : RoomCatalogData.recipes()) {
            RoomCanvas canvas = new RoomCanvas(recipe.width(), recipe.depth(), LabyrinthBlocks::passable);
            recipe.body().accept(canvas);
            canvas.skin(LabyrinthBlocks.skin());
            if (recipe.decorate()) {
                RoomRounding.apply(canvas);
            }
            List<String> problems = RoomShapeRules.check(canvas, recipe.width(), recipe.depth(), recipe.sockets());
            if (!problems.isEmpty()) {
                failures.add(recipe.name() + ": " + String.join("; ", problems));
                continue;
            }
            writes.add(TemplateNbtWriter.save(output, path, recipe.template(), TemplateNbtWriter.write(canvas)));
        }
        if (!failures.isEmpty()) {
            return CompletableFuture.failedFuture(
                    new IllegalStateException("Invalid labyrinth rooms:\n" + String.join("\n", failures)));
        }
        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Thaumaturge labyrinth rooms";
    }
}
