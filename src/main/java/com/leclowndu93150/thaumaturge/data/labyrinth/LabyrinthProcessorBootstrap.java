package com.leclowndu93150.thaumaturge.data.labyrinth;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.ClusteredReplaceProcessor;
import com.leclowndu93150.thaumaturge.content.eldritch.labyrinth.processor.ExposedSurfaceProcessor;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.templatesystem.AlwaysTrueTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.ProcessorRule;
import net.minecraft.world.level.levelgen.structure.templatesystem.RandomBlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public final class LabyrinthProcessorBootstrap {
    static final ResourceKey<StructureProcessorList> ELDRITCH_PALETTE = key("labyrinth/eldritch_palette");
    static final ResourceKey<StructureProcessorList> ELDRITCH_DECOR = key("labyrinth/eldritch_decor");
    static final ResourceKey<StructureProcessorList> OBELISK_WEATHERING = key("obelisk/weathering");
    static final ResourceKey<StructureProcessorList> CRUMBLING = key("labyrinth/crumbling");
    static final ResourceKey<StructureProcessorList> WEEPING = key("labyrinth/weeping");

    private static final float WALL_ROCK_CHANCE = 0.12F;
    private static final float CEILING_CRUST_CHANCE = 0.15F;
    private static final float DECOR_DENSITY = 0.02F;
    private static final int DECOR_PITCH = 5;
    private static final int DECOR_SALT = 17;
    private static final int CRUST_WEIGHT = 6;
    private static final int GLYPH_WEIGHT = 3;
    private static final int TRAP_WEIGHT = 1;
    private static final float OBSIDIAN_CHANCE = 0.25F;
    private static final float CRUMBLE_THRESHOLD = 0.45F;
    private static final int CRUMBLE_PITCH = 4;
    private static final int CRUMBLE_SALT = 31;
    private static final float WEEPING_THRESHOLD = 0.5F;
    private static final int WEEPING_PITCH = 5;
    private static final int WEEPING_SALT = 47;
    private static final int ROCK_WEIGHT = 3;
    private static final int INERT_WEIGHT = 2;
    private static final int OBSIDIAN_TILE_WEIGHT = 3;
    private static final int CRYING_WEIGHT = 1;

    private LabyrinthProcessorBootstrap() {}

    public static void bootstrap(BootstrapContext<StructureProcessorList> context) {
        BlockState stone = LabyrinthBlocks.stone();
        BlockState rock = LabyrinthBlocks.rock();
        BlockState crust = LabyrinthBlocks.crust();
        context.register(
                ELDRITCH_PALETTE,
                new StructureProcessorList(List.of(new RuleProcessor(List.of(
                        new ProcessorRule(
                                new RandomBlockMatchTest(stone.getBlock(), WALL_ROCK_CHANCE),
                                AlwaysTrueTest.INSTANCE,
                                rock),
                        new ProcessorRule(
                                new RandomBlockMatchTest(rock.getBlock(), CEILING_CRUST_CHANCE),
                                AlwaysTrueTest.INSTANCE,
                                crust))))));
        context.register(
                OBELISK_WEATHERING,
                new StructureProcessorList(List.of(new RuleProcessor(List.of(new ProcessorRule(
                        new RandomBlockMatchTest(LabyrinthBlocks.obsidianTile().getBlock(), OBSIDIAN_CHANCE),
                        AlwaysTrueTest.INSTANCE,
                        Blocks.OBSIDIAN.defaultBlockState()))))));
        BlockState glowingCrust = LabyrinthBlocks.glowingCrust();
        SimpleWeightedRandomList<BlockState> wall = SimpleWeightedRandomList.<BlockState>builder()
                .add(glowingCrust, CRUST_WEIGHT)
                .add(LabyrinthBlocks.glyph(), GLYPH_WEIGHT)
                .add(LabyrinthBlocks.trap(), TRAP_WEIGHT)
                .build();
        context.register(
                CRUMBLING,
                new StructureProcessorList(List.of(new ClusteredReplaceProcessor(
                        new BlockMatchTest(stone.getBlock()),
                        SimpleWeightedRandomList.<BlockState>builder()
                                .add(rock, ROCK_WEIGHT)
                                .add(LabyrinthBlocks.inert(), INERT_WEIGHT)
                                .build(),
                        CRUMBLE_THRESHOLD,
                        CRUMBLE_PITCH,
                        CRUMBLE_SALT))));
        context.register(
                WEEPING,
                new StructureProcessorList(List.of(new ClusteredReplaceProcessor(
                        new BlockMatchTest(stone.getBlock()),
                        SimpleWeightedRandomList.<BlockState>builder()
                                .add(LabyrinthBlocks.obsidianTile(), OBSIDIAN_TILE_WEIGHT)
                                .add(Blocks.CRYING_OBSIDIAN.defaultBlockState(), CRYING_WEIGHT)
                                .build(),
                        WEEPING_THRESHOLD,
                        WEEPING_PITCH,
                        WEEPING_SALT))));
        context.register(
                ELDRITCH_DECOR,
                new StructureProcessorList(List.of(new ExposedSurfaceProcessor(
                        new BlockMatchTest(stone.getBlock()),
                        SimpleWeightedRandomList.empty(),
                        wall,
                        SimpleWeightedRandomList.single(glowingCrust),
                        DECOR_DENSITY,
                        DECOR_PITCH,
                        DECOR_SALT))));
    }

    private static ResourceKey<StructureProcessorList> key(String path) {
        return ResourceKey.create(Registries.PROCESSOR_LIST, TTIds.rl(path));
    }
}
