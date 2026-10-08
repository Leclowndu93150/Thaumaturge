package com.leclowndu93150.thaumaturge.content.golem.seals;

import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.IGolemAPI;
import com.leclowndu93150.thaumaturge.api.golems.seals.ISealEntity;
import com.leclowndu93150.thaumaturge.api.golems.seals.SealType;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class SealAccess {
    private static final double REACH_BUFFER = 1.0;

    private SealAccess() {}

    public static boolean mayEdit(Player player, ISealEntity seal) {
        return !seal.isLocked() || player.getUUID().equals(seal.owner()) || player.hasInfiniteMaterials();
    }

    public static boolean isLive(Player player, ISealEntity seal) {
        return SealHandler.getSealEntity(player.level(), seal.pos()) == seal
                && player.canInteractWithBlock(seal.pos().pos(), REACH_BUFFER);
    }

    public static boolean allows(@Nullable ISealEntity seal, IGolemAPI golem) {
        if (seal == null) {
            return true;
        }
        if (seal.isLocked()
                && !golem.ownerIdentity()
                        .map(owner -> owner.equals(seal.owner()))
                        .orElse(false)) {
            return false;
        }
        SealType type = seal.type();
        Set<GolemTrait> traits = golem.properties().traits();
        return type.requiredTraits().stream().map(Holder::value).allMatch(traits::contains)
                && type.forbiddenTraits().stream().map(Holder::value).noneMatch(traits::contains);
    }
}
