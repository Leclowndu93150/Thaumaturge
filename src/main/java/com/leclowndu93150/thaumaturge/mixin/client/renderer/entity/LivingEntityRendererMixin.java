package com.leclowndu93150.thaumaturge.mixin.client.renderer.entity;

import com.leclowndu93150.thaumaturge.client.champion.ChampionRenderState;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionHelper;
import com.leclowndu93150.thaumaturge.content.entity.champion.ChampionModifier;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, S extends LivingEntityRenderState, M extends EntityModel<? super S>> {
    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;F)V", at = @At("TAIL"))
    private void thaumaturge$extractChampionType(T entity, S state, float partialTicks, CallbackInfo ci) {
        ((ChampionRenderState) state).thaumaturge$setChampionType(ChampionHelper.championType(entity));
        ((ChampionRenderState) state).thaumaturge$setEntityId(entity.getId());
    }

    @Inject(method = "getModelTint", at = @At("RETURN"), cancellable = true)
    private void thaumaturge$championTint(S state, CallbackInfoReturnable<Integer> cir) {
        if (((ChampionRenderState) state).thaumaturge$championType() == ChampionModifier.GRIM) {
            cir.setReturnValue(ARGB.multiply(cir.getReturnValue(), ARGB.colorFromFloat(1.0F, 0.6F, 0.6F, 0.6F)));
        }
    }
}
