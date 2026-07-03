package com.example.geology.item;

import com.example.geology.component.GeologyDataComponents;
import com.example.geology.core.CoreLayer;
import com.example.geology.core.CoreSample;
import com.example.geology.core.MineralType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * 岩心柱物品（设计文档 8.2）。
 *
 * <p>岩心钻机下钻完成后产出，携带 {@link CoreSample} 数据组件记录地层序列。
 * <p>玩家通过 tooltip 查看地层摘要：总深度 + 含矿/含空腔标记 + 来源坐标 + 完整地层序列。
 *
 * <p>岩心柱可陈列（学术收藏价值），多钻孔可拼接立体地层模型（M3 博物馆附属）。
 */
public class CoreSampleItem extends Item {

    public CoreSampleItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CoreSample sample = stack.get(GeologyDataComponents.CORE_SAMPLE.get());
        if (sample == null) {
            tooltip.add(Component.translatable("msg.geology.core_sample.empty")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }

        // 摘要：总深度 + 含矿/含空腔标记 + 来源坐标
        tooltip.add(Component.translatable("msg.geology.core_sample.depth", sample.totalDepth())
                .withStyle(ChatFormatting.AQUA));
        if (sample.hasMineral()) {
            tooltip.add(Component.translatable("msg.geology.core_sample.has_mineral")
                    .withStyle(ChatFormatting.GOLD));
        }
        if (sample.hasCavity()) {
            tooltip.add(Component.translatable("msg.geology.core_sample.has_cavity")
                    .withStyle(ChatFormatting.YELLOW));
        }
        tooltip.add(Component.translatable("msg.geology.core_sample.source",
                sample.sourceX(), sample.sourceZ()).withStyle(ChatFormatting.DARK_GRAY));

        // 完整地层序列（默认显示，每层岩性 + 厚度 + 矿物）
        tooltip.add(Component.empty());
        tooltip.add(Component.translatable("msg.geology.core_sample.layers")
                .withStyle(ChatFormatting.WHITE));
        for (CoreLayer layer : sample.layers()) {
            tooltip.add(formatLayer(layer));
        }
    }

    /** 格式化单层岩性为 tooltip 行。 */
    private static Component formatLayer(CoreLayer layer) {
        if (layer.isCavity()) {
            return Component.translatable("msg.geology.core_sample.layer_cavity", layer.thickness())
                    .withStyle(ChatFormatting.YELLOW);
        }
        Component rockName = layer.rock()
                .map(r -> Component.translatable(r.translationKey()))
                .orElse(Component.literal("???"));
        Component layerComp = Component.translatable("msg.geology.core_sample.layer_rock",
                rockName, layer.thickness());
        if (layer.hasMineral()) {
            MineralType mineral = layer.mineral().get();
            layerComp = layerComp.copy()
                    .append(Component.translatable("msg.geology.core_sample.layer_mineral",
                            Component.translatable(mineral.translationKey()))
                            .withStyle(ChatFormatting.GOLD));
        }
        return layerComp;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // 客户端不处理（tooltip 已足够展示信息，右键无额外操作）
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
