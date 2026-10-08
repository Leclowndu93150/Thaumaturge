package com.leclowndu93150.thaumaturge.content.entity.loot;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.entity.EntitySpecialItem;
import com.leclowndu93150.thaumaturge.registry.TTItemTags;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;

@EventBusSubscriber(modid = TTIds.MODID)
public final class FloatingDrops {
    private FloatingDrops() {}

    public static void spawn(ServerLevel level, LivingEntity source, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (stack.is(TTItemTags.FLOATING_DROPS)) {
            level.addFreshEntity(floating(level, source, stack));
        } else {
            source.spawnAtLocation(stack);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onLivingDrops(LivingDropsEvent event) {
        if (!(event.getEntity().level() instanceof ServerLevel level)) {
            return;
        }
        List<ItemEntity> plain = new ArrayList<>();
        for (ItemEntity drop : event.getDrops()) {
            if (!(drop instanceof EntitySpecialItem) && drop.getItem().is(TTItemTags.FLOATING_DROPS)) {
                plain.add(drop);
            }
        }
        for (ItemEntity drop : plain) {
            event.getDrops().remove(drop);
            event.getDrops().add(floating(level, event.getEntity(), drop.getItem()));
        }
    }

    private static EntitySpecialItem floating(ServerLevel level, LivingEntity source, ItemStack stack) {
        return new EntitySpecialItem(
                level, source.getX(), source.getY() + source.getBbHeight() / 2.0F, source.getZ(), stack);
    }
}
