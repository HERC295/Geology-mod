package com.example.geology.block;

import com.example.geology.component.GeologyDataComponents;
import com.example.geology.core.GeologyAttachments;
import com.example.geology.core.IdentificationTestType;
import com.example.geology.core.MineralQuery;
import com.example.geology.core.MineralType;
import com.example.geology.core.SmeltingOutput;
import com.example.geology.core.TestedTraits;
import com.example.geology.item.IdentifiedMineralSampleItem;
import com.example.geology.item.IndustrialItems;
import com.example.geology.menu.IdentificationMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

/**
 * 鉴定台方块实体（设计文档 5.2）。
 *
 * <p><b>三槽物品栏</b>：
 * <ul>
 *   <li>槽 0：样本槽——放入携带 {@code UNKNOWN_MINERAL} 组件的未鉴定矿物样本；</li>
 *   <li>槽 1：试剂槽——放入盐酸（酸溶测试）或素瓷板（条痕测试）等试剂；</li>
 *   <li>槽 2：参照物槽——放入参照物硬度套装（硬度测试）或罗盘（磁性测试）。</li>
 * </ul>
 *
 * <p><b>测试逻辑</b>：玩家通过 GUI 按钮触发测试，BlockEntity 校验对应试剂/参照物在槽中，
 * 读取样本真身矿物的 {@link com.example.geology.core.MineralTraits}，将对应属性写入样本的
 * {@code TESTED_TRAITS} 组件。测试不消耗试剂（M1 简化）。
 *
 * <p><b>候选矿物计算</b>：基于样本外观原型 + 已测试属性过滤 {@link MineralType#byAppearance}，
 * 实时显示候选列表，让玩家观察排除过程。
 *
 * <p><b>同步策略</b>：物品栏通过 Menu 同步到客户端 GUI；BlockEntity 本身无需客户端渲染。
 */
