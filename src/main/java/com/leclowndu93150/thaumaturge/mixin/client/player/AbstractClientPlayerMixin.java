package com.leclowndu93150.thaumaturge.mixin.client.player;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.donator.Donators;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerMixin {
    @Unique
    private static final ResourceLocation THAUMATURGE_CAPE = TTIds.rl("textures/entity/thaumaturge_cape.png");

    @Inject(method = "getSkin", at = @At("RETURN"), cancellable = true)
    private void thaumaturge$donatorCape(CallbackInfoReturnable<PlayerSkin> callback) {
        AbstractClientPlayer player = (AbstractClientPlayer) (Object) this;
        if (Donators.is(player) && player.getData(TTAttachments.DONATOR_CAPE)) {
            PlayerSkin skin = callback.getReturnValue();
            callback.setReturnValue(new PlayerSkin(
                    skin.texture(),
                    skin.textureUrl(),
                    THAUMATURGE_CAPE,
                    skin.elytraTexture(),
                    skin.model(),
                    skin.secure()));
        }
    }
}
