package com.leclowndu93150.thaumaturge.compat.curio;

import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.api.items.IGoggles;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.api.items.RechargeAccess;
import com.leclowndu93150.thaumaturge.compat.curio.client.CuriosityBandCurioRenderer;
import com.leclowndu93150.thaumaturge.compat.curio.client.GoggleCurioRenderer;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.AmuletVisItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.VerdantCharmItem;
import com.leclowndu93150.thaumaturge.content.equipment.bauble.VoidseerCharmItem;
import com.leclowndu93150.thaumaturge.registry.TCAttributes;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.client.ICurioRenderer;
import top.theillusivec4.curios.api.event.CurioAttributeModifierEvent;
import top.theillusivec4.curios.api.type.capability.ICurio;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

public final class ThaumaturgeCuriosCompat {

    private ThaumaturgeCuriosCompat() {}

    public static void init(IEventBus modBus) {
        GogglesAccess.bindCurios(new GogglesAccess.Curios() {
            @Override
            public boolean wearsGoggles(LivingEntity entity) {
                return checkForGoggles(entity);
            }

            @Override
            public boolean anyCurioMatches(LivingEntity entity, Predicate<ItemStack> predicate) {
                return ThaumaturgeCuriosCompat.anyCurioMatches(entity, predicate);
            }
        });
        modBus.addListener(ThaumaturgeCuriosCompat::registerCurio);
        modBus.addListener(ThaumaturgeCuriosCompat::onClientSetup);
        NeoForge.EVENT_BUS.addListener(ThaumaturgeCuriosCompat::onItemAttributeModifier);
    }

