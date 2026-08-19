package com.leclowndu93150.thaumaturge.client.champion;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.world.entity.EntityType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class ChampionLayers {
    private ChampionLayers() {}

    @SubscribeEvent
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> type : event.getEntityTypes()) {
            if (event.getRenderer(type) instanceof LivingEntityRenderer<?, ?, ?> living) {
                addTaintedLayer(living);
            }
        }
    }

    private static <S extends LivingEntityRenderState, M extends EntityModel<? super S>> void addTaintedLayer(LivingEntityRenderer<?, S, M> renderer) {
        renderer.addLayer(new TaintedSwirlLayer<>(renderer));
    }
}
