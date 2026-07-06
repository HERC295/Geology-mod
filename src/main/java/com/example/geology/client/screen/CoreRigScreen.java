package com.example.geology.client.screen;

import com.example.geology.block.CoreRigBlockEntity;
import com.example.geology.item.DrillHeadItem;
import com.example.geology.menu.CoreRigMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;

/**
 * 岩心钻机 GUI 客户端渲染（设计文档 8.1）。
 *
 * <p><b>布局</b>（imageWidth=176, imageHeight=166）：
 * <ul>
 *   <li>标题栏 y=0~10：色条 + 分隔线；</li>
 *   <li>顶行 y=18：4 个钻机槽位（钻头/钻井液/燃料/输出），x=8~80；</li>
 *   <li>y=38~56：下钻按钮 + 右侧状态面板；</li>
 *   <li>y=84 起：玩家背包（3×9）+ 快捷栏（1×9）。</li>
 * </ul>
 *
 * <p><b>下钻交互</b>：点击"开始下钻"按钮触发
 * {@code gameMode.handleInventoryButtonClick}，服务端 {@link CoreRigMenu#clickMenuButton}
 * 执行下钻并同步状态。
 */
public class CoreRigScreen extends AbstractContainerScreen<CoreRigMenu> {

    /** 下钻按钮 X 坐标。 */
    private static final int BUTTON_X = 8;
    /** 下钻按钮 Y 坐标。 */
    private static final int BUTTON_Y = 40;
    /** 下钻按钮宽度。 */
    private static final int BUTTON_WIDTH = 70;
    /** 下钻按钮高度。 */
    private static final int BUTTON_HEIGHT = 18;
    /** 状态面板 X 坐标（右侧，与按钮分离）。 */
    private static final int STATUS_X = 82;
    /** 状态面板 Y 坐标（标题栏下方 2px）。 */
    private static final int STATUS_Y = 13;
    /** 状态面板标题行高。 */
    private static final int STATUS_LINE_HEIGHT_TITLE = 10;
    /** 状态面板普通行高。 */
    private static final int STATUS_LINE_HEIGHT = 9;
    /** 客户端预估磨损上限（4/m，最硬岩性石英岩的磨损值，保守估算）。 */
    private static final int MAX_WEAR_PER_METER = 4;

