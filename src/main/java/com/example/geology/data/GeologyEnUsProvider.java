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
 * 英文语言文件生成。
 * <p>
 * 包含：岩石方块名、矿物方块/物品名、煤阶物品名、工具物品名、创造标签页、
 * 自定义消息、地质省/岩类/矿物/外观/煤阶/成因/矿脉形态翻译键、
 * Patchouli/ClothConfig 界面文本。
 */
public class GeologyEnUsProvider extends LanguageProvider {

    public GeologyEnUsProvider(PackOutput output) {
        super(output, GeologyMod.MOD_ID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.geology", "Geology");
        add("item.geology.geological_hammer", "Geological Hammer");
        add("item.geology.magnifier", "Hand Magnifier");
        add("item.geology.rock_sample", "Rock Sample");
        add("item.geology.rock_sample.named", "%s Sample");
        add("item.geology.geologist_guide", "Geologist's Field Guide");
        add("item.geology.unidentified_mineral_sample", "Unidentified Mineral Sample");
        add("item.geology.geological_almanac", "Geological Almanac");
        add("item.geology.geological_compass", "Geological Compass");
        add("msg.geology.compass.boundary", "Province: %s | Nearest boundary: %s (%s blocks)");
        add("msg.geology.compass.no_boundary", "Province: %s | No boundary within search radius");
        add("block.geology.identification_table", "Identification Table");
        add("msg.geology.almanac.title", "Geological Almanac");
        add("msg.geology.almanac.appearance", "Appearance: %s");
        add("msg.geology.hammer.hit", "Sample: %s (%s) | Province: %s");
        add("msg.geology.hammer.ore", "Mineral sample (%s) — needs identification");
        add("msg.geology.hammer.coal", "Coal sample (%s)");
        add("msg.geology.identification.test.streak", "Streak");
        add("msg.geology.identification.test.hardness", "Hardness");
        add("msg.geology.identification.test.magnetic", "Magnetic");
        add("msg.geology.identification.test.acid", "Acid");
        add("msg.geology.identification.candidates", "Candidates:");
        add("msg.geology.identification.no_sample", "(no sample)");
        add("msg.geology.identification.tested", "Tested traits:");
        add("msg.geology.identification.hint.sample", "Place unidentified mineral sample");
        add("msg.geology.identification.hint.invalid_sample", "Not a sample! Hammer ore to get sample");
        add("msg.geology.identification.hint.reagents", "Add reagent/reference to test");
        add("msg.geology.identification.trait.streak", "Streak: %s");
        add("msg.geology.identification.trait.hardness", "Hardness: %s");
        add("msg.geology.identification.trait.magnetic", "Magnetic: %s");
        add("msg.geology.identification.trait.acid", "Acid: %s");
        add("msg.geology.magnifier.block", "Rock: %s | Class: %s | Province: %s | Rel. depth: %s | Hardness: %s");
        add("msg.geology.magnifier.sample", "Sample: %s | Class: %s | Hardness: %s");
        add("msg.geology.magnifier.ore", "Ore (%s) | %s | Y: %s | Rel. depth: %s | Province: %s");
        add("msg.geology.magnifier.mineral_sample", "Mineral sample (%s) | %s | Streak: %s | Hardness: %s | Magnetic: %s | Acid: %s");
        add("msg.geology.magnifier.trait.untested", "untested");
        add("msg.geology.magnifier.trait.yes", "yes");
        add("msg.geology.magnifier.trait.no", "no");
        add("msg.geology.patchouli_missing", "Patchouli is not installed. Install Patchouli to read the field guide.");

        // 波次 4：岩心钻机与冶炼
        add("block.geology.core_rig", "Core Rig");
        add("item.geology.drill_head_iron", "Iron Drill Head");
        add("item.geology.drill_head_diamond", "Diamond Drill Head");
        add("item.geology.core_sample", "Core Sample");
        add("item.geology.barite_powder", "Barite Powder");
        add("item.geology.lead_ingot", "Lead Ingot");
        add("item.geology.zinc_ingot", "Zinc Ingot");
        add("item.geology.molybdenum_ingot", "Molybdenum Ingot");
        add("item.geology.silver_ingot", "Silver Ingot");
        add("item.geology.sulfur_powder", "Sulfur Powder");
        add("item.geology.slag", "Slag");
        add("item.geology.identified_sample_vanilla_iron", "Identified Iron Ore Sample");
        add("item.geology.identified_sample_vanilla_gold", "Identified Gold Ore Sample");
        add("item.geology.identified_sample_vanilla_copper", "Identified Copper Ore Sample");
        add("item.geology.identified_sample_lead_ingot", "Identified Lead Ore Sample");
        add("item.geology.identified_sample_zinc_ingot", "Identified Zinc Ore Sample");
        add("item.geology.identified_sample_molybdenum_ingot", "Identified Molybdenum Ore Sample");
        add("item.geology.identified_sample_silver_ingot", "Identified Silver Ore Sample");
        add("item.geology.identified_sample_barite_powder", "Identified Barite Sample");
        add("item.geology.identified_sample_collectible", "Identified Collectible Sample");
        add("msg.geology.drill_head.tier", "Tier: %s");
        add("msg.geology.drill_head.tier.iron", "Iron");
        add("msg.geology.drill_head.tier.diamond", "Diamond");
        add("msg.geology.drill_head.durability", "Max Durability: %s");
        add("msg.geology.core_sample.empty", "Empty core sample");
        add("msg.geology.core_sample.depth", "Total depth: %sm");
        add("msg.geology.core_sample.has_mineral", "Mineral vein detected!");
        add("msg.geology.core_sample.has_cavity", "Cavity detected");
        add("msg.geology.core_sample.source", "Source: (%s, %s)");
        add("msg.geology.core_sample.layers", "Strata sequence:");
        add("msg.geology.core_sample.layer_cavity", "  Cavity - %sm");
        add("msg.geology.core_sample.layer_rock", "  %s - %sm");
        add("msg.geology.core_sample.layer_mineral", " [Mineral: %s]");
        add("msg.geology.core_rig.drill_button", "Start Drilling");
        add("msg.geology.core_rig.status", "Status");
        add("msg.geology.core_rig.drill_durability", "Drill: %s");
        add("msg.geology.core_rig.fluid", "Fluid: %s");
        add("msg.geology.core_rig.fuel", "Fuel: %s");
        add("msg.geology.core_rig.output", "Output: %s");
        add("msg.geology.core_rig.estimated_wear", "Est. wear: %s");
        add("msg.geology.core_rig.value_none", "—");
        add("msg.geology.core_rig.fuel_empty", "0/1");
        add("msg.geology.core_rig.fuel_full", "1/1");
        add("msg.geology.core_rig.output_ready", "Ready");
        add("msg.geology.core_rig.output_blocked", "Full");
        add("msg.geology.identified_sample.smeltable", "Smeltable");
        add("msg.geology.identified_sample.output.vanilla_iron", "Smelts into Iron Ingot");
        add("msg.geology.identified_sample.output.vanilla_gold", "Smelts into Gold Ingot");
        add("msg.geology.identified_sample.output.vanilla_copper", "Smelts into Copper Ingot");
        add("msg.geology.identified_sample.output.lead_ingot", "Smelts into Lead Ingot");
        add("msg.geology.identified_sample.output.zinc_ingot", "Smelts into Zinc Ingot");
        add("msg.geology.identified_sample.output.molybdenum_ingot", "Smelts into Molybdenum Ingot");
        add("msg.geology.identified_sample.output.silver_ingot", "Smelts into Silver Ingot");
        add("msg.geology.identified_sample.collectible", "Collectible");
        add("msg.geology.identified_sample.not_smeltable", "Not smeltable");

        // 岩石方块名
        for (RockType rock : RockType.values()) {
            add("block.geology." + rock.getSerializedName(), rock.displayName());
        }

        // 未鉴定矿石方块名（按外观原型命名）
        for (MineralAppearance appearance : MineralAppearance.values()) {
            add("block.geology.unidentified_ore_" + appearance.getSerializedName(),
                    "Unidentified Ore (" + enAppearance(appearance) + ")");
        }

        // 煤阶矿石方块名
        for (CoalRank rank : CoalRank.values()) {
            add("block.geology.coal_ore_" + rank.getSerializedName(),
                    enCoalRank(rank) + " Coal Ore");
        }

        // 煤阶物品名
        for (CoalRank rank : CoalRank.values()) {
            add("item.geology." + rank.getSerializedName(),
                    enCoalRank(rank) + " Coal");
        }

        // 矿物翻译键（用于鉴定结果显示）
        for (MineralType mineral : MineralType.values()) {
            add(mineral.translationKey(), mineral.displayName());
        }

        // 外观原型翻译键
        for (MineralAppearance appearance : MineralAppearance.values()) {
            add(appearance.translationKey(), enAppearance(appearance));
            add(appearance.descriptionKey(), appearance.description());
        }

        // 煤阶翻译键
        for (CoalRank rank : CoalRank.values()) {
            add(rank.translationKey(), enCoalRank(rank));
        }

        // 成因类型翻译键
        for (MineralGenesis genesis : MineralGenesis.values()) {
            add(genesis.translationKey(), enGenesis(genesis));
        }

        // 矿脉形态翻译键
        for (VeinType vein : VeinType.values()) {
            add("veintype.geology." + vein.getSerializedName(), enVeinType(vein));
        }

        // Patchouli 手册
        add("patchouli.geology.landing", "Welcome to the Geologist's Field Guide. This manual will help you understand geological provinces, rock types, and exploration tools.");

        // Cloth Config 配置界面
        add("config.geology.title", "Geology Configuration");
        add("config.geology.title.readonly", "Geology Configuration (Read-only - Server Config)");
        add("config.geology.not_loaded", "Server config is not yet synchronized. Please retry in a moment.");
        add("config.geology.category.general", "General");
        add("config.geology.category.worldgen", "World Generation");
        add("config.geology.mode", "Generation Mode");
        add("config.geology.mode.auto", "Auto");
        add("config.geology.mode.independent", "Independent");
        add("config.geology.mode.integrated", "Integrated");
        add("config.geology.cache_chunk_data", "Cache Chunk Data");
        add("config.geology.replace_stone", "Replace Stone (Full Strata)");
        add("config.geology.replace_vanilla_coal", "Replace Vanilla Coal");
        add("config.geology.replace_vanilla_ores", "Replace Vanilla Ores");
        add("config.geology.compat_mode", "Compatibility Mode");

        // Jade 集成：插件配置界面显示名称
        add("config.jade.plugin_geology.geology_info", "Geology Info");

        for (RockCategory cat : RockCategory.values()) {
            add(cat.translationKey(), enCategory(cat));
        }
        for (ProvinceType p : ProvinceType.values()) {
            add(p.translationKey(), enProvince(p));
        }
    }

    private static String enCategory(RockCategory cat) {
        return switch (cat) {
            case IGNEOUS -> "Igneous";
            case SEDIMENTARY -> "Sedimentary";
            case METAMORPHIC -> "Metamorphic";
        };
    }

    private static String enProvince(ProvinceType p) {
        return switch (p) {
            case ANCIENT_CRATON -> "Ancient Craton";
            case OROGENIC_BELT -> "Orogenic Belt";
            case SEDIMENTARY_BASIN -> "Sedimentary Basin";
            case VOLCANIC_PROVINCE -> "Volcanic Province";
            case SHIELD -> "Shield";
        };
    }

    private static String enAppearance(MineralAppearance a) {
        return switch (a) {
            case METALLIC_GOLD -> "Metallic Gold";
            case METALLIC_SILVER -> "Metallic Silver";
            case METALLIC_BLACK -> "Metallic Black";
            case MASSIVE_DARK -> "Massive Dark";
            case CRYSTAL_CLEAR -> "Crystal Clear";
            case CRYSTAL_COLORED -> "Crystal Colored";
            case EARTHY_RED -> "Earthy Red";
            case VITREOUS_MASSIVE -> "Vitreous Massive";
            case SALINE -> "Saline";
            case PLATY_FIBROUS -> "Platy/Fibrous";
        };
    }

    private static String enCoalRank(CoalRank rank) {
        return switch (rank) {
            case PEAT -> "Peat";
            case LIGNITE -> "Lignite";
            case BITUMINOUS -> "Bituminous";
            case ANTHRACITE -> "Anthracite";
        };
    }

    private static String enGenesis(MineralGenesis g) {
        return switch (g) {
            case IGNEOUS -> "Igneous";
            case HYDROTHERMAL -> "Hydrothermal";
            case SEDIMENTARY -> "Sedimentary";
            case METAMORPHIC -> "Metamorphic";
            case WEATHERING -> "Weathering";
            case PEGMATITIC -> "Pegmatitic";
        };
    }

    private static String enVeinType(VeinType v) {
        return switch (v) {
            case DISCRETE -> "Discrete Vein";
            case LAYERED -> "Layered Vein";
            case STOCKWORK -> "Stockwork Vein";
        };
    }
}
