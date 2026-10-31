package com.leclowndu93150.thaumaturge.mixin.neoforge.client.model.item;

import com.leclowndu93150.thaumaturge.client.color.ArmorTrimTint;
import java.util.List;
import net.minecraft.client.color.item.ItemTintSource;
import net.neoforged.neoforge.client.model.item.TrimmedArmorModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(TrimmedArmorModel.class)
public class TrimmedArmorModelMixin {
    @Unique
    private static final List<ItemTintSource> THAUMATURGE$TRIM_TINTS = List.of(new ArmorTrimTint());

    @ModifyArg(method = "createTrimLayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/item/CuboidItemModelWrapper;<init>(Ljava/util/List;Lnet/minecraft/client/resources/model/geometry/QuadCollection;Lnet/minecraft/client/renderer/item/ModelRenderProperties;Lorg/joml/Matrix4fc;)V"), index = 0)
    private List<ItemTintSource> thaumaturge$armorTrimTint(List<ItemTintSource> tints) {
        return THAUMATURGE$TRIM_TINTS;
    }
}
