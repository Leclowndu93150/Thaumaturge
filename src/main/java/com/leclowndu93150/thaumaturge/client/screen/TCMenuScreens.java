package com.leclowndu93150.thaumaturge.client.screen;

import com.leclowndu93150.thaumaturge.TCIds;
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
import com.leclowndu93150.thaumaturge.registry.TCMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = TCIds.MODID, value = Dist.CLIENT)
public final class TCMenuScreens {
    private TCMenuScreens() {}

    @SubscribeEvent
    public static void onRegister(RegisterMenuScreensEvent event) {
        event.register(TCMenus.RESEARCH_TABLE.get(), ResearchTableScreen::new);
        event.register(TCMenus.DECONSTRUCTION_TABLE.get(), DeconstructionTableScreen::new);
        event.register(TCMenus.ARCANE_WORKBENCH.get(), ArcaneWorkbenchScreen::new);
        event.register(TCMenus.SMELTER.get(), SmelterScreen::new);
        event.register(TCMenus.SPA.get(), SpaScreen::new);
        event.register(TCMenus.POTION_SPRAYER.get(), PotionSprayerScreen::new);
        event.register(TCMenus.FOCAL_MANIPULATOR.get(), FocalManipulatorScreen::new);
        event.register(TCMenus.GOLEM_BUILDER.get(), GolemBuilderScreen::new);
        event.register(TCMenus.GOLEM_LOGISTICS.get(), GolemLogisticsScreen::new);
        event.register(TCMenus.SEAL.get(), SealScreen::new);
        event.register(TCMenus.VOID_SIPHON.get(), VoidSiphonScreen::new);
        event.register(TCMenus.THAUMATORIUM.get(), ThaumatoriumScreen::new);
        event.register(TCMenus.PECH.get(), PechScreen::new);
        event.register(TCMenus.FOCUS_POUCH.get(), FocusPouchScreen::new);
        event.register(TCMenus.TURRET_BASIC.get(), TurretBasicScreen<MenuTurretBasic>::new);
        event.register(TCMenus.TURRET_ADVANCED.get(), TurretAdvancedScreen::new);
        event.register(TCMenus.ARCANE_BORE.get(), ArcaneBoreScreen::new);
        event.register(TCMenus.HAND_MIRROR.get(), HandMirrorScreen::new);
    }
}
