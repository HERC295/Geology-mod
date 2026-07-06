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
 *
 * <p><b>文本颜色</b>：
 * <ul>
 *   <li>{@link #COLOR_TEXT_PRIMARY}：标题等主文本色（白色）；</li>
 *   <li>{@link #COLOR_TEXT_SECONDARY}：状态面板等次文本色（浅灰）；</li>
 *   <li>{@link #COLOR_TEXT_HINT}：操作提示色（暗黄）；</li>
 *   <li>{@link #COLOR_TEXT_ERROR}：错误提示色（红）；</li>
 *   <li>{@link #COLOR_TEXT_TRAIT}：属性值色（亮黄）；</li>
 *   <li>{@link #COLOR_TEXT_CANDIDATE}：候选矿物名色（浅灰）。</li>
 * </ul>
 */
public final class GeologyScreenUtils {

    // ==================== 颜色方案（ARGB）====================
    /** 背景渐变顶部色（深灰偏亮）。 */
    public static final int COLOR_BG_TOP = 0xFF3C3C3C;
    /** 背景渐变底部色（深灰偏暗）。 */
    public static final int COLOR_BG_BOTTOM = 0xFF1F1F1F;
    /** 外边框色（近黑）。 */
    public static final int COLOR_BORDER_OUTER = 0xFF0A0A0A;
    /** 内高光边框色（顶部/左侧 1px 高光）。 */
    public static final int COLOR_BORDER_HIGHLIGHT = 0xFF4A4A4A;

    /** 槽位上左暗边色（凹陷阴影）。 */
    public static final int COLOR_SLOT_DARK = 0xFF373737;
    /** 槽位下右亮边色（凹陷高光）。 */
    public static final int COLOR_SLOT_LIGHT = 0xFFFFFFFF;
    /** 槽位内底色（中灰）。 */
    public static final int COLOR_SLOT_INNER = 0xFF8B8B8B;

    /** 半透明面板背景色。 */
    public static final int COLOR_PANEL_BG = 0x80202020;
    /** 面板标题栏背景色。 */
    public static final int COLOR_PANEL_HEADER = 0xFF404040;

    /** 分隔线色（灰）。 */
    public static final int COLOR_LINE = 0xFF555555;

    // ==================== 旧常量（保留兼容）====================
    /** @deprecated 改用 {@link #COLOR_BG_TOP}/{@link #COLOR_BG_BOTTOM} 渐变。 */
    @Deprecated
    public static final int COLOR_BG = 0xFF2B2B2B;
    /** @deprecated 改用 {@link #drawSlot} 凹陷风格。 */
    @Deprecated
    public static final int COLOR_SLOT = 0xFF8B8B8B;

    // ==================== 文本颜色 ====================
    /** 主文本色（白）。 */
    public static final int COLOR_TEXT_PRIMARY = 0xFFFFFF;
    /** 次文本色（浅灰）。 */
    public static final int COLOR_TEXT_SECONDARY = 0xCCCCCC;
    /** 操作提示色（暗黄）。 */
    public static final int COLOR_TEXT_HINT = 0xAAAA00;
    /** 错误提示色（红）。 */
    public static final int COLOR_TEXT_ERROR = 0xFF5555;
    /** 属性值色（亮黄）。 */
    public static final int COLOR_TEXT_TRAIT = 0xFFFF88;
    /** 候选矿物名色（中灰）。 */
    public static final int COLOR_TEXT_CANDIDATE = 0xAAAAAA;

    // ==================== 布局常量（基于 imageHeight=166）====================
    /** 单个槽位尺寸（16×16）。 */
    public static final int SLOT_SIZE = 16;
    /** 槽位含 1px 边框的总尺寸（18×18）。 */
    public static final int SLOT_BOX = 18;
    /** 槽位边框宽度（1px）。 */
    public static final int SLOT_BORDER = 1;
    /** 背景内边距（左右各 7px）。 */
    public static final int BG_PADDING_X = 7;
    /** 顶行槽位 Y 坐标。 */
    public static final int TOP_SLOT_Y = 18;
    /** 顶行分隔线 Y 坐标（顶行槽位下方）。 */
    public static final int TOP_DIVIDER_Y = 36;
    /** 玩家背包分隔线 Y 坐标。 */
    public static final int INV_DIVIDER_Y = 82;
    /** 玩家背包 Y 坐标起始。 */
    public static final int PLAYER_INV_Y = 84;
    /** 快捷栏 Y 坐标（背包下方 58px）。 */
    public static final int HOTBAR_Y = 142;
    /** 面板高度上限（顶行槽位到玩家背包分隔线之间）。 */
    public static final int PANEL_HEIGHT = 64;
    /** 标题栏高度（含底部分隔线 1px）。 */
    public static final int TITLE_BAR_HEIGHT = 11;

    private GeologyScreenUtils() {
        // 工具类，禁止实例化
    }

    /**
     * 绘制 GUI 主背景：渐变填充 + 双层边框（外暗边 + 内高光）。
     *
     * @param graphics 绘图上下文
     * @param x        左上角 X
     * @param y        左上角 Y
     * @param width    宽
     * @param height   高
     */
    public static void drawBackground(GuiGraphics graphics, int x, int y, int width, int height) {
        // 外暗边框（1px）
        graphics.fill(x, y, x + width, y + height, COLOR_BORDER_OUTER);
        // 内高光边框（1px，顶部+左侧）
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, COLOR_BORDER_HIGHLIGHT);
        graphics.fill(x + 1, y + 1, x + 2, y + height - 1, COLOR_BORDER_HIGHLIGHT);
        // 渐变填充背景
        drawVerticalGradient(graphics, x + 2, y + 2, width - 4, height - 4, COLOR_BG_TOP, COLOR_BG_BOTTOM);
    }

    /**
     * 绘制竖直渐变矩形（从上到下）。
     *
     * @param graphics   绘图上下文
     * @param x          左上角 X
     * @param y          左上角 Y
     * @param width      宽
     * @param height     高
     * @param colorTop   顶部色
     * @param colorBottom 底部色
     */
    public static void drawVerticalGradient(GuiGraphics graphics, int x, int y, int width, int height,
                                            int colorTop, int colorBottom) {
        // 分段填充模拟渐变（每 2px 一段，性能与效果平衡）
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

    /** 插值 ARGB 通道（0-255）。 */
    private static int interpolateChannel(int c1, int c2, int t, int shift) {
        int v1 = (c1 >> shift) & 0xFF;
        int v2 = (c2 >> shift) & 0xFF;
        return v1 + (v2 - v1) * t / 255;
    }

    /** 插值 Alpha 通道。 */
    private static int interpolateAlpha(int c1, int c2, int t) {
        return interpolateChannel(c1, c2, t, 24);
    }

    /**
     * 绘制单个 vanilla 风格凹陷槽位（18×18）。
     *
     * <p>凹陷效果：上左暗边 + 下右亮边 + 中灰内底，模拟 vanilla 容器槽位。
     *
     * @param graphics 绘图上下文
     * @param x        槽位左上角 X（含边框）
     * @param y        槽位左上角 Y（含边框）
     */
    public static void drawSlot(GuiGraphics graphics, int x, int y) {
        // 内底
        graphics.fill(x, y, x + SLOT_BOX, y + SLOT_BOX, COLOR_SLOT_INNER);
        // 上左暗边（凹陷阴影）
        graphics.fill(x, y, x + SLOT_BOX, y + 1, COLOR_SLOT_DARK);
        graphics.fill(x, y, x + 1, y + SLOT_BOX, COLOR_SLOT_DARK);
        // 下右亮边（凹陷高光）
        graphics.fill(x, y + SLOT_BOX - 1, x + SLOT_BOX, y + SLOT_BOX, COLOR_SLOT_LIGHT);
        graphics.fill(x + SLOT_BOX - 1, y, x + SLOT_BOX, y + SLOT_BOX, COLOR_SLOT_LIGHT);
    }

    /**
     * 绘制一组槽位框（rows×cols 个 18×18 凹陷方块）。
     *
     * @param graphics 绘图上下文
     * @param startX   起始 X（含 1px 边框）
     * @param startY   起始 Y（含 1px 边框）
     * @param cols     列数
     * @param rows     行数
     */
    public static void drawSlots(GuiGraphics graphics, int startX, int startY, int cols, int rows) {
        for (int row = 0; row < rows; row++) {
            for (int col = 0; col < cols; col++) {
                int x = startX + col * SLOT_BOX - SLOT_BORDER;
                int y = startY + row * SLOT_BOX - SLOT_BORDER;
                drawSlot(graphics, x, y);
            }
        }
    }

    /**
     * 绘制带标题栏的半透明面板背景。
     *
     * @param graphics 绘图上下文
     * @param x        左上角 X
     * @param y        左上角 Y
     * @param width    宽
     * @param height   高
     */
    public static void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        // 半透明背景
        graphics.fill(x, y, x + width, y + height, COLOR_PANEL_BG);
        // 1px 边框
        graphics.fill(x, y, x + width, y + 1, COLOR_LINE);
        graphics.fill(x, y + height - 1, x + width, y + height, COLOR_LINE);
        graphics.fill(x, y, x + 1, y + height, COLOR_LINE);
        graphics.fill(x + width - 1, y, x + width, y + height, COLOR_LINE);
    }

    /**
     * 绘制标题栏（顶部色条）。
     *
     * @param graphics 绘图上下文
     * @param x        左上角 X
     * @param y        左上角 Y
     * @param width    宽
     */
    public static void drawTitleBar(GuiGraphics graphics, int x, int y, int width) {
        graphics.fill(x, y, x + width, y + 10, COLOR_PANEL_HEADER);
        graphics.fill(x, y + 10, x + width, y + 11, COLOR_LINE);
    }

    /**
     * 绘制水平分隔线（左右各留 {@value #BG_PADDING_X}px 边距）。
     *
     * @param graphics 绘图上下文
     * @param leftPos  GUI 左上角 X
     * @param y        分隔线 Y（顶部）
     * @param width    GUI 总宽
     */
    public static void drawHDivider(GuiGraphics graphics, int leftPos, int y, int width) {
        graphics.fill(leftPos + BG_PADDING_X, y, leftPos + width - BG_PADDING_X, y + 1, COLOR_LINE);
    }

    /**
     * 绘制竖直分隔线。
     *
     * @param graphics 绘图上下文
     * @param x        分隔线 X（左侧）
     * @param topY     顶部 Y
     * @param bottomY  底部 Y
     */
    public static void drawVDivider(GuiGraphics graphics, int x, int topY, int bottomY) {
        graphics.fill(x, topY, x + 1, bottomY, COLOR_LINE);
    }
}
