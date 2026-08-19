package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.research.scan.IScanThing;
import com.leclowndu93150.thaumaturge.content.aura.node.BlockEntityNode;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ScanNode implements IScanThing {
    private static final int MIN_POINTS = 4;
    private static final int POINTS_DIVISOR = 10;
    private static final int TYPE_BONUS = 4;
    private static final int TYPE_BONUS_SMALL = 2;

    @Override
    public boolean checkThing(Player player, @Nullable Object target) {
        return nodeAt(player, target) != null;
    }

    @Override
    public @Nullable Component scanFailure(Player player, @Nullable Object target) {
        BlockEntityNode node = nodeAt(player, target);
        if (node == null) {
            return null;
        }
        for (AspectInstance instance : scanAspects(player, node).entries()) {
            if (AspectPools.hasDiscoveredComponents(player, instance.aspect())) {
                continue;
            }
            for (Holder<IAspect> component : instance.aspect().value().components()) {
                if (!AspectPools.isDiscovered(player, component)) {
                    return Component.translatable("tc.discoveryerror", AspectComponents.help(component));
                }
            }
        }
        return null;
    }

    @Override
    public @Nullable Identifier getResearchKey(Player player, @Nullable Object target) {
        BlockEntityNode node = nodeAt(player, target);
        if (node == null) {
            return null;
        }
        return researchKey(player.level(), node.getBlockPos());
    }

    public static Identifier researchKey(Level level, BlockPos pos) {
        return TCIds.rl("node/" + level.dimension().identifier().getPath() + "/" + pos.asLong());
    }

    @Override
    public void onSuccess(Player player, @Nullable Object target) {
        BlockEntityNode node = nodeAt(player, target);
        if (node == null || !(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        AspectPools.grantAll(serverPlayer, scanAspects(player, node));
    }

    private static @Nullable BlockEntityNode nodeAt(Player player, @Nullable Object target) {
        if (target instanceof BlockPos pos && player.level().getBlockEntity(pos) instanceof BlockEntityNode node) {
            return node;
        }
        return null;
    }

    private static AspectList scanAspects(Player player, BlockEntityNode node) {
        Map<Holder<IAspect>, Integer> points = new LinkedHashMap<>();
        for (AspectInstance entry : node.getAspects().entries()) {
            points.merge(entry.aspect(), Math.max(MIN_POINTS, entry.amount() / POINTS_DIVISOR), Math::max);
        }
        switch (node.getNodeType()) {
            case UNSTABLE -> mergeMax(points, player, TCAspects.PERDITIO, TYPE_BONUS);
            case HUNGRY -> mergeMax(points, player, TCAspects.DESIDERIUM, TYPE_BONUS);
            case TAINTED -> mergeMax(points, player, TCAspects.VITIUM, TYPE_BONUS);
            case PURE -> {
                mergeMax(points, player, TCAspects.VICTUS, TYPE_BONUS_SMALL);
                mergeSum(points, player, TCAspects.ORDO, TYPE_BONUS_SMALL);
            }
            case DARK -> {
                mergeMax(points, player, TCAspects.MORTUUS, TYPE_BONUS_SMALL);
                mergeSum(points, player, TCAspects.TENEBRAE, TYPE_BONUS_SMALL);
            }
            case NORMAL -> {
            }
        }
        AspectList result = AspectList.EMPTY;
        for (Map.Entry<Holder<IAspect>, Integer> entry : points.entrySet()) {
            result = result.add(entry.getKey(), entry.getValue());
        }
        return result;
    }

    private static void mergeMax(Map<Holder<IAspect>, Integer> points, Player player, ResourceKey<IAspect> key, int amount) {
        points.merge(holder(player, key), amount, Math::max);
    }

    private static void mergeSum(Map<Holder<IAspect>, Integer> points, Player player, ResourceKey<IAspect> key, int amount) {
        points.merge(holder(player, key), amount, Integer::sum);
    }

    private static Holder<IAspect> holder(Player player, ResourceKey<IAspect> key) {
        return player.registryAccess().lookupOrThrow(IAspect.REGISTRY_KEY).getOrThrow(key);
    }
}
