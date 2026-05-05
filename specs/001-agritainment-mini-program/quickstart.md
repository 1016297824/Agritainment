# 快速启动指南

**项目**: 农家乐小程序

---

## 技术栈

| 层面 | 技术 |
|------|------|
| 前端 | Uni-app + Vue 3 + Pinia |
| 后端 | Spring Boot 3.2 + Java 21 + MyBatis-Plus |
| 数据库 | MySQL 8.0 |
| 认证 | JWT (JJWT) |
| 文件存储 | 服务器本地 + Nginx |

## 开发环境

```
# 前置依赖
JDK >= 21
Maven >= 3.8
Node.js >= 18.0.0
npm >= 9.0.0
MySQL >= 8.0

# 微信开发者工具
# 下载地址: https://developers.weixin.qq.com/miniprogram/dev/devtools/download.html
```

## 启动步骤

### 1. 数据库初始化

```sql
-- 在 MySQL 客户端中执行
CREATE DATABASE IF NOT EXISTS agritainment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE agritainment;
-- 然后执行 schema.sql 建表
-- 然后执行 data.sql 导入种子数据
```

或命令行：
```bash
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS agritainment DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -p agritainment < server-spring/src/main/resources/schema.sql
mysql -u root -p agritainment < server-spring/src/main/resources/data.sql
```

### 2. 后端

```bash
cd server-spring
# 编辑 src/main/resources/application.yml 配置数据库连接
$env:JAVA_HOME="G:\Java\jdk-21"  # Windows PowerShell
mvn spring-boot:run               # 启动开发服务器 (http://localhost:8080)
```

### 3. 前端

```bash
cd client
npm install
npm run dev:mp-weixin  # 编译微信小程序
# 用微信开发者工具打开 dist/dev/mp-weixin 目录
```

## 配置文件

### 后端 (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/agritainment?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
    username: root
    password: your_password

jwt:
  secret: your-secret-key-must-be-at-least-256-bits-long-for-hs256
  expiration: 604800000  # 7天
```

### 前端 (request.js)

```javascript
const BASE_URL = 'http://localhost:8080/api/v1'
```

## 管理员账号

| 手机号 | 密码 | 角色 |
|--------|------|------|
| 13800000001 | admin123 | 超级管理员 |
| 13800000002 | admin123 | 副管理员 |

## 测试

```bash
# 后端测试
cd server-spring
mvn test

# 前端编译测试
cd client
npx vite build
```
