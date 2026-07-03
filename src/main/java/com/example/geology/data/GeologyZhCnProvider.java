package com.example.geology.data;

import com.example.geology.GeologyMod;
import com.example.geology.core.CoalRank;
import com.example.geology.core.MineralAppearance;
import com.example.geology.core.MineralGenesis;
import com.example.geology.core.MineralType;
import com.example.geology.core.ProvinceType;
import com.example.geology.core.RockCategory;
import com.example.geology.core.RockType;
import com.example.geology.core.VeinType;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

/**
 * 简体中文语言文件生成（完整翻译）。
 * <p>
 * 包含：岩石方块名、矿物方块/物品名、煤阶物品名、工具物品名、消息、
 * 地质省、岩类、矿物、外观、煤阶、成因、矿脉形态、Patchouli/ClothConfig 界面文本。
 */
public class GeologyZhCnProvider extends LanguageProvider {

    public GeologyZhCnProvider(PackOutput output) {
        super(output, GeologyMod.MOD_ID, "zh_cn");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.geology", "地质学");
        add("item.geology.geological_hammer", "地质锤");
        add("item.geology.magnifier", "手持放大镜");
        add("item.geology.rock_sample", "岩石样本");
        add("item.geology.rock_sample.named", "%s 样本");
        add("item.geology.geologist_guide", "地质学家野外指南");
        add("item.geology.unidentified_mineral_sample", "未鉴定矿物样本");
        add("item.geology.geological_almanac", "地质图鉴");
        add("item.geology.geological_compass", "地质罗盘");
        add("msg.geology.compass.boundary", "地质省：%s｜最近省界：%s 方向（%s 格）");
        add("msg.geology.compass.no_boundary", "地质省：%s｜搜索半径内未发现省界");
        add("block.geology.identification_table", "鉴定台");
        add("msg.geology.almanac.title", "地质图鉴");
        add("msg.geology.almanac.appearance", "外观：%s");
        add("msg.geology.hammer.hit", "样本：%s（%s）｜地质省：%s");
        add("msg.geology.hammer.ore", "矿物样本（%s）——待鉴定");
        add("msg.geology.hammer.coal", "煤样（%s）");
        add("msg.geology.identification.test.streak", "条痕测试");
        add("msg.geology.identification.test.hardness", "硬度测试");
        add("msg.geology.identification.test.magnetic", "磁性测试");
        add("msg.geology.identification.test.acid", "酸溶测试");
        add("msg.geology.identification.candidates", "候选矿物：");
        add("msg.geology.identification.no_sample", "（无样本）");
        add("msg.geology.identification.tested", "已测试属性：");
        add("msg.geology.identification.hint.sample", "放入未鉴定矿物样本");
        add("msg.geology.identification.hint.invalid_sample", "请用地质锤敲矿石获取样本");
        add("msg.geology.identification.hint.reagents", "放入试剂/参照物后测试");
        add("msg.geology.identification.trait.streak", "条痕：%s");
        add("msg.geology.identification.trait.hardness", "硬度：%s");
        add("msg.geology.identification.trait.magnetic", "磁性：%s");
        add("msg.geology.identification.trait.acid", "酸反应：%s");
        add("msg.geology.magnifier.block", "岩性：%s｜岩类：%s｜地质省：%s｜相对深度：%s｜硬度：%s");
        add("msg.geology.magnifier.sample", "样本：%s｜岩类：%s｜硬度：%s");
        add("msg.geology.magnifier.ore", "矿石（%s）｜%s｜Y：%s｜相对深度：%s｜地质省：%s");
        add("msg.geology.magnifier.mineral_sample", "矿物样本（%s）｜%s｜条痕：%s｜硬度：%s｜磁性：%s｜酸反应：%s");
        add("msg.geology.magnifier.trait.untested", "未测试");
        add("msg.geology.magnifier.trait.yes", "是");
        add("msg.geology.magnifier.trait.no", "否");
        add("msg.geology.patchouli_missing", "未安装 Patchouli。请安装 Patchouli 后再阅读野外指南。");

        // 波次 4：岩心钻机与冶炼
        add("block.geology.core_rig", "岩心钻机");
        add("item.geology.drill_head_iron", "铁钻头");
        add("item.geology.drill_head_diamond", "金刚石钻头");
        add("item.geology.core_sample", "岩心柱");
        add("item.geology.barite_powder", "重晶石粉");
        add("item.geology.lead_ingot", "铅锭");
        add("item.geology.zinc_ingot", "锌锭");
        add("item.geology.molybdenum_ingot", "钼锭");
        add("item.geology.silver_ingot", "银锭");
        add("item.geology.sulfur_powder", "硫磺粉");
        add("item.geology.slag", "冶炼渣");
        add("item.geology.identified_sample_vanilla_iron", "已鉴定铁矿样本");
        add("item.geology.identified_sample_vanilla_gold", "已鉴定自然金样本");
        add("item.geology.identified_sample_vanilla_copper", "已鉴定黄铜矿样本");
        add("item.geology.identified_sample_lead_ingot", "已鉴定方铅矿样本");
        add("item.geology.identified_sample_zinc_ingot", "已鉴定闪锌矿样本");
        add("item.geology.identified_sample_molybdenum_ingot", "已鉴定辉钼矿样本");
        add("item.geology.identified_sample_silver_ingot", "已鉴定自然银样本");
        add("item.geology.identified_sample_barite_powder", "已鉴定重晶石样本");
        add("item.geology.identified_sample_collectible", "已鉴定矿物收藏样本");
        add("msg.geology.drill_head.tier", "等级：%s");
        add("msg.geology.drill_head.tier.iron", "铁");
        add("msg.geology.drill_head.tier.diamond", "金刚石");
        add("msg.geology.drill_head.durability", "最大耐久：%s");
        add("msg.geology.core_sample.empty", "空白岩心柱");
        add("msg.geology.core_sample.depth", "总深度：%s 米");
        add("msg.geology.core_sample.has_mineral", "检测到矿脉！");
        add("msg.geology.core_sample.has_cavity", "检测到空腔");
        add("msg.geology.core_sample.source", "来源：（%s，%s）");
        add("msg.geology.core_sample.layers", "地层序列：");
        add("msg.geology.core_sample.layer_cavity", "  空腔 - %s 米");
        add("msg.geology.core_sample.layer_rock", "  %s - %s 米");
        add("msg.geology.core_sample.layer_mineral", " [矿物：%s]");
        add("msg.geology.core_rig.drill_button", "开始下钻");
        add("msg.geology.core_rig.status", "状态");
        add("msg.geology.core_rig.drill_durability", "钻头：%s");
        add("msg.geology.core_rig.fluid", "钻井液：%s");
        add("msg.geology.core_rig.fuel", "燃料：%s");
        add("msg.geology.core_rig.output", "输出槽：%s");
        add("msg.geology.core_rig.estimated_wear", "预估磨损：%s");
        add("msg.geology.core_rig.value_none", "—");
        add("msg.geology.core_rig.fuel_empty", "0/1");
        add("msg.geology.core_rig.fuel_full", "1/1");
        add("msg.geology.core_rig.output_ready", "就绪");
        add("msg.geology.core_rig.output_blocked", "已满");
        add("msg.geology.identified_sample.smeltable", "可冶炼");
        add("msg.geology.identified_sample.output.vanilla_iron", "可冶炼为铁锭");
        add("msg.geology.identified_sample.output.vanilla_gold", "可冶炼为金锭");
        add("msg.geology.identified_sample.output.vanilla_copper", "可冶炼为铜锭");
        add("msg.geology.identified_sample.output.lead_ingot", "可冶炼为铅锭");
        add("msg.geology.identified_sample.output.zinc_ingot", "可冶炼为锌锭");
        add("msg.geology.identified_sample.output.molybdenum_ingot", "可冶炼为钼锭");
        add("msg.geology.identified_sample.output.silver_ingot", "可冶炼为银锭");
        add("msg.geology.identified_sample.collectible", "收藏品");
        add("msg.geology.identified_sample.not_smeltable", "不可冶炼");

        // Patchouli 手册
        add("patchouli.geology.landing", "欢迎阅读《地质学家野外指南》。本手册将帮助你了解地质省、岩石类型与勘探工具。");

        // Cloth Config 配置界面
        add("config.geology.title", "地质学配置");
        add("config.geology.title.readonly", "地质学配置（只读 - 服务端配置）");
        add("config.geology.not_loaded", "服务端配置尚未同步，请稍后重试。");
        add("config.geology.category.general", "通用");
        add("config.geology.category.worldgen", "世界生成");
        add("config.geology.mode", "生成模式");
        add("config.geology.mode.auto", "自动");
        add("config.geology.mode.independent", "独立模式");
        add("config.geology.mode.integrated", "集成模式");
        add("config.geology.cache_chunk_data", "缓存区块数据");
        add("config.geology.replace_stone", "替换石层（全量地层）");
        add("config.geology.replace_vanilla_coal", "替换原版煤");
        add("config.geology.replace_vanilla_ores", "替换原版矿石");
        add("config.geology.compat_mode", "兼容模式");

        // 岩石方块名
        for (RockType rock : RockType.values()) {
            add("block.geology." + rock.getSerializedName(), rock.displayNameZh());
        }

        // 未鉴定矿石方块名
        for (MineralAppearance appearance : MineralAppearance.values()) {
            add("block.geology.unidentified_ore_" + appearance.getSerializedName(),
                    "未鉴定矿石（" + zhAppearance(appearance) + "）");
        }

        // 煤阶矿石方块名
        for (CoalRank rank : CoalRank.values()) {
            add("block.geology.coal_ore_" + rank.getSerializedName(),
                    zhCoalRank(rank) + "矿石");
        }

        // 煤阶物品名
        for (CoalRank rank : CoalRank.values()) {
            add("item.geology." + rank.getSerializedName(), zhCoalRank(rank));
        }

        // 矿物翻译键
        for (MineralType mineral : MineralType.values()) {
            add(mineral.translationKey(), mineral.displayNameZh());
        }

        // 外观原型翻译键
        for (MineralAppearance appearance : MineralAppearance.values()) {
            add(appearance.translationKey(), zhAppearance(appearance));
            add(appearance.descriptionKey(), appearance.description());
        }

        // 煤阶翻译键
        for (CoalRank rank : CoalRank.values()) {
            add(rank.translationKey(), zhCoalRank(rank));
        }

        // 成因类型翻译键
        for (MineralGenesis genesis : MineralGenesis.values()) {
            add(genesis.translationKey(), zhGenesis(genesis));
        }

        // 矿脉形态翻译键
        for (VeinType vein : VeinType.values()) {
            add("veintype.geology." + vein.getSerializedName(), zhVeinType(vein));
        }

        for (RockCategory cat : RockCategory.values()) {
            add(cat.translationKey(), zhCategory(cat));
        }
        for (ProvinceType p : ProvinceType.values()) {
            add(p.translationKey(), zhProvince(p));
        }

        // Jade 集成：插件配置界面显示名称
        add("config.jade.plugin_geology.geology_info", "地质信息");
    }

