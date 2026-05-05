# 研究与技术选型

**项目**: 农家乐小程序
**日期**: 2026-05-05

---

## 1. 前端框架选型

### Decision: Uni-app (Vue 3)

**Rationale:**
- 微信小程序原生开发效率低，不支持跨平台
- Taro (React) 生态好但包体积较大
- Uni-app (Vue 3) 学习曲线低，组件生态丰富，支持微信小程序一键发布
- Vue 3 Composition API 代码组织更清晰
- Uni-app 内置小程序 API 兼容层，开发体验好

**Alternatives Considered:**
- 微信原生框架: 开发效率低，无法复用
- Taro (React): 包体积大，React 在小程序端性能略差
- Remax: 社区较小，维护不稳定

---

## 2. 后端框架选型

### Decision: Spring Boot 3.2 + Java 21 + MyBatis-Plus

**Rationale:**
- 团队更熟悉 Java/Spring Boot，维护成本低
- Spring Boot 生态成熟，企业级稳定性
- MyBatis-Plus 简化 CRUD，开发效率高
- Java 21 LTS 支持长期维护
- 10,000 用户 + 50 员工规模，Spring Boot 完全胜任
- 内置 Tomcat，部署简单

**Alternatives Considered:**
- Node.js + Express: 团队不熟悉 JavaScript 后端，维护困难
- Python FastAPI: 性能好但团队不熟悉
- Go Gin: 性能最优但学习成本高

---

## 3. 数据库选型

### Decision: MySQL 8.0

**Rationale:**
- 关系型数据适合本项目的结构化数据（预约、订单、卡券）
- MySQL 8.0 支持 JSON 字段，灵活处理非结构化数据
- 事务支持完善，适合订单和支付场景
- 运维成熟，社区资源丰富

**Alternatives Considered:**
- PostgreSQL: 功能更强但运维复杂度高
- MongoDB: 不适合事务性强的场景
- SQLite: 不支持并发

---

## 4. 缓存方案

### Decision: 第一期不使用缓存，直接查询数据库

**Rationale:**
- 10,000 用户 + 50 员工规模，MySQL 直接查询性能足够
- 减少运维复杂度，降低部署成本
- 后期如有性能瓶颈，可引入 Redis

**Alternatives Considered:**
- Redis: 增加运维复杂度，初期不必要
- 内存缓存: 不支持分布式

---

## 5. 文件存储

### Decision: 服务器本地存储 + Nginx 静态服务

**Rationale:**
- 已有轻量服务器（4核4G，25GB磁盘可用），初期无需额外付费
- 10,000客户规模，图片存储量初期约5-10GB，25GB磁盘足够
- Nginx 提供静态文件服务，性能足够支撑
- 后期图片量大时可迁移至腾讯云 COS

**Alternatives Considered:**
- 腾讯云 COS: 需额外付费，初期成本不必要
- 阿里云 OSS: 同上
- 七牛云: 同上

**迁移路径:**
当磁盘使用超过80%或带宽成为瓶颈时，迁移至 COS，只需修改上传接口和URL前缀

---

## 6. 支付方案

### Decision: 微信支付

**Rationale:**
- 小程序内支付必须使用微信支付
- 会员购买、产品购买统一走微信支付
- 后续可扩展其他支付方式

---

## 7. 消息推送

### Decision: 微信模板消息 + 订阅消息

**Rationale:**
- 小程序内推送首选微信模板消息
- 预约提醒、服务完成通知用订阅消息
- 无需短信成本

---

## 8. 二维码方案

### Decision: 后端生成 + 前端展示

**Rationale:**
- 桌位二维码、卡券二维码由后端生成唯一标识
- 前端使用 qrcode.js 渲染展示
- 扫码验证由小程序 wx.scanCode API 实现

---

## 9. 摄像头接口预留

### Decision: RESTful API 接口设计

**Rationale:**
- 第一期不接入硬件，但预留完整 API
- 摄像头控制接口: PTZ (Pan-Tilt-Zoom) 控制
- 视频流接口: RTSP 转 HLS
- 权限管理接口: 摄像头授权/回收

---

## 10. 项目结构

```
agritainment/
├── client/                    # Uni-app 小程序前端
│   ├── src/
│   │   ├── pages/             # 页面
│   │   │   ├── index/         # 首页
│   │   │   ├── dining/        # 餐饮
│   │   │   ├── products/      # 产品
│   │   │   ├── planting/      # 种植
│   │   │   ├── profile/       # 个人中心
│   │   │   ├── staff/         # 员工页面
│   │   │   └── admin/         # 管理员页面
│   │   ├── components/        # 公共组件
│   │   ├── stores/            # Pinia 状态管理
│   │   ├── api/               # API 请求
│   │   ├── utils/             # 工具函数
│   │   └── styles/            # 全局样式
│   └── package.json
├── server-spring/              # Spring Boot 后端
│   ├── src/main/java/com/agritainment/
│   │   ├── controller/         # 控制器
│   │   ├── service/            # 业务逻辑
│   │   ├── mapper/             # 数据访问
│   │   ├── entity/             # 实体类
│   │   ├── config/             # 配置类
│   │   ├── interceptor/        # 拦截器
│   │   ├── annotation/         # 自定义注解
│   │   ├── common/             # 通用类
│   │   └── util/               # 工具类
│   ├── src/main/resources/
│   │   ├── application.yml     # 配置文件
│   │   ├── schema.sql          # 数据库建表
│   │   └── data.sql            # 种子数据
│   └── pom.xml
└── docs/                      # 文档
```
