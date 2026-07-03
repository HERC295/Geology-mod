package com.example.geology.block;

import com.example.geology.core.MineralType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 未鉴定矿石方块实体。
 *
 * <p>存储矿脉的真身矿物（{@link MineralType}）与鉴定状态。
 * 方块在世界中<b>始终保持未鉴定外观</b>（由 {@link UnidentifiedOreBlock} 的外观原型决定），
 * 真身仅存在于此 BlockEntity 中，玩家肉眼无法看到。
 *
 * <p><b>数据流向</b>：
 * <ol>
 *   <li>{@code MineralVeinFeature} 放置未鉴定矿石方块时，创建 BE 并写入真身矿物；</li>
 *   <li>玩家用地质锤右键方块时，读取 BE 真身，生成携带
 *       {@code UNKNOWN_MINERAL} 组件的样本物品掉落，并消耗 1 次敲击次数；</li>
 *   <li>敲击次数归零（{@value #MAX_HITS} 次）后矿石方块消失；</li>
 *   <li>方块本身不因鉴定而改变外观（路径 X：方块不变，物品转换）。
 * </ol>
 *
 * <p><b>同步策略</b>：真身矿物为服务端独占数据，不同步到客户端。
 * 客户端无需知道真身（那是鉴定台揭示后的结果），因此不重写
 * {@code getUpdateTag} / {@code getUpdatePacket}。
 *
 * @see UnidentifiedOreBlock 关联方块（实现 {@code EntityBlock}）
 * @see MineralType 真身矿物枚举
 */
public class UnidentifiedOreBlockEntity extends BlockEntity {

    /** NBT 键：真身矿物序列化名称。 */
    private static final String KEY_REAL_MINERAL = "real_mineral";
    /** NBT 键：是否已被鉴定。 */
    private static final String KEY_IDENTIFIED = "identified";
    /** NBT 键：剩余可敲击次数。 */
    private static final String KEY_HITS_REMAINING = "hits_remaining";

    /** 地质锤最大敲击次数（敲满后矿石消失）。 */
    public static final int MAX_HITS = 3;

    /** 真身矿物（世界生成时写入，服务端独占）。 */
    private MineralType realMineral;
    /** 是否已被鉴定（M1 预留字段，默认 false）。 */
    private boolean identified;
    /** 剩余可敲击次数，默认 {@value #MAX_HITS}，归零后矿石消失。 */
    private int hitsRemaining = MAX_HITS;

    public UnidentifiedOreBlockEntity(BlockPos pos, BlockState state) {
        super(GeologyBlockEntities.UNIDENTIFIED_ORE.get(), pos, state);
    }

    /**
     * 设置真身矿物（世界生成阶段调用）。
     *
     * @param mineral 真身矿物
     */
    public void setRealMineral(MineralType mineral) {
        this.realMineral = mineral;
        setChanged();
    }

    /**
     * 返回真身矿物。
     *
     * @return 真身矿物，若未设置返回 {@code null}
     */
    public MineralType realMineral() {
        return realMineral;
    }

    /**
     * 标记为已鉴定。
     *
     * @param identified 鉴定状态
     */
    public void setIdentified(boolean identified) {
        this.identified = identified;
        setChanged();
    }

    /** 是否已被鉴定。 */
    public boolean identified() {
        return identified;
    }

    /**
     * 返回剩余可敲击次数。
     *
     * @return 剩余次数，归零表示矿石已耗尽
     */
    public int hitsRemaining() {
        return hitsRemaining;
    }

    /**
     * 消耗一次敲击次数。
     *
     * <p>由地质锤调用，每次敲击递减 1。
     *
     * @return 消耗后是否仍有剩余次数（{@code false} 表示矿石已耗尽，应移除方块）
     */
    public boolean consumeHit() {
        hitsRemaining = Math.max(0, hitsRemaining - 1);
        setChanged();
        return hitsRemaining > 0;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (realMineral != null) {
            tag.putString(KEY_REAL_MINERAL, realMineral.getSerializedName());
        }
        tag.putBoolean(KEY_IDENTIFIED, identified);
        tag.putInt(KEY_HITS_REMAINING, hitsRemaining);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains(KEY_REAL_MINERAL)) {
            realMineral = MineralType.byName(tag.getString(KEY_REAL_MINERAL));
        }
        identified = tag.getBoolean(KEY_IDENTIFIED);
        hitsRemaining = tag.contains(KEY_HITS_REMAINING)
                ? tag.getInt(KEY_HITS_REMAINING) : MAX_HITS;
    }
}
