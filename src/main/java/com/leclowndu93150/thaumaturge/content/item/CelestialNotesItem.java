package com.leclowndu93150.thaumaturge.content.item;

import com.leclowndu93150.thaumaturge.api.aspect.Aspects;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.content.research.pool.AspectPools;
import com.leclowndu93150.thaumaturge.registry.TCDataComponents;
import com.leclowndu93150.thaumaturge.registry.TCItems;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;

public final class CelestialNotesItem extends Item {
    private static final int STUDY_POINTS = 2;

    public CelestialNotesItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack stackOf(CelestialBody body) {
        ItemStack stack = new ItemStack(TCItems.CELESTIAL_NOTES.get());
        stack.set(TCDataComponents.CELESTIAL_BODY.get(), body);
        return stack;
    }

    public static CelestialBody bodyOf(ItemStack stack) {
        return stack.getOrDefault(TCDataComponents.CELESTIAL_BODY.get(), CelestialBody.SUN);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer serverPlayer) {
            ItemStack stack = player.getItemInHand(hand);
            for (ResourceKey<IAspect> key : aspectsFor(bodyOf(stack))) {
                Holder<IAspect> holder = Aspects.resolve(serverPlayer.registryAccess(), key);
                if (holder != null) {
                    AspectPools.grant(serverPlayer, holder, STUDY_POINTS);
                }
            }
            serverPlayer.sendSystemMessage(Component.translatable("tc.celestial.studied").withStyle(ChatFormatting.DARK_PURPLE));
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static List<ResourceKey<IAspect>> aspectsFor(CelestialBody body) {
        String name = body.getSerializedName();
        if (name.startsWith("moon")) {
            return List.of(TCAspects.LUX, TCAspects.TENEBRAE);
        }
        if (name.startsWith("stars")) {
            return List.of(TCAspects.LUX, TCAspects.VACUOS, TCAspects.ALIENIS);
        }
        return List.of(TCAspects.LUX, TCAspects.IGNIS);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        tooltip.accept(Component.translatable("item.thaumaturge.celestial_notes." + bodyOf(stack).getSerializedName() + ".text").withStyle(ChatFormatting.AQUA));
    }
}
