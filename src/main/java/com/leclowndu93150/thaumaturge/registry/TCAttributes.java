package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.event.ItemAttributeModifierEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

@EventBusSubscriber(modid = TCIds.MODID)
public final class TCAttributes {
    private static final double DEFAULT_TAINTED_ATTACK = 2.0;

    public static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, TCIds.MODID);

    public static final Holder<Attribute> VIS_DISCOUNT = ATTRIBUTES.register("vis_discount", () -> new PercentageAttribute("attributes.thaumaturge.vis_discount", 0, 0, 1).setSyncable(true));

    public static final Holder<Attribute> CHAMPION_MOD = ATTRIBUTES.register("champion_mod", () -> new RangedAttribute("attributes.thaumaturge.champion_mod", -2.0, -2.0, 100.0).setSyncable(true));

    public static final Holder<Attribute> TAINTED_MOD = ATTRIBUTES.register("tainted_mod", () -> new RangedAttribute("attributes.thaumaturge.tainted_mod", 0.0, 0.0, 1.0));

    private TCAttributes() {}

    public static void register(IEventBus modBus) {
        ATTRIBUTES.register(modBus);
    }

    @SubscribeEvent
    public static void onItemAttributeModifier(ItemAttributeModifierEvent event) {
        if (event.getItemStack().getItem() instanceof IVisDiscountGear gear) {
            float contribution = (float) gear.getVisDiscount(event.getItemStack()) / 100;
            if (contribution != 0) {
                event.addModifier(VIS_DISCOUNT, new AttributeModifier(BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem()), contribution, AttributeModifier.Operation.ADD_VALUE),
                        gear.getAppliedSlot(event.getItemStack()));
            }
        }
    }

    @SubscribeEvent
    public static void onAttributesCreated(EntityAttributeModificationEvent event) {
        event.add(EntityType.PLAYER, VIS_DISCOUNT);
        for (EntityType<? extends LivingEntity> type : event.getTypes()) {
            if (type != EntityType.PLAYER) {
                event.add(type, CHAMPION_MOD);
                event.add(type, TAINTED_MOD);
                if (!event.has(type, Attributes.ATTACK_DAMAGE)) {
                    event.add(type, Attributes.ATTACK_DAMAGE, DEFAULT_TAINTED_ATTACK);
                }
            }
        }
    }
}
