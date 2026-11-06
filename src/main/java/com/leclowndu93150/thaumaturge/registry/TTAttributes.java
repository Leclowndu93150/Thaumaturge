package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.content.equipment.trim.TrimBonuses;
import java.util.Map;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = TTIds.MODID)
public final class TTAttributes {
    private static final double DEFAULT_ATTACK_DAMAGE = 2.0;

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, TTIds.MODID);

    public static final Holder<Attribute> VIS_DISCOUNT = ATTRIBUTES.register("vis_discount", () -> new PercentageAttribute("attributes.thaumaturge.vis_discount", 0, 0, 1).setSyncable(true));

    private static final float PERCENT = 100.0F;
    private static final Map<EquipmentSlot, Identifier> TRIM_DISCOUNT_IDS = Util.makeEnumMap(EquipmentSlot.class, slot -> TTIds.rl("trim_vis_discount/" + slot.getName()));

    private TTAttributes() {}

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
    }

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        addTrimDiscount(event);
        if (event.getItemStack().getItem() instanceof IVisDiscountGear gear) {
            float contribution = (float) gear.getVisDiscount(event.getItemStack()) / 100;
            if (contribution != 0) {
                event.addModifier(VIS_DISCOUNT, new AttributeModifier(BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()), contribution, AttributeModifier.Operation.ADD_VALUE),
                        gear.getAppliedSlot(event.getItemStack()));
            }
        }
    }

    private static void addTrimDiscount(ItemAttributeModifierEvent event) {
        ItemStack stack = event.getItemStack();
        int discount = TrimBonuses.of(stack).visDiscount();
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (discount != 0 && equippable != null) {
            EquipmentSlot slot = equippable.slot();
            event.addModifier(VIS_DISCOUNT, new AttributeModifier(TRIM_DISCOUNT_IDS.get(slot), discount / PERCENT, AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(slot));
        }
    }

    @SubscribeEvent
    public static void onAttributesCreated(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, VIS_DISCOUNT);
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            if (type != EntityType.PLAYER) {
                if (!event.has(type, Attributes.ATTACK_DAMAGE)) {
                    event.add(type, Attributes.ATTACK_DAMAGE, DEFAULT_ATTACK_DAMAGE);
                }
            }
        }
    }
}
