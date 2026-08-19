package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.damagesource.TCDamageTypes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public final class TCDamageTypeTagsProvider extends DamageTypeTagsProvider {
    public static final TagKey<DamageType> IS_MAGIC = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(TCIds.MODID, "is_magic"));

    public TCDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(DamageTypeTags.BYPASSES_ARMOR).add(TCDamageTypes.TAINT, TCDamageTypes.DISSOLVE);
        tag(DamageTypeTags.BYPASSES_SHIELD).add(TCDamageTypes.TAINT);
        tag(DamageTypeTags.WITCH_RESISTANT_TO).add(TCDamageTypes.TAINT);
        tag(DamageTypeTags.WITHER_IMMUNE_TO).add(TCDamageTypes.TAINT);
        tag(IS_MAGIC).add(TCDamageTypes.TAINT);
        tag(DamageTypeTags.IS_FIRE).add(TCDamageTypes.FOCUS_FIRE);
        tag(DamageTypeTags.IS_PROJECTILE).add(TCDamageTypes.FOCUS_FIRE);
    }
}
