package com.leclowndu93150.thaumaturge.data.spell;

import com.leclowndu93150.thaumaturge.TTIds;
import com.leclowndu93150.thaumaturge.api.aspect.TTAspects;
import com.leclowndu93150.thaumaturge.api.spell.TTSpellParts;
import com.leclowndu93150.thaumaturge.api.spell.affinity.AffinityReaction;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStat;
import com.leclowndu93150.thaumaturge.api.spell.cast.SpellStats;
import com.leclowndu93150.thaumaturge.api.spell.part.SpellPart;
import com.leclowndu93150.thaumaturge.content.spell.delivery.ArchitectDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.AuraDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.BatDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.ChainDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.CloudDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.HierophantDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.MineDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.ProjectileDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.RayDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SelfDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SprayDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.SummonDelivery;
import com.leclowndu93150.thaumaturge.content.spell.delivery.WallDelivery;
import com.leclowndu93150.thaumaturge.content.spell.effect.AspectBurstEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.AspectStrikeEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.BlinkEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.BreakEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.EldritchRendEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.ExchangeEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.HarvestEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.HellbatEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.ImbueEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.PrimalEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.RiftEffect;
import com.leclowndu93150.thaumaturge.content.spell.effect.WardEffect;
import com.leclowndu93150.thaumaturge.content.spell.flow.BranchFlow;
import com.leclowndu93150.thaumaturge.content.spell.flow.DelayFlow;
import com.leclowndu93150.thaumaturge.content.spell.flow.OriginFlow;
import com.leclowndu93150.thaumaturge.content.spell.flow.RelayFlow;
import com.leclowndu93150.thaumaturge.content.spell.flow.SieveFlow;
import com.leclowndu93150.thaumaturge.content.spell.modifier.EchoModifier;
import com.leclowndu93150.thaumaturge.content.spell.modifier.FanModifier;
import com.leclowndu93150.thaumaturge.content.spell.modifier.ScatterModifier;
import com.leclowndu93150.thaumaturge.content.spell.modifier.StatChange;
import com.leclowndu93150.thaumaturge.content.spell.modifier.StatModifier;
import com.leclowndu93150.thaumaturge.registry.TTEntities;
import com.leclowndu93150.thaumaturge.registry.TTSounds;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;

public final class SpellPartBootstrap {
    private static final int MODIFIER = 0x8590AE;
    private static final String LEVEL = "level";

    private SpellPartBootstrap() {}

    public static void bootstrap(BootstrapContext<SpellPart> ctx) {
        flows(ctx);
        deliveries(ctx);
        strikes(ctx);
        effects(ctx);
        modifiers(ctx);
        boss(ctx);
    }

    private static void flows(BootstrapContext<SpellPart> ctx) {
        PartBuilder.of(new OriginFlow()).glyph("root").hidden().register(ctx, TTSpellParts.ORIGIN);
        PartBuilder.of(new BranchFlow(3, 2)).glyph("split_trajectory").complexity(4).power(0.75F).aspect(TTAspects.PERMUTATIO).research("focus_split").register(ctx, TTSpellParts.BRANCH);
        PartBuilder.of(new RelayFlow(12.0)).glyph("split_target").complexity(2).aspect(TTAspects.MOTUS).research("focus_flow")
                .options("direction", 0.0F, "forward", "reflect", "up", "down", "caster", "nearest").register(ctx, TTSpellParts.RELAY);
        PartBuilder.of(new DelayFlow()).glyph("delay").complexity(1).aspect(TTAspects.VACUOS).research("focus_flow").setting("ticks", 5, 60, 5, 20, 0.25F).register(ctx, TTSpellParts.DELAY);
        PartBuilder.of(new SieveFlow()).glyph("sieve").complexity(1).aspect(TTAspects.SENSUS).research("focus_flow")
                .options("keep", 0.0F, "entities", "blocks", "enemies", "allies", "undead", "living").register(ctx, TTSpellParts.SIEVE);
    }

