package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemAddon;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemArm;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemComponent;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemHead;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPartModel;
import com.leclowndu93150.thaumaturge.content.golem.parts.GolemArmDart;
import com.leclowndu93150.thaumaturge.content.golem.parts.GolemLegLevitator;
import com.leclowndu93150.thaumaturge.content.golem.parts.GolemLegWheels;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCGolemParts {
    public static final DeferredRegister<GolemMaterial> MATERIALS = DeferredRegister.create(GolemMaterial.REGISTRY_KEY, TCIds.MODID);
    public static final DeferredRegister<GolemHead> HEADS = DeferredRegister.create(GolemHead.REGISTRY_KEY, TCIds.MODID);
    public static final DeferredRegister<GolemArm> ARMS = DeferredRegister.create(GolemArm.REGISTRY_KEY, TCIds.MODID);
    public static final DeferredRegister<GolemLeg> LEGS = DeferredRegister.create(GolemLeg.REGISTRY_KEY, TCIds.MODID);
    public static final DeferredRegister<GolemAddon> ADDONS = DeferredRegister.create(GolemAddon.REGISTRY_KEY, TCIds.MODID);

    private static final Registry<GolemMaterial> MATERIAL_REGISTRY = MATERIALS.makeRegistry(builder -> builder.sync(false));
    private static final Registry<GolemHead> HEAD_REGISTRY = HEADS.makeRegistry(builder -> builder.sync(false));
    private static final Registry<GolemArm> ARM_REGISTRY = ARMS.makeRegistry(builder -> builder.sync(false));
    private static final Registry<GolemLeg> LEG_REGISTRY = LEGS.makeRegistry(builder -> builder.sync(false));
    private static final Registry<GolemAddon> ADDON_REGISTRY = ADDONS.makeRegistry(builder -> builder.sync(false));

    public static final DeferredHolder<GolemMaterial, GolemMaterial> WOOD = MATERIALS.register("wood", () -> new GolemMaterial(List.of(TCIds.rl("mat_stud_wood")), materialTexture("mat_wood"), 5059370,
            6, 2, 1, stack(TCBlocks.PLANK_GREATWOOD), stack(TCItems.MECHANISM_SIMPLE), List.of(TCGolemTraits.LIGHT)));

    public static final DeferredHolder<GolemMaterial, GolemMaterial> IRON = MATERIALS.register("iron", () -> new GolemMaterial(List.of(TCIds.rl("mat_stud_iron")), materialTexture("mat_iron"),
            16777215, 20, 8, 3, stack(TCItems.PLATE_IRON), stack(TCItems.MECHANISM_SIMPLE), List.of(TCGolemTraits.HEAVY, TCGolemTraits.FIREPROOF, TCGolemTraits.BLASTPROOF)));

    public static final DeferredHolder<GolemMaterial, GolemMaterial> CLAY = MATERIALS.register("clay", () -> new GolemMaterial(List.of(TCIds.rl("mat_stud_clay")), materialTexture("mat_clay"),
            13071447, 10, 4, 2, () -> new ItemStack(Blocks.TERRACOTTA), stack(TCItems.MECHANISM_SIMPLE), List.of(TCGolemTraits.FIREPROOF)));

    public static final DeferredHolder<GolemMaterial, GolemMaterial> BRASS = MATERIALS.register("brass", () -> new GolemMaterial(List.of(TCIds.rl("mat_stud_brass")), materialTexture("mat_brass"),
            15638812, 16, 6, 3, stack(TCItems.PLATE_BRASS), stack(TCItems.MECHANISM_SIMPLE), List.of(TCGolemTraits.LIGHT)));

    public static final DeferredHolder<GolemMaterial, GolemMaterial> THAUMIUM = MATERIALS.register("thaumium",
            () -> new GolemMaterial(List.of(TCIds.rl("mat_stud_thaumium")), materialTexture("mat_thaumium"), 5257074, 24, 10, 4, stack(TCItems.PLATE_THAUMIUM), stack(TCItems.MECHANISM_SIMPLE),
                    List.of(TCGolemTraits.HEAVY, TCGolemTraits.FIREPROOF, TCGolemTraits.BLASTPROOF)));

    public static final DeferredHolder<GolemMaterial, GolemMaterial> VOID = MATERIALS.register("void", () -> new GolemMaterial(List.of(TCIds.rl("mat_stud_void")), materialTexture("mat_void"), 1445161,
            20, 6, 4, stack(TCItems.PLATE_VOID), stack(TCItems.MECHANISM_SIMPLE), List.of(TCGolemTraits.REPAIR)));

    public static final DeferredHolder<GolemHead, GolemHead> HEAD_BASIC = HEADS.register("basic", () -> new GolemHead(List.of(TCIds.rl("mind_clockwork")), partIcon("head_basic"),
            new GolemPartModel(obj("golem_head_basic"), null, GolemPartModel.AttachPoint.HEAD), List.of(stackComponent(TCItems.MIND_CLOCKWORK)), null, List.of()));

    public static final DeferredHolder<GolemHead, GolemHead> HEAD_SMART = HEADS.register("smart",
            () -> new GolemHead(List.of(TCIds.rl("mind_biothaumic")), partIcon("head_smart"),
                    new GolemPartModel(obj("golem_head_smart"), golemTexture("golem_head_other"), GolemPartModel.AttachPoint.HEAD), List.of(stackComponent(TCItems.MIND_BIOTHAUMIC)), null,
                    List.of(TCGolemTraits.SMART, TCGolemTraits.FRAGILE)));

    public static final DeferredHolder<GolemHead, GolemHead> HEAD_SMART_ARMORED = HEADS.register("smart_armored",
            () -> new GolemHead(List.of(TCIds.rl("mind_biothaumic"), TCIds.rl("golem_combat_adv")), partIcon("head_smartarmor"),
                    new GolemPartModel(obj("golem_head_smart_armor"), null, GolemPartModel.AttachPoint.HEAD),
                    List.of(stackComponent(TCItems.MIND_BIOTHAUMIC), stackComponent(TCItems.PLATE_BRASS), GolemComponent.base(), GolemComponent.of(() -> new ItemStack(Blocks.WHITE_WOOL))), null,
                    List.of(TCGolemTraits.SMART)));

    public static final DeferredHolder<GolemHead, GolemHead> HEAD_SCOUT = HEADS.register("scout",
            () -> new GolemHead(List.of(TCIds.rl("golem_vision")), partIcon("head_scout"),
                    new GolemPartModel(obj("golem_head_scout"), golemTexture("golem_head_other"), GolemPartModel.AttachPoint.HEAD),
                    List.of(stackComponent(TCItems.MIND_CLOCKWORK), stackComponent(TCItems.MODULE_VISION)), null, List.of(TCGolemTraits.SCOUT, TCGolemTraits.FRAGILE)));

    public static final DeferredHolder<GolemHead, GolemHead> HEAD_SMART_SCOUT = HEADS.register("smart_scout",
            () -> new GolemHead(List.of(TCIds.rl("golem_vision"), TCIds.rl("mind_biothaumic")), partIcon("head_smartscout"),
                    new GolemPartModel(obj("golem_head_scout_smart"), golemTexture("golem_head_other"), GolemPartModel.AttachPoint.HEAD),
                    List.of(stackComponent(TCItems.MIND_BIOTHAUMIC), stackComponent(TCItems.MODULE_VISION)), null, List.of(TCGolemTraits.SCOUT, TCGolemTraits.SMART, TCGolemTraits.FRAGILE)));

    public static final DeferredHolder<GolemArm, GolemArm> ARMS_BASIC = ARMS.register("basic", () -> new GolemArm(List.of(TCIds.rl("mind_clockwork")), partIcon("arms_basic"),
            new GolemPartModel(obj("golem_arms_basic"), null, GolemPartModel.AttachPoint.ARMS), List.of(), null, List.of()));

    public static final DeferredHolder<GolemArm, GolemArm> ARMS_FINE = ARMS.register("fine",
            () -> new GolemArm(List.of(TCIds.rl("mat_stud_brass")), partIcon("arms_fine"), new GolemPartModel(obj("golem_arms_fine"), null, GolemPartModel.AttachPoint.ARMS),
                    List.of(stackComponent(TCItems.MECHANISM_SIMPLE), GolemComponent.base()), null, List.of(TCGolemTraits.DEFT, TCGolemTraits.FRAGILE)));

    public static final DeferredHolder<GolemArm, GolemArm> ARMS_CLAWS = ARMS.register("claws",
            () -> new GolemArm(List.of(TCIds.rl("golem_combat_adv")), partIcon("arms_claws"),
                    new GolemPartModel(obj("golem_arms_claws"), golemTexture("golem_arms_claws"), GolemPartModel.AttachPoint.ARMS),
                    List.of(stackComponent(TCItems.MODULE_AGGRESSION), GolemComponent.of(() -> new ItemStack(Items.SHEARS, 2)), GolemComponent.base()), null,
                    List.of(TCGolemTraits.FIGHTER, TCGolemTraits.CLUMSY, TCGolemTraits.BRUTAL)));

    public static final DeferredHolder<GolemArm, GolemArm> ARMS_BREAKERS = ARMS.register("breakers",
            () -> new GolemArm(List.of(TCIds.rl("golem_breaker")), partIcon("arms_breakers"),
                    new GolemPartModel(obj("golem_arms_breakers"), golemTexture("golem_arms_breakers"), GolemPartModel.AttachPoint.ARMS),
                    List.of(GolemComponent.of(() -> new ItemStack(Items.DIAMOND, 2)), GolemComponent.base(), GolemComponent.of(() -> new ItemStack(Blocks.PISTON, 2))), null,
                    List.of(TCGolemTraits.BREAKER, TCGolemTraits.CLUMSY, TCGolemTraits.BRUTAL)));

    public static final DeferredHolder<GolemArm, GolemArm> ARMS_DARTS = ARMS.register("darts",
            () -> new GolemArm(List.of(TCIds.rl("golem_combat_adv")), partIcon("arms_darts"),
                    new GolemPartModel(obj("golem_arms_darter"), golemTexture("golem_arms_darter"), GolemPartModel.AttachPoint.ARMS), List.of(stackComponent(TCItems.MODULE_AGGRESSION),
                            GolemComponent.of(() -> new ItemStack(Blocks.DISPENSER, 2)), GolemComponent.of(() -> new ItemStack(Items.ARROW, 32)), GolemComponent.mechanism()),
                    new GolemArmDart(), List.of(TCGolemTraits.FIGHTER, TCGolemTraits.CLUMSY, TCGolemTraits.RANGED, TCGolemTraits.FRAGILE)));

    public static final DeferredHolder<GolemLeg, GolemLeg> LEGS_WALKER = LEGS.register("walker", () -> new GolemLeg(List.of(TCIds.rl("mind_clockwork")), partIcon("legs_walker"),
            new GolemPartModel(obj("golem_legs_walker"), null, GolemPartModel.AttachPoint.LEGS), List.of(GolemComponent.base(), GolemComponent.mechanism()), null, List.of()));

    public static final DeferredHolder<GolemLeg, GolemLeg> LEGS_ROLLER = LEGS.register("roller",
            () -> new GolemLeg(List.of(TCIds.rl("mind_clockwork")), partIcon("legs_roller"),
                    new GolemPartModel(obj("golem_legs_wheel"), golemTexture("golem_legs_wheel"), GolemPartModel.AttachPoint.BODY),
                    List.of(GolemComponent.of(() -> new ItemStack(Items.BOWL, 2)), GolemComponent.of(() -> new ItemStack(Items.LEATHER)), GolemComponent.mechanism()), new GolemLegWheels(),
                    List.of(TCGolemTraits.WHEELED)));

    public static final DeferredHolder<GolemLeg, GolemLeg> LEGS_CLIMBER = LEGS.register("climber", () -> new GolemLeg(List.of(TCIds.rl("golem_climber")), partIcon("legs_climber"),
            new GolemPartModel(obj("golem_legs_climber"), TCIds.rl("textures/block/base_metal.png"), GolemPartModel.AttachPoint.LEGS),
            List.of(GolemComponent.of(() -> new ItemStack(Items.FLINT, 4)), GolemComponent.base(), GolemComponent.mechanism(), GolemComponent.mechanism()), null, List.of(TCGolemTraits.CLIMBER)));

    public static final DeferredHolder<GolemLeg, GolemLeg> LEGS_FLYER = LEGS.register("flyer",
            () -> new GolemLeg(List.of(TCIds.rl("golem_flyer")), partIcon("legs_flyer"),
                    new GolemPartModel(obj("golem_legs_floater"), golemTexture("golem_legs_floater"), GolemPartModel.AttachPoint.BODY), List.of(stackComponent(TCBlocks.LEVITATOR),
                            GolemComponent.of(() -> new ItemStack(TCItems.PLATE_BRASS.get(), 4)), GolemComponent.of(() -> new ItemStack(Items.SLIME_BALL)), GolemComponent.mechanism()),
                    new GolemLegLevitator(), List.of(TCGolemTraits.FLYER, TCGolemTraits.FRAGILE)));

    public static final DeferredHolder<GolemAddon, GolemAddon> ADDON_NONE = ADDONS.register("none",
            () -> new GolemAddon(List.of(TCIds.rl("mind_clockwork")), TCIds.rl("textures/block/base_metal.png"), null, List.of(), null, List.of()));

    public static final DeferredHolder<GolemAddon, GolemAddon> ADDON_ARMORED = ADDONS.register("armored",
            () -> new GolemAddon(List.of(TCIds.rl("golem_combat_adv")), partIcon("addon_armored"), new GolemPartModel(obj("golem_armor"), null, GolemPartModel.AttachPoint.BODY),
                    List.of(GolemComponent.base(), GolemComponent.base(), GolemComponent.base(), GolemComponent.base()), null, List.of(TCGolemTraits.ARMORED, TCGolemTraits.HEAVY)));

    public static final DeferredHolder<GolemAddon, GolemAddon> ADDON_FIGHTER = ADDONS.register("fighter", () -> new GolemAddon(List.of(TCIds.rl("seal_guard")), partIcon("addon_fighter"), null,
            List.of(stackComponent(TCItems.MODULE_AGGRESSION), GolemComponent.mechanism()), null, List.of(TCGolemTraits.FIGHTER)));

    public static final DeferredHolder<GolemAddon, GolemAddon> ADDON_HAULER = ADDONS.register("hauler",
            () -> new GolemAddon(List.of(TCIds.rl("mind_clockwork")), partIcon("addon_hauler"), new GolemPartModel(obj("golem_hauler"), golemTexture("golem_hauler"), GolemPartModel.AttachPoint.BODY),
                    List.of(GolemComponent.of(() -> new ItemStack(Items.LEATHER)), GolemComponent.of(() -> new ItemStack(Blocks.CHEST))), null, List.of(TCGolemTraits.HAULER)));

    private TCGolemParts() {}

    private static Identifier materialTexture(String name) {
        return TCIds.rl("textures/entity/golems/" + name + ".png");
    }

    private static Identifier golemTexture(String name) {
        return TCIds.rl("textures/entity/golems/" + name + ".png");
    }

    private static Identifier partIcon(String name) {
        return TCIds.rl("textures/misc/golem/" + name + ".png");
    }

    private static Identifier obj(String name) {
        return TCIds.rl("models/mesh/" + name + ".tcmesh");
    }

    private static Supplier<ItemStack> stack(Supplier<? extends ItemLike> item) {
        return () -> new ItemStack(item.get());
    }

    private static GolemComponent stackComponent(Supplier<? extends ItemLike> item) {
        return GolemComponent.of(() -> new ItemStack(item.get()));
    }

    public static Registry<GolemMaterial> materials() {
        return MATERIAL_REGISTRY;
    }

    public static Registry<GolemHead> heads() {
        return HEAD_REGISTRY;
    }

    public static Registry<GolemArm> arms() {
        return ARM_REGISTRY;
    }

    public static Registry<GolemLeg> legs() {
        return LEG_REGISTRY;
    }

    public static Registry<GolemAddon> addons() {
        return ADDON_REGISTRY;
    }

    public static void register(IEventBus modBus) {
        MATERIALS.register(modBus);
        HEADS.register(modBus);
        ARMS.register(modBus);
        LEGS.register(modBus);
        ADDONS.register(modBus);
    }
}
