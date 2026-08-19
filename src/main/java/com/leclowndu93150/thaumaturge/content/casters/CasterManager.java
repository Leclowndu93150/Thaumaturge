package com.leclowndu93150.thaumaturge.content.casters;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.casters.FocusElement;
import com.leclowndu93150.thaumaturge.api.casters.FocusEngine;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.casters.FocusSettings;
import com.leclowndu93150.thaumaturge.api.casters.FocusUnit;
import com.leclowndu93150.thaumaturge.api.casters.ICaster;
import com.leclowndu93150.thaumaturge.api.items.GogglesAccess;
import com.leclowndu93150.thaumaturge.api.items.IArchitect;
import com.leclowndu93150.thaumaturge.compat.curio.ThaumaturgeCuriosCompat;
import com.leclowndu93150.thaumaturge.registry.TCAttachments;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCMobEffects;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Supplier;
import net.minecraft.core.NonNullList;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;

public final class CasterManager {
    public static final String REMOVE_FOCUS = "REMOVE";
    private static final int POUCH_SLOT_OFFSET = 1000;
    private static final int TICKS_PER_SECOND = 20;
    private static final int EXHAUST_DISCOUNT_PENALTY = 10;
    private static final float SWAP_SOUND_VOLUME = 0.3F;
    private static final float SWAP_SOUND_PITCH = 1.0F;
    private static final float REMOVE_SOUND_PITCH = 0.9F;
    private static final int DEFAULT_AREA_SIZE = CasterArea.DEFAULT_SIZE;
    private static final int AREA_DIM_ALL = 0;
    private static final int AREA_DIM_X = 1;
    private static final int AREA_DIM_Z = 2;
    private static final int AREA_DIM_Y = 3;

    private CasterManager() {}

    public static float getTotalVisDiscount(Player player) {
        if (player == null) {
            return 0.0F;
        }
        int total = GogglesAccess.totalVisDiscount(player);
        MobEffectInstance exhaust = player.getEffect(TCMobEffects.VIS_EXHAUST);
        MobEffectInstance infectious = player.getEffect(TCMobEffects.INFECTIOUS_VIS_EXHAUST);
        if (exhaust != null || infectious != null) {
            int level1 = exhaust != null ? exhaust.getAmplifier() : 0;
            int level2 = infectious != null ? infectious.getAmplifier() : 0;
            total -= (Math.max(level1, level2) + 1) * EXHAUST_DISCOUNT_PENALTY;
        }
        return total / 100.0F;
    }

    public static boolean consumeVisFromInventory(Player player, float cost) {
        NonNullList<ItemStack> main = player.getInventory().getNonEquipmentItems();
        for (int slot = main.size() - 1; slot >= 0; slot--) {
            ItemStack stack = main.get(slot);
            if (stack.getItem() instanceof ICaster caster && caster.consumeVis(stack, player, cost, true, false)) {
                return true;
            }
        }
        return false;
    }

