package com.example.geology.client.screen;

import com.example.geology.core.MineralAppearance;
import com.example.geology.core.MineralType;
import com.example.geology.menu.AlmanacMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 地质图鉴 GUI 客户端渲染（设计文档 4.5）。
 *
 * <p><b>布局</b>（imageWidth=248, imageHeight=184）：
 * <ul>
 *   <li>标题栏 y=0~10：色条 + 分隔线；</li>
 *   <li>3 列 × 8 行网格 y=18~162：每格 80×18，显示矿物名/外观；</li>
 *   <li>已鉴定：矿物名（亮黄）+ 外观（中灰）；</li>
 *   <li>未鉴定：???（暗灰）+ 外观（更深灰）。</li>
 * </ul>
 *
 * <p>数据来源：{@link AlmanacMenu#isIdentified(MineralType)}（通过 ContainerData 同步）。
 */
public class AlmanacScreen extends AbstractContainerScreen<AlmanacMenu> {

    /** 网格列数。 */
    private static final int COLUMNS = 3;
    /** 网格行数。 */
    private static final int ROWS = 8;
    /** 每列宽度。 */
    private static final int CELL_WIDTH = 80;
    /** 每行高度。 */
    private static final int CELL_HEIGHT = 18;
    /** 网格左侧 X 偏移。 */
    private static final int GRID_X = 8;
    /** 网格顶部 Y 偏移。 */
    private static final int GRID_Y = 18;

    public AlmanacScreen(AlmanacMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = GRID_X + COLUMNS * CELL_WIDTH + GeologyScreenUtils.BG_PADDING_X;  // 8 + 240 + 7 = 255
        this.imageHeight = GRID_Y + ROWS * CELL_HEIGHT + GeologyScreenUtils.BG_PADDING_X;   // 18 + 144 + 7 = 169
        this.titleLabelX = 8;
        this.titleLabelY = 2;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // 整体背景：渐变 + 双层边框
        GeologyScreenUtils.drawBackground(graphics, leftPos, topPos, imageWidth, imageHeight);

        // 标题栏
        GeologyScreenUtils.drawTitleBar(graphics, leftPos, topPos, imageWidth);

        // 网格行分隔线（每行之间画淡线，增强可读性）
        for (int row = 0; row <= ROWS; row++) {
            int y = topPos + GRID_Y + row * CELL_HEIGHT;
            graphics.fill(leftPos + GeologyScreenUtils.BG_PADDING_X, y,
                    leftPos + imageWidth - GeologyScreenUtils.BG_PADDING_X, y + 1,
                    0x40555555);
        }

        // 网格列分隔线
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
        // 标题
        graphics.drawString(font, Component.translatable("msg.geology.almanac.title"),
                titleLabelX, titleLabelY, GeologyScreenUtils.COLOR_TEXT_PRIMARY, true);

        // 矿物条目网格
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

    /**
     * 渲染单条图鉴条目。
     *
     * <p>已鉴定：显示矿物名（亮黄）+ 外观原型（中灰）。
     * <p>未鉴定：显示 ???（暗灰）+ 外观原型（更深灰）—— 外观可见，激发收集欲。
     */
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