    private static void deliveries(BootstrapContext<SpellPart> ctx) {
        PartBuilder.of(new RayDelivery(3.0, true, RayDelivery.Visual.FLASH, 0.0F, 1)).glyph("touch").color(0xAD8585).complexity(2).aspect(TTAspects.AVERSIO).research("base_auromancy").register(ctx,
                TTSpellParts.TOUCH);
        PartBuilder.of(new RayDelivery(16.0, false, RayDelivery.Visual.ARC, 0.66F, 1)).glyph("bolt").color(0xAD9885).complexity(5).aspect(TTAspects.POTENTIA).research("focus_bolt").sound(TTSounds.ZAP)
                .register(ctx, TTSpellParts.BOLT);
        PartBuilder.of(new RayDelivery(16.0, false, RayDelivery.Visual.BEAM, 0.0F, 6)).glyph("beam").color(0xC8B26E).complexity(6).power(0.8F).aspect(TTAspects.LUX).research("focus_beam")
                .setting("range", 8, 24, 4, 12, 0.5F).register(ctx, TTSpellParts.BEAM);
        PartBuilder.of(new ProjectileDelivery(0.33F, 0.33F, 0.01, 0.0F)).glyph("projectile").color(0xADAE85).complexity(4).aspect(TTAspects.MOTUS).research("focus_projectile")
                .setting("speed", 1, 5, 1, 2, 0.5F).register(ctx, TTSpellParts.PROJECTILE);
        PartBuilder.of(new ProjectileDelivery(0.75F, 0.0F, 0.05, 2.0F)).glyph("lob").color(0x9A9A70).complexity(5).power(0.9F).aspect(TTAspects.TERRA).research("focus_lob").register(ctx,
                TTSpellParts.LOB);
        PartBuilder.of(new SprayDelivery(60.0F, 8, 6)).glyph("spray").color(0xA9AE90).complexity(5).power(0.6F).aspect(TTAspects.AER).research("focus_beam").setting("range", 3, 6, 1, 4, 1.0F)
                .register(ctx, TTSpellParts.SPRAY);
        PartBuilder.of(new ChainDelivery(16.0, 6.0, 0.8F, 0.5F)).glyph("chain").color(0xAAAE85).complexity(6).power(0.8F).aspect(TTAspects.POTENTIA).research("focus_beam")
                .setting("jumps", 1, 4, 1, 2, 2.0F).sound(TTSounds.ZAP).register(ctx, TTSpellParts.CHAIN);
        PartBuilder.of(new AuraDelivery(16)).glyph("sprite").color(0xAE9AAE).complexity(5).power(0.7F).aspect(TTAspects.AURAM).research("focus_aura").setting("radius", 2, 6, 1, 3, 1.5F).register(ctx,
                TTSpellParts.AURA);
        PartBuilder.of(new SelfDelivery()).glyph("self").color(0xAE9A85).complexity(1).aspect(TTAspects.HUMANUS).research("focus_aura").register(ctx, TTSpellParts.SELF);
        PartBuilder.of(new CloudDelivery()).glyph("cloud").color(0x99AE85).complexity(4).power(0.5F).aspect(TTAspects.ALKIMIA).research("focus_cloud").setting("radius", 1, 3, 1, 1, 2.0F)
                .setting("duration", 5, 30, 5, 10, 1.0F).register(ctx, TTSpellParts.CLOUD);
        PartBuilder.of(new MineDelivery()).glyph("mine").color(0x85AE85).complexity(4).aspect(TTAspects.VINCULUM).research("focus_mine").options("target", 0.0F, "enemy", "ally").register(ctx,
                TTSpellParts.MINE);
        PartBuilder.of(new WallDelivery(8.0)).glyph("build").color(0x85A0AE).complexity(6).power(0.5F).aspect(TTAspects.PRAEMUNIO).research("focus_lob").setting("width", 3, 7, 2, 3, 1.0F)
                .setting("duration", 5, 20, 5, 10, 1.0F).register(ctx, TTSpellParts.WALL);
        PartBuilder.of(new BatDelivery()).glyph("spellbat").color(0x85AEAC).complexity(8).power(0.33F).aspect(TTAspects.BESTIA).research("focus_spellbat").options("target", 0.0F, "enemy", "ally")
                .register(ctx, TTSpellParts.SPELLBAT);
        PartBuilder.of(new SummonDelivery(3)).glyph("summon").color(0x9A85AE).complexity(9).power(0.4F).aspect(TTAspects.SPIRITUS).research("focus_sprite").setting("duration", 10, 30, 10, 10, 2.0F)
                .register(ctx, TTSpellParts.SPRITE);
        PartBuilder.of(new ArchitectDelivery(16.0)).glyph("plan").color(0x85AE98).complexity(4).aspect(TTAspects.FABRICO).research("focus_plan").max(1).options("method", 0.0F, "full", "surface")
                .register(ctx, TTSpellParts.PLAN);
    }

