package dev.overgrown.thaumaturge.component;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.item.focus.FocusItem;
import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores foci entries on gauntlet items
 */
public class GauntletComponent {
    private final List<FociEntry> entries;
    
    public GauntletComponent(List<FociEntry> entries) {
        this.entries = entries != null ? new ArrayList<>(entries) : new ArrayList<>();
    }
    
    public List<FociEntry> entries() { return new ArrayList<>(entries); }
    public int fociCount() { return entries.size(); }
    
    public static final GauntletComponent DEFAULT = new GauntletComponent(List.of());
    
    /**
     * Represents a focus entry in the gauntlet
     */
    public record FociEntry(Item item, SpellCastPacket.KeyType tier, Identifier aspectId, Identifier modifierId) {
        
        /**
         * Create FociEntry from an ItemStack (focus item)
         */
        public static FociEntry fromItemStack(ItemStack stack) {
            if (!(stack.getItem() instanceof FocusItem focus)) {
                return null;
            }
            
            Identifier aspectId = focus.getAspect(stack);
            Identifier modifierId = focus.getModifier(stack);
            SpellCastPacket.KeyType tier = getTierFromFocus(focus);
            
            return new FociEntry(stack.getItem(), tier, aspectId, modifierId);
        }
        
        private static SpellCastPacket.KeyType getTierFromFocus(FocusItem focus) {
            return switch (focus.getTier()) {
                case "lesser" -> SpellCastPacket.KeyType.PRIMARY;
                case "advanced" -> SpellCastPacket.KeyType.SECONDARY;
                case "greater" -> SpellCastPacket.KeyType.TERNARY;
                default -> SpellCastPacket.KeyType.SECONDARY;
            };
        }
        
        /**
         * Serialize entry to NBT
         */
        public NbtCompound toNbt() {
            NbtCompound nbt = new NbtCompound();
            nbt.putString("Item", net.minecraft.registry.Registries.ITEM.getId(item).toString());
            nbt.putString("Tier", tier.name());
            if (aspectId != null) {
                nbt.putString("AspectId", aspectId.toString());
            }
            nbt.putString("ModifierId", modifierId.toString());
            return nbt;
        }
        
        /**
         * Deserialize entry from NBT
         */
        public static FociEntry fromNbt(NbtCompound nbt) {
            Item item = net.minecraft.registry.Registries.ITEM.get(new Identifier(nbt.getString("Item")));
            SpellCastPacket.KeyType tier = SpellCastPacket.KeyType.valueOf(nbt.getString("Tier"));
            Identifier aspectId = nbt.contains("AspectId") ? 
                new Identifier(nbt.getString("AspectId")) : null;
            Identifier modifierId = new Identifier(nbt.getString("ModifierId"));
            
            return new FociEntry(item, tier, aspectId, modifierId);
        }
    }
    
    /**
     * Read GauntletComponent from ItemStack NBT
     */
    public static GauntletComponent fromNbt(ItemStack stack) {
        NbtCompound nbt = stack.getNbt();
        if (nbt == null || !nbt.contains("GauntletComponent")) {
            return DEFAULT;
        }
        
        NbtList entriesList = nbt.getList("GauntletComponent", NbtElement.COMPOUND_TYPE);
        List<FociEntry> entries = new ArrayList<>();
        
        for (NbtElement element : entriesList) {
            FociEntry entry = FociEntry.fromNbt((NbtCompound) element);
            if (entry != null) {
                entries.add(entry);
            }
        }
        
        return new GauntletComponent(entries);
    }
    
    /**
     * Write GauntletComponent to ItemStack NBT
     */
    public static void toNbt(ItemStack stack, GauntletComponent component) {
        NbtCompound nbt = stack.getOrCreateNbt();
        NbtList entriesList = new NbtList();
        
        for (FociEntry entry : component.entries) {
            entriesList.add(entry.toNbt());
        }
        
        nbt.put("GauntletComponent", entriesList);
    }
    
    /**
     * Get GauntletComponent from ItemStack, creating default if not present
     */
    public static GauntletComponent get(ItemStack stack) {
        return fromNbt(stack);
    }
    
    /**
     * Set GauntletComponent on ItemStack
     */
    public static void set(ItemStack stack, GauntletComponent component) {
        toNbt(stack, component);
    }
    
    /**
     * Add a focus entry to this gauntlet component
     */
    public GauntletComponent withEntry(FociEntry entry) {
        List<FociEntry> newEntries = new ArrayList<>(this.entries);
        newEntries.add(entry);
        return new GauntletComponent(newEntries);
    }
    
    /**
     * Remove a focus entry from this gauntlet component
     */
    public GauntletComponent withoutEntry(int index) {
        if (index < 0 || index >= entries.size()) {
            return this;
        }
        List<FociEntry> newEntries = new ArrayList<>(this.entries);
        newEntries.remove(index);
        return new GauntletComponent(newEntries);
    }
}