package com.leclowndu93150.thaumaturge.client.model;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.client.model.mesh.TCMeshUnbakedModel;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public class TCModelsHandlers {

    public static final Identifier JAR_MODEL_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "jar");

    public static final Identifier JAR_BRAIN_MODEL_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "jar_brain");

    public static final Identifier JAR_NODE_MODEL_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "jar_node");

    public static final Identifier CENTRIFUGE_MODEL_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "centrifuge");

    public static final Identifier GOLEM_BUILDER_MODEL_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "golem_builder");

    public static final Identifier DECON_TABLE_MODEL_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "deconstruction_table");

    public static final Identifier WAND_MODEL_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "wand");

    public static final Identifier NODE_STABILIZER_MODEL_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "node_stabilizer");

    public static final Identifier WAND_IS_STAFF_PROPERTY_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "wand_is_staff");

    public static final Identifier MESH_LOADER_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "mesh");

    @SubscribeEvent
    public static void onRegisterItemModels(RegisterSpecialModelRendererEvent event) {
        event.register(JAR_MODEL_ID, JarItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(JAR_BRAIN_MODEL_ID, JarBrainItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(JAR_NODE_MODEL_ID, JarNodeItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(CENTRIFUGE_MODEL_ID, CentrifugeItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(GOLEM_BUILDER_MODEL_ID, GolemBuilderItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(DECON_TABLE_MODEL_ID, DeconTableItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(WAND_MODEL_ID, WandItemSpecialRenderer.Unbaked.MAP_CODEC);
        event.register(NODE_STABILIZER_MODEL_ID, NodeStabilizerItemSpecialRenderer.Unbaked.MAP_CODEC);
    }

    @SubscribeEvent
    public static void onRegisterConditionalProperties(RegisterConditionalItemModelPropertyEvent event) {
        event.register(WAND_IS_STAFF_PROPERTY_ID, WandIsStaffProperty.MAP_CODEC);
    }

    @SubscribeEvent
    public static void onRegisterLoaders(ModelEvent.RegisterLoaders event) {
        event.register(MESH_LOADER_ID, TCMeshUnbakedModel.Loader.INSTANCE);
    }
}
