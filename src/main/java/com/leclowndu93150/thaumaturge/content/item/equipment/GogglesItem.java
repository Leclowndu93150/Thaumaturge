package com.leclowndu93150.thaumaturge.content.item.equipment;

import com.leclowndu93150.thaumaturge.api.items.IGoggles;
import com.leclowndu93150.thaumaturge.api.items.IRevealer;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.content.equipment.TCMaterials;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class GogglesItem extends ArmorItem implements IGoggles, IRevealer, IVisDiscountGear {
    public GogglesItem(Properties properties) {
        super(TCMaterials.ARMOR_GOGGLES, Type.HELMET, properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    @Override
    public Holder<SoundEvent> getEquipSound() {
        return SoundEvents.ARMOR_EQUIP_LEATHER;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return swapWithEquipmentSlot(this, level, player, hand);
    }

    @Override
    public boolean showIngamePopups(ItemStack stack, LivingEntity wearer) {
        return true;
    }

    @Override
    public boolean showNodes(ItemStack stack, LivingEntity holder) {
        return true;
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return 5;
    }

    // Not a TieredItem/ArmorItem, so the enchantment value has to come from here or the table offers nothing.
    @Override
    public int getEnchantmentValue() {
        return TCItems.GOGGLES_ENCHANTMENT_VALUE;
    }
}
