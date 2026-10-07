package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.client.screen.casters.FocalManipulatorScreen;
import com.leclowndu93150.thaumaturge.client.screen.casters.FocusPouchScreen;
import com.leclowndu93150.thaumaturge.client.screen.construct.ArcaneBoreScreen;
import com.leclowndu93150.thaumaturge.client.screen.construct.TurretAdvancedScreen;
import com.leclowndu93150.thaumaturge.client.screen.construct.TurretBasicScreen;
import com.leclowndu93150.thaumaturge.client.screen.golem.GolemBuilderScreen;
import com.leclowndu93150.thaumaturge.client.screen.golem.GolemLogisticsScreen;
import com.leclowndu93150.thaumaturge.client.screen.golem.SealScreen;
import com.leclowndu93150.thaumaturge.client.screen.research.DeconstructionTableScreen;
import com.leclowndu93150.thaumaturge.client.screen.research.ResearchTableScreen;
import com.leclowndu93150.thaumaturge.client.screen.workbench.ArcaneWorkbenchScreen;
import com.leclowndu93150.thaumaturge.content.entity.construct.MenuTurretBasic;
import com.leclowndu93150.thaumaturge.registry.TTMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TTIds.MODID, value = Dist.CLIENT)
public final class TTMenuScreens {
    private TTMenuScreens() {}

    @SubscribeEvent
    public static void onRegister(RegisterMenuScreensEvent event) {
        event.register(TTMenus.RESEARCH_TABLE.get(), ResearchTableScreen::new);
        event.register(TTMenus.ARCANE_GRINDSTONE.get(), ArcaneGrindstoneScreen::new);
        event.register(TTMenus.DECONSTRUCTION_TABLE.get(), DeconstructionTableScreen::new);
        event.register(TTMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
        event.register(TTMenus.SMELTER.get(), SmelterScreen::new);
        event.register(TTMenus.SPA.get(), SpaScreen::new);
        event.register(TTMenus.POTION_SPRAYER.get(), PotionSprayerScreen::new);
        event.register(TTMenus.FOCAL_MANIPULATOR.get(), FocalManipulatorScreen::new);
        event.register(TTMenus.GOLEM_BUILDER.get(), GolemBuilderScreen::new);
        event.register(TTMenus.GOLEM_LOGISTICS.get(), GolemLogisticsScreen::new);
        event.register(TTMenus.SEAL.get(), SealScreen::new);
        event.register(TTMenus.VOID_SIPHON.get(), VoidSiphonScreen::new);
        event.register(TTMenus.THAUMATORIUM.get(), ThaumatoriumScreen::new);
        event.register(TTMenus.PECH.get(), PechScreen::new);
        event.register(TTMenus.FOCUS_POUCH.get(), FocusPouchScreen::new);
        event.register(TTMenus.TURRET_BASIC.get(), TurretBasicScreen<MenuTurretBasic>::new);
        event.register(TTMenus.TURRET_ADVANCED.get(), TurretAdvancedScreen::new);
        event.register(TTMenus.ARCANE_BORE.get(), ArcaneBoreScreen::new);
        event.register(TTMenus.HAND_MIRROR.get(), HandMirrorScreen::new);
    }
}
