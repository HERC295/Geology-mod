package com.example.geology.client.screen;

import net.minecraft.client.gui.GuiGraphics;

/**
 * GUI 公共绘制工具（Screen 共用常量与方法）。
 *
 * <p>抽取 {@link IdentificationScreen} 与 {@link CoreRigScreen} 共享的颜色方案与槽位绘制逻辑，
 * 保证两个 GUI 视觉一致，避免常量与逻辑在两处重复定义。
 *
 * <p><b>视觉风格</b>：仿 vanilla 深色主题，渐变背景 + 凹陷槽位 + 半透明面板，
 * 替代早期纯色填充的简陋外观。
 *
 * <p><b>颜色方案</b>（ARGB）：
 * <ul>
 *   <li>背景渐变：{@link #COLOR_BG_TOP} → {@link #COLOR_BG_BOTTOM}；</li>
 *   <li>外边框/内高光：{@link #COLOR_BORDER_OUTER} / {@link #COLOR_BORDER_HIGHLIGHT}；</li>
 *   <li>槽位凹陷：{@link #COLOR_SLOT_DARK}（上左暗边）/ {@link #COLOR_SLOT_LIGHT}（下右亮边）/ {@link #COLOR_SLOT_INNER}（内底）；</li>
 *   <li>面板背景：{@link #COLOR_PANEL_BG} / {@link #COLOR_PANEL_HEADER}；</li>
 *   <li>分隔线：{@link #COLOR_LINE}。</li>
 * </ul>
 */
public final class GeologyScreenUtils {

    // ==================== 颜色方案（ARGB）====================
    public static final int COLOR_BG_TOP = 0xFF3C3C3C;
    public static final int COLOR_BG_BOTTOM = 0xFF1F1F1F;
    public static final int COLOR_BORDER_OUTER = 0xFF0A0A0A;
    public static final int COLOR_BORDER_HIGHLIGHT = 0xFF4A4A4A;
    public static final int COLOR_SLOT_DARK = 0xFF373737;
    public static final int COLOR_SLOT_LIGHT = 0xFFFFFFFF;
    public static final int COLOR_SLOT_INNER = 0xFF8B8B8B;
    public static final int COLOR_PANEL_BG = 0x80202020;
    public static final int COLOR_PANEL_HEADER = 0xFF404040;
    public static final int COLOR_LINE = 0xFF555555;

    @Deprecated
    public static final int COLOR_BG = 0xFF2B2B2B;
    @Deprecated
    public static final int COLOR_SLOT = 0xFF8B8B8B;

    public static final int COLOR_TEXT_PRIMARY = 0xFFFFFF;
    public static final int COLOR_TEXT_SECONDARY = 0xCCCCCC;
    public static final int COLOR_TEXT_HINT = 0xAAAA00;
    public static final int COLOR_TEXT_ERROR = 0xFF5555;
    public static final int COLOR_TEXT_TRAIT = 0xFFFF88;
    public static final int COLOR_TEXT_CANDIDATE = 0xAAAAAA;

    // ==================== 布局常量（基于 imageHeight=166）====================
    public static final int SLOT_SIZE = 16;
    public static final int SLOT_BOX = 18;
    public static final int SLOT_BORDER = 1;
    public static final int BG_PADDING_X = 7;
    public static final int TOP_SLOT_Y = 18;
    public static final int TOP_DIVIDER_Y = 36;
    public static final int INV_DIVIDER_Y = 82;
    public static final int PLAYER_INV_Y = 84;
    public static final int HOTBAR_Y = 142;
    public static final int PANEL_HEIGHT = 64;
    public static final int TITLE_BAR_HEIGHT = 11;

    private GeologyScreenUtils() {
    }

    public static void drawBackground(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, COLOR_BORDER_OUTER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, COLOR_BORDER_HIGHLIGHT);
        graphics.fill(x + 1, y + 1, x + 2, y + height - 1, COLOR_BORDER_HIGHLIGHT);
        drawVerticalGradient(graphics, x + 2, y + 2, width - 4, height - 4, COLOR_BG_TOP, COLOR_BG_BOTTOM);
    }

    public static void drawVerticalGradient(GuiGraphics graphics, int x, int y, int width, int height,
                                            int colorTop, int colorBottom) {
        for (int i = 0; i < height; i += 2) {
            int t = height > 1 ? i * 255 / (height - 1) : 0;
            int a = interpolateAlpha(colorTop, colorBottom, t);
            int r = interpolateChannel(colorTop, colorBottom, t, 16);
            int g = interpolateChannel(colorTop, colorBottom, t, 8);
            int b = interpolateChannel(colorTop, colorBottom, t, 0);
            int color = (a << 24) | (r << 16) | (g << 8) | b;
            graphics.fill(x, y + i, x + width, y + i + 2, color);
        }
    }

    private static int interpolateChannel(int c1, int c2, int t, int shift) {
        int v1 = (c1 >> shift) & 0xFF;
        int v2 = (c2 >> shift) & 0xFF;
        return v1 + (v2 - v1) * t / 255;
    }

    private static int interpolateAlpha(int c1, int c2, int t) {
        return interpolateChannel(c1, c2, t, 24);
    }

    public static void drawSlot(GuiGraphics graphics, int x, int y) {
        graphics.fill(x, y, x + SLOT_BOX, y + SLOT_BOX, COLOR_SLOT_INNER);
        graphics.fill(x, y, x + SLOT_BOX, y + 1, COLOR_SLOT_DARK);
        graphics.fill(x, y, x + 1, y + SLOT_BOX, COLOR_SLOT_DARK);
        graphics.fill(x, y + SLOT_BOX - 1, x + SLOT_BOX, y + SLOT_BOX, COLOR_SLOT_LIGHT);
        graphics.fill(x + SLOT_BOX - 1, y, x + SLOT_BOX, y + SLOT_BOX, COLOR_SLOT_LIGHT);
    }

    public static void drawSlots(GuiGraphics graphics, int startX, int startY, int cols, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = startX + col * SLOT_BOX - SLOT_BORDER;
                int y = startY + row * SLOT_BOX - SLOT_BORDER;
                drawSlot(graphics, x, y);
            }
        }
    }

    public static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, COLOR_PANEL_BG);
        graphics.fill(x, y, x + width, y + 1, COLOR_LINE);
        graphics.fill(x, y + height - 1, x + width, y + height, COLOR_LINE);
        graphics.fill(x, y, x + 1, y + height, COLOR_LINE);
        graphics.fill(x + width - 1, y, x + width, y + height, COLOR_LINE);
    }

    public static void drawTitleBar(GuiGraphics graphics, int x, int y, int width) {
        graphics.fill(x, y, x + width, y + 10, COLOR_PANEL_HEADER);
        graphics.fill(x, y + 10, x + width, y + 11, COLOR_LINE);
    }

    public static void drawHDivider(GuiGraphics graphics, int leftPos, int y, int width) {
        graphics.fill(leftPos + BG_PADDING_X, y, leftPos + width - BG_PADDING_X, y + 1, COLOR_LINE);
    }

    public static void drawVDivider(GuiGraphics graphics, int x, int topY, int bottomY) {
        graphics.fill(x, topY, x + 1, bottomY, COLOR_LINE);
    }
}
