package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.content.aspect.AspectCombinations;
import com.leclowndu93150.thaumaturge.content.manabean.BlockEntityManaPod;
import com.leclowndu93150.thaumaturge.content.manabean.BlockManaPod;
import com.leclowndu93150.thaumaturge.gametest.base.TCTestRegistrar;
import com.leclowndu93150.thaumaturge.registry.TCBlocks;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public final class ManaBeanTests {
    private ManaBeanTests() {}

    private static BlockEntityManaPod placePod(GameTestHelper helper, BlockPos pos, int age, ResourceKey<IAspect> aspect) {
        helper.setBlock(pos.above(), Blocks.OAK_LOG);
        helper.getLevel().setBlock(helper.absolutePos(pos), TCBlocks.MANA_POD.get().defaultBlockState().setValue(BlockManaPod.AGE, age), 3);
        BlockEntityManaPod pod = (BlockEntityManaPod) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        if (pod != null && aspect != null) {
            pod.setAspect(aspect);
        }
        return pod;
    }

    public static void register(TCTestRegistrar r) {
        r.add("manabean/combination_lookup", 20, helper -> {
            var registries = helper.getLevel().registryAccess();
            var aspects = registries.lookupOrThrow(IAspect.REGISTRY_KEY);
            Holder<IAspect> combo = AspectCombinations.result(registries, aspects.getOrThrow(TCAspects.AER), aspects.getOrThrow(TCAspects.ORDO));
            if (combo == null || !combo.is(TCAspects.MOTUS)) {
                helper.fail("aer + ordo should combine into motus, got " + combo);
                return;
            }
            helper.succeed();
        });

        r.add("manabean/pod_growth_assigns_aspect", 60, helper -> {
            BlockEntityManaPod pod = placePod(helper, new BlockPos(2, 2, 2), 0, null);
            if (pod == null) {
                helper.fail("Pod block entity missing");
                return;
            }
            for (int i = 0; i < BlockEntityManaPod.MAX_AGE; i++) {
                pod.checkGrowth();
            }
            int age = helper.getLevel().getBlockState(helper.absolutePos(new BlockPos(2, 2, 2))).getValue(BlockManaPod.AGE);
            if (age != BlockEntityManaPod.MAX_AGE) {
                helper.fail("Pod did not reach full age, at " + age);
                return;
            }
            if (pod.aspectKey() == null) {
                helper.fail("Grown pod has no aspect assigned");
                return;
            }
            helper.succeed();
        });

        r.add("manabean/cross_breeding", 60, helper -> {
            placePod(helper, new BlockPos(1, 2, 2), 7, TCAspects.AER);
            placePod(helper, new BlockPos(3, 2, 2), 7, TCAspects.ORDO);
            BlockEntityManaPod center = placePod(helper, new BlockPos(2, 2, 2), 2, null);
            if (center == null) {
                helper.fail("Center pod missing");
                return;
            }
            center.checkGrowth();
            ResourceKey<IAspect> result = center.aspectKey();
            Set<ResourceKey<IAspect>> allowed = Set.of(TCAspects.AER, TCAspects.ORDO, TCAspects.MOTUS);
            if (result == null || !allowed.contains(result)) {
                helper.fail("Cross-breed produced unexpected aspect " + result);
                return;
            }
            helper.succeed();
        });

        r.add("manabean/harvest_copies_aspect", 60, helper -> {
            BlockPos pos = new BlockPos(2, 2, 2);
            placePod(helper, pos, 7, TCAspects.IGNIS);
            helper.getLevel().destroyBlock(helper.absolutePos(pos), true);
            AABB box = new AABB(helper.absolutePos(pos)).inflate(2.0);
            boolean found = false;
            for (ItemEntity drop : helper.getLevel().getEntitiesOfClass(ItemEntity.class, box)) {
                ItemStack stack = drop.getItem();
                if (stack.is(TCItems.MANA_BEAN.get())) {
                    var instance = stack.get(TCDataComponents.CRYSTAL_ASPECT.get());
                    if (instance != null && instance.aspect().is(TCAspects.IGNIS)) {
                        found = true;
                    }
                }
            }
            if (!found) {
                helper.fail("No ignis mana bean dropped from the harvested pod");
                return;
            }
            helper.succeed();
        });
    }
}
