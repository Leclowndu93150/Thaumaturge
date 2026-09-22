package com.leclowndu93150.thaumaturge.client.extensions;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.model.AdvancedAlchemicalFurnaceItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.CentrifugeItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.DeconTableItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.GolemBuilderItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.HungryChestItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.JarBrainItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.JarItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.JarNodeItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.NodeStabilizerItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.client.model.WandItemSpecialRenderer;
import com.leclowndu93150.thaumaturge.registry.TCItems;
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

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCItemRenderExtensions {
    private TCItemRenderExtensions() {}

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        register(event, JarItemSpecialRenderer::new, TCItems.JAR_NORMAL, TCItems.JAR_VOID);
        register(event, JarBrainItemSpecialRenderer::new, TCItems.JAR_BRAIN);
        register(event, JarNodeItemSpecialRenderer::new, TCItems.JAR_NODE);
        register(event, CentrifugeItemSpecialRenderer::new, TCItems.CENTRIFUGE);
        register(event, AdvancedAlchemicalFurnaceItemSpecialRenderer::new, TCItems.ADVANCED_ALCHEMICAL_FURNACE);
        register(event, GolemBuilderItemSpecialRenderer::new, TCItems.GOLEM_BUILDER);
        register(event, DeconTableItemSpecialRenderer::new, TCItems.DECONSTRUCTION_TABLE);
        register(event, WandItemSpecialRenderer::new, TCItems.WAND);
        register(event, () -> new NodeStabilizerItemSpecialRenderer(false), TCItems.NODE_STABILIZER);
        register(event, () -> new NodeStabilizerItemSpecialRenderer(true), TCItems.NODE_STABILIZER_ADVANCED);
        register(event, () -> new NodeStabilizerItemSpecialRenderer(false, true), TCItems.NODE_TRANSDUCER);
        register(event, HungryChestItemSpecialRenderer::new, TCItems.HUNGRY_CHEST);
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