    public CoreRigScreen(CoreRigMenu menu, Inventory playerInventory, Component title) {
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
        addRenderableWidget(Button.builder(
                        Component.translatable("msg.geology.core_rig.drill_button"),
                        button -> sendDrillRequest())
                .bounds(leftPos + BUTTON_X, topPos + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    /** 触发服务端下钻（vanilla InventoryButtonClick 通道）。 */
    private void sendDrillRequest() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, CoreRigMenu.BUTTON_DRILL);
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

        // 右侧状态面板背景（半透明）
        int panelX = leftPos + STATUS_X - 2;
        GeologyScreenUtils.drawPanel(graphics, panelX + 1, topPos + GeologyScreenUtils.TITLE_BAR_HEIGHT,
                imageWidth - STATUS_X - 1, GeologyScreenUtils.PANEL_HEIGHT);

        // 绘制所有槽位框（凹陷风格）
        GeologyScreenUtils.drawSlots(graphics, leftPos + 8, topPos + GeologyScreenUtils.TOP_SLOT_Y, 4, 1);
        GeologyScreenUtils.drawSlots(graphics, leftPos + 8, topPos + GeologyScreenUtils.PLAYER_INV_Y, 9, 3);
        GeologyScreenUtils.drawSlots(graphics, leftPos + 8, topPos + GeologyScreenUtils.HOTBAR_Y, 9, 1);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderStatusPanel(graphics);
        renderTooltip(graphics, mouseX, mouseY);
    }

    /** 渲染状态面板（钻头耐久/钻井液需求/燃料状态/预估磨损）。 */
    private void renderStatusPanel(GuiGraphics graphics) {
        ItemStack drillHead = menu.rigContainer().getItem(CoreRigBlockEntity.SLOT_DRILL_HEAD);
        ItemStack drillingFluid = menu.rigContainer().getItem(CoreRigBlockEntity.SLOT_DRILLING_FLUID);
        ItemStack fuel = menu.rigContainer().getItem(CoreRigBlockEntity.SLOT_FUEL);
        ItemStack output = menu.rigContainer().getItem(CoreRigBlockEntity.SLOT_OUTPUT);

        int x = leftPos + STATUS_X;
        int y = topPos + STATUS_Y;

        // 标题
        graphics.drawString(font, Component.translatable("msg.geology.core_rig.status"),
                x, y, GeologyScreenUtils.COLOR_TEXT_PRIMARY, true);
        y += STATUS_LINE_HEIGHT_TITLE;

        // 钻头耐久
        ChatFormatting drillColor = ChatFormatting.GRAY;
        Component drillInfo = Component.translatable("msg.geology.core_rig.value_none");
        if (drillHead.getItem() instanceof DrillHeadItem drillItem) {
            int remaining = drillHead.getMaxDamage() - drillHead.getDamageValue();
            drillColor = remaining > 0 ? ChatFormatting.GREEN : ChatFormatting.RED;
            drillInfo = Component.literal(remaining + "/" + drillItem.tier().maxDurability());
        }
        graphics.drawString(font, Component.translatable("msg.geology.core_rig.drill_durability",
                        drillInfo.copy().withStyle(drillColor)), x, y, GeologyScreenUtils.COLOR_TEXT_SECONDARY, true);
        y += STATUS_LINE_HEIGHT;

        // 钻井液需求
        int fluidNeeded = (CoreRigBlockEntity.DRILL_DEPTH + CoreRigBlockEntity.FLUID_PER_METERS - 1)
                / CoreRigBlockEntity.FLUID_PER_METERS;
        int fluidHave = drillingFluid.getCount();
        ChatFormatting fluidColor = fluidHave >= fluidNeeded ? ChatFormatting.GREEN : ChatFormatting.RED;
        graphics.drawString(font, Component.translatable("msg.geology.core_rig.fluid",
                        Component.literal(fluidHave + "/" + fluidNeeded).withStyle(fluidColor)),
                x, y, GeologyScreenUtils.COLOR_TEXT_SECONDARY, true);
        y += STATUS_LINE_HEIGHT;

        // 燃料状态
        ChatFormatting fuelColor = (!fuel.isEmpty() && fuel.getBurnTime(RecipeType.SMELTING) > 0)
                ? ChatFormatting.GREEN : ChatFormatting.RED;
        Component fuelInfo = Component.translatable(fuel.isEmpty()
                ? "msg.geology.core_rig.fuel_empty" : "msg.geology.core_rig.fuel_full");
        graphics.drawString(font, Component.translatable("msg.geology.core_rig.fuel",
                        fuelInfo.copy().withStyle(fuelColor)),
                x, y, GeologyScreenUtils.COLOR_TEXT_SECONDARY, true);
        y += STATUS_LINE_HEIGHT;

        // 输出槽状态
        ChatFormatting outputColor = output.isEmpty() ? ChatFormatting.GREEN : ChatFormatting.YELLOW;
        Component outputInfo = Component.translatable(output.isEmpty()
                ? "msg.geology.core_rig.output_ready" : "msg.geology.core_rig.output_blocked");
        graphics.drawString(font, Component.translatable("msg.geology.core_rig.output",
                        outputInfo.copy().withStyle(outputColor)),
                x, y, GeologyScreenUtils.COLOR_TEXT_SECONDARY, true);
        y += STATUS_LINE_HEIGHT_TITLE;

        // 预估钻头磨损（仅当钻头存在时显示）
        if (drillHead.getItem() instanceof DrillHeadItem) {
            int estimatedWear = estimateDrillWear();
            graphics.drawString(font, Component.translatable("msg.geology.core_rig.estimated_wear",
                            estimatedWear), x, y, GeologyScreenUtils.COLOR_TEXT_CANDIDATE, true);
        }
    }

    /**
     * 预估钻头磨损（客户端粗略估算，仅供 GUI 显示）。
     *
     * <p>客户端无法读取下方方块的实际岩性，这里用磨损最大值（{@value #MAX_WEAR_PER_METER}/m）× 深度作为保守上限。
     * 实际磨损由服务端按岩性莫氏硬度计算（见 {@link com.example.geology.core.DrillHeadTier#wearPerMeter}）。
     */
    private int estimateDrillWear() {
        return CoreRigBlockEntity.DRILL_DEPTH * MAX_WEAR_PER_METER;
    }
}