    private static String zhCategory(RockCategory cat) {
        return switch (cat) {
            case IGNEOUS -> "火成岩";
            case SEDIMENTARY -> "沉积岩";
            case METAMORPHIC -> "变质岩";
        };
    }

    private static String zhProvince(ProvinceType p) {
        return switch (p) {
            case ANCIENT_CRATON -> "古老克拉通";
            case OROGENIC_BELT -> "碰撞造山带";
            case SEDIMENTARY_BASIN -> "沉积盆地";
            case VOLCANIC_PROVINCE -> "火山省";
            case SHIELD -> "地盾";
        };
    }

    private static String zhAppearance(MineralAppearance a) {
        return switch (a) {
            case METALLIC_GOLD -> "金属光泽金色";
            case METALLIC_SILVER -> "金属光泽银色";
            case METALLIC_BLACK -> "金属光泽黑色";
            case MASSIVE_DARK -> "致密暗色";
            case CRYSTAL_CLEAR -> "透明晶体";
            case CRYSTAL_COLORED -> "彩色晶体";
            case EARTHY_RED -> "土状红褐色";
            case VITREOUS_MASSIVE -> "玻璃光泽块状";
            case SALINE -> "盐类玻璃光泽";
            case PLATY_FIBROUS -> "片状/纤维状";
        };
    }

    private static String zhCoalRank(CoalRank rank) {
        return switch (rank) {
            case PEAT -> "泥炭";
            case LIGNITE -> "褐煤";
            case BITUMINOUS -> "烟煤";
            case ANTHRACITE -> "无烟煤";
        };
    }

    private static String zhGenesis(MineralGenesis g) {
        return switch (g) {
            case IGNEOUS -> "岩浆矿床";
            case HYDROTHERMAL -> "热液矿床";
            case SEDIMENTARY -> "沉积矿床";
            case METAMORPHIC -> "变质矿床";
            case WEATHERING -> "风化残积矿床";
            case PEGMATITIC -> "伟晶岩矿床";
        };
    }

    private static String zhVeinType(VeinType v) {
        return switch (v) {
            case DISCRETE -> "离散矿脉";
            case LAYERED -> "层状矿脉";
            case STOCKWORK -> "网脉状矿脉";
        };
    }
}
