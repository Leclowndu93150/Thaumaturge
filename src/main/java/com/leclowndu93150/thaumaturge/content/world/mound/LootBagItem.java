package com.leclowndu93150.thaumaturge.content.world.mound;

import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.function.Consumer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;

public final class LootBagItem extends Item {
    private static final float OPEN_VOLUME = 0.75F;

    private final ResourceKey<LootTable> lootTable;

    public LootBagItem(ResourceKey<LootTable> lootTable, Properties properties) {
        super(properties);
        this.lootTable = lootTable;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("tc.lootbag"));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel server) {
            LootTable table = server.getServer().reloadableRegistries().getLootTable(lootTable);
            LootParams params = new LootParams.Builder(server).withParameter(LootContextParams.ORIGIN, player.position()).create(LootContextParamSets.CHEST);
            for (ItemStack loot : table.getRandomItems(params)) {
                server.addFreshEntity(new ItemEntity(server, player.getX(), player.getY(), player.getZ(), loot.copy()));
            }
            player.playSound(TCSounds.COINS.get(), OPEN_VOLUME, 1.0F);
        }
        player.getItemInHand(hand).shrink(1);
        return InteractionResult.SUCCESS;
    }
}
