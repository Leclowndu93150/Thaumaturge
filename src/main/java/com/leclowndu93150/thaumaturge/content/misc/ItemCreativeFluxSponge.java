package com.leclowndu93150.thaumaturge.content.misc;

import com.leclowndu93150.thaumaturge.api.aura.AuraHelper;
import com.leclowndu93150.thaumaturge.content.entity.EntityFluxRift;
import com.leclowndu93150.thaumaturge.registry.TCSounds;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

public final class ItemCreativeFluxSponge extends Item {
    private static final int CHUNK_RADIUS = 4;
    private static final float FLUX_PER_CHUNK = 500.0F;
    private static final double RIFT_SWEEP_RADIUS = 32.0;
    private static final float SQUELCH_VOLUME = 0.15F;
    private static final List<Component> USAGE = List.of(Component.translatable("tooltip.thaumaturge.flux_sponge.drain.0").withStyle(ChatFormatting.GREEN),
            Component.translatable("tooltip.thaumaturge.flux_sponge.drain.1").withStyle(ChatFormatting.GREEN),
            Component.translatable("tooltip.thaumaturge.flux_sponge.rifts.0").withStyle(ChatFormatting.DARK_AQUA),
            Component.translatable("tooltip.thaumaturge.flux_sponge.rifts.1").withStyle(ChatFormatting.DARK_AQUA),
            Component.translatable("tooltip.thaumaturge.flux_sponge.creative").withStyle(ChatFormatting.DARK_PURPLE));

    public ItemCreativeFluxSponge(Item.Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, display, tooltip, flag);
        USAGE.forEach(tooltip);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (level instanceof ServerLevel server) {
            server.playSound(null, player.getX(), player.getY(), player.getZ(), TCSounds.CRAFTSTART.get(), SoundSource.PLAYERS, SQUELCH_VOLUME, 1.0F);
            int absorbed = (int) soakFlux(server, player);
            player.sendSystemMessage(Component.translatable("tc.flux_sponge.drained", absorbed).withStyle(ChatFormatting.GREEN));
            if (player.isShiftKeyDown()) {
                int closed = closeRifts(server, player);
                player.sendSystemMessage(Component.translatable("tc.flux_sponge.rifts", closed).withStyle(ChatFormatting.DARK_AQUA));
            }
        }
        return InteractionResult.SUCCESS;
    }

    private static double soakFlux(ServerLevel level, Player player) {
        int y = player.getBlockY();
        return ChunkPos.rangeClosed(player.chunkPosition(), CHUNK_RADIUS).mapToDouble(chunk -> AuraHelper.drainFlux(level, chunk.getMiddleBlockPosition(y), FLUX_PER_CHUNK, false)).sum();
    }

    private static int closeRifts(ServerLevel level, Player player) {
        List<EntityFluxRift> rifts = level.getEntitiesOfClass(EntityFluxRift.class, player.getBoundingBox().inflate(RIFT_SWEEP_RADIUS));
        rifts.forEach(Entity::discard);
        return rifts.size();
    }
}
