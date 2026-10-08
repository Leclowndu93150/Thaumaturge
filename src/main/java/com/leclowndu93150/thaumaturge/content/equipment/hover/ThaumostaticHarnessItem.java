package com.leclowndu93150.thaumaturge.content.equipment.hover;

import com.leclowndu93150.thaumaturge.api.aspect.AspectComponents;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.items.IHoverGear;
import com.leclowndu93150.thaumaturge.api.items.IVisDiscountGear;
import com.leclowndu93150.thaumaturge.content.equipment.TTMaterials;
import com.leclowndu93150.thaumaturge.content.essentia.item.ComponentEssentia;
import com.leclowndu93150.thaumaturge.content.essentia.jar.BlockEntityJar;
import com.leclowndu93150.thaumaturge.content.essentia.jar.JarItem;
import com.leclowndu93150.thaumaturge.registry.TTDataComponents;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

public final class ThaumostaticHarnessItem extends ArmorItem implements IHoverGear, IVisDiscountGear {
    private static final int VIS_DISCOUNT = 2;

    public ThaumostaticHarnessItem(Properties properties) {
        super(TTMaterials.ARMOR_THAUMOSTATIC_HARNESS, ArmorItem.Type.CHESTPLATE, properties);
    }

    public static ItemStack getJar(ItemStack harness) {
        ItemStack jar = harness.get(TTDataComponents.HARNESS_JAR.get());
        return jar == null ? ItemStack.EMPTY : jar.copy();
    }

    public static void setJar(ItemStack harness, ItemStack jar) {
        if (jar.isEmpty()) {
            harness.remove(TTDataComponents.HARNESS_JAR.get());
        } else {
            harness.set(TTDataComponents.HARNESS_JAR.get(), jar.copy());
        }
    }

    public static boolean isFuelJar(ItemStack jar) {
        return potentia(jar) != null;
    }

    private static @Nullable AspectInstance potentia(ItemStack jar) {
        if (!(jar.getItem() instanceof JarItem)) {
            return null;
        }
        for (AspectInstance entry : ComponentEssentia.jar(jar).getAspects().entries()) {
            if (entry.aspect().is(TTAspects.POTENTIA) && entry.amount() > 0) {
                return entry;
            }
        }
        return null;
    }

    @Override
    public int getHoverFuel(ItemStack stack) {
        AspectInstance fuel = potentia(getJar(stack));
        return fuel == null ? 0 : fuel.amount();
    }

    @Override
    public int getMaxHoverFuel(ItemStack stack) {
        return BlockEntityJar.CAPACITY;
    }

    @Override
    public void consumeHoverFuel(ItemStack stack) {
        ItemStack jar = getJar(stack);
        AspectInstance fuel = potentia(jar);
        if (fuel == null || !(jar.getItem() instanceof JarItem)) {
            return;
        }
        ComponentEssentia<?> contents = ComponentEssentia.jar(jar);
        contents.setAspects(contents.getAspects().remove(fuel.aspect(), 1));
        setJar(stack, jar);
    }

    @Override
    public int getVisDiscount(ItemStack stack) {
        return VIS_DISCOUNT;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(
                    new SimpleMenuProvider(
                            (containerId, inventory, menuPlayer) ->
                                    new MenuThaumostaticHarness(containerId, inventory, hand),
                            player.getItemInHand(hand).getHoverName()),
                    buf -> buf.writeBoolean(hand == InteractionHand.MAIN_HAND));
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
        super.inventoryTick(stack, level, entity, slotId, selected);
        if (entity instanceof ServerPlayer player && player.getItemBySlot(EquipmentSlot.CHEST) == stack) {
            HoverManager.tick(player, stack, this);
        }
    }

    @Override
    public void appendHoverText(
            ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        ItemStack jar = getJar(stack);
        if (jar.getItem() instanceof JarItem) {
            for (AspectInstance entry : ComponentEssentia.jar(jar).getAspects().sortedByTag()) {
                tooltip.add(Component.translatable(
                                "tooltip.thaumaturge.thaumostatic_harness.fuel",
                                AspectComponents.name(entry.aspect()),
                                entry.amount())
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