    private static void strikes(BootstrapContext<SpellPart> ctx) {
        PartBuilder.of(new AspectStrikeEffect(1.0F)).glyph("fire").color(0xFF5A01).complexity(2).aspect(TTAspects.IGNIS).research("base_auromancy").fx("flame").setting("power", 1, 5, 1, 1, 2.0F)
                .setting("duration", 0, 5, 1, 1, 1.0F).register(ctx, TTSpellParts.FIRE);
        PartBuilder.of(new AspectStrikeEffect(2.0F)).glyph("frost").color(0xE1FFFF).complexity(2).aspect(TTAspects.GELUM).research("focus_elemental").fx("frost").setting("power", 1, 5, 1, 1, 2.0F)
                .setting("duration", 2, 10, 1, 2, 1.0F).register(ctx, TTSpellParts.FROST);
        PartBuilder.of(new AspectStrikeEffect(2.0F)).glyph("air").color(0xFFFF7E).complexity(2).aspect(TTAspects.AER).research("focus_elemental").fx("gust").setting("power", 1, 5, 1, 1, 2.0F)
                .register(ctx, TTSpellParts.AIR);
        PartBuilder.of(new AspectStrikeEffect(2.0F)).glyph("earth").color(0x56C000).complexity(3).aspect(TTAspects.TERRA).research("focus_elemental").fx("pebble").setting("power", 1, 5, 1, 1, 3.0F)
                .register(ctx, TTSpellParts.EARTH);
        PartBuilder.of(new AspectStrikeEffect(2.0F)).glyph("flux").color(0x800080).complexity(3).aspect(TTAspects.VITIUM).research("focus_flux").fx("flux").setting("power", 1, 5, 1, 1, 3.0F)
                .register(ctx, TTSpellParts.FLUX);
        PartBuilder.of(new AspectStrikeEffect(2.0F)).glyph("curse").color(0x6A0005).complexity(3).aspect(TTAspects.MORTUUS).research("focus_curse").fx("curse").setting("power", 1, 5, 1, 1, 3.0F)
                .setting("duration", 1, 10, 1, 3, 1.0F).register(ctx, TTSpellParts.CURSE);
        PartBuilder.of(new AspectStrikeEffect(2.0F)).glyph("heal").color(0xDE0005).complexity(3).aspect(TTAspects.VICTUS).research("focus_heal").fx("heal").setting("power", 1, 5, 1, 1, 3.0F)
                .register(ctx, TTSpellParts.HEAL);
    }

