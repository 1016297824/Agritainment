# Token 认证体系：传递与存储流程

> 本文档描述 Agritainment 农家乐小程序项目中用户认证 Token 的完整生命周期，包括创建、存储、传递、验证、过期处理等环节。

---

## 1. 整体架构

项目采用 **JWT (JJWT 0.12.5) + Bearer Token** 认证体系：

| 层级 | 技术选型 | 职责 |
|------|---------|------|
| 服务端 | Spring Boot + JJWT | 签发 JWT、拦截验证、角色鉴权 |
| 客户端 | uni-app + Pinia | 存储 token、请求注入 Authorization header、401 自动处理 |

项目中存在两种不同性质的 Token，互不干扰：

| Token 类型 | 用途 | 存储位置 |
|-----------|------|---------|
| 用户认证 JWT | 客户端与服务端之间的身份凭证 | 客户端 `uni.Storage` + Pinia state |
| 微信 access_token | 服务端调用微信 API（发订阅消息等） | 服务端内存缓存 |

本文档重点描述**用户认证 JWT**的完整流程。

---

## 2. Token 创建（服务端签发）

### 2.1 签发入口

三个 API 入口会签发 JWT：

| 入口 | 路径 | Token 中的角色 | 请求参数 |
|------|------|---------------|---------|
| 手机号+验证码注册 | `POST /api/v1/auth/register` | 固定 `customer` | `{ phone, code }` |
| 手机号+验证码登录 | `POST /api/v1/auth/login` | 取用户实际角色 | `{ phone, code }` |
| 管理员密码登录 | `POST /api/v1/auth/admin-login` | 固定 `admin` | `{ phone, password }` |

### 2.2 JWT 结构

JWT Payload 包含 4 个 claim：

| Claim | 位置 | 类型 | 说明 |
|-------|------|------|------|
| `sub` | 标准 claim | String | 用户 ID |
| `phone` | 自定义 claim | String | 手机号 |
| `role` | 自定义 claim | String | 角色（customer / staff / admin） |
| `isMember` | 自定义 claim | Boolean | 是否会员 |

### 2.3 签发代码

**JwtUtil**（`server/src/main/java/com/agritainment/util/JwtUtil.java`）：

```java
public String generateToken(Long userId, String phone, String role, boolean isMember) {
    return Jwts.builder()
            .subject(userId.toString())
            .claim("phone", phone)
            .claim("role", role)
            .claim("isMember", isMember)
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expiration))
            .signWith(getKey())
            .compact();
}
```

**AuthService**（`server/src/main/java/com/agritainment/service/AuthService.java`）：

```java
// 注册：角色固定为 customer
public String register(String phone, String code) {
    // ... 验证码校验、用户创建 ...
    return jwtUtil.generateToken(user.getId(), phone, "customer", false);
}

// 登录：角色取用户实际角色
public String login(String phone, String code) {
    // ... 验证码校验、用户查找 ...
    return jwtUtil.generateToken(user.getId(), phone, user.getRole(), user.getIsMember());
}

// 管理员登录：角色固定为 admin
public String adminLogin(String phone, String password) {
    // ... 密码校验 ...
    return jwtUtil.generateToken(user.getId(), phone, "admin", false);
}
```

### 2.4 JWT 配置

**application.yml**（`server/src/main/resources/application.yml`）：

```yaml
jwt:
  secret: agritainment-secret-key-must-be-at-least-256-bits-long-for-hs256
  expiration: 604800000   # 7天（毫秒）
```

- 签名算法：HMAC-SHA256
- 有效期：7 天

### 2.5 返回格式

所有签发接口统一返回：

```json
{
  "code": 0,
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIs..."
  }
}
```

---

## 3. Token 存储（客户端）

### 3.1 双重存储机制

Token 在客户端存储在两个位置：

| 存储位置 | 用途 | 读取时机 |
|---------|------|---------|
| Pinia state (`auth.token`) | 响应式状态管理，驱动 UI 更新 | 组件中通过 `useAuthStore()` 访问 |
| `uni.setStorageSync('token')` | 本地持久化，跨会话保持 | API 请求时读取、启动时恢复 |

### 3.2 存储代码

**auth store**（`client/src/stores/auth.js`）：

