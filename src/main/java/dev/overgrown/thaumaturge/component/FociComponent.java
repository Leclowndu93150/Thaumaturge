package dev.overgrown.thaumaturge.component;

import dev.overgrown.thaumaturge.Thaumaturge;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

/**
 * Stores aspect and modifier data on focus items
 */
public class FociComponent {
    private static final String ASPECT_KEY = "AspectId";
    private static final String MODIFIER_KEY = "ModifierId";
    
    private final Identifier aspectId;
    private final Identifier modifierId;
    
    public FociComponent(Identifier aspectId, Identifier modifierId) {
        this.aspectId = aspectId;
        this.modifierId = modifierId != null ? modifierId : Thaumaturge.identifier("stable");
    }
    
    public Identifier aspectId() { return aspectId; }
    public Identifier modifierId() { return modifierId; }
    
    public static final FociComponent DEFAULT = new FociComponent(null, Thaumaturge.identifier("stable"));
    
    /**
     * Read FociComponent from ItemStack NBT
     */
    public static FociComponent fromNbt(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("FociComponent")) {
            return DEFAULT;
        }
        
        NbtCompound component = nbt.getCompound("FociComponent");
        Identifier aspectId = component.contains(ASPECT_KEY) ? 
            new Identifier(component.getString(ASPECT_KEY)) : null;
        Identifier modifierId = component.contains(MODIFIER_KEY) ? 
            new Identifier(component.getString(MODIFIER_KEY)) : Thaumaturge.identifier("stable");
            
        return new FociComponent(aspectId, modifierId);
    }
    
    /**
     * Write FociComponent to ItemStack NBT
     */
    public static void toNbt(ItemStack stack, FociComponent component) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtCompound componentNbt = new NbtCompound();
        
        if (component.aspectId != null) {
            componentNbt.putString(ASPECT_KEY, component.aspectId.toString());
        }
        componentNbt.putString(MODIFIER_KEY, component.modifierId.toString());
        
        nbt.put("FociComponent", componentNbt);
    }
    
    /**
     * Get FociComponent from ItemStack, creating default if not present
     */
    public static FociComponent get(ItemStack stack) {
        return fromNbt(stack);
    }
    
    /**
     * Set FociComponent on ItemStack
     */
    public static void set(ItemStack stack, FociComponent component) {
        toNbt(stack, component);
    }
}