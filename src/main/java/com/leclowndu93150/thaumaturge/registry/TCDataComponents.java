package com.leclowndu93150.thaumaturge.registry;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.AspectInstance;
import com.leclowndu93150.thaumaturge.api.aspect.AspectList;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.casters.FocusPackage;
import com.leclowndu93150.thaumaturge.api.essentia.EssentiaList;
import com.leclowndu93150.thaumaturge.api.wands.WandVis;
import com.leclowndu93150.thaumaturge.content.aura.node.NodeData;
import com.leclowndu93150.thaumaturge.content.casters.CasterArea;
import com.leclowndu93150.thaumaturge.content.equipment.InfusionEnchantments;
import com.leclowndu93150.thaumaturge.content.essentia.EssentiaContentsComponent;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.item.CelestialBody;
import com.leclowndu93150.thaumaturge.content.legacy.LegacyIds;
import com.leclowndu93150.thaumaturge.content.research.link.LinkBinding;
import com.leclowndu93150.thaumaturge.content.research.note.ResearchNoteData;
import com.leclowndu93150.thaumaturge.content.research.share.ShareBinding;
import com.leclowndu93150.thaumaturge.content.wands.WandParts;
import com.mojang.serialization.Codec;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemContainerContents;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class TCDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, TCIds.MODID);

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AspectList>> ASPECTS =
            DATA_COMPONENTS.registerComponentType(
                    "aspects",
                    builder -> builder.persistent(AspectList.CODEC).networkSynchronized(AspectList.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EssentiaList>> ESSENTIA_CONTENTS =
            DATA_COMPONENTS.registerComponentType(
                    "essentia_contents",
                    builder -> builder.persistent(EssentiaContentsComponent.CODEC)
                            .networkSynchronized(EssentiaContentsComponent.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResourceKey<IAspect>>> ASPECT_FILTER =
            DATA_COMPONENTS.registerComponentType(
                    "aspect_filter",
                    builder -> builder.persistent(LegacyIds.ASPECT_KEY_CODEC)
                            .networkSynchronized(ResourceKey.streamCodec(IAspect.REGISTRY_KEY)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CelestialBody>> CELESTIAL_BODY =
            DATA_COMPONENTS.registerComponentType(
                    "celestial_body",
                    builder -> builder.persistent(CelestialBody.CODEC).networkSynchronized(CelestialBody.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<AspectInstance>> CRYSTAL_ASPECT =
            DATA_COMPONENTS.registerComponentType(
                    "crystal_aspect",
                    builder ->
                            builder.persistent(AspectInstance.CODEC).networkSynchronized(AspectInstance.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FocusPackage>> FOCUS_PACKAGE =
            DATA_COMPONENTS.registerComponentType(
                    "focus_package",
                    builder -> builder.persistent(FocusPackage.CODEC).networkSynchronized(FocusPackage.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SocketedFocus>> SOCKETED_FOCUS =
            DATA_COMPONENTS.registerComponentType(
                    "socketed_focus",
                    builder -> builder.persistent(SocketedFocus.CODEC).networkSynchronized(SocketedFocus.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CasterArea>> CASTER_AREA =
            DATA_COMPONENTS.registerComponentType(
                    "caster_area",
                    builder -> builder.persistent(CasterArea.CODEC).networkSynchronized(CasterArea.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockState>> PICKED_BLOCK =
            DATA_COMPONENTS.registerComponentType(
                    "picked_block",
                    builder -> builder.persistent(BlockState.CODEC)
                            .networkSynchronized(ByteBufCodecs.fromCodec(BlockState.CODEC)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> GRAPPLE_LOADED =
            DATA_COMPONENTS.registerComponentType(
                    "grapple_loaded",
                    builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGE =
            DATA_COMPONENTS.registerComponentType(
                    "charge",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<InfusionEnchantments>>
            INFUSION_ENCHANTMENTS = DATA_COMPONENTS.registerComponentType(
                    "infusion_enchantments",
                    builder -> builder.persistent(InfusionEnchantments.CODEC)
                            .networkSynchronized(InfusionEnchantments.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STACK_WARP =
            DATA_COMPONENTS.registerComponentType(
                    "warp",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> RUNIC_CHARGE =
            DATA_COMPONENTS.registerComponentType(
                    "runic_charge",
                    builder -> builder.persistent(ExtraCodecs.POSITIVE_INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TOOL_ORIENTATION =
            DATA_COMPONENTS.registerComponentType(
                    "tool_orientation",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ENERGY =
            DATA_COMPONENTS.registerComponentType(
                    "energy",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GolemProperties>> GOLEM_PROPERTIES =
            DATA_COMPONENTS.registerComponentType(
                    "golem_properties",
                    builder -> builder.persistent(GolemProperties.CODEC)
                            .networkSynchronized(GolemProperties.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> GOLEM_XP =
            DATA_COMPONENTS.registerComponentType(
                    "golem_xp",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STORED_XP =
            DATA_COMPONENTS.registerComponentType(
                    "stored_xp",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> VERDANT_TYPE =
            DATA_COMPONENTS.registerComponentType(
                    "verdant_type",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FORTRESS_MASK =
            DATA_COMPONENTS.registerComponentType(
                    "fortress_mask",
                    builder -> builder.persistent(ExtraCodecs.NON_NEGATIVE_INT)
                            .networkSynchronized(ByteBufCodecs.VAR_INT));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> GOGGLES_UPGRADE =
            DATA_COMPONENTS.registerComponentType(
                    "goggles_upgrade",
                    builder -> builder.persistent(Unit.CODEC).networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ItemContainerContents>> POUCH_CONTENTS =
            DATA_COMPONENTS.registerComponentType(
                    "pouch_contents",
                    builder -> builder.persistent(ItemContainerContents.CODEC)
                            .networkSynchronized(ItemContainerContents.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<GlobalPos>> MIRROR_LINK =
            DATA_COMPONENTS.registerComponentType(
                    "mirror_link",
                    builder -> builder.persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> NOTE_COMPLETE =
            DATA_COMPONENTS.registerComponentType(
                    "note_complete", builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> WHIRLWIND_DISABLED =
            DATA_COMPONENTS.registerComponentType(
                    "whirlwind_disabled",
                    builder -> builder.persistent(Codec.BOOL).networkSynchronized(ByteBufCodecs.BOOL));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ResearchNoteData>> RESEARCH_NOTE =
            DATA_COMPONENTS.registerComponentType(
                    "research_note",
                    builder -> builder.persistent(ResearchNoteData.CODEC)
                            .networkSynchronized(ResearchNoteData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<NodeData>> NODE_DATA =
            DATA_COMPONENTS.registerComponentType(
                    "node_data",
                    builder -> builder.persistent(NodeData.CODEC).networkSynchronized(NodeData.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WandParts>> WAND_PARTS =
            DATA_COMPONENTS.registerComponentType(
                    "wand_parts",
                    builder -> builder.persistent(WandParts.CODEC).networkSynchronized(WandParts.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WandVis>> WAND_VIS =
            DATA_COMPONENTS.registerComponentType(
                    "wand_vis",
                    builder -> builder.persistent(LegacyIds.WAND_VIS_CODEC).networkSynchronized(WandVis.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ShareBinding>> SHARE_BINDING =
            DATA_COMPONENTS.registerComponentType(
                    "share_binding",
                    builder -> builder.persistent(ShareBinding.CODEC).networkSynchronized(ShareBinding.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<LinkBinding>> LINK_BINDING =
            DATA_COMPONENTS.registerComponentType(
                    "link_binding",
                    builder -> builder.persistent(LinkBinding.CODEC).networkSynchronized(LinkBinding.STREAM_CODEC));

    private TCDataComponents() {}

    public static void register(IEventBus modBus) {
        DATA_COMPONENTS.register(modBus);
    }
}
