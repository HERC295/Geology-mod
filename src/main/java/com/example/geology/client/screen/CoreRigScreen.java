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

public class CoreRigScreen extends AbstractContainerScreen<CoreRigMenu> {

    private static final int BUTTON_X = 8;
    private static final int BUTTON_Y = 40;
    private static final int BUTTON_WIDTH = 70;
    private static final int BUTTON_HEIGHT = 18;
    private static final int STATUS_X = 82;
    private static final int STATUS_Y = 18;
    private static final int STATUS_LINE_HEIGHT_TITLE = 11;
    private static final int STATUS_LINE_HEIGHT = 10;
    private static final int MAX_WEAR_PER_METER = 4;

    public CoreRigScreen(CoreRigMenu menu, Inventory playerInventory, Component title) {
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
        addRenderableWidget(Button.builder(
                        Component.translatable("msg.geology.core_rig.drill_button"),
                        button -> sendDrillRequest())
                .bounds(leftPos + BUTTON_X, topPos + BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT)
                .build());
    }

    private void sendDrillRequest() {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, CoreRigMenu.BUTTON_DRILL);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        GeologyScreenUtils.drawBackground(graphics, leftPos, topPos, imageWidth, imageHeight);
        GeologyScreenUtils.drawTitleBar(graphics, leftPos, topPos, imageWidth);
        GeologyScreenUtils.drawHDivider(graphics, leftPos, topPos + GeologyScreenUtils.TOP_DIVIDER_Y, imageWidth);
        GeologyScreenUtils.drawHDivider(graphics, leftPos, topPos + GeologyScreenUtils.INV_DIVIDER_Y, imageWidth);
        int panelX = leftPos + STATUS_X - 2;
        GeologyScreenUtils.drawPanel(graphics, panelX + 1, topPos + GeologyScreenUtils.TITLE_BAR_HEIGHT,
                imageWidth - STATUS_X - 1, GeologyScreenUtils.PANEL_HEIGHT);
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

    private void renderStatusPanel(GuiGraphics graphics) {
        ItemStack drillHead = menu.rigContainer().getItem(CoreRigBlockEntity.SLOT_DRILL_HEAD);
        ItemStack drillingFluid = menu.rigContainer().getItem(CoreRigBlockEntity.SLOT_DRILLING_FLUID);
        ItemStack fuel = menu.rigContainer().getItem(CoreRigBlockEntity.SLOT_FUEL);
        ItemStack output = menu.rigContainer().getItem(CoreRigBlockEntity.SLOT_OUTPUT);

        int x = leftPos + STATUS_X;
        int y = topPos + STATUS_Y;

        graphics.drawString(font, Component.translatable("msg.geology.core_rig.status"),
                x, y, GeologyScreenUtils.COLOR_TEXT_PRIMARY, true);
        y += STATUS_LINE_HEIGHT_TITLE;

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

        int fluidNeeded = (CoreRigBlockEntity.DRILL_DEPTH + CoreRigBlockEntity.FLUID_PER_METERS - 1)
                / CoreRigBlockEntity.FLUID_PER_METERS;
        int fluidHave = drillingFluid.getCount();
        ChatFormatting fluidColor = fluidHave >= fluidNeeded ? ChatFormatting.GREEN : ChatFormatting.RED;
        graphics.drawString(font, Component.translatable("msg.geology.core_rig.fluid",
                        Component.literal(fluidHave + "/" + fluidNeeded).withStyle(fluidColor)),
                x, y, GeologyScreenUtils.COLOR_TEXT_SECONDARY, true);
        y += STATUS_LINE_HEIGHT;

        ChatFormatting fuelColor = (!fuel.isEmpty() && fuel.getBurnTime(RecipeType.SMELTING) > 0)
                ? ChatFormatting.GREEN : ChatFormatting.RED;
        Component fuelInfo = Component.translatable(fuel.isEmpty()
                ? "msg.geology.core_rig.fuel_empty" : "msg.geology.core_rig.fuel_full");
        graphics.drawString(font, Component.translatable("msg.geology.core_rig.fuel",
                        fuelInfo.copy().withStyle(fuelColor)),
                x, y, GeologyScreenUtils.COLOR_TEXT_SECONDARY, true);
        y += STATUS_LINE_HEIGHT;

        ChatFormatting outputColor = output.isEmpty() ? ChatFormatting.GREEN : ChatFormatting.YELLOW;
        Component outputInfo = Component.translatable(output.isEmpty()
                ? "msg.geology.core_rig.output_ready" : "msg.geology.core_rig.output_blocked");
        graphics.drawString(font, Component.translatable("msg.geology.core_rig.output",
                        outputInfo.copy().withStyle(outputColor)),
                x, y, GeologyScreenUtils.COLOR_TEXT_SECONDARY, true);
        y += STATUS_LINE_HEIGHT_TITLE;

        if (drillHead.getItem() instanceof DrillHeadItem) {
            int estimatedWear = estimateDrillWear();
            graphics.drawString(font, Component.translatable("msg.geology.core_rig.estimated_wear",
                            estimatedWear), x, y, GeologyScreenUtils.COLOR_TEXT_CANDIDATE, true);
        }
    }

    private int estimateDrillWear() {
        return CoreRigBlockEntity.DRILL_DEPTH * MAX_WEAR_PER_METER;
    }
}
