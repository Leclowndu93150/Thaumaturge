package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import java.util.Set;
import net.minecraft.core.Holder;
import org.jspecify.annotations.Nullable;

public final class SealAccess {
    private SealAccess() {}

    public static boolean allows(@Nullable ISealEntity seal, IGolemAPI golem) {
        if (seal == null) {
            return true;
        }
        if (seal.isLocked() && !golem.ownerIdentity().map(owner -> owner.equals(seal.owner())).orElse(false)) {
            return false;
        }
        SealType type = seal.type();
        Set<GolemTrait> traits = golem.properties().traits();
        return type.requiredTraits().stream().map(Holder::value).allMatch(traits::contains) && type.forbiddenTraits().stream().map(Holder::value).noneMatch(traits::contains);
    }
}
