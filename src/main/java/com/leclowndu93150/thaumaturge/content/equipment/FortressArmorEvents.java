package com.leclowndu93150.thaumaturge.content.equipment;

import com.leclowndu93150.thaumaturge.TCIds;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.ArmorType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = TCIds.MODID)
public final class FortressArmorEvents {
    private static final Identifier SET_ARMOR_ID = TCIds.rl("fortress_set_armor");
    private static final Identifier SET_TOUGHNESS_ID = TCIds.rl("fortress_set_toughness");
    private static final EquipmentSlot[] SET_SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS};
    private static final float MAGIC_ABSORB_DIVISOR = 35.0F;
    private static final float FIRE_ABSORB_DIVISOR = 20.0F;
    private static final int MASK_ANGRY_GHOST = 1;
    private static final int MASK_SIPPING_FIEND = 2;
    private static final float WITHER_CHANCE_DIVISOR = 10.0F;
    private static final int WITHER_TICKS = 80;
    private static final float LIFESTEAL_CHANCE_DIVISOR = 12.0F;

    private FortressArmorEvents() {}

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof Player player) || !event.getSlot().isArmor()) {
            return;
        }
        int pieces = 0;
        int maskedPieces = 0;
        for (EquipmentSlot slot : SET_SLOTS) {
            ItemStack piece = player.getItemBySlot(slot);
            if (piece.getItem() instanceof FortressArmorItem) {
                pieces++;
                if (FortressArmorItem.mask(piece) != FortressArmorItem.NO_MASK) {
                    maskedPieces++;
                }
            }
        }
        applyBonus(player, player.getAttribute(Attributes.ARMOR), SET_ARMOR_ID, (pieces > 0 ? 1 : 0) + maskedPieces);
        applyBonus(player, player.getAttribute(Attributes.ARMOR_TOUGHNESS), SET_TOUGHNESS_ID, pieces > 0 ? 1 : 0);
    }

    private static void applyBonus(Player player, AttributeInstance attribute, Identifier id, int amount) {
        if (attribute == null) {
            return;
        }
        attribute.removeModifier(id);
        if (amount > 0) {
            attribute.addTransientModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_VALUE));
        }
    }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player victim) {
            absorbSpecialDamage(event, victim);
            angryGhostRetaliation(event, victim);
        }
        sippingFiendLifesteal(event);
    }

    private static void absorbSpecialDamage(LivingIncomingDamageEvent event, Player victim) {
        boolean magic = event.getSource().is(DamageTypeTags.WITCH_RESISTANT_TO);
        boolean fire = event.getSource().is(DamageTypeTags.IS_FIRE);
        if (!magic && !fire) {
            return;
        }
        float fortressDefense = 0.0F;
        float robeDefense = 0.0F;
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            if (!slot.isArmor()) {
                continue;
            }
            ItemStack piece = victim.getItemBySlot(slot);
            if (piece.getItem() instanceof FortressArmorItem) {
                fortressDefense += TCMaterials.ARMOR_FORTRESS.defense().get(armorType(slot));
            } else if (piece.getItem() instanceof VoidRobeArmorItem) {
                robeDefense += TCMaterials.ARMOR_VOID_ROBE.defense().get(armorType(slot));
            }
        }
        float ratio = 0.0F;
        if (magic) {
            ratio = (fortressDefense + robeDefense) / MAGIC_ABSORB_DIVISOR;
        } else if (fortressDefense > 0.0F) {
            ratio = fortressDefense / FIRE_ABSORB_DIVISOR;
        }
        if (ratio > 0.0F) {
            event.setAmount(event.getAmount() * Math.max(0.0F, 1.0F - ratio));
        }
    }

    private static void angryGhostRetaliation(LivingIncomingDamageEvent event, Player victim) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
            return;
        }
        ItemStack helm = victim.getItemBySlot(EquipmentSlot.HEAD);
        if (helm.getItem() instanceof FortressArmorItem && FortressArmorItem.mask(helm) == MASK_ANGRY_GHOST && victim.getRandom().nextFloat() < event.getAmount() / WITHER_CHANCE_DIVISOR) {
            attacker.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_TICKS));
        }
    }

    private static void sippingFiendLifesteal(LivingIncomingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof Player leecher)) {
            return;
        }
        ItemStack helm = leecher.getItemBySlot(EquipmentSlot.HEAD);
        if (helm.getItem() instanceof FortressArmorItem && FortressArmorItem.mask(helm) == MASK_SIPPING_FIEND && leecher.getRandom().nextFloat() < event.getAmount() / LIFESTEAL_CHANCE_DIVISOR) {
            leecher.heal(1.0F);
        }
    }

    private static ArmorType armorType(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> ArmorType.HELMET;
            case CHEST -> ArmorType.CHESTPLATE;
            case LEGS -> ArmorType.LEGGINGS;
            default -> ArmorType.BOOTS;
        };
    }
}
