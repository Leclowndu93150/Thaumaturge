package com.leclowndu93150.thaumaturge.content.spell.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.leclowndu93150.thaumaturge.api.spell.affinity.ActionContext;
import com.leclowndu93150.thaumaturge.api.spell.cast.CastContext;
import net.minecraft.world.damagesource.DamageTypes;
import org.junit.jupiter.api.Test;

class DamageActionScalingTest {
    @Test
    void powerMultiplierScalesBaseAndPerPowerDamage() {
        CastContext cast = mock(CastContext.class);
        when(cast.setting("power")).thenReturn(5);
        DamageAction flux = new DamageAction(DamageTypes.INDIRECT_MAGIC, 3.0F, 1.0F, 1.0F, false);
        assertEquals(8.0F, flux.damage(new ActionContext(cast, null, null, 5.0F, 0, 0)), 0.0001F);
        assertEquals(2.0F, flux.damage(new ActionContext(cast, null, null, 1.25F, 0, 0)), 0.0001F);
        assertEquals(0.0F, flux.damage(new ActionContext(cast, null, null, 0.0F, 0, 0)), 0.0001F);
    }

    @Test
    void missingPowerSettingUsesOneAndIncludesSplashFalloff() {
        CastContext cast = mock(CastContext.class);
        DamageAction action = new DamageAction(DamageTypes.INDIRECT_MAGIC, 3.0F, 1.0F, 1.0F, false);
        assertEquals(3.0F, action.damage(new ActionContext(cast, null, null, 0.75F, 0, 0)), 0.0001F);
    }
}