    private static void registerCurio(RegisterCapabilitiesEvent event) {
        registerPlain(event, TCItems.GOGGLES_REVEALING.get(), TCItems.AMULET_MUNDANE.get(), TCItems.RING_MUNDANE.get(), TCItems.GIRDLE_MUNDANE.get(), TCItems.RING_APPRENTICE.get(),
                TCItems.AMULET_FANCY.get(), TCItems.RING_FANCY.get(), TCItems.GIRDLE_FANCY.get(), TCItems.CHARM_UNDYING.get(), TCItems.CLOUD_RING.get(), TCItems.CURIOSITY_BAND.get(),
                TCItems.FOCUS_POUCH.get());
        event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> tickingCurio(stack), TCItems.AMULET_VIS.get());
        event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> tickingCurio(stack), TCItems.AMULET_VIS_CRAFTED.get());
        event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> tickingCurio(stack), TCItems.VERDANT_CHARM.get());
        event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> tickingCurio(stack), TCItems.VOIDSEER_CHARM.get());
    }

    private static void registerPlain(RegisterCapabilitiesEvent event, Item... items) {
        for (Item item : items) {
            event.registerItem(CuriosCapability.ITEM, (stack, ctx) -> (ICurio) () -> stack, item);
        }
    }

    private static ICurio tickingCurio(ItemStack stack) {
        return new ICurio() {
            @Override
            public ItemStack getStack() {
                return stack;
            }

            @Override
            public void curioTick(SlotContext slotContext) {
                LivingEntity wearer = slotContext.entity();
                if (stack.getItem() instanceof AmuletVisItem amulet) {
                    amulet.wornTick(stack, wearer);
                } else if (stack.getItem() instanceof VerdantCharmItem charm) {
                    charm.wornTick(stack, wearer);
                } else if (stack.getItem() instanceof VoidseerCharmItem charm) {
                    charm.wornTick(stack, wearer);
                }
            }

            @Override
            public void onUnequip(SlotContext slotContext, ItemStack newStack) {
                if (stack.getItem() instanceof VoidseerCharmItem && !newStack.is(stack.getItem())) {
                    VoidseerCharmItem.clearDiscount(slotContext.entity());
                }
            }
        };
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        ICurioRenderer.register(TCItems.GOGGLES_REVEALING.get(), GoggleCurioRenderer::new);
        ICurioRenderer.register(TCItems.CURIOSITY_BAND.get(), CuriosityBandCurioRenderer::new);
    }

    public static boolean checkForGoggles(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(entity);
        if (invOpt.isEmpty())
            return false;
        ICuriosItemHandler inv = invOpt.get();
        Optional<SlotResult> slotOpt = inv.findFirstCurio(stack -> stack.getItem() instanceof IGoggles);
        if (slotOpt.isEmpty())
            return false;
        ItemStack stack = slotOpt.get().stack();
        if (stack.isEmpty() || !(stack.getItem() instanceof IGoggles g)) {
            return false;
        }
        return g.showIngamePopups(stack, entity);
    }

    public static List<ItemStack> equippedCurios(LivingEntity entity) {
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(entity);
        if (invOpt.isEmpty()) {
            return List.of();
        }
        IItemHandlerModifiable equipped = invOpt.get().getEquippedCurios();
        List<ItemStack> stacks = new ArrayList<>(equipped.getSlots());
        for (int slot = 0; slot < equipped.getSlots(); slot++) {
            ItemStack stack = equipped.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                stacks.add(stack);
            }
        }
        return stacks;
    }

    public static boolean anyCurioMatches(LivingEntity entity, Predicate<ItemStack> predicate) {
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(entity);
        return invOpt.isPresent() && invOpt.get().findFirstCurio(predicate).isPresent();
    }

    public static boolean isCurioEquipped(LivingEntity entity, Item item) {
        return anyCurioMatches(entity, stack -> stack.is(item));
    }

    public static ItemStack extractCurio(LivingEntity entity, Item item) {
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(entity);
        if (invOpt.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ICuriosItemHandler inv = invOpt.get();
        Optional<SlotResult> slotOpt = inv.findFirstCurio(stack -> stack.is(item));
        if (slotOpt.isEmpty()) {
            return ItemStack.EMPTY;
        }
        int index = slotIndex(inv, slotOpt.get());
        if (index < 0) {
            return ItemStack.EMPTY;
        }
        ItemStack removed = inv.getEquippedCurios().getStackInSlot(index).copy();
        inv.getEquippedCurios().setStackInSlot(index, ItemStack.EMPTY);
        return removed;
    }

    public static boolean rechargeFirstCurio(Player player) {
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(player);
        if (invOpt.isEmpty()) {
            return false;
        }
        IItemHandlerModifiable equipped = invOpt.get().getEquippedCurios();
        for (int slot = 0; slot < equipped.getSlots(); slot++) {
            if (RechargeAccess.rechargeItem(player.level(), equipped.getStackInSlot(slot), player.blockPosition(), player, 1) > 0.0F) {
                return true;
            }
        }
        return false;
    }

    public static List<CurioPouchRef> equippedPouches(LivingEntity entity, Predicate<ItemStack> predicate) {
        Optional<ICuriosItemHandler> invOpt = CuriosApi.getCuriosInventory(entity);
        if (invOpt.isEmpty()) {
            return List.of();
        }
        IItemHandlerModifiable equipped = invOpt.get().getEquippedCurios();
        List<CurioPouchRef> refs = new ArrayList<>();
        for (int slot = 0; slot < equipped.getSlots(); slot++) {
            ItemStack stack = equipped.getStackInSlot(slot);
            if (!stack.isEmpty() && predicate.test(stack)) {
                refs.add(new CurioPouchRef(equipped, slot));
            }
        }
        return refs;
    }

    public record CurioPouchRef(IItemHandlerModifiable handler, int slot) {
        public ItemStack stack() {
            return handler.getStackInSlot(slot);
        }
    }

    private static int slotIndex(ICuriosItemHandler inv, SlotResult result) {
        IItemHandlerModifiable equipped = inv.getEquippedCurios();
        for (int slot = 0; slot < equipped.getSlots(); slot++) {
            if (equipped.getStackInSlot(slot) == result.stack()) {
                return slot;
            }
        }
        return -1;
    }

    private static void onItemAttributeModifier(CurioAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        if (stack.getItem() instanceof IVisDiscountGear gear) {
            float contribution = (float) gear.getVisDiscount(stack) / 100;
            if (contribution != 0) {
                event.addModifier(TCAttributes.VIS_DISCOUNT, new AttributeModifier(BuiltInRegistries.ITEM.getKey(stack.getItem()), contribution, AttributeModifier.Operation.ADD_VALUE));
            }
        }
    }
}
