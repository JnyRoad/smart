# smart-h5 迁移 Next.js 可行性评估报告

日期：2026-06-11
范围：smart-web-app（smart-h5）全量功能盘点 + 迁移 Next.js 的风险评估
结论先行：**技术上可行，但当前项目形态下收益极低、成本与风险极高，不建议直接迁移 Next.js；如目标是架构现代化，建议优先 Vue 3 + Vite（或 Nuxt 3）路线。**

---

## 1. 现状盘点

### 1.1 技术栈

| 维度 | 现状 |
|------|------|
| 框架 | Vue 2.6.11（已 EOL，2023-12 停止维护） |
| 构建 | @vue/cli-service 3.x + webpack 4 系工具链 |
| UI 库 | cube-ui 1.x（仅支持 Vue 2，已停止维护） |
| 状态 | Vuex 3 + localStorage（AES 加密敏感字段） |
| 路由 | vue-router 3，13 个业务模块（+lock 子模块、test 调试页），共 69 个业务页面路由（逐页清单见 [2026-06-11-page-inventory.md](./2026-06-11-page-inventory.md)） |
| 请求 | axios 0.19 二次封装（多后端模块路由、防重复提交、401 微信 OAuth 重定向） |
| 私有包 | @tce/tce-components、@tce/tce-util、@tce/tce-font（私有 npm registry，Vue 2 专用） |
| 多租户 | js-conditional-compile-loader 条件编译，BUILD_MODULE 支持 h5/jiantao/junya/shunluo/face 五套变体 |
| 部署 | Docker + nginx，SPA 静态托管，后端走 smart-gateway 反代 |
| 测试 | 无单元/集成/E2E 测试，仅 3 个契约检查脚本（scripts/check-*.js） |

### 1.2 业务功能模块（14 个）

login（微信 OAuth/短信/密码，4 页）、home（工作台/公告，3 页）、visitor（访客管理，16 页，含许昌/合肥两套流程）、check-in（宿舍签到，3 页）、dorm（宿舍水电，2 页）、dorm-exit（离宿，3 页）、dorm-repairs（报修，3 页）、good-release-live/work（物品放行，3+11 页）、return-factory（返厂，2 页）、backLog（待办/审批，12 页）、mine（2 页）、help（2 页）、lock（门禁动态密码，2 页，挂载于 /dorm 路由下）。逐页权威清单见 [2026-06-11-page-inventory.md](./2026-06-11-page-inventory.md)。

代码规模：134 个 .vue 文件（实测，含组件），约 3.7 万行。

### 1.3 运行环境强假设

- **嵌在微信浏览器内运行**：微信 OAuth（snsapi_base 静默授权）是唯一主登录链路；wx.scanQRCode 扫码贯穿离宿/物品放行核心流程；JSSDK 签名依赖当前 URL。
- 纯客户端渲染 SPA，重度依赖 `window.__SMART_CONFIG__` 运行时注入、localStorage、userAgent 设备探测。
- **无任何 SEO / SSR 需求迹象**：内部园区工具，不对搜索引擎开放。

---

## 2. 迁移 Next.js 意味着什么

Next.js 是 React 框架。迁移不是"换构建工具"，而是 **Vue → React 的全量重写**：

