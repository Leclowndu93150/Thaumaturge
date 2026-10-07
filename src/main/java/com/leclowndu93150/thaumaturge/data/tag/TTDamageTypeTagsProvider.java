package com.leclowndu93150.thaumaturge.data.tag;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.damagesource.TTDamageTypes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;

public final class TTDamageTypeTagsProvider extends DamageTypeTagsProvider {
    public static final TagKey<DamageType> IS_MAGIC = TagKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(TTIds.MODID, "is_magic"));

    public TTDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider);
    }

    @Override
    protected void addTags(HolderLookup.Provider lookupProvider) {
        tag(DamageTypeTags.BYPASSES_ARMOR).add(TTDamageTypes.TAINT, TTDamageTypes.DISSOLVE);
        tag(DamageTypeTags.BYPASSES_SHIELD).add(TTDamageTypes.TAINT);
        tag(DamageTypeTags.WITCH_RESISTANT_TO).add(TTDamageTypes.TAINT);
        tag(DamageTypeTags.WITHER_IMMUNE_TO).add(TTDamageTypes.TAINT);
        tag(IS_MAGIC).add(TTDamageTypes.TAINT, TTDamageTypes.ELDRITCH_SPELL);
        tag(DamageTypeTags.IS_FIRE).add(TTDamageTypes.FOCUS_FIRE);
        tag(DamageTypeTags.IS_PROJECTILE).add(TTDamageTypes.FOCUS_FIRE);
    }
}
