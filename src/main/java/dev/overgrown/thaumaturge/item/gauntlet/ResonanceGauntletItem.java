package dev.overgrown.thaumaturge.item.gauntlet;

import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.item.focus.FocusItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class ResonanceGauntletItem extends Item {
    private final int slots;

    public ResonanceGauntletItem(Settings settings, int slots) {
        super(settings);
        this.slots = slots;
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack stack = user.getStackInHand(hand);

        if (user.isSneaking()) {
            GauntletComponent component = ModComponents.getGauntletState(stack);
            
            if (!component.entries().isEmpty()) {
                for (GauntletComponent.FociEntry entry : component.entries()) {
                    ItemStack focusStack = new ItemStack(entry.item());
                    if (entry.modifierId() != null && !entry.modifierId().toString().equals("thaumaturge:stable")) {
                        focusStack.getOrCreateNbt().putString("Modifier", entry.modifierId().toString());
                    }
                    user.giveItemStack(focusStack);
                }

                setFoci(stack, new NbtList());
                ModComponents.setGauntletState(stack, GauntletComponent.DEFAULT);
                return TypedActionResult.success(stack);
            }
        }
        return TypedActionResult.pass(stack);
    }

    public NbtList getFoci(ItemStack stack) {
        NbtCompound nbt = stack.getOrCreateNbt();
        return nbt.getList("Foci", NbtCompound.COMPOUND_TYPE);
    }

    public void setFoci(ItemStack stack, NbtList foci) {
        stack.getOrCreateNbt().put("Foci", foci);
    }

    public int getSlots() {
        return slots;
    }

    public boolean addFocus(ItemStack gauntletStack, ItemStack focusStack) {
        if (!(focusStack.getItem() instanceof FocusItem)) return false;

        NbtList foci = getFoci(gauntletStack);
        GauntletComponent component = ModComponents.getGauntletState(gauntletStack);
        if (foci.size() >= slots || component.fociCount() >= slots) return false;

        GauntletComponent.FociEntry entry = GauntletComponent.FociEntry.fromItemStack(focusStack);
        if (entry == null) return false;
        GauntletComponent newComponent = component.withEntry(entry);
        ModComponents.setGauntletState(gauntletStack, newComponent);

        ItemStack focusCopy = focusStack.copy();
        focusCopy.setCount(1);
        NbtCompound focusNbt = new NbtCompound();
        focusCopy.writeNbt(focusNbt);
        foci.add(focusNbt);
        setFoci(gauntletStack, foci);

        return true;
    }

    public boolean removeFocus(ItemStack gauntletStack, int index) {
        GauntletComponent component = ModComponents.getGauntletState(gauntletStack);
        NbtList foci = getFoci(gauntletStack);

        if (index < 0 || (index >= component.fociCount() && index >= foci.size())) return false;

        if (index < component.fociCount()) {
            GauntletComponent newComponent = component.withoutEntry(index);
            ModComponents.setGauntletState(gauntletStack, newComponent);
        }

        if (index < foci.size()) {
            foci.remove(index);
            setFoci(gauntletStack, foci);
        }

        return true;
    }
}