```javascript
export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: uni.getStorageSync('token') || '',      // 启动时从本地恢复
    userInfo: uni.getStorageSync('userInfo') || null
  }),
  actions: {
    async login(phone, code) {
      const data = await authApi.login(phone, code)
      this.token = data.token                     // ① 写入 Pinia state
      uni.setStorageSync('token', data.token)     // ② 持久化到本地存储
      await this.fetchUserInfo()                  // ③ 拉取用户信息并持久化
      this.bindWxOpenid()                         // ④ 绑定微信 openid
      this.navigateToRoleHome()                   // ⑤ 按角色跳转首页
    },
    async adminLogin(phone, password) {
      const data = await authApi.adminLogin(phone, password)
      this.token = data.token
      uni.setStorageSync('token', data.token)
      await this.fetchUserInfo()
      this.navigateToRoleHome()
    },
    async register(phone, code) {
      const data = await authApi.register(phone, code)
      this.token = data.token
      uni.setStorageSync('token', data.token)
      await this.fetchUserInfo()
      this.bindWxOpenid()
    }
  }
})
```

### 3.3 登录状态判断

```javascript
getters: {
  isLoggedIn: (state) => !!state.token,    // 仅基于 token 是否存在判断
  isMember: (state) => state.userInfo?.isMember || false,
  role: (state) => state.userInfo?.role || 'customer',
  isStaff: (state) => state.userInfo?.role === 'staff',
  isAdmin: (state) => state.userInfo?.role === 'admin',
  userId: (state) => state.userInfo?.id
}
```

> **注意**：`isLoggedIn` 仅检查 token 是否存在，不做有效性预检。Token 过期场景依赖 401 响应被动处理。

---

## 4. Token 传递（客户端 → 服务端）

### 4.1 HTTP 请求注入

**request.js**（`client/src/api/request.js`）在每次请求时从本地存储读取 token，注入 `Authorization` header：

```javascript
function request(options) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')          // 从 Storage 读取
    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data,
      header: {
        'Content-Type': 'application/json',
        'Authorization': token ? `Bearer ${token}` : '', // Bearer 方式注入
        ...options.header
      },
      // ...
    })
  })
}
```

### 4.2 文件上传注入

文件上传同样注入 token：

```javascript
export function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.uploadFile({
      url: BASE_URL + '/upload',
      filePath,
      name: 'file',
      header: {
        'Authorization': token ? `Bearer ${token}` : ''
      },
      // ...
    })
  })
}
```

### 4.3 请求格式

```
GET /api/v1/auth/me HTTP/1.1
Host: localhost:8080
Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
Content-Type: application/json
```

---

## 5. Token 验证（服务端拦截器）

### 5.1 两层拦截器架构

请求到达服务端后，依次经过两层拦截器：

```
请求 → AuthInterceptor（认证） → RoleInterceptor（鉴权） → Controller
```

### 5.2 第一层：认证拦截器

**AuthInterceptor**（`server/src/main/java/com/agritainment/interceptor/AuthInterceptor.java`）：

```
1. 放行 OPTIONS 预检请求
2. 从 Authorization header 提取 Bearer token
3. 调用 jwtUtil.parseToken(token) 验证签名和过期
4. 验证成功 → 注入 request 属性
5. 验证失败 → 返回 { code: 40101, message: "Token无效或已过期" }
```

注入的 request 属性：

| 属性名 | 来源 | 类型 | 用途 |
|-------|------|------|------|
| `userId` | `claims.getSubject()` | Long | 标识当前用户 |
| `role` | `claims.get("role")` | String | 角色鉴权 |
| `isMember` | `claims.get("isMember")` | Boolean | 会员判断 |

### 5.3 第二层：角色拦截器

**RoleInterceptor**（`server/src/main/java/com/agritainment/interceptor/RoleInterceptor.java`）：

```
1. 读取 request.getAttribute("role")
2. 检查 @RequireRole 注解要求的角色
3. 角色匹配 → 放行
4. 角色不匹配 → 返回 { code: 40301, message: "权限不足" }
```

### 5.4 排除路径（不需要 Token）

**WebConfig**（`server/src/main/java/com/agritainment/config/WebConfig.java`）：

| 路径 | 说明 |
|------|------|
| `/api/v1/auth/login` | 登录 |
| `/api/v1/auth/register` | 注册 |
| `/api/v1/auth/admin-login` | 管理员登录 |
| `/api/v1/auth/sms-code` | 发送验证码 |
| `/api/v1/journals/shared` | 公开动态 |
| `/api/v1/products` | 公开产品列表 |
| `/api/v1/products/{id}` | 公开产品详情 |
| `/api/v1/dining/dishes` | 公开菜品列表 |

