# 工作内容与计划

## 已完成工作

### 1. GUI 文字偏下修复 + 鉴定系统修复 + DRY 整合 + RTF 源码检查（第 3 轮）
- **GUI 文字偏下**:三个 Screen 的 `titleLabelY` 6→2,右侧面板 Y 18→13,行高压缩。
- **候选矿物空样本提示删除**:`IdentificationScreen.renderCandidates` 无样本时整体不显示。
- **鉴定结果不唯一修复**:`GARNET` 硬度 7.0→6.5,与 `TOURMALINE`(7.0)区分。
- **DRY 整合**:`ROCK_NOISE_SEED_MASK` 提取到 `RockStrata` 作为单一来源,删除 `IntegratedGeologyProvider` 本地重复。
- **RTF 最新源码重合检查**:从 `Knowledge-book-QwQ/ReTerraForged` 1.21.1 分支获取 StrataRule/Cell/GeneratorContext/RTFRandomState 源码,与地质学模组全面对比。
  - Mixin 拦截点(`StrataRule$Source.tryApply` 返回 null)经源码验证正确。
  - RTF 源码无 geology 字段,补丁 jar 注入仍必需。
  - 无真正重合,三层隔离架构(Mixin + Populator + Provider)清晰。

### 2. GUI 布局修复（第 2 轮）
- **问题**：用户反馈"ui大小不同文字错乱"。
- **根因**：
  - `IdentificationScreen` / `CoreRigScreen` 的 `imageHeight=190`（非 vanilla 标准 166），底部 24px 空白。
  - `CoreRigScreen` 槽位标签 y=36 与顶行分隔线 y=36 重叠。
  - `IdentificationScreen` 提示文字 y=72 与 vanilla "物品栏" 标签 y=72 重叠。
  - `AlmanacScreen` 网格 3×80=240px 超过 `imageWidth=220`，矿物名被截断；无背景样式。
- **修复**：
  - 两个容器 Screen 改为标准 `imageHeight=166`。
  - `CoreRigScreen` 移除槽位标签（信息已在状态面板显示）。
  - `IdentificationScreen` 将提示整合到右侧面板首行（样本为空/无效时显示提示，否则显示已测试属性）。
  - `GeologyScreenUtils.PANEL_HEIGHT` 从 80→64，新增 `TITLE_BAR_HEIGHT=11`。
  - `AlmanacScreen` 尺寸改为 `255×169`，新增渐变背景 + 标题栏 + 行列分隔线。
- **编译**：BUILD SUCCESSFUL。

### 2. RTF 集成编译验证（Task 6）
- **Mixin 签名修复**：查证 RTF `StrataRule.apply` 实际签名，发现返回 private 内部类 `Source`。改用 `CallbackInfoReturnable<Object>` 绕过 protected 访问限制。
- **RTF jar 补丁构建**：GitHub 直连 SSL 失败，采用"补丁 jar"方案——从 GitHub MCP 逐文件下载 5 个修改的 RTF 源文件，用 `gradlew printCompileClasspath` 导出 classpath，`javac --release 21` 编译后 `jar uf` 注入旧 jar。
- **GeologyProviders import 修正**：`WorldGenLevel` 路径多了 `.levelgen`，修正后级联的 6 个"引用不明确"错误全部消失。
- **结果**：`compileJava` + `runData`（228 文件）均 BUILD SUCCESSFUL。

### 2. runClient 游戏内实测
- **首次崩溃修复**：Mixin 拦截 `StrataRule.apply` 返回 null → `ImmutableList.Builder.add` NPE。根因：`apply` 返回 `SurfaceRule` 是非 null 契约。修复为拦截 `StrataRule$Source.tryApply` 返回 `BlockState` null（合法契约）。
- **实测结果**：客户端成功进入世界，RTF 集成日志确认（`cell populator registered`），区块生成正常，无 ERROR/Exception。

### 3. 删除鉴定完成聊天提示
- `IdentificationTableBlockEntity` 移除 `sendSystemMessage`，保留图鉴记录。
- 中英文语言文件移除 `msg.geology.identification.complete` 键。

### 4. 未鉴定矿石敲击次数系统
- **需求**：地质锤敲未鉴定矿石 3 次后矿石方块消失（原先无限敲击）。
- **实现**：
  - `UnidentifiedOreBlockEntity` 新增 `hitsRemaining`（默认 `MAX_HITS=3`）+ `consumeHit()` + NBT 持久化。
  - `GeologicalHammerItem.handleUnidentifiedOre` 掉落样本后调用 `consumeHit()`，归零则 `level.removeBlock`。
  - 煤阶矿石与岩石方块不受影响（仍无限敲击）。
- **编译**：BUILD SUCCESSFUL。

## 当前状态
- 代码已编译通过，待 `runClient` 游戏内验证敲击次数机制。
- RTF 集成完整启用，Populator 注册成功，地层替换由地质学 feature 阶段接管。

## 后续计划

### 短期（本轮待验证）
- [ ] `runClient` 验证：地质锤敲未鉴定矿石 3 次后方块消失。
- [ ] 验证：鉴定台完成 4 项测试后不再发聊天消息。

### 中期
- [ ] 洞穴系统生成（参考 JJThunder/Tectonic/Subterranean Wilderness）。
- [ ] 限高扩展：支持 -1024~2032 高度范围。
- [ ] RTF jar 正式构建：从 `feature/geology-backend` 分支完整构建替换补丁 jar（当前为补丁注入）。

### 架构待优化
- [ ] `GeologyAccessor.from()` 的 `lookup.applyCell(x, z)` 签名 bug 修复（当前地质学侧绕过此接口）。
- [ ] 物品模型 JSON 缺失 WARN 排查（2 个非关键警告）。
