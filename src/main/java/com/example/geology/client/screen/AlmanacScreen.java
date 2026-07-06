package com.example.geology.client.screen;

import com.example.geology.core.MineralAppearance;
import com.example.geology.core.MineralType;
import com.example.geology.menu.AlmanacMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

public class AlmanacScreen extends AbstractContainerScreen<AlmanacMenu> {

    private static final int COLUMNS = 3;
    private static final int ROWS = 8;
    private static final int CELL_WIDTH = 80;
    private static final int CELL_HEIGHT = 18;
    private static final int GRID_X = 8;
    private static final int GRID_Y = 18;

    public AlmanacScreen(AlmanacMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GRID_X + COLUMNS * CELL_WIDTH + GeologyScreenUtils.BG_PADDING_X;
        this.imageHeight = GRID_Y + ROWS * CELL_HEIGHT + GeologyScreenUtils.BG_PADDING_X;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GeologyScreenUtils.drawBackground(graphics, leftPos, topPos, imageWidth, imageHeight);
        GeologyScreenUtils.drawTitleBar(graphics, leftPos, topPos, imageWidth);

        for (int row = 0; row <= ROWS; row++) {
            int y = topPos + GRID_Y + row * CELL_HEIGHT;
            graphics.fill(leftPos + GeologyScreenUtils.BG_PADDING_X, y,
                    leftPos + imageWidth - GeologyScreenUtils.BG_PADDING_X, y + 1,
                    0x40555555);
        }

        for (int col = 0; col <= COLUMNS; col++) {
            int x = leftPos + GRID_X + col * CELL_WIDTH;
            graphics.fill(x, topPos + GeologyScreenUtils.TITLE_BAR_HEIGHT,
                    x + 1, topPos + GRID_Y + ROWS * CELL_HEIGHT,
                    0x40555555);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, Component.translatable("msg.geology.almanac.title"),
                titleLabelX, titleLabelY, GeologyScreenUtils.COLOR_TEXT_PRIMARY, true);

        MineralType[] minerals = MineralType.values();
        int idx = 0;
        for (int row = 0; row < ROWS && idx < minerals.length; row++) {
            for (int col = 0; col < COLUMNS && idx < minerals.length; col++) {
                MineralType mineral = minerals[idx];
                int x = GRID_X + col * CELL_WIDTH;
                int y = GRID_Y + row * CELL_HEIGHT;
                renderEntry(graphics, mineral, x, y);
                idx++;
            }
        }
    }

    private void renderEntry(GuiGraphics graphics, MineralType mineral, int x, int y) {
        boolean identified = menu.isIdentified(mineral);
        MineralAppearance appearance = mineral.appearance();

        if (identified) {
            graphics.drawString(font, Component.translatable(mineral.translationKey()),
                    x, y, GeologyScreenUtils.COLOR_TEXT_TRAIT, true);
            graphics.drawString(font, Component.translatable("msg.geology.almanac.appearance",
                            Component.translatable(appearance.translationKey())),
                    x, y + 8, GeologyScreenUtils.COLOR_TEXT_CANDIDATE, true);
        } else {
            graphics.drawString(font, Component.literal("???"),
                    x, y, 0x888888, true);
            graphics.drawString(font, Component.translatable("msg.geology.almanac.appearance",
                            Component.translatable(appearance.translationKey())),
                    x, y + 8, 0x666666, true);
        }
    }
}
