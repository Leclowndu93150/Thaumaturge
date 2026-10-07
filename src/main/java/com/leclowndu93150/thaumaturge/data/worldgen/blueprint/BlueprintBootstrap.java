package com.leclowndu93150.thaumaturge.data.worldgen.blueprint;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintSource;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintTarget;
import com.leclowndu93150.thaumaturge.registry.TTBlocks;
import java.util.List;
import java.util.Map;
import net.minecraft.core.Direction;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class BlueprintBootstrap {
    private static final int DEFAULT_PRIORITY = 50;

    private BlueprintBootstrap() {}

    public static void bootstrap(BootstrapContext<Blueprint> ctx) {
        register(ctx, "infernal_furnace", new Blueprint(3, 3, 3,
                Map.of('B', part(block(Blocks.NETHER_BRICKS), toBlock(TTBlocks.NETHER_BRICKS_PLACEHOLDER.get())), 'O', part(block(Blocks.OBSIDIAN), toBlock(TTBlocks.OBSIDIAN_PLACEHOLDER.get())), 'L',
                        part(block(Blocks.LAVA), toBlockOpposite(TTBlocks.INFERNAL_FURNACE.get())), 'I', part(block(Blocks.IRON_BARS), BlueprintTarget.Air.INSTANCE)),
                List.of(List.of("BOB", "O O", "BOB"), List.of("BOB", "OLO", "BIB"), List.of("BOB", "OOO", "BOB"))));

        register(ctx, "golem_press",
                new Blueprint(2, 2, 2,
                        Map.of('I', part(block(Blocks.IRON_BARS), toBlock(TTBlocks.PLACEHOLDER_IRON_BARS.get())), 'C', part(block(Blocks.CAULDRON), toBlock(TTBlocks.PLACEHOLDER_CAULDRON.get())), 'A',
                                part(block(Blocks.ANVIL), toBlock(TTBlocks.PLACEHOLDER_ANVIL.get())), 'P',
                                part(state(Blocks.PISTON.defaultBlockState().setValue(BlockStateProperties.EXTENDED, false).setValue(BlockStateProperties.FACING, Direction.UP)),
                                        toBlock(TTBlocks.GOLEM_BUILDER.get())),
                                'T', part(block(TTBlocks.TABLE_STONE.get()), toBlock(TTBlocks.PLACEHOLDER_TABLE.get()))),
                        List.of(List.of("  ", "I "), List.of("CA", "PT"))));

        register(ctx, "advanced_alchemical_furnace",
                new Blueprint(3, 2, 3,
                        Map.of('A', part(block(TTBlocks.ALEMBIC.get()), toBlock(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ALEMBIC_PLACEHOLDER.get())), 'C',
                                part(block(TTBlocks.ALCHEMICAL_CONSTRUCT.get()), toBlock(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_CONSTRUCT_PLACEHOLDER.get())), 'V',
                                part(block(TTBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get()), toBlock(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_ADVANCED_CONSTRUCT_PLACEHOLDER.get())), 'N',
                                part(block(TTBlocks.ADVANCED_ALCHEMICAL_CONSTRUCT.get()), toBlock(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE_NOZZLE.get())), 'F',
                                part(block(TTBlocks.SMELTER_BASIC.get()), toBlock(TTBlocks.ADVANCED_ALCHEMICAL_FURNACE.get()))),
                        List.of(List.of("ACA", "C C", "ACA"), List.of("VNV", "NFN", "VNV"))));

        register(ctx, "thaumatorium",
                new Blueprint(1, 3, 1,
                        Map.of('T', part(block(TTBlocks.ALCHEMICAL_CONSTRUCT.get()), toBlock(TTBlocks.THAUMATORIUM_TOP.get())), 'M',
                                part(block(TTBlocks.ALCHEMICAL_CONSTRUCT.get()), new BlueprintTarget.BlockTarget(TTBlocks.THAUMATORIUM.get(), true, false)), 'B',
                                part(block(TTBlocks.CRUCIBLE.get()), BlueprintTarget.Keep.INSTANCE)),
                        List.of(List.of("T"), List.of("M"), List.of("B"))));

        register(ctx, "infusion_altar", altar(TTBlocks.STONE_ARCANE.get(), TTBlocks.PILLAR_ARCANE.get(), TTBlocks.PEDESTAL_ARCANE.get()));
        register(ctx, "infusion_altar_ancient", altar(TTBlocks.STONE_ANCIENT_TILE.get(), TTBlocks.PILLAR_ANCIENT.get(), TTBlocks.PEDESTAL_ANCIENT.get()));
        register(ctx, "infusion_altar_eldritch", altar(TTBlocks.STONE_ELDRITCH_TILE.get(), TTBlocks.PILLAR_ELDRITCH.get(), TTBlocks.PEDESTAL_ELDRITCH.get()));
    }

    private static Blueprint altar(Block stone, Block pillar, Block pedestal) {
        return new Blueprint(3, 3, 3,
                Map.of('I', part(block(TTBlocks.INFUSION_MATRIX.get()), BlueprintTarget.Keep.INSTANCE), 'A', part(block(stone), BlueprintTarget.Air.INSTANCE), 'P',
                        part(block(pedestal), BlueprintTarget.Keep.INSTANCE), 'E', part(block(stone), toPillar(pillar, Direction.EAST)), 'N', part(block(stone), toPillar(pillar, Direction.NORTH)),
                        'S', part(block(stone), toPillar(pillar, Direction.SOUTH)), 'W', part(block(stone), toPillar(pillar, Direction.WEST))),
                List.of(List.of("   ", " I ", "   "), List.of("A A", "   ", "A A"), List.of("E N", " P ", "S W")));
    }

    private static void register(BootstrapContext<Blueprint> ctx, String name, Blueprint blueprint) {
        ctx.register(ResourceKey.create(Blueprint.REGISTRY_KEY, TTIds.rl(name)), blueprint);
    }

    private static BlueprintPart part(BlueprintSource source, BlueprintTarget target) {
        return new BlueprintPart(source, target, DEFAULT_PRIORITY);
    }

    private static BlueprintSource block(Block block) {
        return new BlueprintSource.BlockSource(block);
    }

    private static BlueprintSource state(BlockState state) {
        return new BlueprintSource.StateSource(state);
    }

    private static BlueprintTarget toBlock(Block block) {
        return new BlueprintTarget.BlockTarget(block, false, false);
    }

    private static BlueprintTarget toBlockOpposite(Block block) {
        return new BlueprintTarget.BlockTarget(block, false, true);
    }

    private static BlueprintTarget toPillar(Block pillar, Direction facing) {
        return new BlueprintTarget.StateTarget(pillar.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
    }
}
