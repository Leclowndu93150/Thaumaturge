package com.leclowndu93150.thaumaturge.content.item.equipment;

import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import com.leclowndu93150.thaumaturge.registry.TTItems;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class GogglesItem extends ArmorItem implements IVisDiscountGear {
    public GogglesItem(Properties properties) {
        super(
                TTMaterials.ARMOR_GOGGLES,
                Type.HELMET,
                properties.component(TTDataComponents.GOGGLES_UPGRADE.get(), Unit.INSTANCE));
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
    public int getVisDiscount(ItemStack stack) {
        return 5;
    }

    // Not a TieredItem/ArmorItem, so the enchantment value has to come from here or the table offers nothing.
    @Override
    public int getEnchantmentValue() {
        return TTItems.GOGGLES_ENCHANTMENT_VALUE;
    }
}
