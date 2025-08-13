package dev.overgrown.thaumaturge;

import dev.overgrown.aspectslib.AspectsLib;
import dev.overgrown.thaumaturge.component.ModComponents;
import dev.overgrown.thaumaturge.registry.ModItems;
import dev.overgrown.thaumaturge.spell.impl.ignis.IgnisEffect;
import dev.overgrown.thaumaturge.spell.modifier.ModifierRegistry;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifier;
import dev.overgrown.thaumaturge.spell.modifier.PowerModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.ScatterModifier;
import dev.overgrown.thaumaturge.spell.modifier.ScatterModifierEffect;
import dev.overgrown.thaumaturge.spell.modifier.StableModifier;
import dev.overgrown.thaumaturge.spell.networking.SpellCastPacket;
import dev.overgrown.thaumaturge.spell.pattern.AspectRegistry;
import net.fabricmc.api.ModInitializer;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Thaumaturge implements ModInitializer {
    public static final String MOD_ID = "thaumaturge";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static Identifier identifier(String path) {
        return new Identifier(MOD_ID, path);
    }

    @Override
    public void onInitialize() {
        // Components
        ModComponents.register();
        
        // Items
        ModItems.initialize();

        // Spell components
        registerAspectEffects();
        registerModifierEffects();

        // Networking
        SpellCastPacket.registerServer();

        LOGGER.info("Thaumaturge initialized!");
    }

    private void registerAspectEffects() {
        AspectRegistry.register(AspectsLib.identifier("ignis"), new IgnisEffect());
    }

    private void registerModifierEffects() {
        ModifierRegistry.register(identifier("power"), new PowerModifier());
        ModifierRegistry.register(identifier("scatter"), new ScatterModifier());

        ModifierRegistry.register(identifier("power_modifier"), new PowerModifierEffect());
        ModifierRegistry.register(identifier("scatter_modifier"), new ScatterModifierEffect());

        ModifierRegistry.register(identifier("stable"), new StableModifier());
    }
}
