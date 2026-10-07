package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.model.mesh.TTMeshUnbakedModel;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public class TTModelsHandlers {

    public static final Identifier JAR_MODEL_ID = Identifier.fromNamespaceAndPath(TTIds.MODID, "jar");

    public static final Identifier JAR_BRAIN_MODEL_ID = Identifier.fromNamespaceAndPath(TTIds.MODID, "jar_brain");

    public static final Identifier JAR_NODE_MODEL_ID = Identifier.fromNamespaceAndPath(TTIds.MODID, "jar_node");

    public static final Identifier GOLEM_BUILDER_MODEL_ID = Identifier.fromNamespaceAndPath(TTIds.MODID, "golem_builder");

    public static final Identifier ADVANCED_ALCHEMICAL_FURNACE_MODEL_ID = Identifier.fromNamespaceAndPath(TTIds.MODID, "advanced_alchemical_furnace");

    public static final Identifier DECON_TABLE_MODEL_ID = Identifier.fromNamespaceAndPath(TTIds.MODID, "deconstruction_table");

    public static final Identifier WAND_MODEL_ID = Identifier.fromNamespaceAndPath(TTIds.MODID, "wand");

    public static final Identifier NODE_STABILIZER_MODEL_ID = Identifier.fromNamespaceAndPath(TTIds.MODID, "node_stabilizer");

    public static final Identifier NITOR_MODEL_ID = Identifier.fromNamespaceAndPath(TTIds.MODID, "nitor");

    public static final Identifier WAND_IS_STAFF_PROPERTY_ID = Identifier.fromNamespaceAndPath(TTIds.MODID, "wand_is_staff");

    public static final Identifier MESH_LOADER_ID = Identifier.fromNamespaceAndPath(TTIds.MODID, "mesh");

    @SubscribeEvent
    public static void onRegisterItemModels(RegisterSpecialModelRendererEvent event) {
        event.register(TTIds.rl("golem"), GolemItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(JAR_MODEL_ID, JarItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(JAR_BRAIN_MODEL_ID, JarBrainItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(JAR_NODE_MODEL_ID, JarNodeItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(GOLEM_BUILDER_MODEL_ID, GolemBuilderItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(ADVANCED_ALCHEMICAL_FURNACE_MODEL_ID, AdvancedAlchemicalFurnaceItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(DECON_TABLE_MODEL_ID, DeconTableItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(WAND_MODEL_ID, WandItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(NODE_STABILIZER_MODEL_ID, NodeStabilizerItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(NITOR_MODEL_ID, NitorItemSpecialRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void onRegisterConditionalProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(WAND_IS_STAFF_PROPERTY_ID, WandIsStaffProperty.MAP_CODEC);
    }

    @SubscribeEvent
    public static void onRegisterLoaders(ModelEvent.RegisterLoaders event) {
        event.register(MESH_LOADER_ID, TTMeshUnbakedModel.Loader.INSTANCE);
    }
}
