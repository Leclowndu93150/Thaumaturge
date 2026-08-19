package com.leclowndu93150.thaumaturge.content.entity.construct;

import com.leclowndu93150.thaumaturge.registry.TCMenus;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.jspecify.annotations.Nullable;

public final class MenuTurretAdvanced extends MenuTurretBasic {
    public static final int AMMO_X = 42;
    public static final int BUTTON_ANIMAL = 1;
    public static final int BUTTON_MOB = 2;
    public static final int BUTTON_PLAYER = 3;
    public static final int BUTTON_FRIENDLY = 4;

    public MenuTurretAdvanced(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        super(TCMenus.TURRET_ADVANCED.get(), containerId, playerInventory, playerInventory.player.level().getEntity(buf.readVarInt()) instanceof EntityTurretCrossbowAdvanced t ? t : null, AMMO_X,
                AMMO_Y);
    }

    private MenuTurretAdvanced(int containerId, Inventory playerInventory, @Nullable EntityTurretCrossbowAdvanced turret) {
        super(TCMenus.TURRET_ADVANCED.get(), containerId, playerInventory, turret, AMMO_X, AMMO_Y);
    }

    public static void open(Player player, EntityTurretCrossbowAdvanced turret) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(new SimpleMenuProvider((id, inv, p) -> new MenuTurretAdvanced(id, inv, turret), turret.getDisplayName()), buf -> buf.writeVarInt(turret.getId()));
        }
    }

    public @Nullable EntityTurretCrossbowAdvanced advancedTurret() {
        return turret instanceof EntityTurretCrossbowAdvanced advanced ? advanced : null;
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        EntityTurretCrossbowAdvanced advanced = advancedTurret();
        if (advanced == null) {
            return super.clickMenuButton(player, id);
        }
        switch (id) {
            case BUTTON_ANIMAL -> advanced.setTargetAnimal(!advanced.getTargetAnimal());
            case BUTTON_MOB -> advanced.setTargetMob(!advanced.getTargetMob());
            case BUTTON_PLAYER -> advanced.setTargetPlayer(!advanced.getTargetPlayer());
            case BUTTON_FRIENDLY -> advanced.setTargetFriendly(!advanced.getTargetFriendly());
            default -> {
                return super.clickMenuButton(player, id);
            }
        }
        return true;
    }
}
