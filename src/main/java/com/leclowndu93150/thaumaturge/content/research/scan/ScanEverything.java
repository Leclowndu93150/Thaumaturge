package com.leclowndu93150.thaumaturge.content.research.scan;

import com.leclowndu93150.thaumaturge.Thaumaturge;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanTarget;
import com.leclowndu93150.thaumaturge.api.research.scan.ScanningManager;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public final class ScanEverything {
    private ScanEverything() {}

    public static int scan(ServerPlayer player) {
        List<ScanTarget> targets = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            ItemStack stack = item.getDefaultInstance();
            if (!stack.isEmpty()) {
                targets.add(ScanTarget.stack(stack));
            }
        }
        List<Entity> creatures = new ArrayList<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            Entity creature = type.canSummon() ? create(type, player.serverLevel()) : null;
            if (creature != null) {
                creatures.add(creature);
                targets.add(ScanTarget.entity(creature));
            }
        }
        ScanningManager.scanSilently(player, targets);
        creatures.forEach(Entity::discard);
        return targets.size();
    }

    private static @Nullable Entity create(EntityType<?> type, ServerLevel level) {
        try {
            return type.create(level);
        } catch (RuntimeException e) {
            Thaumaturge.LOGGER.error("Could not create a {} to scan it", BuiltInRegistries.ENTITY_TYPE.getKey(type), e);
            return null;
        }
    }
}
