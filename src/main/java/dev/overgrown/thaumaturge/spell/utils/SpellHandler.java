package dev.overgrown.thaumaturge.spell.utils;

import dev.overgrown.thaumaturge.Thaumaturge;
import dev.overgrown.thaumaturge.component.GauntletComponent;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.item.focus.FocusItem;
import dev.overgrown.thaumaturge.item.gauntlet.ResonanceGauntletItem;
import dev.overgrown.thaumaturge.registry.ModItems;
import dev.overgrown.thaumaturge.spell.modifier.ModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry;
import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.pattern.AspectEffect;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import dev.overgrown.thaumaturge.spell.tier.AoeSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.SelfSpellDelivery;
import dev.overgrown.thaumaturge.spell.tier.TargetedSpellDelivery;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.entity.Entity;

public final class SpellHandler {

    private static final Identifier POTENTIA_ID = Thaumaturge.identifier("potentia");
    private static final Identifier VINCULUM_ID = Thaumaturge.identifier("vinculum");

    private SpellHandler() {}

    public static void tryCastSpell(ServerPlayerEntity player, SpellCastPacket.KeyType keyType) {
        List<FociEntry> entries = getEquippedFociEntries(player, keyType);
        if (entries.isEmpty()) return;

        List<FociEntry> nonVinculumEntries = new ArrayList<>();
        List<FociEntry> vinculumEntries = new ArrayList<>();
        
        for (FociEntry entry : entries) {
            if (entry.aspectId().equals(VINCULUM_ID)) {
                vinculumEntries.add(entry);
            } else {
                nonVinculumEntries.add(entry);
            }
        }

        boolean hasPotentia = entries.stream().anyMatch(entry -> entry.aspectId().equals(POTENTIA_ID));

        if (hasPotentia) {
            handlePotentiaSpell(player, keyType, nonVinculumEntries, vinculumEntries);
        } else {
            Object delivery = createDelivery(player, keyType);
            
            for (FociEntry entry : nonVinculumEntries) {
                AspectEffect aspectEffect = AspectRegistry.get(entry.aspectId()).orElse(null);
                ModifierEffect modifierEffect = ModifierRegistry.get(entry.modifierId());

                if (modifierEffect != null) {
                    applyEffect(delivery, modifierEffect);
                }

                if (aspectEffect != null) {
                    applyEffect(delivery, aspectEffect);
                }
            }

            for (FociEntry entry : vinculumEntries) {
                AspectEffect aspectEffect = AspectRegistry.get(entry.aspectId()).orElse(null);
                ModifierEffect modifierEffect = ModifierRegistry.get(entry.modifierId());

                if (modifierEffect != null) {
                    applyEffect(delivery, modifierEffect);
                }

                if (aspectEffect != null) {
                    applyEffect(delivery, aspectEffect);
                }
            }
            
            executeDelivery(delivery, player);
        }
    }

    private static void handlePotentiaSpell(ServerPlayerEntity player, SpellCastPacket.KeyType keyType,
                                           List<FociEntry> nonVinculumEntries, List<FociEntry> vinculumEntries) {
        List<Entity> nearbyEntities = player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(16));
        Entity target = nearbyEntities.isEmpty() ? null : nearbyEntities.get(0);
        
        TargetedSpellDelivery dummyDelivery = target != null ? 
            new TargetedSpellDelivery(player, target) :
            new TargetedSpellDelivery(player, player.getBlockPos().offset(player.getHorizontalFacing()), player.getHorizontalFacing());

        for (FociEntry entry : nonVinculumEntries) {
            if (entry.aspectId().equals(POTENTIA_ID)) continue;

            AspectEffect aspectEffect = AspectRegistry.get(entry.aspectId()).orElse(null);
            ModifierEffect modifierEffect = ModifierRegistry.get(entry.modifierId());

            if (modifierEffect != null) applyEffect(dummyDelivery, modifierEffect);
            if (aspectEffect != null) applyEffect(dummyDelivery, aspectEffect);
        }