---

## 6. Token 过期与失效处理

### 6.1 服务端：Token 过期检测

```java
// JwtUtil.parseToken() 内部
Jwts.parser()
    .verifyWith(getKey())
    .build()
    .parseSignedClaims(token)   // 过期时抛出 ExpiredJwtException
    .getPayload();
```

`AuthInterceptor` 捕获异常后返回：

```json
{ "code": 40101, "message": "Token无效或已过期" }
```

### 6.2 客户端：401 自动处理

**request.js** 检测到 40101 或 HTTP 401 后：

```javascript
if (res.data.code === 40101 || res.statusCode === 401) {
    uni.removeStorageSync('token')       // 清除本地 token
    uni.removeStorageSync('userInfo')    // 清除用户信息
    if (token) {
        uni.showToast({ title: '登录已过期，请重新登录', icon: 'none' })
        setTimeout(() => {
            uni.navigateTo({ url: '/pages/login/index' })  // 跳转登录页
        }, 1500)
    }
    reject(res.data)
}
```

### 6.3 客户端：403 权限不足

```javascript
if (res.data.code === 40301) {
    uni.showToast({ title: '权限不足', icon: 'none' })
    reject(res.data)
}
```

---

## 7. 主动登出

**auth store** 的 `logout()` 方法：

```javascript
logout() {
    this.token = ''                        // 清除 Pinia state
    this.userInfo = null
    uni.removeStorageSync('token')         // 清除本地存储
    uni.removeStorageSync('userInfo')
    uni.navigateTo({ url: '/pages/login/index' })  // 跳转登录页
}
```

---

## 8. 启动恢复

### 8.1 Pinia Store 初始化

```javascript
state: () => ({
    token: uni.getStorageSync('token') || '',
    userInfo: uni.getStorageSync('userInfo') || null
})
```

应用启动时，auth store 从 `uni.Storage` 恢复 token 和 userInfo。

### 8.2 App.vue 启动检查

```javascript
onLaunch() {
    const store = useAuthStore()
    if (!store.isLoggedIn) {   // isLoggedIn = !!token
        uni.switchTab({ url: '/pages/profile/index' })
    }
}
```

### 8.3 路由守卫

**guard.js**（`client/src/utils/guard.js`）：

```javascript
export function checkAuth() {
    const token = uni.getStorageSync('token')
    if (!token) {
        // 未登录 → 跳转登录页
        uni.redirectTo({ url: '/pages/login/index' })
        return false
    }
    return true
}

export function checkRole(allowedRoles) {
    const userInfo = uni.getStorageSync('userInfo')
    if (!userInfo || !userInfo.role) {
        uni.navigateTo({ url: '/pages/login/index' })
        return false
    }
    if (allowedRoles && !allowedRoles.includes(userInfo.role)) {
        uni.showToast({ title: '权限不足', icon: 'none' })
        setTimeout(() => {
            uni.switchTab({ url: '/pages/index/index' })
        }, 1500)
        return false
    }
    return true
}
```

---

## 9. 微信 OpenID 绑定（附加流程）

登录/注册成功后，客户端自动绑定微信 openid，用于后续推送订阅消息：

```
1. 客户端调用 uni.login({ provider: 'weixin' }) 获取微信临时 code
2. 发送 POST /api/v1/auth/wx-login { code, userId }
3. 服务端 WechatService.code2Session(code) 用 code 换取 openid
4. AuthService.bindOpenid(userId, openid) 将 openid 写入用户记录
```

此流程不涉及用户认证 JWT 的变更，仅将 openid 绑定到已有用户。

---

## 10. 完整流程图

