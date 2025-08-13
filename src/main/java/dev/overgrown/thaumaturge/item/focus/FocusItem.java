package dev.overgrown.thaumaturge.item.focus;

import dev.overgrown.thaumaturge.component.FociComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.registry.ModItems;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

public interface FocusItem {
    String getTier();
    Identifier getAspect(ItemStack stack);
    void setModifier(ItemStack stack, Identifier modifier);

    static ItemStack createFocus(Identifier aspect, Identifier modifier) {
        ItemStack stack = new ItemStack(ModItems.ADVANCED_FOCUS);
        FociComponent component = new FociComponent(aspect, modifier);
        ModComponents.setFociComponent(stack, component);
        return stack;
    }

    static Identifier getDefaultModifier() {
        return new Identifier("thaumaturge", "stable");
    }

    default Identifier getModifier(ItemStack stack) {
        FociComponent component = ModComponents.getFociComponent(stack);
        if (component.modifierId() != null) {
            return component.modifierId();
        }

        NbtCompound nbt = stack.getNbt();
        if (nbt != null && nbt.contains("Modifier")) {
            return new Identifier(nbt.getString("Modifier"));
        }
        
        return getDefaultModifier();
    }
}