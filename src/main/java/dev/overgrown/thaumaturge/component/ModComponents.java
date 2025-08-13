package dev.overgrown.thaumaturge.component;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.item.gauntlet.ResonanceGauntletItem;
import net.minecraft.item.ItemStack;

/**
 * Provides convenient methods to work with components stored as NBT data
 */
public class ModComponents {
    
    /**
     * Initialize components system
     */
    public static void register() {
        Thaumaturge.LOGGER.info("Registering Mod Components for " + Thaumaturge.MOD_ID);
    }

    public static final String MAX_FOCI = "max_foci";
    public static final String GAUNTLET_STATE = "gauntlet_state";
    public static final String FOCI_COMPONENT = "foci_component";
    
    /**
     * Get maximum foci capacity for a gauntlet
     */
    public static int getMaxFoci(ItemStack stack) {
        if (stack.getItem() instanceof ResonanceGauntletItem gauntlet) {
            return gauntlet.getSlots();
        }
        return 0;
    }
    
    /**
     * Check if an ItemStack has max foci data (i.e., is a gauntlet)
     */
    public static boolean hasMaxFoci(ItemStack stack) {
        return stack.getItem() instanceof ResonanceGauntletItem;
    }
    
    /**
     * Get GauntletComponent from ItemStack
     */
    public static GauntletComponent getGauntletState(ItemStack stack) {
        return GauntletComponent.get(stack);
    }
    
    /**
     * Set GauntletComponent on ItemStack
     */
    public static void setGauntletState(ItemStack stack, GauntletComponent component) {
        GauntletComponent.set(stack, component);
    }
    
    /**
     * Get FociComponent from ItemStack
     */
    public static FociComponent getFociComponent(ItemStack stack) {
        return FociComponent.get(stack);
    }
    
    /**
     * Set FociComponent on ItemStack
     */
    public static void setFociComponent(ItemStack stack, FociComponent component) {
        FociComponent.set(stack, component);
    }
}