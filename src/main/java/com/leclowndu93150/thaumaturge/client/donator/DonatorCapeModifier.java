package com.leclowndu93150.thaumaturge.client.donator;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.content.donator.Donators;
import com.leclowndu93150.thaumaturge.registry.TTAttachments;
import net.minecraft.client.entity.ClientAvatarEntity;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.core.ClientAsset;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerSkin;
import net.neoforged.neoforge.client.renderstate.AvatarRenderStateModifier;

public final class DonatorCapeModifier extends AvatarRenderStateModifier {
    private static final ClientAsset.ResourceTexture CAPE = new ClientAsset.ResourceTexture(TTIds.rl("entity/thaumaturge_cape"));

    @Override
    public <T extends Avatar & ClientAvatarEntity> void accept(T avatar, AvatarRenderState state) {
        if (avatar instanceof Player player && Donators.is(player) && player.getData(TTAttachments.DONATOR_CAPE)) {
            PlayerSkin skin = state.skin;
            state.skin = new PlayerSkin(skin.body(), CAPE, skin.elytra(), skin.model(), skin.secure());
        }
    }
}
