package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.content.casters.MenuFocalManipulator;
import com.leclowndu93150.thaumaturge.content.casters.MenuFocusPouch;
import com.leclowndu93150.thaumaturge.content.device.MenuVoidSiphon;
import com.leclowndu93150.thaumaturge.content.device.mirror.MenuHandMirror;
import com.leclowndu93150.thaumaturge.content.device.sprayer.MenuPotionSprayer;
import com.leclowndu93150.thaumaturge.content.device.bore.MenuArcaneBore;
import com.leclowndu93150.thaumaturge.content.entity.construct.MenuTurretAdvanced;
import com.leclowndu93150.thaumaturge.content.entity.construct.MenuTurretBasic;
import com.leclowndu93150.thaumaturge.content.essentia.smeltery.MenuSmelter;
import com.leclowndu93150.thaumaturge.content.essentia.thaumatorium.MenuThaumatorium;
import com.leclowndu93150.thaumaturge.content.golem.logistics.MenuGolemLogistics;
import com.leclowndu93150.thaumaturge.content.golem.press.MenuGolemBuilder;
import com.leclowndu93150.thaumaturge.content.golem.seals.MenuSealBase;
import com.leclowndu93150.thaumaturge.content.pech.MenuPech;
import com.leclowndu93150.thaumaturge.content.research.decon.MenuDeconstructionTable;
import com.leclowndu93150.thaumaturge.content.research.table.MenuResearchTable;
import com.leclowndu93150.thaumaturge.content.spa.MenuSpa;
import com.leclowndu93150.thaumaturge.content.workbench.MenuArcaneWorkbench;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, TCIds.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<MenuFocalManipulator>> FOCAL_MANIPULATOR = MENUS.register("focal_manipulator", () -> IMenuTypeExtension.create(MenuFocalManipulator::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuResearchTable>> RESEARCH_TABLE = MENUS.register("research_table", () -> IMenuTypeExtension.create(MenuResearchTable::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuDeconstructionTable>> DECONSTRUCTION_TABLE = MENUS.register("deconstruction_table",
            () -> IMenuTypeExtension.create(MenuDeconstructionTable::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuArcaneWorkbench>> ARCANE_WORKBENCH = MENUS.register("arcane_workbench", () -> IMenuTypeExtension.create(MenuArcaneWorkbench::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuSpa>> SPA = MENUS.register("spa", () -> IMenuTypeExtension.create(MenuSpa::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuPotionSprayer>> POTION_SPRAYER = MENUS.register("potion_sprayer", () -> IMenuTypeExtension.create(MenuPotionSprayer::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuSmelter>> SMELTER = MENUS.register("smelter", () -> IMenuTypeExtension.create(MenuSmelter::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuGolemBuilder>> GOLEM_BUILDER = MENUS.register("golem_builder", () -> IMenuTypeExtension.create(MenuGolemBuilder::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuThaumatorium>> THAUMATORIUM = MENUS.register("thaumatorium", () -> IMenuTypeExtension.create(MenuThaumatorium::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuVoidSiphon>> VOID_SIPHON = MENUS.register("void_siphon", () -> IMenuTypeExtension.create(MenuVoidSiphon::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuSealBase>> SEAL = MENUS.register("seal", () -> IMenuTypeExtension.create(MenuSealBase::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuGolemLogistics>> GOLEM_LOGISTICS = MENUS.register("golem_logistics", () -> IMenuTypeExtension.create(MenuGolemLogistics::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuFocusPouch>> FOCUS_POUCH = MENUS.register("focus_pouch", () -> IMenuTypeExtension.create(MenuFocusPouch::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuPech>> PECH = MENUS.register("pech", () -> IMenuTypeExtension.create(MenuPech::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuTurretBasic>> TURRET_BASIC = MENUS.register("turret_basic", () -> IMenuTypeExtension.create(MenuTurretBasic::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuTurretAdvanced>> TURRET_ADVANCED = MENUS.register("turret_advanced", () -> IMenuTypeExtension.create(MenuTurretAdvanced::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuArcaneBore>> ARCANE_BORE = MENUS.register("arcane_bore", () -> IMenuTypeExtension.create(MenuArcaneBore::new));

    public static final DeferredHolder<MenuType<?>, MenuType<MenuHandMirror>> HAND_MIRROR = MENUS.register("hand_mirror", () -> IMenuTypeExtension.create(MenuHandMirror::new));

    private TCMenus() {}

    public static void register(IEventBus modBus) {
        MENUS.register(modBus);
    }
}
