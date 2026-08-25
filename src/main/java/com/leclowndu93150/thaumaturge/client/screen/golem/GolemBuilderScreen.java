package com.leclowndu93150.thaumaturge.client.screen.golem;

import com.leclowndu93150.thaumaturge.TCIds;
import com.leclowndu93150.thaumaturge.api.aspect.IAspect;
import com.leclowndu93150.thaumaturge.api.aspect.TCAspects;
import com.leclowndu93150.thaumaturge.api.capability.IPlayerKnowledge;
import com.leclowndu93150.thaumaturge.api.capability.KnowledgeAccess;
import com.leclowndu93150.thaumaturge.api.golems.GolemTrait;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemAddon;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemArm;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemHead;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemLeg;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemMaterial;
import com.leclowndu93150.thaumaturge.api.golems.parts.GolemPart;
import com.leclowndu93150.thaumaturge.api.items.InvHelper;
import com.leclowndu93150.thaumaturge.client.render.GuiBlend;
import com.leclowndu93150.thaumaturge.client.screen.AbstractTCContainerScreen;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCButtonIcon;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCHoverButton;
import com.leclowndu93150.thaumaturge.client.screen.widget.TCScrollButton;
import com.leclowndu93150.thaumaturge.content.golem.GolemProperties;
import com.leclowndu93150.thaumaturge.content.golem.press.BlockEntityGolemBuilder;
import com.leclowndu93150.thaumaturge.content.golem.press.MenuGolemBuilder;
import com.leclowndu93150.thaumaturge.network.ServerboundGolemPressPayload;
import com.leclowndu93150.thaumaturge.registry.TCGolemParts;
import com.leclowndu93150.thaumaturge.registry.TCGolemTraits;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor.ARGB32;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jspecify.annotations.Nullable;

public final class GolemBuilderScreen extends AbstractTCContainerScreen<MenuGolemBuilder> {
    private static final ResourceLocation TEXTURE = TCIds.rl("textures/gui/gui_golembuilder.png");
    private static final ResourceLocation MATERIAL_ICON = TCIds.rl("textures/item/golem.png");

    private static final int IMAGE_WIDTH = 208;
    private static final int IMAGE_HEIGHT = 224;
    private static final int MISSING_U = 240;
    private static final int MISSING_V = 0;
    private static final int COMPONENT_GRID_X = 144;
    private static final int COMPONENT_GRID_Y = 16;
    private static final int PROGRESS_X = 145;
    private static final int PROGRESS_Y = 89;
    private static final int PROGRESS_U = 209;
    private static final int PROGRESS_V = 89;
    private static final int PROGRESS_WIDTH = 46;
    private static final int PROGRESS_HEIGHT = 6;
    private static final int SOCKET_U = 228;
    private static final int SOCKET_V = 124;
    private static final int SOCKET_SIZE = 24;
    private static final int STAT_Y = 108;
    private static final int STAT_HEARTS_X = 48;
    private static final int STAT_ARMOR_X = 72;
    private static final int STAT_DAMAGE_X = 97;
    private static final int COST_RIGHT_X = 162;
    private static final int COST_Y = 24;
    private static final int CRAFT_X = 120;
    private static final int CRAFT_Y = 104;
    private static final int CRAFT_WIDTH = 24;
    private static final int CRAFT_HEIGHT = 16;
    private static final int CRAFT_U = 216;
    private static final int CRAFT_V = 64;
    private static final int CRAFT_DISABLED_V = 40;
    private static final int WHITE = 0xFFFFFFFF;

    private static int headIndex;
    private static int matIndex;
    private static int armIndex;
    private static int legIndex;
    private static int addonIndex;

    private final List<GolemHead> valHeads = new ArrayList<>();
    private final List<GolemMaterial> valMats = new ArrayList<>();
    private final List<GolemArm> valArms = new ArrayList<>();
    private final List<GolemLeg> valLegs = new ArrayList<>();
    private final List<GolemAddon> valAddons = new ArrayList<>();

    private GolemProperties props = GolemProperties.createDefault();
    private float hearts;
    private float armor;
    private float damage;
    private int cost;
    private boolean allFound;
    private List<ItemStack> components = List.of();
    private boolean[] owns = new boolean[0];
    private boolean disableAll;
    private boolean[] lastStuff;
    private CraftButton craftButton;

