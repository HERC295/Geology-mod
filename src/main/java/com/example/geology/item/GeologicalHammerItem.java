package com.example.geology.item;

import com.example.geology.api.GeologyProvider;
import com.example.geology.api.GeologyProviders;
import com.example.geology.block.CoalOreBlock;
import com.example.geology.block.RockBlock;
import com.example.geology.block.UnidentifiedOreBlock;
import com.example.geology.block.UnidentifiedOreBlockEntity;
import com.example.geology.component.GeologyDataComponents;
import com.example.geology.core.CoalRank;
import com.example.geology.core.MineralAppearance;
import com.example.geology.core.MineralType;
import com.example.geology.core.ProvinceType;
import com.example.geology.core.RockType;
import com.example.geology.core.TestedTraits;
import com.example.geology.util.GeologyUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 地质锤：右键方块可敲取样本，并显示基础地质信息。
 *
 * <p>支持三类方块：
 * <ul>
 *   <li><b>未鉴定矿石</b>（{@link UnidentifiedOreBlock}）：读取 BlockEntity 真身矿物，
 *       掉落携带 {@code UNKNOWN_MINERAL} 组件的未鉴定矿物样本。敲击
 *       {@link UnidentifiedOreBlockEntity#MAX_HITS} 次后矿石方块消失。</li>
 *   <li><b>煤阶矿石</b>（{@link CoalOreBlock}）：掉落对应煤阶物品。方块不破坏。</li>
 *   <li><b>岩石方块</b>（{@link RockBlock}/stone/deepslate）：掉落携带
 *       {@code ROCK_TYPE} 组件的岩石样本。方块不破坏。</li>
 * </ul>
 *
 * <p>每次使用消耗 1 点耐久。前期勘探工具，地表+地下双用途。
 */
public class GeologicalHammerItem extends Item {

    public GeologicalHammerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            BlockState state = level.getBlockState(context.getClickedPos());
            return isTargetBlock(state) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }

        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        // 分派到对应的处理逻辑
        if (block instanceof UnidentifiedOreBlock oreBlock) {
            return handleUnidentifiedOre(context, player, level, pos, oreBlock);
        }
        if (block instanceof CoalOreBlock coalBlock) {
            return handleCoalOre(context, player, level, pos, coalBlock);
        }
        if (GeologyUtil.isStoneLike(state)) {
            return handleRock(context, player, level, pos, state);
        }
        return InteractionResult.PASS;
    }

    /** 客户端预测：是否为地质锤可交互的方块。 */
    private boolean isTargetBlock(BlockState state) {
        Block block = state.getBlock();
        return block instanceof UnidentifiedOreBlock
                || block instanceof CoalOreBlock
                || GeologyUtil.isStoneLike(state);
    }

    /**
     * 处理未鉴定矿石：读取 BE 真身，掉落矿物样本，消耗 1 次敲击次数。
     *
     * <p>敲击 {@link UnidentifiedOreBlockEntity#MAX_HITS} 次后矿石方块消失。
     * 若 BE 未设置真身（创造模式放置），从外观原型成员中随机选一作为 fallback。
     */
    private InteractionResult handleUnidentifiedOre(UseOnContext context, Player player,
                                                     Level level, BlockPos pos,
                                                     UnidentifiedOreBlock oreBlock) {
        MineralAppearance appearance = oreBlock.appearance();
        MineralType realMineral = null;

        BlockEntity be = level.getBlockEntity(pos);
        UnidentifiedOreBlockEntity oreBE = be instanceof UnidentifiedOreBlockEntity u ? u : null;
        if (oreBE != null) {
            realMineral = oreBE.realMineral();
        }

        // fallback：创造模式放置的方块无真身，从外观原型成员中随机选一
        if (realMineral == null) {
            var members = MineralType.byAppearance(appearance);
            if (members.isEmpty()) {
                return InteractionResult.PASS;
            }
            RandomSource random = level.getRandom();
            realMineral = members.toArray(new MineralType[0])[random.nextInt(members.size())];
        }

        // 掉落未鉴定矿物样本（真身隐藏在数据组件中，tooltip 不显示）
        ItemStack sample = new ItemStack(MineralItems.UNIDENTIFIED_MINERAL_SAMPLE.get());
        sample.set(GeologyDataComponents.UNKNOWN_MINERAL.get(), realMineral);
        sample.set(GeologyDataComponents.TESTED_TRAITS.get(), TestedTraits.empty());
        Block.popResource(level, pos.above(), sample);

        consumeDurability(context, player);

        player.sendSystemMessage(Component.translatable("msg.geology.hammer.ore",
                Component.translatable(appearance.translationKey())));

        // 消耗敲击次数：归零后移除方块（无掉落物，样本已由上方掉落）
        if (oreBE != null && !oreBE.consumeHit()) {
            level.removeBlock(pos, false);
        }
        return InteractionResult.CONSUME;
    }

    /**
     * 处理煤阶矿石：掉落对应煤阶物品。
     */
    private InteractionResult handleCoalOre(UseOnContext context, Player player,
                                             Level level, BlockPos pos, CoalOreBlock coalBlock) {
        CoalRank rank = coalBlock.rank();
        ItemStack coalItem = new ItemStack(MineralItems.COAL_ITEMS.get(rank).get());
        Block.popResource(level, pos.above(), coalItem);

        consumeDurability(context, player);

        player.sendSystemMessage(Component.translatable("msg.geology.hammer.coal",
                Component.translatable(rank.translationKey())));
        return InteractionResult.CONSUME;
    }

    /**
     * 处理岩石方块：掉落岩石样本并显示岩性信息。
     *
     * <p>优先读取方块自身的 {@link RockType}（模组岩石方块），
     * 仅对原版 stone/deepslate fallback 时才用 {@link GeologyProvider} 计算。
     */
    private InteractionResult handleRock(UseOnContext context, Player player,
                                          Level level, BlockPos pos, BlockState state) {
        ServerLevel serverLevel = (ServerLevel) level;
        GeologyProvider gen = GeologyProviders.get(serverLevel);
        ProvinceType province = gen.province(pos.getX(), pos.getZ());

        RockType rock;
        if (state.getBlock() instanceof RockBlock rockBlock) {
            rock = rockBlock.rockType();
        } else {
            int surface = GeologyUtil.surfaceY(gen, serverLevel, pos.getX(), pos.getZ());
            int relDepth = Math.max(0, surface - pos.getY());
            rock = gen.disturbedRockAt(province, relDepth, pos.getX(), pos.getY(), pos.getZ());
        }

        ItemStack sample = new ItemStack(GeologyItems.ROCK_SAMPLE.get());
        sample.set(GeologyDataComponents.ROCK_TYPE.get(), rock);
        Block.popResource(level, pos.above(), sample);

        consumeDurability(context, player);

        player.sendSystemMessage(Component.translatable("msg.geology.hammer.hit",
                Component.translatable(rock.translationKey()),
                Component.translatable(rock.category().translationKey()),
                Component.translatable(province.translationKey())));
        return InteractionResult.CONSUME;
    }

    /** 消耗地质锤 1 点耐久。 */
    private void consumeDurability(UseOnContext context, Player player) {
        ItemStack tool = context.getItemInHand();
        EquipmentSlot slot = context.getHand() == InteractionHand.MAIN_HAND
                ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        tool.hurtAndBreak(1, player, slot);
    }
}
