# Spec-Kit 技能完整参考指南

> 本文档系统整理了 Spec-Kit 全部 **22 个技能**的详细信息（含 2 个别名，共 24 个命令），包括技能名称、功能描述、适用场景、核心特性及完整工作流程。
>
> Spec-Kit 是一个**规范驱动开发（Spec-Driven Development）**框架，以工程化的方式管理从需求到实现的完整生命周期。

---

## 目录

### 一、核心工作流技能（14 个）

1. [speckit-constitution — 项目宪法](#1-speckit-constitution--项目宪法)
2. [speckit-specify — 功能规格](#2-speckit-specify--功能规格)
3. [speckit-clarify — 澄清规格](#3-speckit-clarify--澄清规格)
4. [speckit-plan — 实现规划](#4-speckit-plan--实现规划)
5. [speckit-tasks — 任务生成](#5-speckit-tasks--任务生成)
6. [speckit-implement — 任务执行](#6-speckit-implement--任务执行)
7. [speckit-analyze — 交叉分析](#7-speckit-analyze--交叉分析)
8. [speckit-checklist — 检查清单](#8-speckit-checklist--检查清单)
9. [speckit-taskstoissues — 任务转议题](#9-speckit-taskstoissues--任务转议题)
10. [speckit-git-initialize — Git 初始化](#10-speckit-git-initialize--git-初始化)
11. [speckit-git-feature — 功能分支](#11-speckit-git-feature--功能分支)
12. [speckit-git-validate — 分支验证](#12-speckit-git-validate--分支验证)
13. [speckit-git-remote — 远程检测](#13-speckit-git-remote--远程检测)
14. [speckit-git-commit — 自动提交](#14-speckit-git-commit--自动提交)

### 二、Sync 扩展技能（5 个）

15. [speckit-sync-analyze — 漂移分析](#15-speckit-sync-analyze--漂移分析)
16. [speckit-sync-propose — 解决提议](#16-speckit-sync-propose--解决提议)
17. [speckit-sync-apply — 应用解决](#17-speckit-sync-apply--应用解决)
18. [speckit-sync-conflicts — 冲突检测](#18-speckit-sync-conflicts--冲突检测)
19. [speckit-sync-backfill — 回填规格](#19-speckit-sync-backfill--回填规格)

### 三、Iterate 扩展技能（2 个）

20. [speckit-iterate-define — 迭代定义](#20-speckit-iterate-define--迭代定义)
21. [speckit-iterate-apply — 迭代应用](#21-speckit-iterate-apply--迭代应用)

### 四、Markitdown 扩展技能（1 个）

22. [speckit-markitdown-convert — 文档转换](#22-speckit-markitdown-convert--文档转换)

### 五、Red Team 扩展技能（2 个）

23. [speckit-red-team-gate — 红队门禁](#23-speckit-red-team-gate--红队门禁)
24. [speckit-red-team-run — 红队运行](#24-speckit-red-team-run--红队运行)

### 六、别名

| 别名 | 指向 |
|------|------|
| `/speckit-drift` | `/speckit-sync-analyze` |
| `/speckit-convert` | `/speckit-markitdown-convert` |

---

## 1. speckit-constitution — 项目宪法

### 功能描述

**主要作用：** 创建或更新项目宪法（Constitution），定义项目的核心治理原则和非协商性规则。宪法是整个 Spec-Kit 体系的最高准则，所有规格、计划、任务都必须符合宪法约束。

**适用场景：**
- 项目初始化时设定治理原则
- 需要新增、修改或删除项目原则时
- 需要更新版本、批准日期、修订日期时

**核心特性：**
- 语义化版本管理：MAJOR（移除/重定义原则）、MINOR（新增原则）、PATCH（措辞修正）
- 一致性传播：更新宪法后自动检查所有模板、命令文件、文档是否与新原则一致
- Sync Impact Report：记录版本变更、修改的原则、新增/删除的章节
- 扩展钩子支持：`before_constitution` / `after_constitution` 钩子
- 模板驱动：基于 `.specify/templates/constitution-template.md` 运行

### 完整工作流程

**步骤 1：加载现有宪法**
- 读取 `.specify/memory/constitution.md`
- 识别所有 `[PLACEHOLDER_TOKEN]` 占位符

**步骤 2：收集/推导值**
- 用户输入提供 → 使用
- 从仓库上下文推断（README、文档、历史版本）
- 治理日期：`RATIFICATION_DATE` 原始采纳日期，`LAST_AMENDED_DATE` 有变更时为今天
- `CONSTITUTION_VERSION` 按语义化版本规则递增

**步骤 3：起草更新内容**
- 替换所有占位符为具体文本
- 保留标题层级；移除已替换的注释
- 每条原则：简洁名称 + 不可协商的规则 + 明确理由
- 治理章节：修订程序、版本策略、合规审查期望

**步骤 4：一致性传播检查**
- `plan-template.md` — 宪法检查是否与新原则对齐
- `spec-template.md` — 范围/需求对齐
- `tasks-template.md` — 任务分类反映新原则
- `.specify/templates/commands/*.md` — 无过时的智能体特定引用
- 运行时指南文档 — 更新原则引用

**步骤 5：生成 Sync Impact Report**
- 版本变更：old → new
- 修改的原则列表
- 新增/删除的章节
- 模板更新状态（✅ / ⚠）
- 后续 TODO

**步骤 6：验证**
- 无未解释的括号令牌残留
- 版本行与报告匹配
- 日期为 ISO 格式
- 原则陈述可声明、可测试、无模糊语言

**步骤 7：写入文件并输出总结**
- 写入 `.specify/memory/constitution.md`
- 输出：新版本、版本递增理由、手动跟进文件、建议 commit message

**输出结果：** 更新后的宪法文件、Sync Impact Report、一致性传播状态。

---

## 2. speckit-specify — 功能规格

### 功能描述

**主要作用：** 从自然语言功能描述创建或更新功能规格说明（spec.md）。这是 Spec-Kit 工作流的**第一步**，将人类意图转化为结构化的、可测试的需求文档。

**适用场景：**
- 新功能开始时，创建功能规格
- 需要从需求描述中提取结构化规格

**核心特性：**
- 自动生成短名称（2-4 词）和编号目录
- 分支创建集成（通过 `before_specify` 钩子）
- 编号策略：sequential（`003-`）或 timestamp（`20260319-143022-`）
- 规格质量验证：自动生成检查清单并验证
- 智能推断：最多 3 个 `[NEEDS CLARIFICATION]` 标记，其余合理推断
- 澄清优先级：范围 > 安全/隐私 > 用户体验 > 技术细节
- 非技术语言：面向业务干系人，禁止实现细节
- 扩展钩子支持：`before_specify` / `after_specify`

### 完整工作流程

**步骤 1：生成短名称**
- 分析描述提取关键词
- 2-4 词动作-名词格式

**步骤 2：分支创建（可选，通过钩子）**
- 如果 `before_specify` 钩子运行，会创建/切换到 git 分支

**步骤 3：创建规格目录**
- 解析 `SPECIFY_FEATURE_DIRECTORY` 路径
- 检查 `.specify/init-options.json` 确定编号方式
- 创建 `specs/<prefix>-<short-name>/` 目录
- 复制模板并持久化到 `.specify/feature.json`

**步骤 4：填充规格内容**
- 解析用户描述 → 提取参与者、动作、数据、约束
- 对不明确的方面：推断合理默认值（最多 3 个标记需要澄清）
- 填充用户场景、功能需求（可测试）、成功标准（可度量、技术无关）
- 识别关键实体（如涉及数据）

**步骤 5：规格质量验证**
- 生成 `checklists/requirements.md` 检查清单
- 对照检查清单逐项验证：内容质量、需求完整性、功能就绪度
- 如果 `[NEEDS CLARIFICATION]` 标记残留：逐一询问用户（最多 3 个）
- 最多迭代 3 次修复

**步骤 6：报告完成**
- 输出目录路径、文件路径、检查清单结果、下一阶段建议

**输出结果：** `specs/<NNN-name>/spec.md`、`checklists/requirements.md`。

---

## 3. speckit-clarify — 澄清规格

### 功能描述

**主要作用：** 识别功能规格中的不明确和缺失决策点，通过**最多 5 个高度聚焦的问题**逐一澄清，将答案直接编码回规格文件。

**适用场景：**
- `/speckit-specify` 完成后、`/speckit-plan` 之前
- 规格中存在模糊需求或缺失关键信息时

**核心特性：**
- 结构化歧义扫描：按 12 个分类体系检查覆盖度（功能范围、数据模型、UX 流程、非功能属性、集成依赖、边缘情况等）
- 最多 5 个问题：每题可多选（2-5 选项）或短回答（≤5 词）
- 逐题交互：一次一个，不提前透露后续问题
- 增量更新：每题接受后立即写回规格文件
- 推荐机制：每题给出推荐选项并附理由
- 覆盖度摘要：完成后输出各分类状态（Resolved / Deferred / Clear / Outstanding）
- 扩展钩子支持：`before_clarify` / `after_clarify`

### 完整工作流程

**步骤 1：初始化**
- 运行 `check-prerequisites.ps1 -Json -PathsOnly` 获取 `FEATURE_DIR`、`FEATURE_SPEC`

**步骤 2：结构化歧义与覆盖度扫描**
- 按 12 个分类体系检查：功能范围、数据模型、UX 流程、非功能质量、集成依赖、边缘情况、约束与权衡、术语一致性、完成信号等
- 每个分类标记状态：Clear / Partial / Missing

**步骤 3：生成优先队列**
- 筛选候选问题（最多 5 个），按 Impact × Uncertainty 排序
- 排除已回答、琐碎偏好或计划层面的问题

**步骤 4：逐题交互循环**
- 每次一个，推荐选项置顶、附理由
- 支持 "yes" / "recommended" / "suggested" 快捷接受
- 验证回答有效性后记录

**步骤 5：每答即集成**
- 在 `## Clarifications` → `### Session YYYY-MM-DD` 下追加 `- Q: ... → A: ...`
- 立即将澄清应用到对应章节（需求、数据模型、边缘情况等）
- 每题后保存文件（原子覆写）

**步骤 6：验证与报告**
- 验证：每答一个要点、≤5 题、无矛盾、MD 结构有效
- 输出：问题数、更新章节、覆盖度摘要表、建议下一步

**输出结果：** 更新的 `spec.md`（含澄清记录）、覆盖度摘要表。

---

## 4. speckit-plan — 实现规划

### 功能描述

**主要作用：** 执行实现规划工作流，使用计划模板生成设计产物。将需求规格转化为技术架构、数据模型、接口合约和实施计划。

**适用场景：**
- `/speckit-clarify` 完成后（规格就绪）
- 需要将业务需求转化为技术设计时

**核心特性：**
- 两阶段流程：Phase 0（研究与未知解决）→ Phase 1（设计与合约）
- 自动生成多种产物：research.md、data-model.md、contracts/、quickstart.md
- 宪法合规检查：Phase 0 前和 Phase 1 后各检查一次
- 门禁机制：宪法违规未合理解释则 ERROR 中断
- 智能体上下文更新：自动更新 `.trae/rules/project_rules.md` 中的 SpecKit 引用
- 扩展钩子支持：`before_plan` / `after_plan`

### 完整工作流程

**步骤 1：Setup**
- 运行 `setup-plan.ps1 -Json` 获取路径变量
- 读取 `FEATURE_SPEC` 和 `.specify/memory/constitution.md`

**Phase 0：大纲与研究**

1. **提取未知项** — 从 Technical Context 中识别 NEEDS CLARIFICATION、依赖、集成
2. **派遣研究代理** — 每个未知项一个独立的搜索研究任务
3. **整合发现** — 生成 `research.md`：Decision / Rationale / Alternatives considered

**Phase 1：设计与合约**

1. **数据模型** — 从规格提取实体 → `data-model.md`（实体名、字段、关系、校验规则、状态转换）
2. **接口合约** — 如项目有外部接口，定义合约 → `/contracts/`
3. **智能体上下文更新** — 更新项目规则文件中的计划引用
4. **宪法复检** — Post-design 宪法合规验证

**步骤 2：报告**
- 输出：分支、plan.md 路径、生成产物清单

**输出结果：** `plan.md`、`research.md`、`data-model.md`、`contracts/`、`quickstart.md`。

---

## 5. speckit-tasks — 任务生成

### 功能描述

**主要作用：** 基于可用的设计产物生成可执行的、按依赖关系排序的 `tasks.md`。任务按用户故事组织，支持独立实现和测试。

**适用场景：**
- `/speckit-plan` 完成后
- 需要将设计转化为可执行的任务清单时

**核心特性：**
- 按用户故事组织：每个故事独立一个 Phase（Phase 3+）
- 严格的 Checkbox 格式：`- [ ] [TaskID] [P?] [Story?] Description with file path`
- Phase 结构：Setup → Foundational → User Stories → Polish
- [P] 并行标记：无依赖关系的任务可并行执行
- MVP 优先：默认建议 Phase 1 + Phase 2 + US1 作为 MVP 范围
- 独立测试标准：每个用户故事定义独立测试方式
- 扩展钩子支持：`before_tasks` / `after_tasks`

### 完整工作流程

**步骤 1：Setup**
- 运行 `check-prerequisites.ps1 -Json` 获取 `FEATURE_DIR` 和可用文档

**步骤 2：加载设计文档**
- 必需：plan.md、spec.md
- 可选：data-model.md、contracts/、research.md、quickstart.md

**步骤 3：执行任务生成**
- 从 spec.md 提取用户故事及优先级
- 映射实体 → 用户故事、合约 → 用户故事
- 生成依赖图、并行执行示例
- 验证任务完整性

**步骤 4：生成 tasks.md**
- Phase 1：Setup（项目初始化）
- Phase 2：Foundational（所有用户故事的前置条件）
- Phase 3+：每个用户故事一个 Phase
- Final Phase：润色与横切关注点
- Dependencies 章节、Parallel 示例、Implementation Strategy

**步骤 5：报告**
- 总任务数、每故事任务数、并行机会、MVP 范围建议

**输出结果：** `tasks.md`（含依赖图和执行策略）。

---

## 6. speckit-implement — 任务执行

### 功能描述

**主要作用：** 按 `tasks.md` 中定义的任务逐阶段执行实现。自动检测项目设置、创建忽略文件、追踪进度并验证完成。

**适用场景：**
- `/speckit-tasks` 完成后
- 需要按任务清单逐步实现功能时

**核心特性：**
- Phase-by-phase 执行：顺序完成每个阶段
- 自动项目设置验证：检测技术栈并创建/验证 `.gitignore`、`.dockerignore` 等
- 检查清单门禁：如有未完成检查清单，询问是否继续
- 进度追踪：每任务完成后报告并标记 `[X]`
- TDD 支持：测试任务在实现任务之前执行
- 错误处理：非并行任务失败则停止；并行任务 [P] 允许部分失败
- 扩展钩子支持：`before_implement` / `after_implement`

### 完整工作流程

**步骤 1：Setup**
- 运行 `check-prerequisites.ps1 -Json -RequireTasks -IncludeTasks`
- 解析 `FEATURE_DIR` 和 `AVAILABLE_DOCS`

**步骤 2：检查清单状态**
- 扫描 `checklists/` 目录
- 如有未完成项 → 生成状态表 → 询问用户是否继续

**步骤 3：加载实现上下文**
- tasks.md、plan.md
- 可选：data-model.md、contracts/、research.md、quickstart.md

**步骤 4：项目设置验证**
- 检测 Git/Docker/ESLint/Prettier 等工具
- 自动创建或验证对应的 ignore 文件
- 按技术栈补充通用忽略模式

**步骤 5：解析任务结构**
- 提取 Phases、任务依赖、[P] 标记、文件路径

**步骤 6：执行实现**
- Phase-by-phase：Setup → Tests（如有）→ Core → Integration → Polish
- 顺序任务按序执行；并行任务可并发
- 文件协调：影响同一文件的必须顺序执行

**步骤 7：完成验证**
- 验证所有任务完成
- 检查实现是否匹配规格
- 确认测试通过

**输出结果：** 所有任务标记完成、功能实现完成。

---

## 7. speckit-analyze — 交叉分析

### 功能描述

**主要作用：** 在 `tasks.md` 生成后，对 spec.md、plan.md、tasks.md 三个核心产物进行**只读**的跨产物一致性和质量分析。识别重复、歧义、覆盖缺口和宪法冲突。

**适用场景：**
- `/speckit-tasks` 完成后、`/speckit-implement` 前
- 确保三个核心产物在实现前对齐

**核心特性：**
- **严格只读**：不修改任何文件，仅输出分析报告
- 六类检测：重复、歧义、不完整、宪法对齐、覆盖缺口、不一致
- 严重度分级：CRITICAL > HIGH > MEDIUM > LOW
- 宪法权威：宪法冲突自动为 CRITICAL
- 覆盖度统计：需求覆盖率、模糊计数、重复计数
- 最多 50 条发现，超出部分聚合摘要
- 可选的修复建议（需用户明确批准）
- 扩展钩子支持：`before_analyze` / `after_analyze`

### 完整工作流程

**步骤 1：初始化分析上下文**
- 运行 `check-prerequisites.ps1 -Json -RequireTasks -IncludeTasks`
- 确定 SPEC、PLAN、TASKS 路径

**步骤 2：渐进式加载产物**
- spec.md：概述、功能需求、成功标准、用户故事、边缘情况
- plan.md：架构/技术栈选择、数据模型引用、阶段、技术约束
- tasks.md：任务 ID、描述、阶段分组、[P] 标记、文件路径
- constitution.md：原则名称和 MUST/SHOULD 规范性声明

**步骤 3：构建语义模型**
- 需求清单（FR-XXX / SC-XXX key + 描述 slug）
- 用户故事/动作清单
- 任务覆盖映射（任务 → 需求/故事）
- 宪法规则集

**步骤 4：六类检测**
- A. 重复检测：近重复需求
- B. 歧义检测：模糊形容词（fast、scalable 等无度量标准）
- C. 不完整：需求缺宾语或度量、故事缺验收标准
- D. 宪法对齐：冲突 MUST 原则
- E. 覆盖缺口：需求无任务覆盖、任务无需求映射
- F. 不一致：术语漂移、实体引用矛盾、任务排序矛盾

**步骤 5：严重度分配** — CRITICAL / HIGH / MEDIUM / LOW

**步骤 6：输出分析报告** — Markdown 表格（ID / Category / Severity / Location / Summary / Recommendation）

**步骤 7：提供后续建议** — 如有 CRITICAL → 建议先解决再 implement；如有 LOW/MEDIUM → 可继续但提示改进

**步骤 8：提供修复选项** — "是否需要具体修复建议？"（需用户明确批准）

**输出结果：** 分析报告（含覆盖度统计、宪法对齐状态）。

---

## 8. speckit-checklist — 检查清单

### 功能描述

**主要作用：** 生成自定义检查清单，**测试需求质量而非实现**。核心理念是"检查清单是需求的单元测试"——验证需求的完整性、清晰度、一致性和可度量性。

**适用场景：**
- 需要按特定领域审查需求质量时（UX、API、安全、性能等）
- 在规划或实现前确保需求质量

**核心特性：**
- "需求的单元测试"：验证需求本身的质量，而非实现是否正确
- 动态意图澄清：最多 3 个上下文问题确定范围和深度
- 按质量维度分组：完整性、清晰度、一致性、可度量性、覆盖度、边缘情况
- 强制追踪引用：≥80% 项需包含 `[Spec §X.Y]` 或 `[Gap]`、`[Ambiguity]` 标记
- 追加模式：相同域名文件追加而非覆盖
- 绝对禁止实现验证类条目（"Verify"、"Test"、"Confirm" 等）
- 扩展钩子支持：`before_checklist` / `after_checklist`

### 完整工作流程

**步骤 1：Setup**
- `check-prerequisites.ps1 -Json` 解析路径

**步骤 2：澄清意图**
- 提取信号：领域关键词、风险指标、干系人提示
- 生成最多 3 个上下文问题（范围、风险优先级、深度、受众、边界排除）
- 如果无法交互，使用默认值

**步骤 3：理解用户请求**
- 结合参数和澄清答案
- 导出检查清单主题、必须项、焦点映射

**步骤 4：加载功能上下文**
- 渐进式加载 spec.md、plan.md、tasks.md
- 仅加载相关部分

**步骤 5：生成检查清单**
- 创建 `FEATURE_DIR/checklists/` 目录
- 文件名：`[domain].md`（如 `ux.md`、`api.md`、`security.md`）
- 每个条目：问句格式 + 质量维度标签 + 追踪引用
- 格式：`- [ ] CHK### <问题> [质量维度, 引用]`

**步骤 6：报告**
- 输出文件路径、条目数、焦点区域、深度级别

**输出结果：** `checklists/<domain>.md` 检查清单文件。

---

## 9. speckit-taskstoissues — 任务转议题

### 功能描述

**主要作用：** 将 `tasks.md` 中的任务转换为 GitHub Issues，在对应仓库中创建可追踪的工作项。

**适用场景：**
- 任务清单就绪，需要创建 GitHub Issues 供团队协作

**核心特性：**
- 自动检测 Git Remote URL（仅支持 GitHub）
- 逐个任务创建 Issue
- 安全约束：绝不向不匹配 remote URL 的仓库创建 Issue
- 扩展钩子支持：`before_taskstoissues` / `after_taskstoissues`

### 完整工作流程

**步骤 1：初始化**
- `check-prerequisites.ps1 -Json -RequireTasks -IncludeTasks`
- 解析 `FEATURE_DIR` 和任务路径

**步骤 2：获取 Git Remote**
- `git config --get remote.origin.url`
- **仅 GitHub URL 继续**（否则 STOP）

**步骤 3：创建 Issues**
- 对 tasks.md 中每个任务，使用 GitHub MCP 创建 Issue
- 仅在与 remote URL 匹配的仓库中创建

**输出结果：** GitHub Issues 已创建并与任务对应。

---

## 10. speckit-git-initialize — Git 初始化

### 功能描述

**主要作用：** 在当前项目目录初始化 Git 仓库（如果尚不存在）。

**适用场景：**
- 新项目初始化时
- 项目规范驱动开发开始前

**核心特性：**
- 幂等性：已存在 Git 仓库则跳过
- 优雅降级：Git 不可用时发出警告但继续
- 可定制：通过替换脚本添加自定义 .gitignore、分支命名、LFS、钩子
- 初始提交："Initial commit from Specify template"

### 完整工作流程

1. 运行脚本：`.specify/extensions/git/scripts/powershell/initialize-repo.ps1`
2. 脚本检查：Git 可用？已是仓库？→ 跳过或执行 `git init; git add .; git commit`
3. 失败处理：`git init`/`add`/`commit` 任一失败则停止并报错

**输出结果：** `✓ Git repository initialized`

---

## 11. speckit-git-feature — 功能分支

### 功能描述

**主要作用：** 为功能规格创建并切换到新的 Git 功能分支。**仅处理分支创建**，规格目录和文件由 `/speckit-specify` 核心工作流处理。

**适用场景：**
- 作为 `before_specify` 钩子被 `/speckit-specify` 触发
- 需要按规范命名创建功能分支

**核心特性：**
- 双编号策略：sequential（`003-`）或 timestamp（`20260319-143022-`）
- 自动短名称生成（2-4 词）
- 环境变量覆盖：`GIT_BRANCH_NAME` 可强制指定分支名
- JSON 输出：`BRANCH_NAME` 和 `FEATURE_NUM`
- 优雅降级：非 Git 目录则跳过并警告

### 完整工作流程

1. 验证 Git 可用
2. 确定编号策略（检查 `git-config.yml` → `init-options.json` → 默认 sequential）
3. 生成短名称
4. 运行脚本创建分支（Bash 或 PowerShell）
5. 输出 JSON：`{ "BRANCH_NAME": "003-user-auth", "FEATURE_NUM": "003" }`

**输出结果：** 分支已创建并切换，返回 `BRANCH_NAME` 和 `FEATURE_NUM`。

---

## 12. speckit-git-validate — 分支验证

### 功能描述

**主要作用：** 验证当前 Git 分支是否符合功能分支命名规范。

**适用场景：**
- 开发过程中验证当前分支命名
- 作为工作流钩子自动触发

**核心特性：**
- 两种命名模式验证：sequential（`^[0-9]{3,}-`）和 timestamp（`^[0-9]{8}-[0-9]{6}-`）
- 自动检测对应 spec 目录是否存在
- 优雅降级：Git 不可用时检查 `SPECIFY_FEATURE` 环境变量

### 完整工作流程

1. `git rev-parse --abbrev-ref HEAD` 获取分支名
2. 匹配 sequential 或 timestamp 模式
3. 匹配：输出 `✓ On feature branch: <name>` + 检查对应 spec 目录
4. 不匹配：输出 `✗ Not on a feature branch. Current branch: <name>`

**输出结果：** 验证结果（✓ 或 ✗）和 spec 目录状态。

---

## 13. speckit-git-remote — 远程检测

### 功能描述

**主要作用：** 检测 Git Remote URL，用于 GitHub 集成（如 Issue 创建）。

**适用场景：**
- 需要确定仓库的 GitHub 远程地址时
- 作为 `/speckit-taskstoissues` 的前置步骤

**核心特性：**
- 自动解析 HTTPS 和 SSH 格式
- 提取 owner 和 repo name
- 验证是否为 GitHub 远程（非 GitHub 不作为错误）
- 优雅降级：无 Git/无远程 → 返回空结果

### 完整工作流程

1. `git config --get remote.origin.url`
2. 解析 URL → owner、repo name、isGitHub
3. 支持 HTTPS 和 SSH 格式

**输出结果：** `{ owner: "...", repo: "...", isGitHub: true/false }`

---

## 14. speckit-git-commit — 自动提交

### 功能描述

**主要作用：** 在 Spec Kit 命令完成后自动 stage 并 commit 所有变更。

**适用场景：**
- 作为钩子在核心命令后自动触发
- 需要自动记录规范变更时

**核心特性：**
- 事件感知：根据钩子事件名（`after_specify`、`before_plan` 等）查找配置
- 配置驱动：`.specify/extensions/git/git-config.yml` 控制
- 默认禁用：`auto_commit.default: false`
- 每命令可覆盖：`after_specify.enabled: true` + 自定义 message
- 优雅降级：无 Git/无配置/无变更 → 跳过

### 完整工作流程

1. 确定事件名（如 `after_specify`）
2. 检查 `git-config.yml` 中的 `auto_commit` 配置
3. 查找事件特定 key → 回退到 `default`
4. 如果启用且有未提交变更 → `git add .; git commit -m "<message>"`

**输出结果：** 变更已提交（或跳过）。

---

## 15. speckit-sync-analyze — 漂移分析

### 功能描述

**主要作用：** 分析规格与实现之间的漂移。将功能需求（FR-*）、成功标准（SC-*）和验收场景与实际代码库进行对比，识别分歧。

**别名：** `/speckit-drift`

**适用场景：**
- 项目演进过程中规格与代码逐渐偏离
- 需要审计需求覆盖度
- 检测无规格覆盖的代码（unspecced code）

**核心特性：**
- 四类发现：Aligned（对齐）、Drifted（漂移）、Not Implemented（未实现）、Unspecced（无规格）
- 双格式报告：human-readable（.md）+ machine-readable（.json）
- 支持单规格分析：`--spec <id>`
- 只读分析，不修改任何文件
- 严重度分级：minor / moderate / major

### 完整工作流程

**步骤 1：发现规格**
- `find specs -name "spec.md"` 扫描所有规格文件
- 提取每个规格的 FR-*、SC-*、验收场景

**步骤 2：分析实现**
- 对每个规格：检查需求是否有对应实现
- 启发式匹配：CLI 命令 → Command 类、服务 → Service 类、实体 → 实体文件

**步骤 3：检测无规格代码**
- 找出没有被任何规格引用的代码功能

**步骤 4：生成漂移报告**
- 结构化报告：Summary、Detailed Findings、Unspecced Code、Inter-Spec Conflicts

**步骤 5：保存报告**
- `.specify/sync/drift-report.md` + `.specify/sync/drift-report.json`

**输出结果：** 漂移报告（Markdown + JSON），含对齐度统计。

---

## 16. speckit-sync-propose — 解决提议

### 功能描述

**主要作用：** 为漂移报告中的每个漂移项生成解决方案提议。AI 分析每个分歧并提议规格更新（回填）或代码变更（对齐）。

**适用场景：**
- `/speckit-sync-analyze` 完成后
- 需要决定如何处理检测到的漂移

**核心特性：**
- 三种解决方向：Backfill（代码→规格）、Align（规格→代码）、Human Decision（需人工判断）
- 对 Unspecced 功能自动生成新规格草案
- 交互模式：`--interactive` 逐项确认
- 双格式输出：Markdown + JSON
- 信心度评分：HIGH / MEDIUM / LOW

### 完整工作流程

**步骤 1：加载漂移报告**
- 读取 `.specify/sync/drift-report.json`

**步骤 2：逐项分析漂移需求**
- Backfill：代码有经过测试的功能 → 更新规格
- Align：规格最近审查批准 → 修改代码
- Human Decision：两种解释都合理 → 列出选项和问题

**步骤 3：处理无规格功能**
- 建议新 spec ID、草拟标题和用户故事、提取需求

**步骤 4：处理未实现需求**
- 判断：仍需要？应从规格删除？应实现？

**步骤 5：生成提议文档**
- `.specify/sync/proposals.md` + `.specify/sync/proposals.json`

**输出结果：** 解决方案提议文档。

---

## 17. speckit-sync-apply — 应用解决

### 功能描述

**主要作用：** 应用已批准的漂移解决方案。更新规格或生成实现任务。

**适用场景：**
- `/speckit-sync-propose` 完成且提议已审查批准

**核心特性：**
- 安全优先：自动备份、`--dry-run` 预览
- 四种应用方式：回填（修改 spec）、新规格（创建 spec 目录）、对齐任务（生成 align-tasks.md）、废弃（标记旧规格）
- 变更追踪：记录 before/after
- GitHub Issue 集成：`--create-issues` 为对齐任务创建 Issue
- 自动提交：`--auto-commit`

### 完整工作流程

**步骤 1：加载已批准提议**
- 过滤 `approved: true` 的提议

**步骤 2：应用回填提议**
- 读取原始 spec → 定位被更新需求 → 替换文本 → 更新元数据

**步骤 3：应用新规格提议**
- 创建 `specs/{spec-id}/` → 写入 `spec.md` → 创建空 `tasks.md`

**步骤 4：生成对齐实现任务**
- 生成 `.specify/sync/align-tasks.md` → 可选创建 GitHub Issues

**步骤 5：处理废弃决议**
- 添加 `superseded_by` 字段、交叉引用

**步骤 6：生成 Apply Report** → `.specify/sync/apply-report.md`

**输出结果：** 规格已更新、新任务已生成、apply 报告。

---

## 18. speckit-sync-conflicts — 冲突检测

### 功能描述

**主要作用：** 检测规格之间或规格与设计文档之间的矛盾。呈现出来供人工解决。

**适用场景：**
- 多规格项目中出现矛盾需求
- 设计文档与原始规格不一致

**核心特性：**
- 四类冲突检测：同功能不同行为、过时约束、范围重叠、隐式冲突
- 建议解决路径：Supersede（取代）、Merge（合并）、Deprecate（废弃）、Human Required（需人工）
- 语义索引：按功能区域、实体类型、行为类型构建需求索引
- 可指定范围：`--spec <id>` 或 `--include-design-docs`

### 完整工作流程

**步骤 1：从所有来源提取需求**
- specs/*/spec.md → FR-*、SC-*、约束、假设
- design docs → 设计决策、约束、行为规范

**步骤 2：构建需求索引**
- 按特征区域、实体类型、行为类型建立语义索引

**步骤 3：检测直接冲突**
- Type 1：同功能不同行为
- Type 2：过时约束
- Type 3：范围重叠
- Type 4：隐式冲突

**步骤 4：确定解决路径** — Supersede / Merge / Deprecate / Human Required

**步骤 5：生成冲突报告** → `.specify/sync/conflicts.md`

**输出结果：** 冲突报告（含建议解决方案）。

---

## 19. speckit-sync-backfill — 回填规格

### 功能描述

**主要作用：** 从已有的、无规格覆盖的代码功能生成完整的规格文档。从实现、测试和文档中提取意图。

**适用场景：**
- 有代码但没有规格的功能
- 需要为遗留代码补全规格文档

**核心特性：**
- 完整产物生成：spec.md + plan.md + quickstart.md + tasks.md
- 从实现推断：从 Commands → 需求、从 Services → 业务规则、从 Tests → 验收场景
- 回填声明：自动添加 "Generated from existing implementation" 标记
- 预览模式（默认）和创建模式（`--create`）
- 自动确定下一个可用 spec ID
- 人工审查提醒：回填的规格应始终由人审查

### 完整工作流程

**步骤 1：发现功能范围**
- 搜索相关文件：Commands、Services、Tests、Docs

**步骤 2：分析实现**
- Commands → 命令名、描述、选项、校验、错误处理
- Services → 公共方法（→ 需求）、业务逻辑、依赖、边缘情况
- Tests → 测试场景（→ 验收场景）、预期行为

**步骤 3：推断用户故事**
- 从命令/服务分析生成用户故事 + 验收场景

**步骤 4：提取需求**
- 方法签名 → 功能需求、校验逻辑 → 安全需求、业务逻辑 → 业务需求

**步骤 5：生成规格结构** — 标准模板 + 回填声明

**步骤 6：确定 Spec ID** — 下一个可用编号

**步骤 7：生成 Plan + Quickstart + Review Tasks**

**步骤 8：输出选项** — 预览（默认）或 `--create` 写入文件

**输出结果（--create）：** `specs/{id}/spec.md`、`plan.md`、`quickstart.md`、`tasks.md`。

---

## 20. speckit-iterate-define — 迭代定义

### 功能描述

**主要作用：** 分析变更请求与当前规格状态和实现进度的关系，生成可审查的迭代计划（写入 `pending-iteration.md`）。**不修改任何规格产物**，仅写入待定迭代文件。

**适用场景：**
- 功能开发过程中出现变更请求
- 需要在已有进度基础上增加/修改/移除需求

**核心特性：**
- 范围分类：Feature-wide / Phase-level / Task-level / Subtraction / Pivot
- 进度感知：自动检测已完成任务、当前阶段、adhoc 变更
- 影响评估：明确哪些产物需要更新及原因
- 风险检查：已完成任务是否被失效、是否有范围违规、下游依赖是否断裂
- Git 集成：`git diff` 和 `git log` 检测代码变更与任务映射
- 安全可重跑：再次运行会覆盖（需确认）
- Pivot 警告：方向重大变化建议重新 `/speckit-specify`

### 完整工作流程

**步骤 1：初始化特性上下文**
- 运行 `check-prerequisites.sh --json --paths-only` 解析 `FEATURE_DIR`

**步骤 2：检查已有迭代**
- 如果 `pending-iteration.md` 已存在 → 警告并询问是否覆盖

**步骤 3：加载当前产物**
- 必需：spec.md
- 可选：plan.md、tasks.md、data-model.md、research.md、quickstart.md、checklists/

**步骤 4：评估实现进度**
- A. 任务状态分析：已完成 vs 剩余、当前 Phase
- B. Git 检测：`git diff --name-status`、`git log --oneline`
- C. 构建进度摘要

**步骤 5：分析变更请求**
- 范围分类 + 影响评估（哪些产物需更新）+ 与进度交叉引用

**步骤 6：呈现影响摘要**
- 变更请求、范围、进度状态、产物更新表、风险检查 → 等待用户确认

**步骤 7：写入待定迭代文件**
- `FEATURE_DIR/pending-iteration.md`（含 frontmatter、变更摘要、进度、影响评估、计划变更）

**步骤 8：报告和下一步**
- 路径 + "可审查/编辑后 apply"

**输出结果：** `pending-iteration.md`（等待审查和应用）。

---

## 21. speckit-iterate-apply — 迭代应用

### 功能描述

**主要作用：** 执行 `pending-iteration.md` 中定义的迭代计划，更新所有 `/speckit-implement` 依赖的产物。应用后可**直接 `/speckit-implement`**，跳过 `/speckit-plan` 和 `/speckit-tasks`。

**适用场景：**
- `/speckit-iterate-define` 完成后，迭代计划经审查确认

**核心特性：**
- 按依赖顺序更新：spec → data-model → plan → tasks → quickstart → research
- 稳定 ID：不重新编号已有 Task ID 或 Requirement ID
- Git 进度标记：自动将确认完成的任务从 `[ ]` 改为 `[x]`
- 迭代日志：在 spec.md 中追加 `## Iterations` 记录
- 交叉产物一致性验证：需求-任务覆盖、任务-文件引用、无矛盾
- 原子保存：每文件更新后立即保存
- 清理：成功后删除 `pending-iteration.md`

### 完整工作流程

**步骤 1：初始化** — 检查 `pending-iteration.md` 存在性

**步骤 2：加载迭代计划** — 解析 frontmatter、变更摘要、影响评估、计划变更

**步骤 3：加载当前产物** — 所有可用产物

**步骤 4：确认应用** — 呈现摘要 → 等待 "yes"

**步骤 5：按顺序应用变更**
- spec.md：新增/修改/移除需求、用户故事、边缘情况
- data-model.md：新增/修改实体
- plan.md：更新架构、文件结构、阶段描述
- tasks.md：新增（下一可用 ID）/修改（原位）/移除（删除线 + 注释）
- quickstart.md / research.md

**步骤 6：标记 Git 确认的任务完成**
- 验证 mapped files → `[ ]` → `[x]`

**步骤 7：添加迭代日志** — `spec.md` 中追加迭代记录

**步骤 8：一致性验证** — 需求-任务覆盖、任务-文件引用、无矛盾

**步骤 9：清理** — 删除 `pending-iteration.md`

**步骤 10：报告** — 产物更新摘要、任务变更、一致性警告、下一步建议

**输出结果：** 所有规格产物已更新，可直接 `/speckit-implement`。

---

## 22. speckit-markitdown-convert — 文档转换

### 功能描述

**主要作用：** 使用 `markitdown` CLI 将文档（PDF、DOCX、PPTX、XLSX 等）转换为 Markdown 格式，放入 spec 目录作为参考材料。

**别名：** `/speckit-convert`

**适用场景：**
- 有外部文档（PDF、Word、PowerPoint、Excel 等）需要纳入规格工作流
- 将业务需求文档转换为可引用的 Markdown

**核心特性：**
- 多格式支持：Tier 1（PDF、DOCX、PPTX、XLSX）、Tier 2（HTML、CSV、JSON、XML、图片、音频、EPub、ZIP）
- 预检：验证 markitdown 和 Python 3.10+ 可用
- 自动输出路径：`.specify/specs/<filename>.md`
- 元数据头：自动注入转换来源信息
- 质量扫描：检测空输出、乱码、缺失内容
- 错误引导：PDF 提取不佳 → Azure Document Intelligence；图片 → markitdown-ocr 插件

### 完整工作流程

**Step 0：预检**
- 验证 markitdown 已安装（`markitdown --version`）
- 验证 Python 3.10+

**Step 1：解析输入文件**
- 解析文件路径和可选的输出路径
- 验证文件存在且可访问
- 检测文件格式（Tier 1 / Tier 2 / 未知但尝试）

**Step 2：确定输出路径**
- 自定义路径 → 直接使用
- 默认：`.specify/specs/<filename>.md`（如有 `.specify/` 目录）或同目录

**Step 3：确认** — 向用户显示输入/格式/输出 → 等待确认

**Step 4：转换**
- `markitdown "<file>" -o "<output>"`
- 错误处理：缺依赖 → 建议安装；文件未找到 → 请用户纠正

**Step 5：后处理**
- 添加元数据头（来源文件、格式、转换时间、版本）
- 质量审查：空输出、乱码、缺失内容 → 标记并建议

**Step 6：输出摘要** — 输出路径、文件大小、下一步建议

**输出结果：** 转换后的 Markdown 文件（含元数据头）。

---

## 23. speckit-red-team-gate — 红队门禁

### 功能描述

**主要作用：** 门禁检查（非工作流）—— 检查当前功能规格是否符合红队审查触发条件。作为 `/speckit-plan` 的 `before_plan` 钩子自动运行，阻止在无红队发现报告的情况下进入规划阶段。

**适用场景：**
- 每次 `/speckit-plan` 调用前自动触发
- 确保敏感/高风险规格在规划前经过红队审查

**核心特性：**
- 六类触发条件关键词扫描（大小写不敏感、词边界）：
  - `money_path`：fee、amount、currency、invoice 等
  - `regulatory_path`：KYC、AML、GDPR、compliance 等
  - `ai_llm`：LLM、Claude、GPT、prompt、classification 等
  - `immutability_audit`：immutable、audit trail、tamper 等
  - `multi_party`：approval、sign-off、role-based、permission gate 等
  - `contracts`：contract、interface、API boundary、payload 等
- 四种门禁结果：NOT REQUIRED / SATISFIED / BLOCKED / WAIVED
- 故意宽泛匹配：宁多勿漏（假正可接受、假负不可接受）
- 支持显式退出：`--skip-red-team-gate: <reason>`
- 幂等性：同规格两次运行结果相同

### 完整工作流程

**步骤 1：解析目标规格**
- 从参数或 `check-prerequisites.sh` 获取路径

**步骤 2：扫描触发条件**
- 对六类关键词逐个扫描
- 记录匹配的类别

**步骤 3：检查发现报告**
- Glob：`specs/<feature-id>/red-team-findings-*.md`
- 及 `99_Archive/red-team/<feature-id>/`

**步骤 4：输出门禁决策**
- 无匹配 → NOT REQUIRED → PROCEED
- 有匹配 + 有报告 → SATISFIED → PROCEED
- 有匹配 + 无报告 → BLOCKED → HALT
- 显式退出 `--skip-red-team-gate:` → WAIVED → PROCEED

**输出结果：** PROCEED 或 HALT 决策（HALT 时 `/speckit-plan` 必须停止）。

---

## 24. speckit-red-team-run — 红队运行

### 功能描述

**主要作用：** 在 `/speckit-plan` 锁定架构之前，使用并行对抗性透镜代理攻击功能规格。汇总发现并引导维护者将每个发现归入四种解决类别之一。

**适用场景：**
- 规格涉及敏感领域（资金路径、合规、AI/LLM、不可变审计、多方审批、合约）
- 门禁检测到需要红队审查的规格

**核心特性：**
- 并行对抗：多个透镜代理同时从不同角度攻击规格
- 触发匹配：自动根据规格内容匹配适合的透镜
- 透镜选择：≤5 自动选、>5 需确认（支持 `--yes` 非交互模式）
- 三透镜最少：低于 3 建议增加多样性
- 发现分级：CRITICAL / HIGH / MEDIUM / LOW（每透镜上限可配）
- 四种解决类别：spec-fix / new-OQ / accepted-risk / out-of-scope
- 硬性规则：绝不修改历史 SpecKit 工作记录（`specs/<id>/` 下文件）
- 断点续传：会话中断后可从最后解决的发现恢复
- 失败处理：个别透镜失败不中止整个会话
- 狗食（dogfood）会话：验证协议自身的首次运行

### 完整工作流程

**步骤 1：调用解析**
- `<target-spec-path>`（必需）
- `--yes`、`--lenses`、`--dry-run`、`--session-suffix`

**步骤 2：前置条件检查**
- 目标规格存在
- Lens 目录存在（`.specify/extensions/red-team/red-team-lenses.yml`）
- 并可解析、非空、每项有必需字段
- 宪法声明触发标准（软检查，缺失时引导模式运行）

**步骤 3：触发匹配**
- 扫描规格中六类触发关键词
- 零匹配 → 信息提示并停止（非错误）
- 有匹配 → 记录匹配列表

**步骤 4：透镜选择**
- 筛选匹配触发条件的透镜
- ≤5 → 自动使用；>5 → 排名前 5 建议 + 用户确认
- 支持显式 `--lenses` 覆盖

**步骤 5：并行调度**
- 为每个选定透镜构建对抗提示
- 同一批并行调度所有透镜代理
- 记录每透镜起止时间

**步骤 6：发现汇总**
- 解析各代理响应 → 结构化发现
- 强制每透镜发现上限
- 处理透镜故障（继续，不中止）
- 写入初始报告：`specs/<id>/red-team-findings-<YYYY-MM-DD>.md`

**步骤 7：解决流程**
- 逐发现交互：显示发现 → 询问解决类别
- spec-fix：编辑**前向规范文件**（绝不编辑历史 SpecKit 记录）
- new-OQ：路由到规格的 `## Open Questions`
- accepted-risk：路由到 `## Accepted Risks`
- out-of-scope：交叉引用其他规格
- 每发现更新报告状态

**步骤 8：完成与清理**
- 所有发现解决后更新会话元数据
- 狗食会话：提示维护者写验证决策

**输出结果：** `red-team-findings-<date>.md` 报告（含所有发现和解决方案）。

---

## 技能工作流关系图

```
┌─────────────────────────────────────────────────────┐
│              Spec-Kit 核心工作流管道                   │
│                                                       │
│  constitution ──────────────────────────────────┐     │
│  （项目治理原则，全程约束）                       │     │
│                                                  │     │
│  specify → clarify → plan → tasks → implement   │     │
│  （规格） （澄清） （规划） （任务） （执行）      │     │
│     │                            │              │     │
│     ├── checklist ───────────────┤              │     │
│     │   （需求质量检查清单）       │              │     │
│     │                            │              │     │
│     └── analyze ─────────────────┤              │     │
│         （交叉一致性分析）         │              │     │
│                                  │              │     │
│                    taskstoissues │              │     │
│                    （任务→Issue） │              │     │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              Sync 扩展（规格-代码同步）                │
│                                                       │
│  sync-analyze → sync-propose → sync-apply            │
│  （漂移分析）   （解决提议）   （应用解决）             │
│       │              │                                │
│       ├── sync-conflicts                             │
│       │   （冲突检测）                                 │
│       │                                               │
│       └── sync-backfill                              │
│           （代码→规格回填）                            │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              Iterate 扩展（迭代变更管理）              │
│                                                       │
│  iterate-define → iterate-apply                      │
│  （定义迭代）      （应用迭代）                        │
│       │                                               │
│       └──→ 产物更新后直接 implement                   │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              Red Team 扩展（对抗性审查）               │
│                                                       │
│  red-team-gate ──→ red-team-run                      │
│  （门禁：触发？）   （执行：攻击规格）                  │
│       │                                               │
│       └── BLOCKED → 必须先 run 才能 plan              │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              Markitdown 扩展                          │
│                                                       │
│  markitdown-convert                                  │
│  （外部文档→Markdown 参考材料）                        │
└─────────────────────────────────────────────────────┘

┌─────────────────────────────────────────────────────┐
│              Git 工具链                               │
│                                                       │
│  git-initialize                                      │
│  git-feature ←── (specify 钩子触发)                   │
│  git-validate                                        │
│  git-remote  ←── (taskstoissues 前)                  │
│  git-commit  ←── (各命令钩子触发)                     │
└─────────────────────────────────────────────────────┘
```

---

> **文档版本：** v1.0  
> **生成日期：** 2026-05-11  
> **涵盖技能数：** 22 个独特技能（24 个命令含 2 个别名）