    public GolemBuilderScreen(MenuGolemBuilder menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, IMAGE_WIDTH, IMAGE_HEIGHT);
    }

    @Override
    protected void init() {
        super.init();
        valHeads.clear();
        TCGolemParts.heads().forEach(head -> {
            if (knowsAll(head.research())) valHeads.add(head);
        });
        valMats.clear();
        TCGolemParts.materials().forEach(mat -> {
            if (knowsAll(mat.research())) valMats.add(mat);
        });
        valArms.clear();
        TCGolemParts.arms().forEach(arm -> {
            if (knowsAll(arm.research())) valArms.add(arm);
        });
        valLegs.clear();
        TCGolemParts.legs().forEach(leg -> {
            if (knowsAll(leg.research())) valLegs.add(leg);
        });
        valAddons.clear();
        TCGolemParts.addons().forEach(addon -> {
            if (knowsAll(addon.research())) valAddons.add(addon);
        });
        if (headIndex >= valHeads.size()) {
            headIndex = 0;
        }
        if (matIndex >= valMats.size()) {
            matIndex = 0;
        }
        if (armIndex >= valArms.size()) {
            armIndex = 0;
        }
        if (legIndex >= valLegs.size()) {
            legIndex = 0;
        }
        if (addonIndex >= valAddons.size()) {
            addonIndex = 0;
        }
        gatherInfo();
    }

    private boolean knowsAll(List<ResourceLocation> research) {
        if (minecraft == null || minecraft.player == null) {
            return false;
        }
        IPlayerKnowledge knowledge = KnowledgeAccess.of(minecraft.player);
        for (ResourceLocation id : research) {
            if (!knowledge.isResearchComplete(id)) {
                return false;
            }
        }
        return true;
    }

    private void gatherInfo() {
        clearWidgets();
        craftButton = new CraftButton(leftPos + CRAFT_X, topPos + CRAFT_Y, this::craft);
        addRenderableWidget(craftButton);
        addScrollPair(
                valHeads.size(),
                112,
                16,
                () -> headIndex--,
                () -> headIndex++,
                () -> headIndex,
                valHeads::size,
                this::setHeadIndex);
        addScrollPair(
                valMats.size(),
                16,
                16,
                () -> matIndex--,
                () -> matIndex++,
                () -> matIndex,
                valMats::size,
                this::setMatIndex);
        addScrollPair(
                valArms.size(),
                112,
                40,
                () -> armIndex--,
                () -> armIndex++,
                () -> armIndex,
                valArms::size,
                this::setArmIndex);
        addScrollPair(
                valLegs.size(),
                112,
                64,
                () -> legIndex--,
                () -> legIndex++,
                () -> legIndex,
                valLegs::size,
                this::setLegIndex);
        addScrollPair(
                valAddons.size(),
                16,
                64,
                () -> addonIndex--,
                () -> addonIndex++,
                () -> addonIndex,
                valAddons::size,
                this::setAddonIndex);
        if (!valHeads.isEmpty()) {
            addPartButton(
                    120,
                    24,
                    valHeads.get(headIndex).icon(),
                    "head",
                    keyOf(TCGolemParts.heads(), valHeads.get(headIndex)),
                    WHITE);
        }
        if (!valMats.isEmpty()) {
            addPartButton(
                    24,
                    24,
                    MATERIAL_ICON,
                    "material",
                    keyOf(TCGolemParts.materials(), valMats.get(matIndex)),
                    valMats.get(matIndex).itemColor());
        }
        if (!valArms.isEmpty()) {
            addPartButton(
                    120,
                    48,
                    valArms.get(armIndex).icon(),
                    "arm",
                    keyOf(TCGolemParts.arms(), valArms.get(armIndex)),
                    WHITE);
        }
        if (!valLegs.isEmpty()) {
            addPartButton(
                    120,
                    72,
                    valLegs.get(legIndex).icon(),
                    "leg",
                    keyOf(TCGolemParts.legs(), valLegs.get(legIndex)),
                    WHITE);
        }
        if (!valAddons.isEmpty()
                && !"none"
                        .equals(keyOf(TCGolemParts.addons(), valAddons.get(addonIndex))
                                .getPath())) {
            addPartButton(
                    24,
                    72,
                    valAddons.get(addonIndex).icon(),
                    "addon",
                    keyOf(TCGolemParts.addons(), valAddons.get(addonIndex)),
                    WHITE);
        }
        if (valHeads.isEmpty() || valMats.isEmpty() || valArms.isEmpty() || valLegs.isEmpty() || valAddons.isEmpty()) {
            props = GolemProperties.createDefault();
            components = List.of();
            owns = new boolean[0];
            allFound = false;
            hearts = 0.0F;
            armor = 0.0F;
            damage = 0.0F;
            if (craftButton != null) {
                craftButton.active = false;
            }
            return;
        }
        props = GolemProperties.createDefault();
        props.setHead(valHeads.get(headIndex));
        props.setMaterial(valMats.get(matIndex));
        props.setArms(valArms.get(armIndex));
        props.setLegs(valLegs.get(legIndex));
        props.setAddon(valAddons.get(addonIndex));
        BlockEntityGolemBuilder builder = menu.blockEntity();
        if (builder != null) {
            PacketDistributor.sendToServer(
                    new ServerboundGolemPressPayload(builder.getBlockPos(), props.copy(), false));
        }
        redoComps();
        GolemTrait[] tags = props.getTraits().toArray(new GolemTrait[0]);
        if (tags.length > 0) {
            int yy = tags.length <= 4 ? (tags.length - 1) % 4 * 8 : 24;
            int xx = (tags.length - 1) / 4 % 4 * 8;
            int row = 0;
            int col = 0;
            for (GolemTrait tag : tags) {
                TCHoverButton button = TCHoverButton.centered(
                        leftPos + 72 + col * 16 - xx,
                        topPos + 48 + 16 * row - yy,
                        16,
                        new TCButtonIcon.TextureIcon(tag.icon()),
                        Component.translatable(
                                GolemTrait.nameKey(TCGolemTraits.registry().getKey(tag))),
                        () -> {});
                button.setDescription(Component.translatable(
                        GolemTrait.descriptionKey(TCGolemTraits.registry().getKey(tag))));
                addRenderableWidget(button);
                if (++row > 3) {
                    row = 0;
                    col++;
                }
            }
        }
        int health = 10 + props.getMaterial().healthMod();
        if (props.hasTrait(TCGolemTraits.FRAGILE.get())) {
            health = (int) (health * 0.75);
        }
        hearts = health / 2.0F;
        int armorValue = props.getMaterial().armor();
        if (props.hasTrait(TCGolemTraits.ARMORED.get())) {
            armorValue = (int) Math.max(armorValue * 1.5, armorValue + 1);
        }
        if (props.hasTrait(TCGolemTraits.FRAGILE.get())) {
            armorValue = (int) (armorValue * 0.75);
        }
        armor = armorValue / 2.0F;
        double damageValue = props.hasTrait(TCGolemTraits.FIGHTER.get())
                ? props.getMaterial().damage()
                : 0.0;
        if (props.hasTrait(TCGolemTraits.BRUTAL.get())) {
            damageValue = Math.max(damageValue * 1.5, damageValue + 1.0);
        }
        damage = (float) (damageValue / 2.0);
    }

    private void setHeadIndex(int index) {
        headIndex = index;
    }

    private void setMatIndex(int index) {
        matIndex = index;
    }

    private void setArmIndex(int index) {
        armIndex = index;
    }

    private void setLegIndex(int index) {
        legIndex = index;
    }

    private void setAddonIndex(int index) {
        addonIndex = index;
    }

    private interface IndexSetter {
        void set(int index);
    }

    private interface IndexGetter {
        int get();
    }

    private interface SizeGetter {
        int size();
    }

    private void addScrollPair(
            int optionCount,
            int baseX,
            int baseY,
            Runnable decrement,
            Runnable increment,
            IndexGetter index,
            SizeGetter size,
            IndexSetter setter) {
        if (optionCount <= 1) {
            return;
        }
        addRenderableWidget(TCScrollButton.of(
                leftPos + baseX - 5 - 6,
                topPos - 5 + baseY + 8,
                TCScrollButton.Direction.LEFT,
                Component.empty(),
                () -> {
                    decrement.run();
                    if (index.get() < 0) {
                        setter.set(size.size() - 1);
                    }
                    gatherInfo();
                }));
        addRenderableWidget(TCScrollButton.of(
                leftPos + baseX - 5 + 22,
                topPos - 5 + baseY + 8,
                TCScrollButton.Direction.RIGHT,
                Component.empty(),
                () -> {
                    increment.run();
                    if (index.get() >= size.size()) {
                        setter.set(0);
                    }
                    gatherInfo();
                }));
    }

    private void addPartButton(int x, int y, ResourceLocation icon, String kind, ResourceLocation id, int color) {
        TCHoverButton button = TCHoverButton.centered(
                leftPos + x,
                topPos + y,
                16,
                new TCButtonIcon.TextureIcon(icon),
                Component.translatable(GolemPart.nameKey(kind, id)),
                () -> {});
        button.setDescription(Component.translatable(GolemPart.descriptionKey(kind, id)));
        button.setTintColor(ARGB32.opaque(color));
        addRenderableWidget(button);
    }

    private static <T> ResourceLocation keyOf(Registry<T> registry, T value) {
        ResourceLocation key = registry.getKey(value);
        return key == null ? ResourceLocation.fromNamespaceAndPath("thaumaturge", "unknown") : key;
    }

    private void computeOwnership() {
        if (valHeads.isEmpty() || valMats.isEmpty() || valArms.isEmpty() || valLegs.isEmpty() || valAddons.isEmpty()) {
            allFound = false;
            if (craftButton != null) {
                craftButton.active = false;
            }
            return;
        }
        allFound = true;
        cost = props.getTraits().size() * 2;
        components = props.generateComponents();
        BlockEntityGolemBuilder builder = menu.blockEntity();
        Player player = minecraft.player;
        owns = new boolean[components.size()];
        for (int i = 0; i < components.size(); i++) {
            cost += components.get(i).getCount();
            owns[i] = builder != null && builder.hasStuff != null && builder.hasStuff.length > i && builder.hasStuff[i];
            if (!owns[i] && player != null) {
                owns[i] = InvHelper.isPlayerCarryingAmount(player, components.get(i), true);
            }
            if (!owns[i]) {
                allFound = false;
            }
        }
        for (var widget : children()) {
            if (widget instanceof TCButton button) {
                button.active = !disableAll;
            }
        }
        if (!disableAll && craftButton != null) {
            craftButton.active = allFound;
        }
    }

    private void redoComps() {
        computeOwnership();
        if (!components.isEmpty()) {
            Holder<IAspect> machina = machinaHolder();
            if (machina != null) {
                TCHoverButton aspectButton = TCHoverButton.centered(
                        leftPos + 152,
                        topPos + 24,
                        16,
                        new TCButtonIcon.AspectIcon(machina),
                        Component.translatable(
                                "aspect.thaumaturge." + machina.value().tag()),
                        () -> {});
                aspectButton.setDescription(Component.translatable(
                        "aspect.thaumaturge." + machina.value().tag() + ".desc"));
                addRenderableWidget(aspectButton);
            }
            int row = 1;
            int col = 0;
            for (ItemStack stack : components) {
                TCHoverButton componentButton = TCHoverButton.centered(
                        leftPos + 152 + col * 16,
                        topPos + 24 + 16 * row,
                        16,
                        new TCButtonIcon.StackIcon(stack),
                        Component.empty(),
                        () -> {});
                componentButton.setMessage(stack.getHoverName());
                addRenderableWidget(componentButton);
                if (++row > 3) {
                    row = 0;
                    col++;
                }
            }
        }
        for (var widget : children()) {
            if (widget instanceof TCButton button) {
                button.active = !disableAll;
            }
        }
        if (!disableAll && craftButton != null) {
            craftButton.active = allFound;
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        computeOwnership();
    }

    private @Nullable Holder<IAspect> machinaHolder() {
        if (minecraft == null || minecraft.level == null) {
            return null;
        }
        return minecraft
                .level
                .registryAccess()
                .lookupOrThrow(IAspect.REGISTRY_KEY)
                .get(TCAspects.MACHINA)
                .orElse(null);
    }

    private void craft() {
        if (!allFound) {
            return;
        }
        BlockEntityGolemBuilder builder = menu.blockEntity();
        if (builder != null) {
            PacketDistributor.sendToServer(new ServerboundGolemPressPayload(builder.getBlockPos(), props.copy(), true));
            disableAll = true;
        }
    }

    @Override
    protected void renderBackgroundOverlay(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!components.isEmpty()) {
            int row = 1;
            int col = 0;
            for (int i = 0; i < components.size(); i++) {
                if (owns.length > i && !owns[i]) {
                    GuiBlend.blitTinted(
                            graphics,
                            TEXTURE,
                            leftPos + COMPONENT_GRID_X + col * 16,
                            topPos + COMPONENT_GRID_Y + 16 * row,
                            MISSING_U,
                            MISSING_V,
                            16,
                            16,
                            256,
                            256,
                            ARGB32.color(128, 0xFFFFFF));
                }
                if (++row > 3) {
                    row = 0;
                    col++;
                }
            }
        }
        if (menu.cost() > 0) {
            graphics.blit(
                    TEXTURE,
                    leftPos + PROGRESS_X,
                    topPos + PROGRESS_Y,
                    PROGRESS_U,
                    PROGRESS_V,
                    (int) (PROGRESS_WIDTH * (1.0F - (float) menu.cost() / Math.max(1, menu.maxCost()))),
                    PROGRESS_HEIGHT,
                    256,
                    256);
            if (!disableAll) {
                disableAll = true;
                computeOwnership();
            }
        } else if (disableAll) {
            disableAll = false;
            computeOwnership();
        }
        BlockEntityGolemBuilder builder = menu.blockEntity();
        if (builder != null && builder.hasStuff != lastStuff) {
            lastStuff = builder.hasStuff;
            computeOwnership();
        }
        drawCentered(graphics, String.valueOf(hearts), leftPos + STAT_HEARTS_X, topPos + STAT_Y);
        drawCentered(graphics, String.valueOf(armor), leftPos + STAT_ARMOR_X, topPos + STAT_Y);
        drawCentered(graphics, String.valueOf(damage), leftPos + STAT_DAMAGE_X, topPos + STAT_Y);
    }

    private void drawCentered(GuiGraphics graphics, String text, int centerX, int y) {
        graphics.drawString(font, text, centerX - font.width(text) / 2, y, WHITE, true);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (!components.isEmpty()) {
            String costText = String.valueOf(cost);
            graphics.drawString(font, costText, COST_RIGHT_X - font.width(costText), COST_Y, WHITE, true);
        }
        graphics.blit(TEXTURE, 12, 12, SOCKET_U, SOCKET_V, SOCKET_SIZE, SOCKET_SIZE, 256, 256);
        graphics.blit(TEXTURE, 12, 60, SOCKET_U, SOCKET_V, SOCKET_SIZE, SOCKET_SIZE, 256, 256);
        graphics.blit(TEXTURE, 108, 12, SOCKET_U, SOCKET_V, SOCKET_SIZE, SOCKET_SIZE, 256, 256);
        graphics.blit(TEXTURE, 108, 36, SOCKET_U, SOCKET_V, SOCKET_SIZE, SOCKET_SIZE, 256, 256);
        graphics.blit(TEXTURE, 108, 60, SOCKET_U, SOCKET_V, SOCKET_SIZE, SOCKET_SIZE, 256, 256);
    }

    static final class CraftButton extends TCButton {
        private CraftButton(int x, int y, Runnable onPress) {
            super(x, y, CRAFT_WIDTH, CRAFT_HEIGHT, Component.empty(), onPress);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int alpha = isHovered() && active ? 255 : 230;
            GuiBlend.blitTinted(
                    graphics,
                    TEXTURE,
                    getX(),
                    getY(),
                    CRAFT_U,
                    CRAFT_V,
                    CRAFT_WIDTH,
                    CRAFT_HEIGHT,
                    256,
                    256,
                    ARGB32.color(alpha, 0xFFFFFF));
            if (!active) {
                graphics.blit(TEXTURE, getX(), getY(), CRAFT_U, CRAFT_DISABLED_V, CRAFT_WIDTH, CRAFT_HEIGHT, 256, 256);
            }
        }
    }
}
