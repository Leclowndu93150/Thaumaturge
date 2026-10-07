package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.wands.IWandRodOnUpdate;
import com.leclowndu93150.thaumaturge.api.wands.WandCap;
import com.leclowndu93150.thaumaturge.api.wands.WandRod;
import com.leclowndu93150.thaumaturge.content.wands.WandRodPrimalOnUpdate;
import java.util.List;
import net.minecraft.core.Registry;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TTWandParts {
    public static final DeferredRegister<WandCap> CAPS = DeferredRegister.create(WandCap.REGISTRY_KEY, TTIds.MODID);
    public static final DeferredRegister<WandRod> RODS = DeferredRegister.create(WandRod.REGISTRY_KEY, TTIds.MODID);

    private static final Registry<WandCap> CAP_REGISTRY = CAPS.makeRegistry(builder -> builder.sync(false));
    private static final Registry<WandRod> ROD_REGISTRY = RODS.makeRegistry(builder -> builder.sync(false));

    public static final DeferredHolder<WandCap, WandCap> CAP_IRON = CAPS.register("iron", () -> new WandCap(1.1F, List.of(), 0.0F, 1, capTexture("iron")));
    public static final DeferredHolder<WandCap, WandCap> CAP_COPPER = CAPS.register("copper", () -> new WandCap(1.1F, List.of(TTAspects.ORDO, TTAspects.PERDITIO), 1.0F, 2, capTexture("copper")));
    public static final DeferredHolder<WandCap, WandCap> CAP_GOLD = CAPS.register("gold", () -> new WandCap(1.0F, List.of(), 0.0F, 3, capTexture("gold")));
    public static final DeferredHolder<WandCap, WandCap> CAP_SILVER = CAPS.register("silver",
            () -> new WandCap(1.0F, List.of(TTAspects.AER, TTAspects.TERRA, TTAspects.IGNIS, TTAspects.AQUA), 0.95F, 4, capTexture("silver")));
    public static final DeferredHolder<WandCap, WandCap> CAP_THAUMIUM = CAPS.register("thaumium", () -> new WandCap(0.9F, List.of(), 0.0F, 6, capTexture("thaumium")));
    public static final DeferredHolder<WandCap, WandCap> CAP_VOID = CAPS.register("void", () -> new WandCap(0.8F, List.of(), 0.0F, 9, capTexture("void")));

    public static final DeferredHolder<WandRod, WandRod> ROD_WOOD = RODS.register("wood", () -> wand(25, 1, "wood", null, false));
    public static final DeferredHolder<WandRod, WandRod> ROD_GREATWOOD = RODS.register("greatwood", () -> wand(50, 3, "greatwood", null, false));
    public static final DeferredHolder<WandRod, WandRod> ROD_OBSIDIAN = RODS.register("obsidian", () -> wand(75, 6, "obsidian", new WandRodPrimalOnUpdate(TTAspects.TERRA), false));
    public static final DeferredHolder<WandRod, WandRod> ROD_BLAZE = RODS.register("blaze", () -> wand(75, 6, "blaze", new WandRodPrimalOnUpdate(TTAspects.IGNIS), true));
    public static final DeferredHolder<WandRod, WandRod> ROD_ICE = RODS.register("ice", () -> wand(75, 6, "ice", new WandRodPrimalOnUpdate(TTAspects.AQUA), false));
    public static final DeferredHolder<WandRod, WandRod> ROD_QUARTZ = RODS.register("quartz", () -> wand(75, 6, "quartz", new WandRodPrimalOnUpdate(TTAspects.ORDO), false));
    public static final DeferredHolder<WandRod, WandRod> ROD_BONE = RODS.register("bone", () -> wand(75, 6, "bone", new WandRodPrimalOnUpdate(TTAspects.PERDITIO), false));
    public static final DeferredHolder<WandRod, WandRod> ROD_REED = RODS.register("reed", () -> wand(75, 6, "reed", new WandRodPrimalOnUpdate(TTAspects.AER), false));
    public static final DeferredHolder<WandRod, WandRod> ROD_SILVERWOOD = RODS.register("silverwood", () -> wand(100, 9, "silverwood", null, false));

    public static final DeferredHolder<WandRod, WandRod> STAFF_GREATWOOD = RODS.register("greatwood_staff", () -> staff(125, 8, "greatwood", null, false, false));
    public static final DeferredHolder<WandRod, WandRod> STAFF_OBSIDIAN = RODS.register("obsidian_staff", () -> staff(175, 14, "obsidian", new WandRodPrimalOnUpdate(TTAspects.TERRA), false, false));
    public static final DeferredHolder<WandRod, WandRod> STAFF_BLAZE = RODS.register("blaze_staff", () -> staff(175, 14, "blaze", new WandRodPrimalOnUpdate(TTAspects.IGNIS), true, false));
    public static final DeferredHolder<WandRod, WandRod> STAFF_ICE = RODS.register("ice_staff", () -> staff(175, 14, "ice", new WandRodPrimalOnUpdate(TTAspects.AQUA), false, false));
    public static final DeferredHolder<WandRod, WandRod> STAFF_QUARTZ = RODS.register("quartz_staff", () -> staff(175, 14, "quartz", new WandRodPrimalOnUpdate(TTAspects.ORDO), false, false));
    public static final DeferredHolder<WandRod, WandRod> STAFF_BONE = RODS.register("bone_staff", () -> staff(175, 14, "bone", new WandRodPrimalOnUpdate(TTAspects.PERDITIO), false, false));
    public static final DeferredHolder<WandRod, WandRod> STAFF_REED = RODS.register("reed_staff", () -> staff(175, 14, "reed", new WandRodPrimalOnUpdate(TTAspects.AER), false, false));
    public static final DeferredHolder<WandRod, WandRod> STAFF_SILVERWOOD = RODS.register("silverwood_staff", () -> staff(250, 24, "silverwood", null, false, false));
    public static final DeferredHolder<WandRod, WandRod> STAFF_PRIMAL = RODS.register("primal_staff", () -> staff(250, 32, "primal", new WandRodPrimalOnUpdate(), false, true));

    private TTWandParts() {}

    private static WandRod wand(int capacity, int craftCost, String texture, IWandRodOnUpdate onUpdate, boolean glow) {
        Identifier gate = "wood".equals(texture) ? TTIds.rl("unlock_auromancy") : TTIds.rl("rod_" + texture);
        return new WandRod(capacity, craftCost, rodTexture(texture), onUpdate, glow, false, false, gate);
    }

    private static WandRod staff(int capacity, int craftCost, String texture, IWandRodOnUpdate onUpdate, boolean glow, boolean runes) {
        Identifier gate = "primal".equals(texture) ? TTIds.rl("staff_primal") : TTIds.rl("staves");
        return new WandRod(capacity, craftCost, rodTexture(texture), onUpdate, glow, true, runes, gate);
    }

    private static Identifier capTexture(String name) {
        return TTIds.rl("textures/models/wand_cap_" + name + ".png");
    }

    private static Identifier rodTexture(String name) {
        return TTIds.rl("textures/models/wand_rod_" + name + ".png");
    }

    public static Registry<WandCap> caps() {
        return CAP_REGISTRY;
    }

    public static Registry<WandRod> rods() {
        return ROD_REGISTRY;
    }

    public static void register(IEventBus modBus) {
        CAPS.register(modBus);
        RODS.register(modBus);
    }
}
