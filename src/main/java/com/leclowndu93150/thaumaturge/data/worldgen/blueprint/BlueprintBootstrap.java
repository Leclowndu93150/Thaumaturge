package com.leclowndu93150.thaumaturge.data.worldgen.blueprint;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.recipe.Blueprint;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintPart;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintSource;
import com.leclowndu93150.thaumaturge.api.recipe.BlueprintTarget;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
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
                Map.of('B', part(block(Blocks.NETHER_BRICKS), toBlock(TCBlocks.NETHER_BRICKS_PLACEHOLDER.get())), 'O', part(block(Blocks.OBSIDIAN), toBlock(TCBlocks.OBSIDIAN_PLACEHOLDER.get())), 'L',
                        part(block(Blocks.LAVA), toBlockOpposite(TCBlocks.INFERNAL_FURNACE.get())), 'I', part(block(Blocks.IRON_BARS), BlueprintTarget.Air.INSTANCE)),
                List.of(List.of("BOB", "O O", "BOB"), List.of("BOB", "OLO", "BIB"), List.of("BOB", "OOO", "BOB"))));

        register(ctx, "golem_press",
                new Blueprint(2, 2, 2,
                        Map.of('I', part(block(Blocks.IRON_BARS), toBlock(TCBlocks.PLACEHOLDER_IRON_BARS.get())), 'C', part(block(Blocks.CAULDRON), toBlock(TCBlocks.PLACEHOLDER_CAULDRON.get())), 'A',
                                part(block(Blocks.ANVIL), toBlock(TCBlocks.PLACEHOLDER_ANVIL.get())), 'P',
                                part(state(Blocks.PISTON.defaultBlockState().setValue(BlockStateProperties.EXTENDED, false).setValue(BlockStateProperties.FACING, Direction.UP)),
                                        toBlock(TCBlocks.GOLEM_BUILDER.get())),
                                'T', part(block(TCBlocks.TABLE_STONE.get()), toBlock(TCBlocks.PLACEHOLDER_TABLE.get()))),
                        List.of(List.of("  ", "I "), List.of("CA", "PT"))));

        register(ctx, "thaumatorium",
                new Blueprint(1, 3, 1,
                        Map.of('T', part(block(TCBlocks.ALCHEMICAL_CONSTRUCT.get()), toBlock(TCBlocks.THAUMATORIUM_TOP.get())), 'M',
                                part(block(TCBlocks.ALCHEMICAL_CONSTRUCT.get()), toBlock(TCBlocks.THAUMATORIUM.get())), 'B', part(block(TCBlocks.CRUCIBLE.get()), BlueprintTarget.Keep.INSTANCE)),
                        List.of(List.of("T"), List.of("M"), List.of("B"))));

        register(ctx, "infusion_altar", altar(TCBlocks.STONE_ARCANE.get(), TCBlocks.PILLAR_ARCANE.get(), TCBlocks.PEDESTAL_ARCANE.get()));
        register(ctx, "infusion_altar_ancient", altar(TCBlocks.STONE_ANCIENT_TILE.get(), TCBlocks.PILLAR_ANCIENT.get(), TCBlocks.PEDESTAL_ANCIENT.get()));
        register(ctx, "infusion_altar_eldritch", altar(TCBlocks.STONE_ELDRITCH_TILE.get(), TCBlocks.PILLAR_ELDRITCH.get(), TCBlocks.PEDESTAL_ELDRITCH.get()));
    }

    private static Blueprint altar(Block stone, Block pillar, Block pedestal) {
        return new Blueprint(3, 3, 3,
                Map.of('I', part(block(TCBlocks.INFUSION_MATRIX.get()), BlueprintTarget.Keep.INSTANCE), 'A', part(block(stone), BlueprintTarget.Air.INSTANCE), 'P',
                        part(block(pedestal), BlueprintTarget.Keep.INSTANCE), 'E', part(block(stone), toPillar(pillar, Direction.EAST)), 'N', part(block(stone), toPillar(pillar, Direction.NORTH)),
                        'S', part(block(stone), toPillar(pillar, Direction.SOUTH)), 'W', part(block(stone), toPillar(pillar, Direction.WEST))),
                List.of(List.of("   ", " I ", "   "), List.of("A A", "   ", "A A"), List.of("E N", " P ", "S W")));
    }

    private static void register(BootstrapContext<Blueprint> ctx, String name, Blueprint blueprint) {
        ctx.register(ResourceKey.create(Blueprint.REGISTRY_KEY, TCIds.rl(name)), blueprint);
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
