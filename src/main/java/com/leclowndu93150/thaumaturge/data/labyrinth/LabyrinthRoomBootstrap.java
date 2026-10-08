package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomTransforms;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.definition.RoomType;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public final class LabyrinthRoomBootstrap {
    private LabyrinthRoomBootstrap() {}

    public static void bootstrap(BootstrapContext<RoomType> context) {
        HolderGetter<StructureProcessorList> processors = context.lookup(Registries.PROCESSOR_LIST);
        for (RoomRecipe recipe : RoomCatalogData.recipes()) {
            context.register(
                    recipe.key(),
                    new RoomType(
                            SimpleWeightedRandomList.single(recipe.template()),
                            recipe.width(),
                            recipe.depth(),
                            recipe.sockets(),
                            RoomTransforms.ALL,
                            recipe.weight(),
                            recipe.maxPerMaze(),
                            0,
                            recipe.processors().map(processors::getOrThrow),
                            recipe.decorate(),
                            recipe.varietyGroup().map(TTIds::rl)));
        }
    }
}