public class IdentificationTableBlockEntity extends net.minecraft.world.level.block.entity.BlockEntity
        implements Container, MenuProvider {

    /** 样本槽索引。 */
    public static final int SLOT_SAMPLE = 0;
    /** 试剂槽索引。 */
    public static final int SLOT_REAGENT = 1;
    /** 参照物槽索引。 */
    public static final int SLOT_REFERENCE = 2;
    /** 总槽位数。 */
    public static final int CONTAINER_SIZE = 3;

    private final NonNullList<ItemStack> items = NonNullList.withSize(CONTAINER_SIZE, ItemStack.EMPTY);

    public IdentificationTableBlockEntity(BlockPos pos, BlockState state) {
        super(GeologyBlockEntities.IDENTIFICATION_TABLE.get(), pos, state);
    }

    /**
     * 执行鉴定测试（由 Menu 的 ServerboundPacket 调用）。
     *
     * <p>校验样本槽含未鉴定矿物样本，根据测试类型校验试剂/参照物槽，
     * 读取样本真身矿物对应属性，写入 TESTED_TRAITS 组件。
     *
     * @param testType 测试类型
     * @param player 执行测试的玩家（保留参数，后续阶段用于消耗耐久等扩展）
     * @return 是否成功执行（试剂齐全且样本有效）
     */
    public boolean performTest(IdentificationTestType testType, ServerPlayer player) {
        ItemStack sample = items.get(SLOT_SAMPLE);
        MineralType mineral = sample.get(GeologyDataComponents.UNKNOWN_MINERAL.get());
        if (mineral == null) {
            return false;
        }

        // 校验对应槽位是否含所需物品（M1 简化：仅检查槽非空，具体物品匹配留给后续阶段）
        switch (testType) {
            case STREAK, ACID -> {
                if (items.get(SLOT_REAGENT).isEmpty()) {
                    return false;
                }
            }
            case HARDNESS, MAGNETIC -> {
                if (items.get(SLOT_REFERENCE).isEmpty()) {
                    return false;
                }
            }
        }

        TestedTraits current = sample.getOrDefault(GeologyDataComponents.TESTED_TRAITS.get(), TestedTraits.empty());
        var traits = mineral.traits();
        TestedTraits updated = switch (testType) {
            case STREAK -> new TestedTraits(
                    Optional.of(traits.streakColor()),
                    current.hardness(), current.magnetic(), current.acidReaction());
            case HARDNESS -> new TestedTraits(
                    current.streakColor(),
                    Optional.of(traits.hardness()),
                    current.magnetic(), current.acidReaction());
            case MAGNETIC -> new TestedTraits(
                    current.streakColor(), current.hardness(),
                    Optional.of(traits.magnetic()),
                    current.acidReaction());
            case ACID -> new TestedTraits(
                    current.streakColor(), current.hardness(), current.magnetic(),
                    Optional.of(traits.acidReaction()));
        };
        sample.set(GeologyDataComponents.TESTED_TRAITS.get(), updated);

        if (level != null) {
            level.playSound(null, worldPosition, SoundEvents.UI_STONECUTTER_SELECT_RECIPE,
                    SoundSource.BLOCKS, 0.6F, 1.2F);
        }
        setChanged();

        // 测试后若候选矿物唯一确定，记录到玩家图鉴（设计文档 4.5）
        Set<MineralType> remaining = candidates();
        if (remaining.size() == 1) {
            MineralType identified = remaining.iterator().next();
            Set<MineralType> known = new LinkedHashSet<>(player.getData(GeologyAttachments.IDENTIFIED_MINERALS.get()));
            if (known.add(identified)) {
                player.setData(GeologyAttachments.IDENTIFIED_MINERALS.get(), known);
            }
            // 全部 4 项测试完成后，若矿物可冶炼，将未鉴定样本转换为已鉴定样本（设计文档 9.3）
            if (updated.fullyTested()) {
                convertToIdentifiedSample(identified);
            }
        }
        return true;
    }

    /**
     * 将样本槽中的未鉴定样本转换为已鉴定样本物品。
     *
     * <p>转换规则：
     * <ul>
     *   <li>可冶炼矿物（{@link SmeltingOutput} 非 {@link SmeltingOutput#NONE}）：
     *       转换为对应冶炼产出的 {@link IdentifiedMineralSampleItem}，玩家可放入熔炉冶炼；</li>
     *   <li>不可冶炼矿物（{@link SmeltingOutput#NONE}，如萤石/石英脉/宝石等）：
     *       转换为 {@link com.example.geology.item.CollectibleSampleItem}，作为图鉴收集品。</li>
     * </ul>
     *
     * <p>无论何种产物，都保留原样本的真身矿物组件，供图鉴/溯源/tooltip 使用。
     */
    private void convertToIdentifiedSample(MineralType mineral) {
        SmeltingOutput output = mineral.smeltingOutput();
        ItemStack identifiedSample;
        if (output == SmeltingOutput.NONE) {
            // 不可冶炼矿物转换为收藏样本物品（图鉴收集品，无冶炼价值）
            identifiedSample = new ItemStack(IndustrialItems.COLLECTIBLE_SAMPLE.get());
        } else {
            // 可冶炼矿物转换为对应冶炼产出的已鉴定样本物品
            identifiedSample = new ItemStack(IndustrialItems.IDENTIFIED_MINERAL_SAMPLES.get(output).get());
        }
        // 保留原样本的真身矿物组件，供图鉴/溯源/tooltip 使用
        identifiedSample.set(GeologyDataComponents.UNKNOWN_MINERAL.get(), mineral);
        items.set(SLOT_SAMPLE, identifiedSample);
    }

    /**
     * 计算当前样本的候选矿物列表（基于外观原型 + 已测试属性过滤）。
     *
     * <p>实际逻辑委托 {@link MineralQuery#candidatesFor}，保证服务端/客户端口径一致。
     *
     * @return 候选矿物集合；样本无效时返回空集
     */
    public Set<MineralType> candidates() {
        ItemStack sample = items.get(SLOT_SAMPLE);
        MineralType mineral = sample.get(GeologyDataComponents.UNKNOWN_MINERAL.get());
        if (mineral == null) {
            return Set.of();
        }
        TestedTraits traits = sample.getOrDefault(GeologyDataComponents.TESTED_TRAITS.get(), TestedTraits.empty());
        return MineralQuery.candidatesFor(mineral.appearance(), traits);
    }

    /** 方块破坏时掉落物品栏内容。 */
    public void dropContents() {
        if (level == null) {
            return;
        }
        for (int i = 0; i < CONTAINER_SIZE; i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                net.minecraft.world.Containers.dropItemStack(level, worldPosition.getX(),
                        worldPosition.getY(), worldPosition.getZ(), stack);
            }
            items.set(i, ItemStack.EMPTY);
        }
    }

    @Override
    public int getContainerSize() {
        return CONTAINER_SIZE;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(items, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        items.clear();
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.geology.identification_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return new IdentificationMenu(containerId, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelper.saveAllItems(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelper.loadAllItems(tag, items, registries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }
}
