package com.leclowndu93150.thaumaturge.content.misc;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

public final class ItemCreativeFluxSponge extends Item {
    private static final int CHUNK_RADIUS = 4;
    private static final float DRAIN_PER_CHUNK = 500.0F;
    private static final double RIFT_RANGE = 32.0;

    public ItemCreativeFluxSponge(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable("tooltip.thaumaturge.flux_sponge.drain.0").withStyle(ChatFormatting.GREEN));
        tooltip.accept(Component.translatable("tooltip.thaumaturge.flux_sponge.drain.1").withStyle(ChatFormatting.GREEN));
        tooltip.accept(Component.translatable("tooltip.thaumaturge.flux_sponge.rifts.0").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.accept(Component.translatable("tooltip.thaumaturge.flux_sponge.rifts.1").withStyle(ChatFormatting.DARK_AQUA));
        tooltip.accept(Component.translatable("tooltip.thaumaturge.flux_sponge.creative").withStyle(ChatFormatting.DARK_PURPLE));
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide()) {
            player.swing(hand);
            level.playLocalSound(player.getX(), player.getY(), player.getZ(), TCSounds.CRAFTSTART.get(), SoundSource.PLAYERS, 0.15F, 1.0F, false);
            return InteractionResult.SUCCESS;
        }
        int drained = 0;
        BlockPos pos = player.blockPosition();
        for (int x = -CHUNK_RADIUS; x <= CHUNK_RADIUS; x++) {
            for (int z = -CHUNK_RADIUS; z <= CHUNK_RADIUS; z++) {
                drained += (int) AuraHelper.drainFlux(level, pos.offset(16 * x, 0, 16 * z), DRAIN_PER_CHUNK, false);
            }
        }
        player.sendSystemMessage(Component.translatable("tc.flux_sponge.drained", drained).withStyle(ChatFormatting.GREEN));
        if (player.isShiftKeyDown()) {
            List<EntityFluxRift> rifts = level.getEntitiesOfClass(EntityFluxRift.class, new AABB(player.blockPosition()).inflate(RIFT_RANGE));
            for (EntityFluxRift rift : rifts) {
                rift.discard();
            }
            player.sendSystemMessage(Component.translatable("tc.flux_sponge.rifts", rifts.size()).withStyle(ChatFormatting.DARK_AQUA));
        }
        return InteractionResult.SUCCESS;
    }
}