```
┌──────────┐    POST /auth/login     ┌──────────┐   generateToken()   ┌───────────┐
│  登录页   │ ──────────────────────► │AuthCtrl  │ ─────────────────► │ AuthService│
│ (client) │   {phone, code}         │ (server) │                     │ (server)  │
└──────────┘                         └──────────┘                     └─────┬─────┘
     ▲                                                                    │
     │                       { token: "eyJ..." }                          │
     │ ◄──────────────────────────────────────────────────────────────────┘
     │
     │  ① this.token = data.token
     │  ② uni.setStorageSync('token', ...)
     │  ③ uni.setStorageSync('userInfo', ...)
     │  ④ bindWxOpenid()
     │  ⑤ navigateToRoleHome()
     │
     ▼
┌──────────┐   GET /api/v1/xxx        ┌────────────────┐   parseToken()   ┌──────────┐
│  业务页面 │ ──────────────────────► │AuthInterceptor │ ──────────────► │ JwtUtil  │
│ (client) │  Authorization:          │   (server)     │                  │ (server) │
└──────────┘  Bearer eyJ...           └───────┬────────┘                  └──────────┘
                                              │
                    ┌─────────────────────────┤
                    │ 验证失败                 │ 验证成功
                    ▼                         ▼
             { code: 40101 }          注入 userId/role/isMember
                    │                         │
                    ▼                         ▼
             客户端清除 token          ┌────────────────┐
             跳转登录页               │RoleInterceptor │
                                     │   (server)     │
                                     └───────┬────────┘
                                             │
                    ┌────────────────────────┤
                    │ 角色不匹配              │ 角色匹配
                    ▼                        ▼
             { code: 40301 }          ┌────────────────┐
             提示"权限不足"            │  Controller    │
                                     │   (server)     │
                                     └────────────────┘
```

---

## 11. 涉及文件清单

### 服务端

| 文件 | 职责 |
|------|------|
| `server/.../util/JwtUtil.java` | JWT 创建、解析、签名验证 |
| `server/.../controller/AuthController.java` | 认证 API 入口（register/login/admin-login/wx-login/me） |
| `server/.../service/AuthService.java` | 认证业务逻辑 + JWT 签发 + openid 绑定 |
| `server/.../interceptor/AuthInterceptor.java` | 请求拦截：提取 Bearer token、验证、注入用户属性 |
| `server/.../interceptor/RoleInterceptor.java` | 角色鉴权：读取注入的 role 属性校验权限 |
| `server/.../config/WebConfig.java` | 拦截器注册 + 排除路径配置 |
| `server/.../service/WechatService.java` | 微信 access_token 缓存 + code2Session + 订阅消息 |
| `server/.../annotation/RequireRole.java` | 角色要求注解 |
| `server/.../entity/User.java` | 用户实体（含 openid 字段） |
| `server/.../resources/application.yml` | JWT secret + expiration 配置 |

### 客户端

| 文件 | 职责 |
|------|------|
| `client/src/stores/auth.js` | Pinia auth store：token 状态管理 + 登录/登出/注册 |
| `client/src/api/request.js` | 请求封装：token 注入 header + 401 自动清除跳转 |
| `client/src/api/index.js` | API 接口定义（authApi 等） |
| `client/src/utils/guard.js` | 路由守卫：checkAuth/checkRole/navigateWithGuard |
| `client/src/utils/tabBar.js` | 角色首页路由映射 |
| `client/src/App.vue` | 启动时检查 isLoggedIn 决定跳转 |
| `client/src/pages/login/index.vue` | 用户登录页 |
| `client/src/pages/admin/login/index.vue` | 管理员登录页 |
| `client/src/components/AppTabBar.vue` | 底部导航栏：从 userInfo 读取角色决定 tab |

---

## 12. 设计特点与注意事项

1. **双重存储**：Token 同时存在于 Pinia state（响应式）和 `uni.Storage`（持久化），但 API 请求时从 `uni.getStorageSync` 读取而非 Pinia state，确保即使 store 未初始化也能获取 token。

2. **被动过期**：不做 token 有效期预检，完全依赖 401 响应被动触发清除和跳转。优点是实现简单，缺点是用户可能在 token 过期后第一次操作才感知到需要重新登录。

3. **无 Token 刷新机制**：当前未实现 refresh token，token 过期后需重新登录获取新 token。

4. **JWT 无状态**：服务端不存储 token 状态，token 一旦签发在有效期内始终有效，无法主动使某个 token 失效（如踢人下线场景无法支持）。

5. **微信 openid 绑定**：登录/注册成功后自动调用 `uni.login()` 获取微信 code，发送到 `/auth/wx-login` 绑定 openid，用于后续推送订阅消息。此流程不修改 JWT。

6. **两种 Token 互不干扰**：用户认证 JWT（客户端存储传递）和微信 access_token（服务端内存缓存，用于调用微信 API），各自独立管理。
