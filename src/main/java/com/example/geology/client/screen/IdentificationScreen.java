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

/**
 * 鉴定台 GUI 客户端渲染（设计文档 5.2）。
 *
 * <p><b>布局</b>（imageWidth=176, imageHeight=166）：
 * <ul>
 *   <li>标题栏 y=0~10：色条 + 分隔线；</li>
 *   <li>顶行 y=18：3 个鉴定台槽位（样本/试剂/参照物），x=8~62；</li>
 *   <li>y=38~70：4 个测试按钮（2×2 网格，40×14）+ 右侧信息面板；</li>
 *   <li>y=84 起：玩家背包（3×9）+ 快捷栏（1×9）。</li>
 * </ul>
 *
 * <p><b>测试交互</b>：点击测试按钮触发
 * {@link net.minecraft.client.multiplayer.ClientPacketListener#handleInventoryButtonClick}，
 * 服务端 {@link IdentificationMenu#clickMenuButton} 执行测试并同步状态。
 */
public class IdentificationScreen extends AbstractContainerScreen<IdentificationMenu> {

    /** 鉴定台槽位数量（样本/试剂/参照物）。 */
    private static final int TABLE_SLOT_COUNT = 3;
    /** 测试按钮宽度。 */
    private static final int BUTTON_WIDTH = 40;
    /** 测试按钮高度。 */
    private static final int BUTTON_HEIGHT = 14;
    /** 测试按钮起始 Y 坐标。 */
    private static final int BUTTON_Y = 38;
    /** 测试按钮起始 X 坐标。 */
    private static final int BUTTON_X = 8;
    /** 测试按钮列间距。 */
    private static final int BUTTON_SPACING_X = BUTTON_WIDTH + 4;
    /** 测试按钮行间距。 */
    private static final int BUTTON_SPACING_Y = BUTTON_HEIGHT + 2;
    /** 右侧信息面板 X 坐标。 */
    private static final int PANEL_X = 96;
    /** 信息面板左侧分隔线 X 偏移。 */
    private static final int PANEL_DIVIDER_OFFSET = 4;
    /** 已测试属性面板 Y 坐标（标题栏下方 2px）。 */
    private static final int TRAITS_Y = 13;
    /** 已测试属性标题行高。 */
    private static final int TRAITS_TITLE_LINE_HEIGHT = 10;
    /** 已测试属性普通行高。 */
    private static final int TRAITS_LINE_HEIGHT = 9;
    /** 候选矿物列表 Y 坐标（已测试属性下方 2px 间隙）。 */
    private static final int CANDIDATES_Y = 54;
    /** 候选矿物列表标题行高。 */
    private static final int CANDIDATES_TITLE_LINE_HEIGHT = 10;
    /** 候选矿物列表普通行高。 */
    private static final int CANDIDATES_LINE_HEIGHT = 9;
    /** 候选矿物列表最大显示行数。 */
    private static final int CANDIDATES_MAX_ROWS = 3;

    public IdentificationScreen(IdentificationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
        this.inventoryLabelY = 72;
        this.titleLabelX = 8;
        this.titleLabelY = 2;
    }

    @Override
    protected void init() {
        super.init();
        // 4 个测试按钮排成 2×2 网格
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

    /** 触发服务端测试（vanilla InventoryButtonClick 通道）。 */
    private void sendTestRequest(IdentificationTestType testType) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, testType.ordinal());
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // 整体背景：渐变 + 双层边框
        GeologyScreenUtils.drawBackground(graphics, leftPos, topPos, imageWidth, imageHeight);

        // 标题栏
        GeologyScreenUtils.drawTitleBar(graphics, leftPos, topPos, imageWidth);

        // 顶行槽位下方分隔线
        GeologyScreenUtils.drawHDivider(graphics, leftPos, topPos + GeologyScreenUtils.TOP_DIVIDER_Y, imageWidth);
        // 玩家背包上方分隔线
        GeologyScreenUtils.drawHDivider(graphics, leftPos, topPos + GeologyScreenUtils.INV_DIVIDER_Y, imageWidth);

        // 右侧信息面板背景（半透明）
        int panelX = leftPos + PANEL_X - PANEL_DIVIDER_OFFSET;
        GeologyScreenUtils.drawPanel(graphics, panelX + 1, topPos + GeologyScreenUtils.TITLE_BAR_HEIGHT,
                imageWidth - PANEL_X - 3, GeologyScreenUtils.PANEL_HEIGHT);

        // 绘制所有槽位框（凹陷风格）
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

    /** 渲染已测试属性摘要（条痕/硬度/磁性/酸反应）。 */
    private void renderTestedTraits(GuiGraphics graphics) {
        ItemStack sample = menu.getSlot(0).getItem();
        if (sample.isEmpty()) {
            // 样本槽为空时显示提示
            int x = leftPos + PANEL_X;
            int y = topPos + TRAITS_Y;
            graphics.drawString(font, Component.translatable("msg.geology.identification.hint.sample"),
                    x, y, GeologyScreenUtils.COLOR_TEXT_HINT, true);
            return;
        }
        MineralType mineral = sample.get(GeologyDataComponents.UNKNOWN_MINERAL.get());
        if (mineral == null) {
            // 放入的不是未鉴定矿物样本
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

    /** 渲染候选矿物列表（实时反映已测试属性的过滤结果）。 */
    private void renderCandidates(GuiGraphics graphics) {
        Set<MineralType> candidates = menu.candidates();
        if (candidates.isEmpty()) {
            return;
        }
        int x = leftPos + PANEL_X;
        int y = topPos + CANDIDATES_Y;

        graphics.drawString(font, Component.translatable("msg.geology.identification.candidates"),
                x, y, GeologyScreenUtils.COLOR_TEXT_PRIMARY, true);
        y += CANDIDATES_TITLE_LINE_HEIGHT;

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
