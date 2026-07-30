# 更新日志

## 2026-07-06 (第 3 轮)

### 修复
- **GUI 文字偏下**：三个 Screen 的标题与右侧描述文字超出标题栏色条。
  - `CoreRigScreen` / `IdentificationScreen` / `AlmanacScreen` 的 `titleLabelY` 从 6 改为 2。
  - `CoreRigScreen` 状态面板 Y 从 18 改为 13,行高压缩。
  - `IdentificationScreen` 属性面板 Y 从 18 改为 13,候选列表 Y 从 58 改为 54。
- **候选矿物空样本提示**：无样本时"候选矿物：(无样本)"冗余显示。
  - `IdentificationScreen.renderCandidates` 改为 `candidates.isEmpty()` 时整体不显示(包括标题)。
- **鉴定结果不唯一**：`GARNET` 与 `TOURMALINE` 鉴定特征完全相同(同为 CRYSTAL_COLORED + 白色条痕 + 7.0F 硬度 + 无磁性 + 无酸反应),四个测试做完后候选列表仍显示两个,样本无法转换。
  - `MineralType.GARNET` 硬度从 7.0F 改为 6.5F(地质学合理:石榴石硬度范围 6.5-7.5)。
  - CRYSTAL_COLORED 组四种矿物硬度各不相同:APATITE 5.0 / GARNET 6.5 / TOURMALINE 7.0 / ZIRCON 7.5。

### 重构
- **DRY 整合:ROCK_NOISE_SEED_MASK 单一来源**:`0xC2B2AE3D27D4EB4FL` 在 `GeologyGenerator` 和 `IntegratedGeologyProvider` 重复定义,目的是让独立/集成模式 rockNoise 一致。
  - 提取到 `RockStrata.ROCK_NOISE_SEED_MASK` 作为 public 常量,两处引用改为 `RockStrata.ROCK_NOISE_SEED_MASK`。
  - 删除 `IntegratedGeologyProvider` 的本地重复常量。

### 检查
- **RTF 最新源码重合检查**:从 GitHub `Knowledge-book-QwQ/ReTerraForged` 1.21.1 分支获取最新源码,与地质学模组全面对比。
  - **StrataRule**:RTF 内置地层规则,与 `RockStrataFeature` 职责重叠 → 已由 `GeologyRTFStrataMixin` 拦截 `StrataRule$Source.tryApply` 返回 null 禁用,职责归地质学 feature 阶段。Mixin 拦截点经源码验证正确(`tryApply` 返回 `@Nullable BlockState`,null 是合法契约)。
  - **Cell**:RTF 源码无 geology 字段,4 个 geology 字段(`geologyProvinceId`/`geologyDisturbance`/`geologyGradient`/`geologyStrataSeed`)仍由补丁 jar 注入,必需。
  - **GeneratorContext / RTFRandomState**:RTF 源码接口与 `IntegratedGeologyProvider` / `RTFAccess` 调用一致,无重合。
  - **feature 目录**:RTF 的 BushFeature/ErodeFeature 等为地表装饰,与 `RockStrataFeature`(地层替换)/`MineralVeinFeature`(矿脉)职责不同,无重合。
  - **rock 标签**:RTF 有 `data/reterraforged/tags/block/rock.json`,地质学用原版 `STONE_ORE_REPLACEABLES`,命名空间不同,无重合。
  - **结论**:无真正重合,三层隔离架构(Mixin + Populator + Provider)清晰。

## 2026-07-06

### 修复
- **GUI 文字错乱**：三个 Screen 的 `imageHeight=190` 非 vanilla 标准，导致底部 24px 空白、文字与分隔线重叠。
  - `IdentificationScreen` / `CoreRigScreen` 改为标准 `imageHeight=166`。
  - `CoreRigScreen` 移除槽位标签（与顶行分隔线 y=36 重叠，且信息已在状态面板显示）。
  - `IdentificationScreen` 移除独立提示行（与 `inventoryLabelY=72` 重叠），将提示整合到右侧面板首行。
  - `GeologyScreenUtils.PANEL_HEIGHT` 从 80 调整为 64，适配新高度。
- **AlmanacScreen 网格溢出**：3 列 × 80px = 240px 超过 `imageWidth=220`，矿物名被截断。
  - 尺寸改为 `255×169`（8 + 240 + 7 = 255），网格完整显示。
  - 新增渐变背景 + 标题栏 + 行列分隔线，视觉风格与其他 GUI 统一。

## 2026-06-30

### 新增
- **未鉴定矿石敲击次数系统**：地质锤敲未鉴定矿石现在有 3 次上限，敲满 3 次后矿石方块消失。
  - `UnidentifiedOreBlockEntity` 新增 `hitsRemaining` 字段（默认 `MAX_HITS=3`），持久化到 NBT。
  - 新增 `consumeHit()` 方法：递减剩余次数，归零返回 `false`。
  - 新增 `hitsRemaining()` getter。

### 修改
- **地质锤敲矿石逻辑**（`GeologicalHammerItem.handleUnidentifiedOre`）：
  - 掉落样本后调用 `oreBE.consumeHit()`，归零则 `level.removeBlock(pos, false)` 移除方块。
  - 移除方块时不产生额外掉落物（样本已由上方掉落）。
  - 煤阶矿石与岩石方块的逻辑不变（仍为无限敲击）。

### 删除
- **鉴定完成聊天提示**：删除"已鉴定为：%s！"消息。
  - `IdentificationTableBlockEntity` 移除 `player.sendSystemMessage(...)` 调用，保留图鉴记录逻辑。
  - `GeologyZhCnProvider` / `GeologyEnUsProvider` 移除 `msg.geology.identification.complete` 翻译键。

### 修复
- **Mixin StrataRule 签名崩溃**：首次 `runClient` 因拦截 `StrataRule.apply` 返回 null 触发 `ImmutableList` NPE。
  - 改为拦截 `StrataRule$Source.tryApply(int,int,int)` 返回 `BlockState` null（合法契约）。
  - 重试后玩家成功进入世界，区块生成正常。
- **GeologyProviders import 路径**：`WorldGenLevel` import 多了 `.levelgen`，修正为 `net.minecraft.world.level.WorldGenLevel`。
- **RTF jar 旧版缺类**：补丁编译 5 个 RTF 源文件（Cell/GeologyHooks/PopulateCellEvent/TileGenerator/GeneratorContext）注入旧 jar。

## 2026-06-30 (M1 阶段)

### 完成
- RTF 集成架构：GeologyPopulator 注册回调填充 Cell geology 字段。
- IntegratedGeologyProvider 从 RTF cache 读预算 Cell。
- GeologyProviders 三重载：`get(ServerLevel)` / `get(WorldGenLevel)` / `get(long)`。
- 全部 7 处调用方迁移到统一 `get(Level)` + `GeologyUtil.surfaceY`。
- Mixin `GeologyRTFStrataMixin` 禁用 RTF 地层替换。
- `runData` 生成 228 个文件，`runClient` 进入世界无崩溃。
