package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.gametest.base.TCTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;

public final class EssentiaTests {
    private static final BlockPos JAR_POS = new BlockPos(2, 2, 3);

    private EssentiaTests() {}

    public static void register(TCTestRegistrar r) {
        r.add("essentia/jar_fill_to_capacity", 40, helper -> {
            BlockEntityJar jar = placeJar(helper);
            if (jar == null) {
                return;
            }
            Holder<IAspect> ignis = resolve(helper, TCAspects.IGNIS);
            int leftover = jar.fill(ignis, BlockEntityJar.CAPACITY + 50);
            if (leftover != 50) {
                helper.fail("Expected 50 leftover past capacity, got " + leftover);
                return;
            }
            int stored = jar.getContents(helper.getLevel().registryAccess()).amountOf(ignis);
            if (stored != BlockEntityJar.CAPACITY) {
                helper.fail("Jar should hold exactly capacity, holds " + stored);
                return;
            }
            helper.succeed();
        });

        r.add("essentia/jar_rejects_mixed_aspects", 40, helper -> {
            BlockEntityJar jar = placeJar(helper);
            if (jar == null) {
                return;
            }
            Holder<IAspect> ignis = resolve(helper, TCAspects.IGNIS);
            Holder<IAspect> aqua = resolve(helper, TCAspects.AQUA);
            jar.fill(ignis, 10);
            int leftover = jar.fill(aqua, 10);
            if (leftover != 10) {
                helper.fail("Jar accepted a second aspect type; leftover " + leftover);
                return;
            }
            helper.succeed();
        });

        r.add("essentia/jar_take_roundtrip", 40, helper -> {
            BlockEntityJar jar = placeJar(helper);
            if (jar == null) {
                return;
            }
            Holder<IAspect> ignis = resolve(helper, TCAspects.IGNIS);
            jar.fill(ignis, 40);
            if (!jar.drain(ignis, 25)) {
                helper.fail("drain refused a valid withdrawal");
                return;
            }
            int stored = jar.getContents(helper.getLevel().registryAccess()).amountOf(ignis);
            if (stored != 15) {
                helper.fail("Expected 15 essentia after withdrawal, found " + stored);
                return;
            }
            if (jar.drain(ignis, 100)) {
                helper.fail("drain allowed overdraw");
                return;
            }
            helper.succeed();
        });
    }

    private static Holder<IAspect> resolve(GameTestHelper helper, net.minecraft.resources.ResourceKey<IAspect> key) {
        Holder<IAspect> holder = Aspects.resolve(helper.getLevel().registryAccess(), key);
        if (holder == null) {
            throw new IllegalStateException("Aspect " + key + " missing from the registry");
        }
        return holder;
    }

    private static BlockEntityJar placeJar(GameTestHelper helper) {
        helper.setBlock(JAR_POS.below(), Blocks.STONE.defaultBlockState());
        helper.setBlock(JAR_POS, TCBlocks.JAR_NORMAL.get().defaultBlockState());
        BlockEntityJar jar = helper.getBlockEntity(JAR_POS, BlockEntityJar.class);
        if (jar == null) {
            helper.fail("Jar block entity missing after placement");
        }
        return jar;
    }
}
