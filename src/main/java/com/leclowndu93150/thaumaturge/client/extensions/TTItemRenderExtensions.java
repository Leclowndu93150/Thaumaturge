package com.leclowndu93150.thaumaturge.client.extensions;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.AdvancedAlchemicalFurnaceItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.DeconTableItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.GolemBuilderItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.GolemItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.HungryChestItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.JarBrainItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.JarItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.JarNodeItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.NitorItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.NodeStabilizerItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.WandItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import java.util.function.Supplier;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.common.util.Lazy;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTItemRenderExtensions {
    private TTItemRenderExtensions() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        register(event, JarItemSpecialRenderer::new, TTItems.JAR_NORMAL, TTItems.JAR_VOID);
        register(event, JarBrainItemSpecialRenderer::new, TTItems.JAR_BRAIN);
        register(event, JarNodeItemSpecialRenderer::new, TTItems.JAR_NODE);
        register(event, AdvancedAlchemicalFurnaceItemSpecialRenderer::new, TTItems.ADVANCED_ALCHEMICAL_FURNACE);
        register(event, GolemBuilderItemSpecialRenderer::new, TTItems.GOLEM_BUILDER);
        register(event, GolemItemSpecialRenderer::new, TTItems.GOLEM_PLACER);
        register(event, DeconTableItemSpecialRenderer::new, TTItems.DECONSTRUCTION_TABLE);
        register(event, WandItemSpecialRenderer::new, TTItems.WAND);
        register(event, () -> new NodeStabilizerItemSpecialRenderer(false), TTItems.NODE_STABILIZER);
        register(event, () -> new NodeStabilizerItemSpecialRenderer(true), TTItems.NODE_STABILIZER_ADVANCED);
        register(event, () -> new NodeStabilizerItemSpecialRenderer(false, true), TTItems.NODE_TRANSDUCER);
        register(event, HungryChestItemSpecialRenderer::new, TTItems.HUNGRY_CHEST);
        TTItems.NITORS.values().forEach(item -> register(event, NitorItemSpecialRenderer::new, item));
    }

    @SafeVarargs
    private static void register(
            RegisterClientExtensionsEvent event,
            Supplier<BlockEntityWithoutLevelRenderer> factory,
            Holder<Item>... items) {
        event.registerItem(new BewlrExtension(factory), items);
    }

    private static final class BewlrExtension implements IClientItemExtensions {
        private final Lazy<BlockEntityWithoutLevelRenderer> renderer;

        private BewlrExtension(Supplier<BlockEntityWithoutLevelRenderer> factory) {
            this.renderer = Lazy.of(factory);
        }

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return renderer.get();
        }
    }
}