    public static void changeFocus(ItemStack casterStack, Level level, Player player, String focusKey) {
        if (!(casterStack.getItem() instanceof ICaster caster)) {
            return;
        }
        NonNullList<ItemStack> main = player.getInventory().getNonEquipmentItems();
        TreeMap<String, Integer> foci = new TreeMap<>();
        Map<Integer, PouchHandle> pouches = new HashMap<>();
        int pouchCount = 0;
        if (ModList.get().isLoaded(TCIds.CURIOS)) {
            for (ThaumaturgeCuriosCompat.CurioPouchRef ref : ThaumaturgeCuriosCompat.equippedPouches(player, stack -> stack.getItem() instanceof FocusPouchItem)) {
                pouchCount++;
                pouches.put(pouchCount, new PouchHandle(ref::stack, () -> ref.handler().setStackInSlot(ref.slot(), ref.stack())));
                indexPouch(foci, ref.stack(), pouchCount);
            }
        }
        for (int slot = 0; slot < main.size(); slot++) {
            ItemStack stack = main.get(slot);
            if (stack.getItem() instanceof ItemFocus focus) {
                String sortKey = focus.getSortingHelper(stack);
                if (sortKey != null) {
                    foci.put(sortKey, slot);
                }
            }
            if (stack.getItem() instanceof FocusPouchItem) {
                pouchCount++;
                int pouchSlot = slot;
                pouches.put(pouchCount, new PouchHandle(() -> player.getInventory().getItem(pouchSlot), () -> player.getInventory().setChanged()));
                indexPouch(foci, stack, pouchCount);
            }
        }
        if (REMOVE_FOCUS.equals(focusKey) || foci.isEmpty()) {
            ItemStack current = caster.getFocusStack(casterStack);
            if (!current.isEmpty() && (addFocusToPouch(current.copy(), pouches) || player.getInventory().add(current.copy()))) {
                caster.setFocus(casterStack, ItemStack.EMPTY);
                player.playSound(TCSounds.TICKS.get(), SWAP_SOUND_VOLUME, REMOVE_SOUND_PITCH);
            }
            return;
        }
        String newKey = focusKey;
        if (newKey == null || foci.get(newKey) == null) {
            newKey = newKey != null ? foci.higherKey(newKey) : null;
        }
        if (newKey == null || foci.get(newKey) == null) {
            newKey = foci.firstKey();
        }
        int encoded = foci.get(newKey);
        ItemStack picked;
        if (encoded < POUCH_SLOT_OFFSET) {
            picked = main.get(encoded).copy();
            if (picked.isEmpty()) {
                return;
            }
            player.getInventory().setItem(encoded, ItemStack.EMPTY);
        } else {
            PouchHandle pouch = pouches.get(encoded / POUCH_SLOT_OFFSET);
            if (pouch == null) {
                return;
            }
            picked = fetchFocusFromPouch(pouch, encoded % POUCH_SLOT_OFFSET);
            if (picked.isEmpty()) {
                return;
            }
        }
        player.playSound(TCSounds.TICKS.get(), SWAP_SOUND_VOLUME, SWAP_SOUND_PITCH);
        ItemStack current = caster.getFocusStack(casterStack);
        if (!current.isEmpty() && (addFocusToPouch(current.copy(), pouches) || player.getInventory().add(current.copy()))) {
            caster.setFocus(casterStack, ItemStack.EMPTY);
        }
        if (caster.getFocusStack(casterStack).isEmpty()) {
            caster.setFocus(casterStack, picked);
        } else if (!addFocusToPouch(picked, pouches)) {
            player.getInventory().add(picked);
        }
    }

    private static void indexPouch(TreeMap<String, Integer> foci, ItemStack pouch, int pouchId) {
        NonNullList<ItemStack> contents = FocusPouchItem.getInventory(pouch);
        for (int slot = 0; slot < contents.size(); slot++) {
            ItemStack stack = contents.get(slot);
            if (stack.getItem() instanceof ItemFocus focus) {
                String sortKey = focus.getSortingHelper(stack);
                if (sortKey != null) {
                    foci.put(sortKey, slot + pouchId * POUCH_SLOT_OFFSET);
                }
            }
        }
    }

    private static ItemStack fetchFocusFromPouch(PouchHandle pouch, int focusSlot) {
        ItemStack pouchStack = pouch.getter().get();
        if (!(pouchStack.getItem() instanceof FocusPouchItem)) {
            return ItemStack.EMPTY;
        }
        NonNullList<ItemStack> contents = FocusPouchItem.getInventory(pouchStack);
        ItemStack focus = contents.get(focusSlot);
        if (!(focus.getItem() instanceof ItemFocus)) {
            return ItemStack.EMPTY;
        }
        ItemStack copy = focus.copy();
        contents.set(focusSlot, ItemStack.EMPTY);
        FocusPouchItem.setInventory(pouchStack, contents);
        pouch.markChanged().run();
        return copy;
    }

    private static boolean addFocusToPouch(ItemStack focus, Map<Integer, PouchHandle> pouches) {
        for (PouchHandle pouch : pouches.values()) {
            ItemStack pouchStack = pouch.getter().get();
            if (!(pouchStack.getItem() instanceof FocusPouchItem)) {
                continue;
            }
            NonNullList<ItemStack> contents = FocusPouchItem.getInventory(pouchStack);
            for (int slot = 0; slot < contents.size(); slot++) {
                if (contents.get(slot).isEmpty()) {
                    contents.set(slot, focus.copy());
                    FocusPouchItem.setInventory(pouchStack, contents);
                    pouch.markChanged().run();
                    return true;
                }
            }
        }
        return false;
    }

