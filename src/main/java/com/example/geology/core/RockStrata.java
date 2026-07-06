package com.example.geology.core;

/**
 * 岩石地层逻辑。
 *
 * <p>根据地质省类型与相对深度（地表为 0，向下递增）推断岩性。
 * 采用简化的层序模型，由 {@link #rockAt(ProvinceType, int)} 返回任意相对深度的概念岩性。
 *
 * <p>本类为纯函数工具类，无状态，线程安全。世界生成（{@link com.example.geology.world.RockStrataFeature}）
 * 与工具物品的 fallback 路径都通过 {@link com.example.geology.api.GeologyProvider#disturbedRockAt}
 * 间接调用本类，保证岩性分层逻辑单一来源。
 *
 * <p>后续阶段会接入更精细的层序与矿脉分布，可通过数据包驱动替换本类逻辑。
 */
public final class RockStrata {

    private RockStrata() {
    }

    /**
     * 给定地质省与相对深度，返回该位置的概念岩性。
     *
     * @param province 地质省
     * @param relDepth 相对深度（地表=0，向下为正）
     * @return 该位置的岩性
     */
    public static RockType rockAt(ProvinceType province, int relDepth) {
        return switch (province) {
            // 沉积盆地：砂岩→泥岩→石灰岩→页岩→白云岩→砾岩→花岗岩基底
            case SEDIMENTARY_BASIN -> relDepth < 10 ? RockType.SANDSTONE
                    : relDepth < 20 ? RockType.MUDSTONE
                    : relDepth < 35 ? RockType.LIMESTONE
                    : relDepth < 55 ? RockType.SHALE
                    : relDepth < 75 ? RockType.DOLOMITE
                    : relDepth < 95 ? RockType.CONGLOMERATE
                    : RockType.GRANITE;
            // 古克拉通：花岗岩→片麻岩→花岗岩→片麻岩（金伯利岩由扰动逻辑偶发插入）
            case ANCIENT_CRATON -> relDepth < 20 ? RockType.GRANITE
                    : relDepth < 50 ? RockType.GNEISS
                    : relDepth < 85 ? RockType.GRANITE
                    : RockType.GNEISS;
            // 造山带：片麻岩→大理岩→片岩→石英岩→角闪岩
            case OROGENIC_BELT -> relDepth < 20 ? RockType.GNEISS
                    : relDepth < 40 ? RockType.MARBLE
                    : relDepth < 60 ? RockType.SCHIST
                    : relDepth < 80 ? RockType.QUARTZITE
                    : RockType.AMPHIBOLITE;
            // 火山省：玄武岩→安山岩→辉长岩→橄榄岩→玄武岩
            case VOLCANIC_PROVINCE -> relDepth < 15 ? RockType.BASALT
                    : relDepth < 35 ? RockType.ANDESITE
                    : relDepth < 60 ? RockType.GABBRO
                    : relDepth < 85 ? RockType.PERIDOTITE
                    : RockType.BASALT;
            // 地盾：片麻岩→花岗岩→片岩→片麻岩→榴辉岩
            case SHIELD -> relDepth < 25 ? RockType.GNEISS
                    : relDepth < 50 ? RockType.GRANITE
                    : relDepth < 75 ? RockType.SCHIST
                    : relDepth < 100 ? RockType.GNEISS
                    : RockType.ECLOGITE;
        };
    }

    /**
     * 岩性扰动阈值（{@code v > THRESHOLD} 时插入特殊岩性）。常量化避免 DRY 违规。
     */
    private static final double DISTURB_THRESHOLD = 0.78;
    /** 金伯利岩生成阈值（古克拉通深层，极稀有）。 */
    private static final double KIMBERLITE_THRESHOLD = 0.92;
    /** 金伯利岩最小相对深度（古克拉通深部）。 */
    private static final int KIMBERLITE_MIN_DEPTH = 60;
    /** 浅层不扰动的最大相对深度（避免破坏地表层序）。 */
    private static final int DISTURB_SKIP_DEPTH = 5;
    /**
     * 扰动噪声采样尺度（越大扰动斑块越大）。包级可见,供同包
     * {@link GeologyGenerator#disturbanceAt} 复用,保证扰动基底与
     * {@link #disturb} 的噪声尺度一致（DRY）。
     */
    static final double DISTURB_NOISE_SCALE = 52.0;
    /**
     * 岩性扰动噪声的种子掩码（与 seed 异或生成 rockNoise 种子）。
     *
     * <p><b>单一来源</b>:供 {@link GeologyGenerator}(独立模式)与
     * {@link com.example.geology.integration.reterraforged.IntegratedGeologyProvider}(集成模式)
     * 共用,保证两种模式的 rockNoise 在相同 seed 下产生完全一致的扰动噪声,
     * 实现"独立模式与集成模式岩性扰动行为一致"的 DRY 契约。
     */
    public static final long ROCK_NOISE_SEED_MASK = 0xC2B2AE3D27D4EB4FL;

    /**
     * 根据地质省、相对深度与噪声，返回考虑了岩性扰动的岩性。
     *
     * <p>本方法是岩性扰动的<b>单一来源</b>，独立模式（{@link GeologyGenerator}）
     * 与集成模式（{@code IntegratedGeologyProvider}）共用，保证两种模式
     * 的扰动行为一致（DRY 原则）。
     *
     * <p>扰动规则：
     * <ul>
     *   <li>浅层（{@code relDepth <= 5}）不扰动，保护地表层序；</li>
     *   <li>古克拉通深层（{@code relDepth > 60}）且噪声 {@code > 0.92} 时插入金伯利岩
     *       （金刚石唯一母岩，极稀有）；</li>
     *   <li>噪声 {@code > 0.78} 时按地质省插入特殊岩性
     *       （沉积盆地→燧石结核，火山省→凝灰岩，造山带/地盾→蛇纹岩）；</li>
     *   <li>其余返回基础岩性。</li>
     * </ul>
     *
     * @param base      基础岩性（由 {@link #rockAt} 计算）
     * @param province  地质省
     * @param relDepth  相对深度（地表=0，向下为正）
     * @param x         世界 X 坐标
     * @param y         世界 Y 坐标
     * @param z         世界 Z 坐标
     * @param rockNoise 岩性扰动噪声（与 seed 相关，线程安全）
     * @return 考虑扰动后的岩性
     */
    public static RockType disturb(RockType base, ProvinceType province, int relDepth,
                                   int x, int y, int z, ValueNoise rockNoise) {
        if (relDepth <= DISTURB_SKIP_DEPTH) {
            return base;
        }
        double v = rockNoise.noise2D(x / DISTURB_NOISE_SCALE, (z + y) / DISTURB_NOISE_SCALE);

        // 金伯利岩：古克拉通深层，极稀有，金刚石唯一母岩
        if (province == ProvinceType.ANCIENT_CRATON
                && relDepth > KIMBERLITE_MIN_DEPTH
                && v > KIMBERLITE_THRESHOLD) {
            return RockType.KIMBERLITE;
        }

        // 常规岩性扰动
        if (v > DISTURB_THRESHOLD) {
            return switch (province) {
                case SEDIMENTARY_BASIN -> RockType.CHERT_NODULE;
                case VOLCANIC_PROVINCE -> RockType.TUFF;
                case OROGENIC_BELT -> RockType.SERPENTINITE;
                case SHIELD -> RockType.SERPENTINITE;
                default -> base;
            };
        }
        return base;
    }
}
