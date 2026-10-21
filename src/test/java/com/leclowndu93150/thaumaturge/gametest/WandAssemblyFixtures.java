package com.leclowndu93150.thaumaturge.gametest;

import com.leclowndu93150.thaumaturge.api.recipe.ResearchGate;
import com.leclowndu93150.thaumaturge.api.wands.RegisterWandAssemblyEvent;
import com.leclowndu93150.thaumaturge.api.wands.WandAssemblyPart;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.RegisterEvent;

@EventBusSubscriber(modid = "thaumaturge")
public final class WandAssemblyFixtures {
    public static final Identifier ROD = Identifier.parse("assembly_rods:shared");
    public static final Identifier OTHER_ROD = Identifier.parse("assembly_other:shared");
    public static final Identifier DISABLED_ROD = Identifier.parse("assembly_rods:disabled");
    public static final Identifier CAP = Identifier.parse("assembly_caps:shared");
    public static final Identifier ROD_RESEARCH = Identifier.parse("thaumaturge:rod_greatwood");
    public static final Identifier CAP_RESEARCH = Identifier.parse("thaumaturge:rod_silverwood");

    private WandAssemblyFixtures() {}

    @SubscribeEvent
    public static void registerParts(RegisterEvent event) {
        event.register(WandRod.REGISTRY_KEY, registration -> {
            for (Identifier id : List.of(ROD, OTHER_ROD, DISABLED_ROD)) {
                registration.register(id, new WandRod(25, 2, Identifier.parse("thaumaturge:textures/models/wand_rod_wood.png"), null, false, false, false, ROD_RESEARCH));
            }
        });
        event.register(WandCap.REGISTRY_KEY, registration -> registration.register(CAP, new WandCap(1.0F, List.of(), 1.0F, 3, Identifier.parse("thaumaturge:textures/models/wand_cap_iron.png"))));
    }

    @SubscribeEvent
    public static void registerAssembly(RegisterWandAssemblyEvent event) {
        event.registerRod(new WandAssemblyPart<>(ResourceKey.create(WandRod.REGISTRY_KEY, ROD), Ingredient.of(Items.BLAZE_ROD), List.of(), Set.of(WandAssemblyPart.Variant.WAND), true));
        event.registerRod(new WandAssemblyPart<>(ResourceKey.create(WandRod.REGISTRY_KEY, OTHER_ROD), Ingredient.of(Items.BONE), List.of(), Set.of(WandAssemblyPart.Variant.WAND), true));
        event.registerRod(new WandAssemblyPart<>(ResourceKey.create(WandRod.REGISTRY_KEY, DISABLED_ROD), Ingredient.of(Items.BREEZE_ROD), List.of(), Set.of(WandAssemblyPart.Variant.WAND), false));
        event.registerCap(new WandAssemblyPart<>(ResourceKey.create(WandCap.REGISTRY_KEY, CAP), Ingredient.of(Items.GOLD_NUGGET), List.of(new ResearchGate(CAP_RESEARCH, Optional.empty(), false)),
                Set.of(WandAssemblyPart.Variant.WAND), true));
    }
}