    private record PouchHandle(Supplier<ItemStack> getter, Runnable markChanged) {
    }

    public static void toggleMisc(ItemStack casterStack, Level level, Player player, int mod) {
        if (!(casterStack.getItem() instanceof ICaster caster)) {
            return;
        }
        FocusPackage core = ItemFocus.getPackage(caster.getFocusStack(casterStack));
        if (core == null || !containsArchitect(core)) {
            return;
        }
        int dim = getAreaDim(casterStack);
        if (mod == 0) {
            int areaX = getAreaX(casterStack);
            int areaY = getAreaY(casterStack);
            int areaZ = getAreaZ(casterStack);
            int max = getAreaSize(casterStack);
            switch (dim) {
                case AREA_DIM_ALL -> {
                    areaX++;
                    areaZ++;
                    areaY++;
                }
                case AREA_DIM_X -> areaX++;
                case AREA_DIM_Z -> areaZ++;
                case AREA_DIM_Y -> areaY++;
                default -> {
                }
            }
            if (areaX > max) {
                areaX = 0;
            }
            if (areaZ > max) {
                areaZ = 0;
            }
            if (areaY > max) {
                areaY = 0;
            }
            setArea(casterStack, new CasterArea(areaX, areaY, areaZ, dim));
        }
        if (mod == 1) {
            if (++dim > AREA_DIM_Y) {
                dim = AREA_DIM_ALL;
            }
            CasterArea area = area(casterStack);
            setArea(casterStack, new CasterArea(area.x(), area.y(), area.z(), dim));
        }
    }

    private static boolean containsArchitect(FocusPackage core) {
        for (FocusUnit unit : core.units()) {
            if (FocusEngine.element(unit.element()) instanceof IArchitect) {
                return true;
            }
        }
        return false;
    }

    public static int socketedPlanMethod(ItemStack casterStack) {
        if (!(casterStack.getItem() instanceof ICaster caster)) {
            return 0;
        }
        FocusPackage core = ItemFocus.getPackage(caster.getFocusStack(casterStack));
        if (core == null) {
            return 0;
        }
        for (FocusUnit unit : core.units()) {
            FocusElement element = FocusEngine.element(unit.element());
            if (element instanceof IArchitect) {
                return FocusSettings.of(element, unit.settings()).value("method");
            }
        }
        return 0;
    }

    private static CasterArea area(ItemStack stack) {
        CasterArea area = stack.get(TCDataComponents.CASTER_AREA.get());
        return area == null ? CasterArea.DEFAULT : area;
    }

    private static void setArea(ItemStack stack, CasterArea area) {
        stack.set(TCDataComponents.CASTER_AREA.get(), area);
    }

    private static int getAreaSize(ItemStack stack) {
        return stack.getItem() instanceof ICaster ? DEFAULT_AREA_SIZE : 0;
    }

    public static int getAreaDim(ItemStack stack) {
        return area(stack).dim();
    }

    public static int getAreaX(ItemStack stack) {
        return Math.min(area(stack).x(), getAreaSize(stack));
    }

    public static int getAreaY(ItemStack stack) {
        return Math.min(area(stack).y(), getAreaSize(stack));
    }

    public static int getAreaZ(ItemStack stack) {
        return Math.min(area(stack).z(), getAreaSize(stack));
    }

    public static boolean isOnCooldown(LivingEntity entity) {
        return entity.getData(TCAttachments.CASTER_COOLDOWN) > entity.level().getGameTime();
    }

    public static float getCooldown(LivingEntity entity) {
        long remaining = entity.getData(TCAttachments.CASTER_COOLDOWN) - entity.level().getGameTime();
        return remaining > 0 ? (float) remaining / TICKS_PER_SECOND : 0.0F;
    }

    public static void setCooldown(LivingEntity entity, int ticks) {
        if (ticks == 0) {
            entity.setData(TCAttachments.CASTER_COOLDOWN, 0L);
        } else {
            entity.setData(TCAttachments.CASTER_COOLDOWN, entity.level().getGameTime() + ticks);
        }
    }
}