    private static void effects(BootstrapContext<SpellPart> ctx) {
        PartBuilder.of(new AspectBurstEffect(2.0F, 0.75F)).glyph("burst").color(0xFF5A01).complexity(6).chooses(AffinityReaction.STRIKE, TTAspects.IGNIS).research("focus_burst")
                .setting("power", 1, 5, 1, 2, 2.0F).setting("radius", 1, 4, 1, 2, 2.0F).register(ctx, TTSpellParts.BURST);
        PartBuilder.of(new ImbueEffect(3.0F)).glyph("imbue").color(0xCF00FF).complexity(4).chooses(AffinityReaction.IMBUE, TTAspects.VICTUS).research("focus_imbue")
                .setting("potency", 1, 3, 1, 1, 3.0F).setting("duration", 1, 10, 1, 3, 1.0F).register(ctx, TTSpellParts.IMBUE);
        PartBuilder.of(new BreakEffect(0.25F, 0.25F, 0.1F)).glyph("break").color(0x8A4A08).complexity(3).aspect(TTAspects.PERDITIO).research("focus_break").fx("crack")
                .sound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.END_GATEWAY_SPAWN)).setting("power", 1, 5, 1, 1, 3.0F).register(ctx, TTSpellParts.BREAK);
        PartBuilder.of(new ExchangeEffect(0.25F, 0.25F, 0.1F)).glyph("exchange").color(0x578357).complexity(5).aspect(TTAspects.PERMUTATIO).research("focus_exchange")
                .sound(BuiltInRegistries.SOUND_EVENT.wrapAsHolder(SoundEvents.ENCHANTMENT_TABLE_USE)).register(ctx, TTSpellParts.EXCHANGE);
        PartBuilder.of(new RiftEffect()).glyph("rift").color(0x2F1165).complexity(3).aspect(TTAspects.ALIENIS).research("focus_rift").fx("rift").setting("depth", 8, 32, 8, 8, 2.0F)
                .setting("duration", 2, 10, 1, 4, 0.5F).register(ctx, TTSpellParts.RIFT);
        PartBuilder.of(new WardEffect(0.5F)).glyph("ward").color(0xFFE8CF).complexity(4).aspect(TTAspects.ORDO).research("focus_ward").fx("ward").register(ctx, TTSpellParts.WARD);
        PartBuilder.of(new PrimalEffect(4.0F, 1.5F, 0.01F)).glyph("primal").color(0xA5A1C1).complexity(20).vis(2.0F).aspect(TTAspects.PRAECANTATIO).research("focus_primal").fx("primal")
                .setting("power", 1, 5, 1, 1, 3.0F).register(ctx, TTSpellParts.PRIMAL);
        PartBuilder.of(new HellbatEffect(16, 32.0)).glyph("hellbat").color(0xDC3502).complexity(8).aspect(TTAspects.BESTIA).research("focus_hellbat").fx("flame").setting("bats", 1, 3, 1, 1, 8.0F)
                .register(ctx, TTSpellParts.HELLBAT);
        PartBuilder.of(new BlinkEffect(48.0)).glyph("blink").color(0x6A2FA0).complexity(7).aspect(TTAspects.ALIENIS).research("focus_blink").fx("rift").max(1).register(ctx, TTSpellParts.BLINK);
        PartBuilder.of(new HarvestEffect()).glyph("harvest").color(0x6B8E23).complexity(4).aspect(TTAspects.HERBA).research("focus_harvest").fx("leaf").setting("radius", 0, 3, 1, 1, 2.0F)
                .register(ctx, TTSpellParts.HARVEST);
    }

    private static void modifiers(BootstrapContext<SpellPart> ctx) {
        levelled(change(SpellStats.POWER, StatChange.Operation.MULTIPLY, 1.0F, 0.25F, true), 3.0F).glyph("potency").complexity(3).aspect(TTAspects.POTENTIA).research("focus_amplify").register(ctx,
                TTSpellParts.AMPLIFY);
        levelled(change(SpellStats.DURATION, StatChange.Operation.MULTIPLY, 1.0F, 0.5F, true), 2.0F).glyph("lingering").complexity(2).aspect(TTAspects.VINCULUM).research("focus_amplify").register(ctx,
                TTSpellParts.EXTEND);
        levelled(change(SpellStats.RADIUS, StatChange.Operation.ADD, 0.0F, 1.0F, true), 3.0F).glyph("spread").complexity(3).aspect(TTAspects.AER).research("focus_amplify").register(ctx,
                TTSpellParts.WIDEN);
        levelled(change(SpellStats.PIERCE, StatChange.Operation.ADD, 0.0F, 1.0F, true), 2.0F).glyph("pierce").complexity(2).aspect(TTAspects.AVERSIO).research("focus_trajectory").register(ctx,
                TTSpellParts.PIERCE);
        levelled(change(SpellStats.BOUNCE, StatChange.Operation.ADD, 0.0F, 1.0F, true), 1.0F).glyph("bounce").complexity(1).aspect(TTAspects.VOLATUS).research("focus_projectile", 2).register(ctx,
                TTSpellParts.BOUNCE);
        levelled(change(SpellStats.FORTUNE, StatChange.Operation.ADD, 0.0F, 1.0F, true), 3.0F).glyph("fortune").complexity(3).aspect(TTAspects.DESIDERIUM).research("focus_silk").register(ctx,
                TTSpellParts.FORTUNE);
        PartBuilder.of(new StatModifier(List.of(change(SpellStats.HOMING, StatChange.Operation.SET, 1.0F, 0.0F, false)))).glyph("seeker").color(MODIFIER).complexity(4).aspect(TTAspects.SENSUS)
                .research("focus_projectile", 2).register(ctx, TTSpellParts.HOMING);
        PartBuilder.of(new StatModifier(List.of(change(SpellStats.SILK_TOUCH, StatChange.Operation.SET, 1.0F, 0.0F, false)))).glyph("silk").color(MODIFIER).complexity(4).aspect(TTAspects.INSTRUMENTUM)
                .research("focus_silk").register(ctx, TTSpellParts.SILK_TOUCH);
        PartBuilder.of(new StatModifier(List.of(change(SpellStats.SPEED, StatChange.Operation.MULTIPLY, 1.5F, 0.0F, false)))).glyph("quicken").color(MODIFIER).complexity(2).cooldownMultiplier(0.8F)
                .aspect(TTAspects.MOTUS).research("focus_amplify").register(ctx, TTSpellParts.QUICKEN);
        PartBuilder.of(new StatModifier(List.of(change(SpellStats.POWER, StatChange.Operation.MULTIPLY, 0.85F, 0.0F, false)))).glyph("frugal").color(MODIFIER).complexity(1).visMultiplier(0.7F)
                .aspect(TTAspects.ORDO).research("focus_amplify").max(1).register(ctx, TTSpellParts.EFFICIENT);
        PartBuilder.of(new ScatterModifier(2.0F)).glyph("scatter").color(MODIFIER).complexity(2).aspect(TTAspects.PERMUTATIO).research("focus_scatter").max(1).setting("forks", 2, 8, 1, 3, 1.0F)
                .setting("cone", 10, 90, 10, 30, 0.0F).register(ctx, TTSpellParts.SCATTER);
        PartBuilder.of(new FanModifier(2.0F)).glyph("fan").color(MODIFIER).complexity(2).aspect(TTAspects.AER).research("focus_trajectory").max(1).setting("count", 2, 7, 1, 3, 1.0F)
                .setting("arc", 30, 120, 15, 60, 0.0F).register(ctx, TTSpellParts.FAN);
        PartBuilder.of(new EchoModifier(10, 0.6F)).glyph("echo").color(MODIFIER).complexity(4).aspect(TTAspects.PERMUTATIO).research("focus_echo").setting("repeats", 1, 3, 1, 1, 4.0F).register(ctx,
                TTSpellParts.ECHO);
    }

    private static PartBuilder levelled(StatChange change, float perLevel) {
        return PartBuilder.of(new StatModifier(List.of(change))).color(MODIFIER).setting(LEVEL, 1, 3, 1, 1, perLevel);
    }

    private static StatChange change(SpellStat stat, StatChange.Operation operation, float base, float perLevel, boolean levelled) {
        return new StatChange(stat.id(), operation, base, perLevel, levelled ? Optional.of(LEVEL) : Optional.empty(), Optional.empty());
    }

    private static void boss(BootstrapContext<SpellPart> ctx) {
        hierophant(ctx, TTIds.ELDRITCH_CRESCENT, TTEntities.HIEROPHANT_CRESCENT.get());
        hierophant(ctx, TTIds.ELDRITCH_SIGIL, TTEntities.HIEROPHANT_SIGIL.get());
        hierophant(ctx, TTIds.ELDRITCH_NOVA, TTEntities.HIEROPHANT_NOVA.get());
        hierophant(ctx, TTIds.ELDRITCH_HAMMER, TTEntities.HIEROPHANT_HAMMER.get());
        PartBuilder.of(new EldritchRendEffect(0.35)).glyph("flux").color(0xAC80D0).hidden().register(ctx, ResourceKey.create(SpellPart.REGISTRY_KEY, TTIds.ELDRITCH_REND));
    }

    private static void hierophant(BootstrapContext<SpellPart> ctx, Identifier id, EntityType<?> spell) {
        PartBuilder.of(new HierophantDelivery(spell)).glyph("flux").color(0xAC80D0).hidden().setting("left", 0, 1, 1, 0, 0.0F).register(ctx, ResourceKey.create(SpellPart.REGISTRY_KEY, id));
    }
}
