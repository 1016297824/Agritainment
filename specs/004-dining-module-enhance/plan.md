# Implementation Plan: 餐饮模块功能增强

**Branch**: `005-modify-catering` | **Date**: 2026-05-11 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `specs/004-dining-module-enhance/spec.md`

## Summary

对餐饮模块进行三项核心改造：(1) 修复桌位预约的日期独立性 bug——当前 createReservation 直接更新 DiningTable.status 导致跨日期状态污染；(2) 实现预约到点餐的业务闭环——通过 reservation_id 关联订单与预约；(3) 将单一页面拆分为预约/点餐两个页面，通过底部导航和路由守卫控制业务流程。

技术方案：后端去除 createReservation 中的 table.status 直接更新，订单创建接口增加 reservation_id 参数替代 table_qr。前端使用 uni-app 路由和 Pinia store 管理跨页面状态。

## Technical Context

**Language/Version**: Java 21 (backend), JavaScript ES6+ / Vue 3 Options API (frontend)
**Primary Dependencies**: Spring Boot 3.2, MyBatis-Plus, uni-app, Vue 3, Pinia
**Storage**: MySQL 8.0
**Testing**: 手动测试 + 现有构建验证
**Target Platform**: 微信小程序 (mp-weixin) 为主，H5 为辅
**Project Type**: mini-program + web-service
**Performance Goals**: API 响应 < 200ms, 页面交互无感知延迟
**Constraints**: 遵循现有代码风格（前端 Options API，后端 Spring 分层）
**Scale/Scope**: 小型餐厅（~20桌，~100道菜），单时段并发预约 < 50

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principle | Status | Notes |
|-----------|--------|-------|
| I. 用户体验优先 | ✅ PASS | 预约到点餐的自动跳转、购物车即时反馈、空状态引导 |
| II. 安全底线 | ✅ PASS | 复用现有 JWT 认证和 @RequireRole 拦截，无需新增安全机制 |
| III. 一致性 | ✅ PASS | 前端 Options API + Pinia，后端 Spring 分层 + MyBatis-Plus |
| IV. 渐进增强 | ✅ PASS | P1 修复预约 bug → P2 点餐功能 → P2 页面拆分，每步独立可测 |

## Project Structure

### Documentation (this feature)

```text
specs/004-dining-module-enhance/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── api.md
└── tasks.md             # Phase 2 output (speckit-tasks)
```

### Source Code (repository root)

```text
client/src/
├── pages/dining/
│   ├── index.vue              # [MODIFIED] 预约页面（原餐饮主页改造）
│   └── order/
│       └── index.vue           # [MODIFIED] 点餐页面（增加预约关联）
├── api/
│   └── dining.js              # [MODIFIED] 更新 API 调用参数
└── stores/
    └── dining.js              # [NEW] 餐饮模块 Pinia store

server/src/main/java/com/agritainment/
├── controller/
│   └── DiningController.java  # [MODIFIED] 调整接口参数
├── service/
│   └── DiningService.java     # [MODIFIED] 核心逻辑修复
├── entity/
│   ├── TableReservation.java  # [UNCHANGED]
│   ├── DiningTable.java       # [UNCHANGED]
│   ├── Order.java             # [UNCHANGED]
│   └── Dish.java              # [UNCHANGED]
└── dto/
    ├── CreateReservationRequest.java  # [UNCHANGED]
    └── CreateOrderRequest.java        # [MODIFIED] 增加 reservation_id
```

**Structure Decision**: 沿用现有 client/server 双层结构。前端在 dining 目录下重构页面，新增 Pinia store 管理跨页面状态。后端在现有实体和 DTO 基础上做最小修改。

## Complexity Tracking

> 无违反 Constitution 的情况，无需填写。