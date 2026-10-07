package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshGeometry;
import com.leclowndu93150.thaumaturge.client.render.crystal.CrystalUnbakedModel;
import com.leclowndu93150.thaumaturge.client.render.warding.WardedGlassUnbakedModel;
import com.leclowndu93150.thaumaturge.content.item.CelestialBody;
import com.leclowndu93150.thaumaturge.content.wands.WandVisHelper;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTModelsHandlers {
    public static final ResourceLocation WAND_IS_STAFF_PROPERTY_ID = TTIds.rl("wand_is_staff");
    public static final ResourceLocation LINKED_PROPERTY_ID = TTIds.rl("linked");
    public static final ResourceLocation LOADED_PROPERTY_ID = TTIds.rl("loaded");
    public static final ResourceLocation NOTE_COMPLETE_PROPERTY_ID = TTIds.rl("note_complete");
    public static final ResourceLocation FILLED_PROPERTY_ID = TTIds.rl("filled");
    public static final ResourceLocation MARKED_PROPERTY_ID = TTIds.rl("marked");
    public static final ResourceLocation VERDANT_TYPE_PROPERTY_ID = TTIds.rl("verdant_type");
    public static final ResourceLocation CELESTIAL_BODY_PROPERTY_ID = TTIds.rl("celestial_body");
    public static final ResourceLocation MESH_LOADER_ID = TTIds.rl("mesh");
    public static final ResourceLocation CRYSTAL_LOADER_ID = TTIds.rl("crystal");
    public static final ResourceLocation WARDED_GLASS_LOADER_ID = TTIds.rl("warded_glass");

    private TTModelsHandlers() {}

    @SubscribeEvent
    public static void onRegisterGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(MESH_LOADER_ID, TTMeshGeometry.Loader.INSTANCE);
        event.register(CRYSTAL_LOADER_ID, CrystalUnbakedModel.Loader.INSTANCE);
        event.register(WARDED_GLASS_LOADER_ID, WardedGlassUnbakedModel.Loader.INSTANCE);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(
                    TTItems.WAND.get(),
                    WAND_IS_STAFF_PROPERTY_ID,
                    (stack, level, entity, seed) ->
                            WandVisHelper.getParts(stack).rod().staff() ? 1.0F : 0.0F);
            registerComponentFlag(TTItems.MIRROR.get(), LINKED_PROPERTY_ID, TTDataComponents.MIRROR_LINK.get());
            registerComponentFlag(
                    TTItems.MIRROR_ESSENTIA.get(), LINKED_PROPERTY_ID, TTDataComponents.MIRROR_LINK.get());
            registerComponentFlag(TTItems.GRAPPLE_GUN.get(), LOADED_PROPERTY_ID, TTDataComponents.GRAPPLE_LOADED.get());
            registerComponentFlag(
                    TTItems.RESEARCH_NOTE.get(), NOTE_COMPLETE_PROPERTY_ID, TTDataComponents.NOTE_COMPLETE.get());
            registerComponentFlag(TTItems.PHIAL.get(), FILLED_PROPERTY_ID, TTDataComponents.ASPECTS.get());
            registerComponentFlag(TTItems.LABEL.get(), MARKED_PROPERTY_ID, TTDataComponents.ASPECT_FILTER.get());
            ItemProperties.register(
                    TTItems.VERDANT_CHARM.get(),
                    VERDANT_TYPE_PROPERTY_ID,
                    (stack, level, entity, seed) -> stack.getOrDefault(TTDataComponents.VERDANT_TYPE.get(), 0));
            ItemProperties.register(
                    TTItems.CELESTIAL_NOTES.get(), CELESTIAL_BODY_PROPERTY_ID, (stack, level, entity, seed) -> {
                        CelestialBody body = stack.get(TTDataComponents.CELESTIAL_BODY.get());
                        return body == null ? 0.0F : body.ordinal();
                    });
        });
    }

    private static void registerComponentFlag(Item item, ResourceLocation id, DataComponentType<?> component) {
        ItemProperties.register(item, id, (stack, level, entity, seed) -> stack.has(component) ? 1.0F : 0.0F);
    }
}
