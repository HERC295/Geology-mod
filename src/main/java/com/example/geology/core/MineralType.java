package com.example.geology.core;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 矿物类型枚举（M1 共 24 种）。
 *
 * <p>每种矿物携带完整的地质学定义：成因、外观原型、宿主岩性、偏好地质省、
 * 深度范围、鉴定特征与冶炼档次。世界生成通过 {@link MineralAppearance}
 * 放置未鉴定方块，真身由本枚举确定但隐藏在 BlockEntity / 数据组件中。
 *
 * <p><b>鉴定流程</b>：玩家在鉴定台通过 {@link MineralTraits} 中的四种测试
 * （条痕/硬度/磁性/酸反应）逐步揭示属性，最终唯一确定矿物。
 * {@link #byTraits} 方法根据已测试属性缩小候选范围。
 *
 * <p><b>冶炼规则</b>：未鉴定矿石永远不可冶炼。鉴定后按 {@link SmeltingTier}
 * 分档：SIMPLE（熔炉直接烧）、STANDARD（焙烧）、COMPLEX（M1 不产出锭）、
 * NONE（非金属不可冶炼）。
 *
 * @see MineralAppearance 外观原型（世界生成唯一入口）
 * @see MineralTraits 鉴定特征
 * @see SmeltingTier 冶炼档次
 */
public enum MineralType implements StringRepresentable {

    // ==================== 金属矿物（14 种）====================

    /** 铬铁矿：岩浆成因，橄榄岩宿主，火山省深层。复杂档冶炼。 */
    CHROMITE("chromite", MineralGenesis.IGNEOUS, MineralAppearance.MASSIVE_DARK,
            EnumSet.of(RockType.PERIDOTITE), EnumSet.of(ProvinceType.VOLCANIC_PROVINCE),
            40, 120,
            new MineralTraits("棕色", 5.5F, false, false),
            SmeltingTier.COMPLEX, "Chromite", "铬铁矿"),

    /** 钛铁矿：岩浆成因，辉长岩宿主，火山省中深。复杂档冶炼。 */
    ILMENITE("ilmenite", MineralGenesis.IGNEOUS, MineralAppearance.MASSIVE_DARK,
            EnumSet.of(RockType.GABBRO), EnumSet.of(ProvinceType.VOLCANIC_PROVINCE),
            25, 80,
            new MineralTraits("黑色", 5.5F, true, false),
            SmeltingTier.COMPLEX, "Ilmenite", "钛铁矿"),

    /** 黄铜矿：热液成因，石英脉宿主，造山带中深。标准档焙烧产铜锭。 */
    CHALCOPYRITE("chalcopyrite", MineralGenesis.HYDROTHERMAL, MineralAppearance.METALLIC_GOLD,
            EnumSet.of(RockType.QUARTZITE), EnumSet.of(ProvinceType.OROGENIC_BELT),
            10, 50,
            new MineralTraits("绿黑色", 3.5F, false, false),
            SmeltingTier.STANDARD, "Chalcopyrite", "黄铜矿"),

    /** 方铅矿：热液成因，石灰岩宿主，造山带中深。标准档焙烧产铅锭。 */
    GALENA("galena", MineralGenesis.HYDROTHERMAL, MineralAppearance.METALLIC_SILVER,
            EnumSet.of(RockType.LIMESTONE), EnumSet.of(ProvinceType.OROGENIC_BELT),
            10, 50,
            new MineralTraits("铅灰色", 2.5F, false, false),
            SmeltingTier.STANDARD, "Galena", "方铅矿"),

    /** 闪锌矿：热液成因，石灰岩宿主，与方铅矿共生。标准档焙烧产锌锭。 */
    SPHALERITE("sphalerite", MineralGenesis.HYDROTHERMAL, MineralAppearance.METALLIC_BLACK,
            EnumSet.of(RockType.LIMESTONE), EnumSet.of(ProvinceType.OROGENIC_BELT),
            10, 50,
            new MineralTraits("浅棕色", 3.5F, false, false),
            SmeltingTier.STANDARD, "Sphalerite", "闪锌矿"),

    /** 自然金：热液成因，石英脉宿主，造山带/地盾中浅。简易档直接烧。 */
    NATIVE_GOLD("native_gold", MineralGenesis.HYDROTHERMAL, MineralAppearance.METALLIC_GOLD,
            EnumSet.of(RockType.QUARTZITE), EnumSet.of(ProvinceType.OROGENIC_BELT, ProvinceType.SHIELD),
            5, 40,
            new MineralTraits("金色", 2.5F, false, false),
            SmeltingTier.SIMPLE, "Native Gold", "自然金"),

    /** 自然银：热液成因，石英脉宿主，造山带浅层。简易档直接烧。 */
    NATIVE_SILVER("native_silver", MineralGenesis.HYDROTHERMAL, MineralAppearance.METALLIC_SILVER,
            EnumSet.of(RockType.QUARTZITE), EnumSet.of(ProvinceType.OROGENIC_BELT),
            0, 30,
            new MineralTraits("银白色", 2.5F, false, false),
            SmeltingTier.SIMPLE, "Native Silver", "自然银"),

    /** 辉钼矿：热液成因，石英脉宿主，造山带中深。标准档焙烧产钼锭。 */
    MOLYBDENITE("molybdenite", MineralGenesis.HYDROTHERMAL, MineralAppearance.METALLIC_SILVER,
            EnumSet.of(RockType.QUARTZITE), EnumSet.of(ProvinceType.OROGENIC_BELT),
            10, 50,
            new MineralTraits("灰色", 1.0F, false, false),
            SmeltingTier.STANDARD, "Molybdenite", "辉钼矿"),

    /** 黄铁矿：热液成因，多种宿主，全省全深。"愚人金"，外观与自然金相似。 */
    PYRITE("pyrite", MineralGenesis.HYDROTHERMAL, MineralAppearance.METALLIC_GOLD,
            EnumSet.of(RockType.SANDSTONE, RockType.SHALE, RockType.LIMESTONE, RockType.QUARTZITE),
            EnumSet.allOf(ProvinceType.class),
            0, 120,
            new MineralTraits("绿黑色", 6.0F, false, false),
            SmeltingTier.STANDARD, "Pyrite", "黄铁矿"),

    /** 赤铁矿：沉积成因，砂岩/页岩宿主，沉积盆地/地盾中深。标准档产铁锭。 */
    HEMATITE("hematite", MineralGenesis.SEDIMENTARY, MineralAppearance.EARTHY_RED,
            EnumSet.of(RockType.SANDSTONE, RockType.SHALE),
            EnumSet.of(ProvinceType.SEDIMENTARY_BASIN, ProvinceType.SHIELD),
            20, 70,
            new MineralTraits("红色", 5.5F, false, false),
            SmeltingTier.STANDARD, "Hematite", "赤铁矿"),

    /** 褐铁矿：风化成因，砂岩宿主，古克拉通浅层。铁帽常见矿物。标准档产铁锭。 */
    LIMONITE("limonite", MineralGenesis.WEATHERING, MineralAppearance.EARTHY_RED,
            EnumSet.of(RockType.SANDSTONE), EnumSet.of(ProvinceType.ANCIENT_CRATON),
            0, 15,
            new MineralTraits("黄褐色", 4.0F, false, false),
            SmeltingTier.STANDARD, "Limonite", "褐铁矿"),

    /** 菱铁矿：沉积成因，页岩/石灰岩宿主，沉积盆地中层。标准档产铁锭。遇酸起泡。 */
    SIDERITE("siderite", MineralGenesis.SEDIMENTARY, MineralAppearance.VITREOUS_MASSIVE,
            EnumSet.of(RockType.SHALE, RockType.LIMESTONE),
            EnumSet.of(ProvinceType.SEDIMENTARY_BASIN),
            10, 50,
            new MineralTraits("白色", 4.0F, false, true),
            SmeltingTier.STANDARD, "Siderite", "菱铁矿"),

    /** 磁铁矿：变质成因，片岩/片麻岩宿主，地盾/古克拉通中深。带磁性。标准档产铁锭。 */
    MAGNETITE("magnetite", MineralGenesis.METAMORPHIC, MineralAppearance.METALLIC_BLACK,
            EnumSet.of(RockType.SCHIST, RockType.GNEISS),
            EnumSet.of(ProvinceType.SHIELD, ProvinceType.ANCIENT_CRATON),
            20, 70,
            new MineralTraits("黑色", 6.0F, true, false),
            SmeltingTier.STANDARD, "Magnetite", "磁铁矿"),

    /** 铝土矿：风化成因，石灰岩风化壳，古克拉通浅层。复杂档（需电解铝）。 */
    BAUXITE("bauxite", MineralGenesis.WEATHERING, MineralAppearance.EARTHY_RED,
            EnumSet.of(RockType.LIMESTONE), EnumSet.of(ProvinceType.ANCIENT_CRATON),
            0, 15,
            new MineralTraits("红色", 3.0F, false, false),
            SmeltingTier.COMPLEX, "Bauxite", "铝土矿"),

    // ==================== 非金属矿物（7 种）====================

    /** 萤石：热液成因，花岗岩/石英脉宿主，造山带中层。试剂/光学/冶炼助剂。 */
    FLUORITE("fluorite", MineralGenesis.HYDROTHERMAL, MineralAppearance.CRYSTAL_CLEAR,
            EnumSet.of(RockType.GRANITE, RockType.QUARTZITE),
            EnumSet.of(ProvinceType.OROGENIC_BELT),
            10, 50,
            new MineralTraits("白色", 4.0F, false, false),
            SmeltingTier.NONE, "Fluorite", "萤石"),

    /** 重晶石：热液成因，石灰岩/页岩宿主，沉积盆地中层。钻井液消耗品。 */
    BARITE("barite", MineralGenesis.HYDROTHERMAL, MineralAppearance.VITREOUS_MASSIVE,
            EnumSet.of(RockType.LIMESTONE, RockType.SHALE),
            EnumSet.of(ProvinceType.SEDIMENTARY_BASIN),
            10, 50,
            new MineralTraits("白色", 3.0F, false, false),
            SmeltingTier.NONE, "Barite", "重晶石"),

    /** 石英脉：热液成因，多种宿主，全省全深。标型矿物，金矿指示。 */
    QUARTZ_VEIN("quartz_vein", MineralGenesis.HYDROTHERMAL, MineralAppearance.CRYSTAL_CLEAR,
            EnumSet.of(RockType.GRANITE, RockType.GNEISS, RockType.QUARTZITE),
            EnumSet.allOf(ProvinceType.class),
            0, 120,
            new MineralTraits("白色", 7.0F, false, false),
            SmeltingTier.NONE, "Quartz Vein", "石英脉"),

    /** 石膏：沉积成因，页岩宿主，沉积盆地中层。建筑/医疗原料。 */
    GYPSUM("gypsum", MineralGenesis.SEDIMENTARY, MineralAppearance.SALINE,
            EnumSet.of(RockType.SHALE), EnumSet.of(ProvinceType.SEDIMENTARY_BASIN),
            10, 40,
            new MineralTraits("白色", 2.0F, false, false),
            SmeltingTier.NONE, "Gypsum", "石膏"),

    /** 岩盐：沉积成因，蒸发岩相，沉积盆地中层。食盐/化工原料。 */
    HALITE("halite", MineralGenesis.SEDIMENTARY, MineralAppearance.SALINE,
            EnumSet.of(RockType.SHALE, RockType.MUDSTONE),
            EnumSet.of(ProvinceType.SEDIMENTARY_BASIN),
            10, 40,
            new MineralTraits("白色", 2.5F, false, false),
            SmeltingTier.NONE, "Halite", "岩盐"),

    /** 石墨：变质成因，片岩/片麻岩宿主，造山带中层。润滑/电极原料。 */
    GRAPHITE("graphite", MineralGenesis.METAMORPHIC, MineralAppearance.PLATY_FIBROUS,
            EnumSet.of(RockType.SCHIST, RockType.GNEISS),
            EnumSet.of(ProvinceType.OROGENIC_BELT),
            10, 50,
            new MineralTraits("黑色", 1.5F, false, false),
            SmeltingTier.NONE, "Graphite", "石墨"),

    /** 磷灰石：沉积成因，石灰岩宿主，沉积盆地中层。肥料/火柴原料。遇酸微弱反应。 */
    APATITE("apatite", MineralGenesis.SEDIMENTARY, MineralAppearance.CRYSTAL_COLORED,
            EnumSet.of(RockType.LIMESTONE), EnumSet.of(ProvinceType.SEDIMENTARY_BASIN),
            10, 50,
            new MineralTraits("白色", 5.0F, false, true),
            SmeltingTier.NONE, "Apatite", "磷灰石"),

    // ==================== 宝石矿物（3 种）====================

    /** 石榴石：变质成因，片岩/片麻岩宿主，造山带中层。宝石（多样色变种）。 */
    GARNET("garnet", MineralGenesis.METAMORPHIC, MineralAppearance.CRYSTAL_COLORED,
            EnumSet.of(RockType.SCHIST, RockType.GNEISS),
            EnumSet.of(ProvinceType.OROGENIC_BELT),
            10, 50,
            new MineralTraits("白色", 6.5F, false, false),
            SmeltingTier.NONE, "Garnet", "石榴石"),

    /** 电气石：伟晶岩成因，花岗伟晶岩宿主，古克拉通中层。宝石。 */
    TOURMALINE("tourmaline", MineralGenesis.PEGMATITIC, MineralAppearance.CRYSTAL_COLORED,
            EnumSet.of(RockType.GRANITE), EnumSet.of(ProvinceType.ANCIENT_CRATON),
            10, 50,
            new MineralTraits("白色", 7.0F, false, false),
            SmeltingTier.NONE, "Tourmaline", "电气石"),

    /** 锆石：伟晶岩成因，伟晶岩/砂岩宿主，全省中层。测年原料（M3 年代学用）。 */
    ZIRCON("zircon", MineralGenesis.PEGMATITIC, MineralAppearance.CRYSTAL_COLORED,
            EnumSet.of(RockType.GRANITE, RockType.SANDSTONE),
            EnumSet.allOf(ProvinceType.class),
            10, 50,
            new MineralTraits("白色", 7.5F, false, false),
            SmeltingTier.NONE, "Zircon", "锆石");

    public static final Codec<MineralType> CODEC = StringRepresentable.fromEnum(MineralType::values);
    public static final StreamCodec<ByteBuf, MineralType> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> values()[i], MineralType::ordinal);

    private final String name;
    private final MineralGenesis genesis;
    private final MineralAppearance appearance;
    private final Set<RockType> hostRocks;
    private final Set<ProvinceType> provincePref;
    private final int minDepth;
    private final int maxDepth;
    private final MineralTraits traits;
    private final SmeltingTier smeltingTier;
    private final String displayName;
    private final String displayNameZh;

    /** 按外观原型分组的不可变索引。 */
    private static final Map<MineralAppearance, Set<MineralType>> BY_APPEARANCE =
            Collections.unmodifiableMap(Stream.of(values())
                    .collect(Collectors.groupingBy(
                            MineralType::appearance,
                            Collectors.collectingAndThen(
                                    Collectors.toSet(),
                                    Collections::unmodifiableSet))));

    MineralType(String name, MineralGenesis genesis, MineralAppearance appearance,
                Set<RockType> hostRocks, Set<ProvinceType> provincePref,
                int minDepth, int maxDepth,
                MineralTraits traits, SmeltingTier smeltingTier,
                String displayName, String displayNameZh) {
        this.name = name;
        this.genesis = genesis;
        this.appearance = appearance;
        this.hostRocks = Collections.unmodifiableSet(hostRocks);
        this.provincePref = Collections.unmodifiableSet(provincePref);
        this.minDepth = minDepth;
        this.maxDepth = maxDepth;
        this.traits = traits;
        this.smeltingTier = smeltingTier;
        this.displayName = displayName;
        this.displayNameZh = displayNameZh;
    }

    public MineralGenesis genesis() {
        return genesis;
    }

    public MineralAppearance appearance() {
        return appearance;
    }

    /** 宿主岩性集合（不可变）。 */
    public Set<RockType> hostRocks() {
        return hostRocks;
    }

    /** 偏好地质省集合（不可变）。 */
    public Set<ProvinceType> provincePref() {
        return provincePref;
    }

    public int minDepth() {
        return minDepth;
    }

    public int maxDepth() {
        return maxDepth;
    }

    public MineralTraits traits() {
        return traits;
    }

    public SmeltingTier smeltingTier() {
        return smeltingTier;
    }

    /**
     * 返回该矿物的冶炼主产出（设计文档 9.3）。
     *
     * <p>映射规则：
     * <ul>
     *   <li>自然金 → {@link SmeltingOutput#VANILLA_GOLD}；</li>
     *   <li>自然银 → {@link SmeltingOutput#SILVER_INGOT}（原版无银锭）；</li>
     *   <li>黄铜矿 → {@link SmeltingOutput#VANILLA_COPPER}（焙烧）；</li>
     *   <li>方铅矿 → {@link SmeltingOutput#LEAD_INGOT}（焙烧）；</li>
     *   <li>闪锌矿 → {@link SmeltingOutput#ZINC_INGOT}（焙烧）；</li>
     *   <li>辉钼矿 → {@link SmeltingOutput#MOLYBDENUM_INGOT}（焙烧）；</li>
     *   <li>赤铁矿/磁铁矿/褐铁矿/菱铁矿 → {@link SmeltingOutput#VANILLA_IRON}；</li>
     *   <li>黄铁矿 → {@link SmeltingOutput#NONE}（M1 不产锭，留 M2 硫酸工艺）；</li>
     *   <li>复杂档（铝土矿/铬铁矿/钛铁矿）与非金属 → {@link SmeltingOutput#NONE}。</li>
     * </ul>
     *
     * <p><b>未鉴定矿石不可冶炼</b>（硬规则）：本方法仅用于已鉴定矿物的冶炼配方注册，
     * 未鉴定矿石方块不注册任何冶炼配方。
     *
     * @return 冶炼主产出类型
     */
    public SmeltingOutput smeltingOutput() {
        return switch (this) {
            case NATIVE_GOLD -> SmeltingOutput.VANILLA_GOLD;
            case NATIVE_SILVER -> SmeltingOutput.SILVER_INGOT;
            case CHALCOPYRITE -> SmeltingOutput.VANILLA_COPPER;
            case GALENA -> SmeltingOutput.LEAD_INGOT;
            case SPHALERITE -> SmeltingOutput.ZINC_INGOT;
            case MOLYBDENITE -> SmeltingOutput.MOLYBDENUM_INGOT;
            case HEMATITE, MAGNETITE, LIMONITE, SIDERITE -> SmeltingOutput.VANILLA_IRON;
            case BARITE -> SmeltingOutput.BARITE_POWDER;
            // 黄铁矿 M1 不产锭（留 M2 硫酸工艺）；复杂档与其他非金属不产出
            default -> SmeltingOutput.NONE;
        };
    }

    /** 翻译键。 */
    public String translationKey() {
        return "mineral.geology." + name;
    }

    /** 英文显示名（用于语言文件生成）。 */
    public String displayName() {
        return displayName;
    }

    /** 中文显示名（用于语言文件生成）。 */
    public String displayNameZh() {
        return displayNameZh;
    }

    /**
     * 返回属于指定外观原型的所有矿物（不可变集合）。
     *
     * @param appearance 外观原型
     * @return 该原型的矿物集合
     */
    public static Set<MineralType> byAppearance(MineralAppearance appearance) {
        return BY_APPEARANCE.getOrDefault(appearance, Set.of());
    }

    /**
     * 按序列化名称查找矿物类型。
     *
     * <p>用于 BlockEntity NBT 反序列化与数据包解析。
     *
     * @param name 序列化名称（{@link #getSerializedName()}）
     * @return 对应的矿物类型
     * @throws IllegalArgumentException 若名称未知
     */
    public static MineralType byName(String name) {
        for (MineralType mineral : values()) {
            if (mineral.getSerializedName().equals(name)) {
                return mineral;
            }
        }
        throw new IllegalArgumentException("Unknown mineral: " + name);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
