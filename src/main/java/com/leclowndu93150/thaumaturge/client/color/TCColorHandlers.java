package com.leclowndu93150.thaumaturge.client.color;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.resources.Identifier;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCColorHandlers {
    public static final Identifier ASPECT_COLOR_TINT_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "aspect_color");
    public static final Identifier CRYSTAL_ASPECT_TINT_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "crystal_aspect");
    public static final Identifier FOCUS_COLOR_TINT_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "focus_color");
    public static final Identifier GOLEM_MATERIAL_TINT_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "golem_material");
    public static final Identifier NOTE_COLOR_TINT_ID = Identifier.fromNamespaceAndPath(TCIds.MODID, "note_color");

    private TCColorHandlers() {}

    @SubscribeEvent
    public static void onRegisterItemTintSources(RegisterColorHandlersEvent.ItemTintSources event) {
        event.register(ASPECT_COLOR_TINT_ID, AspectColorTint.MAP_CODEC);
        event.register(CRYSTAL_ASPECT_TINT_ID, CrystalAspectTint.MAP_CODEC);
        event.register(FOCUS_COLOR_TINT_ID, FocusColorTint.MAP_CODEC);
        event.register(GOLEM_MATERIAL_TINT_ID, GolemMaterialTint.MAP_CODEC);
        event.register(NOTE_COLOR_TINT_ID, NoteColorTint.MAP_CODEC);
    }
}
