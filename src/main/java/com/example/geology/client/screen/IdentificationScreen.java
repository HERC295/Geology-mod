package com.example.geology.client.screen;

import com.example.geology.component.GeologyDataComponents;
import com.example.geology.core.IdentificationTestType;
import com.example.geology.core.MineralType;
import com.example.geology.core.TestedTraits;
import com.example.geology.menu.IdentificationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;

public class IdentificationScreen extends AbstractContainerScreen<IdentificationMenu> {

    private static final int TABLE_SLOT_COUNT = 3;
    private static final int BUTTON_WIDTH = 40;
    private static final int BUTTON_HEIGHT = 14;
    private static final int BUTTON_Y = 38;
    private static final int BUTTON_X = 8;
    private static final int BUTTON_SPACING_X = BUTTON_WIDTH + 4;
    private static final int BUTTON_SPACING_Y = BUTTON_HEIGHT + 2;
    private static final int PANEL_X = 96;
    private static final int PANEL_DIVIDER_OFFSET = 4;
    private static final int TRAITS_Y = 18;
    private static final int TRAITS_TITLE_LINE_HEIGHT = 10;
    private static final int TRAITS_LINE_HEIGHT = 9;
    private static final int CANDIDATES_Y = 58;
    private static final int CANDIDATES_TITLE_LINE_HEIGHT = 11;
    private static final int CANDIDATES_LINE_HEIGHT = 9;
    private static final int CANDIDATES_MAX_ROWS = 3;