        for (FociEntry entry : vinculumEntries) {
            AspectEffect aspectEffect = AspectRegistry.get(entry.aspectId()).orElse(null);
            ModifierEffect modifierEffect = ModifierRegistry.get(entry.modifierId());

            if (modifierEffect != null) applyEffect(dummyDelivery, modifierEffect);
            if (aspectEffect != null) applyEffect(dummyDelivery, aspectEffect);
        }
    }

    private static Object createDelivery(ServerPlayerEntity player, SpellCastPacket.KeyType keyType) {
        return switch (keyType) {
            case PRIMARY -> new SelfSpellDelivery(player);
            case SECONDARY -> {
                List<Entity> nearbyEntities = player.getWorld().getOtherEntities(player, player.getBoundingBox().expand(16));
                Entity entityTarget = nearbyEntities.isEmpty() ? null : nearbyEntities.get(0);
                yield entityTarget != null ? 
                    new TargetedSpellDelivery(player, entityTarget) : 
                    new TargetedSpellDelivery(player, player.getBlockPos().offset(player.getHorizontalFacing()), player.getHorizontalFacing());
            }
            case TERNARY -> new AoeSpellDelivery(player, player.getBlockPos(), 3.0f);
        };
    }

    private static void applyEffect(Object delivery, Object effect) {
        if (delivery instanceof SelfSpellDelivery self) {
            if (effect instanceof AspectEffect ae) ae.applySelf(self);
            if (effect instanceof ModifierEffect me) me.applySelf(self);
        } else if (delivery instanceof TargetedSpellDelivery targeted) {
            if (effect instanceof AspectEffect ae) ae.applyTargeted(targeted);
            if (effect instanceof ModifierEffect me) me.applyTargeted(targeted);
        } else if (delivery instanceof AoeSpellDelivery aoe) {
            if (effect instanceof AspectEffect ae) ae.applyAoe(aoe);
            if (effect instanceof ModifierEffect me) me.applyAoe(aoe);
        }
    }

    private static void executeDelivery(Object delivery, ServerPlayerEntity player) {
        if (delivery instanceof SelfSpellDelivery self) {
            self.execute(player);
        } else if (delivery instanceof TargetedSpellDelivery targeted) {
            targeted.execute(player);
        } else if (delivery instanceof AoeSpellDelivery aoe) {
            aoe.execute(player);
        }
    }

    public static List<FociEntry> getEquippedFociEntries(ServerPlayerEntity player, SpellCastPacket.KeyType keyType) {
        List<FociEntry> entries = new ArrayList<>();
        ItemStack gauntlet = findGauntlet(player);
        
        if (gauntlet.isEmpty() || !ModComponents.hasMaxFoci(gauntlet)) {
            return entries;
        }

        GauntletComponent gauntletComponent = ModComponents.getGauntletState(gauntlet);
        
        for (GauntletComponent.FociEntry entry : gauntletComponent.entries()) {
            if (entry.tier() == keyType) {
                entries.add(new FociEntry(entry.item(), getTierString(entry.tier()), entry.aspectId(), entry.modifierId()));
            }
        }

        return entries;
    }
    
    private static String getTierString(SpellCastPacket.KeyType keyType) {
        return switch (keyType) {
            case PRIMARY -> "lesser";
            case SECONDARY -> "advanced";
            case TERNARY -> "greater";
        };
    }

    public static SpellCastPacket.KeyType getFociTier(Item item) {
        if (item == ModItems.LESSER_FOCUS) return SpellCastPacket.KeyType.PRIMARY;
        if (item == ModItems.ADVANCED_FOCUS) return SpellCastPacket.KeyType.SECONDARY;
        if (item == ModItems.GREATER_FOCUS) return SpellCastPacket.KeyType.TERNARY;
        return null;
    }

    private static ItemStack findGauntlet(PlayerEntity player) {
        // Check hands first
        for (Hand hand : Hand.values()) {
            ItemStack stack = player.getStackInHand(hand);
            if (stack.getItem() instanceof ResonanceGauntletItem) {
                return stack;
            }
        }

        // Check equipment slots
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = player.getEquippedStack(slot);
            if (stack.getItem() instanceof ResonanceGauntletItem) {
                return stack;
            }
        }

        return ItemStack.EMPTY;
    }

    public record FociEntry(Item item, String tier, Identifier aspectId, Identifier modifierId) {
    }
}