1. 127 个 .vue 组件全部用 React/JSX 重写（约 2.5 万行模板+逻辑）。
2. cube-ui → 更换为 antd-mobile / 其他 React 移动端组件库，所有表单、弹层、滚动容器交互重做。
3. @tce/* 三个私有 Vue 2 包没有 React 版本，需要内部重新开发或反向实现。
4. Vuex → Zustand/Redux，mixins（form.js 统一提交流程）→ hooks，filters → 工具函数。
5. 条件编译多租户（5 套构建变体）在 Next.js 无原生等价物，需用环境变量 + 动态导入 + 多份构建配置重新设计。
6. 部署模型变化：纯静态 nginx → Node 服务（或 `output: 'export'` 退化为静态导出，但那样 Next.js 的 SSR 能力完全用不上）。

## 3. 风险评估

| # | 风险 | 等级 | 说明 |
|---|------|------|------|
| 1 | **收益与成本严重不匹配** | 🔴 高 | Next.js 核心价值是 SSR/SEO/RSC/边缘渲染。本项目是微信内嵌的内部工具，无 SEO 需求、无首屏 SSR 收益。迁移后获得的能力几乎都用不上 |
| 2 | **零测试覆盖下的全量重写** | 🔴 高 | 无单测/E2E，重写后只能靠人工回归 75+ 页面、5 套租户变体、许昌+合肥双流程。回归遗漏概率极高 |
| 3 | **微信链路重建** | 🔴 高 | OAuth 静默授权、JSSDK config 签名（对 URL 敏感，Next.js 路由行为不同）、scanQRCode 扫码，全部需在 React 下重新调通，且只能在微信真机环境验证 |
| 4 | **@tce 私有包断供** | 🔴 高 | 三个私有包是 Vue 2 专用，React 版不存在；表单体系（tce-form 系列）是全站表单基础 |
| 5 | **多租户条件编译重新设计** | 🟡 中 | 5 套 BUILD_MODULE 变体迁移方案需要专门设计与逐一验证 |
| 6 | **团队技能切换** | 🟡 中 | 现有代码全是 Vue 范式（mixins/filters/options API），团队若无 React 储备，开发+维护成本双升 |
| 7 | **部署架构变化** | 🟡 中 | 引入 Node 运行时（除非静态导出），Docker/nginx/网关链路需要改造，运维面扩大 |
| 8 | **双轨期业务停摆或并行维护** | 🟡 中 | 预估 2-3 人 2-3 个月重写期间，新需求要么冻结要么双端实现 |

## 4. 替代方案对比

| 方案 | 内容 | 成本 | 收益 | 建议 |
|------|------|------|------|------|
| A. 迁移 Next.js（用户提议） | Vue→React 全量重写 | 极高（2-3 人月 ×2-3） | 低（SSR 用不上；仅获得 React 生态） | ❌ 不推荐，除非公司层面统一 React 技术栈 |
| B. **Vue 3 + Vite 升级** | 同生态升级：Vue 2→3、vue-cli→Vite、cube-ui→Vant 4、Vuex→Pinia | 中（组件可渐进迁移，模板大部分复用） | 高：摆脱 EOL 框架、构建提速 10 倍+、生态恢复活水 | ✅ 推荐主路线 |
| C. Nuxt 3 | 想要"Next.js 式架构"但留在 Vue 生态 | 中高 | 与 B 类似 + SSR 能力（但同样用不上） | ⚠️ 仅当确有服务端渲染诉求 |
| D. 维持现状 + 局部加固 | 锁定依赖、补测试 | 低 | 低（技术债继续累积） | 短期兜底可接受 |

## 5. 结论与建议

1. **"Next.js 比较领先"对本项目不成立**：领先体现在 SSR/RSC/SEO/边缘渲染，而 smart-h5 是微信内嵌、纯客户端渲染的内部工具，这些能力一项都消费不到。迁移等于花全量重写的钱，买一堆用不上的特性。
2. **真正的痛点是 Vue 2 EOL + webpack 4 工具链老化**，对症方案是 **方案 B（Vue 3 + Vite + Vant + Pinia）**：模板/业务逻辑大量复用，可按模块渐进迁移，风险可控。
3. 若公司确有"统一 React 技术栈"的组织级决策，方案 A 才值得做；届时必须先补 E2E 回归测试（至少覆盖访客、离宿、物品放行三条微信扫码主链路），并先做微信 JSSDK 在 Next.js 下的 spike 验证，再启动重写。
4. 无论选哪条路线，**@tce 私有包的源码归属与维护权**必须先确认，否则任何迁移都会卡死在表单体系上。