    public IdentificationScreen(IdentificationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < IdentificationTestType.values().length; i++) {
            IdentificationTestType testType = IdentificationTestType.values()[i];
            int x = leftPos + BUTTON_X + (i % 2) * BUTTON_SPACING_X;
            int y = topPos + BUTTON_Y + (i / 2) * BUTTON_SPACING_Y;
            addRenderableWidget(Button.builder(
                            Component.translatable("msg.geology.identification.test." + testType.name().toLowerCase()),
                            button -> sendTestRequest(testType))
                    .bounds(x, y, BUTTON_WIDTH, BUTTON_HEIGHT)
                    .build());
        }
    }

    private void sendTestRequest(IdentificationTestType testType) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, testType.ordinal());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GeologyScreenUtils.drawBackground(graphics, leftPos, topPos, imageWidth, imageHeight);
        GeologyScreenUtils.drawTitleBar(graphics, leftPos, topPos, imageWidth);
        GeologyScreenUtils.drawHDivider(graphics, leftPos, topPos + GeologyScreenUtils.TOP_DIVIDER_Y, imageWidth);
        GeologyScreenUtils.drawHDivider(graphics, leftPos, topPos + GeologyScreenUtils.INV_DIVIDER_Y, imageWidth);
        int panelX = leftPos + PANEL_X - PANEL_DIVIDER_OFFSET;
        GeologyScreenUtils.drawPanel(graphics, panelX + 1, topPos + GeologyScreenUtils.TITLE_BAR_HEIGHT,
                imageWidth - PANEL_X - 3, GeologyScreenUtils.PANEL_HEIGHT);
        GeologyScreenUtils.drawSlots(graphics, leftPos + 8, topPos + GeologyScreenUtils.TOP_SLOT_Y,
                TABLE_SLOT_COUNT, 1);
        GeologyScreenUtils.drawSlots(graphics, leftPos + 8, topPos + GeologyScreenUtils.PLAYER_INV_Y, 9, 3);
        GeologyScreenUtils.drawSlots(graphics, leftPos + 8, topPos + GeologyScreenUtils.HOTBAR_Y, 9, 1);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTestedTraits(graphics);
        renderCandidates(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    private void renderTestedTraits(GuiGraphics graphics) {
        ItemStack sample = menu.getSlot(0).getItem();
        if (sample.isEmpty()) {
            int x = leftPos + PANEL_X;
            int y = topPos + TRAITS_Y;
            graphics.drawString(font, Component.translatable("msg.geology.identification.hint.sample"),
                    x, y, GeologyScreenUtils.COLOR_TEXT_HINT, true);
            return;
        }
        MineralType mineral = sample.get(GeologyDataComponents.UNKNOWN_MINERAL.get());
        if (mineral == null) {
            int x = leftPos + PANEL_X;
            int y = topPos + TRAITS_Y;
            graphics.drawString(font, Component.translatable("msg.geology.identification.hint.invalid_sample"),
                    x, y, GeologyScreenUtils.COLOR_TEXT_ERROR, true);
            return;
        }

        TestedTraits traits = sample.getOrDefault(
                GeologyDataComponents.TESTED_TRAITS.get(), TestedTraits.empty());
        int x = leftPos + PANEL_X;
        int y = topPos + TRAITS_Y;

        graphics.drawString(font, Component.translatable("msg.geology.identification.tested"),
                x, y, GeologyScreenUtils.COLOR_TEXT_PRIMARY, true);
        y += TRAITS_TITLE_LINE_HEIGHT;

        graphics.drawString(font, Component.translatable("msg.geology.identification.trait.streak",
                formatOptional(traits.streakColor().isPresent(),
                        () -> Component.literal(traits.streakColor().get()))),
                x, y, GeologyScreenUtils.COLOR_TEXT_SECONDARY, true);
        y += TRAITS_LINE_HEIGHT;
        graphics.drawString(font, Component.translatable("msg.geology.identification.trait.hardness",
                formatFloat(traits.hardness())), x, y, GeologyScreenUtils.COLOR_TEXT_SECONDARY, true);
        y += TRAITS_LINE_HEIGHT;
        graphics.drawString(font, Component.translatable("msg.geology.identification.trait.magnetic",
                formatBool(traits.magnetic())), x, y, GeologyScreenUtils.COLOR_TEXT_SECONDARY, true);
        y += TRAITS_LINE_HEIGHT;
        graphics.drawString(font, Component.translatable("msg.geology.identification.trait.acid",
                formatBool(traits.acidReaction())), x, y, GeologyScreenUtils.COLOR_TEXT_SECONDARY, true);
    }

    private void renderCandidates(GuiGraphics graphics) {
        Set<MineralType> candidates = menu.candidates();
        int x = leftPos + PANEL_X;
        int y = topPos + CANDIDATES_Y;

        graphics.drawString(font, Component.translatable("msg.geology.identification.candidates"),
                x, y, GeologyScreenUtils.COLOR_TEXT_PRIMARY, true);
        y += CANDIDATES_TITLE_LINE_HEIGHT;

        if (candidates.isEmpty()) {
            graphics.drawString(font, Component.translatable("msg.geology.identification.no_sample"),
                    x, y, GeologyScreenUtils.COLOR_TEXT_CANDIDATE, true);
            return;
        }

        int rowCount = 0;
        for (MineralType candidate : candidates) {
            if (rowCount >= CANDIDATES_MAX_ROWS) {
                break;
            }
            graphics.drawString(font,
                    Component.translatable(candidate.translationKey()),
                    x, y, GeologyScreenUtils.COLOR_TEXT_TRAIT, true);
            y += CANDIDATES_LINE_HEIGHT;
            rowCount++;
        }
    }

    private static Component formatOptional(boolean tested, Supplier<Component> value) {
        return tested ? value.get()
                : Component.translatable("msg.geology.magnifier.trait.untested");
    }

    private static Component formatFloat(Optional<Float> value) {
        return value.isPresent()
                ? Component.literal(String.format("%.1f", value.get()))
                : Component.translatable("msg.geology.magnifier.trait.untested");
    }

    private static Component formatBool(Optional<Boolean> value) {
        if (value.isEmpty()) {
            return Component.translatable("msg.geology.magnifier.trait.untested");
        }
        return value.get()
                ? Component.translatable("msg.geology.magnifier.trait.yes")
                : Component.translatable("msg.geology.magnifier.trait.no");
    }
}
