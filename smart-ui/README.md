# smart-ui

许昌裕同智慧园区项目前端工程。

基于 Vue 2 + Element UI + Avue 的中后台单页应用，对接 `smart-gateway` 网关下的多个微服务（认证、平台、算法、调度、文件等），覆盖人员/车辆/门禁/告警/预约/宿舍/会议/工单等园区业务场景。

---

## 目录

- [技术栈](#技术栈)
- [基础环境](#基础环境)
- [快速开始](#快速开始)
- [项目脚本](#项目脚本)
- [目录结构](#目录结构)
- [配置说明](#配置说明)
- [构建与部署](#构建与部署)
- [反向代理](#反向代理)
- [代码规范](#代码规范)
- [安全说明](#安全说明)
- [常见问题](#常见问题)

---

## 技术栈

| 类型 | 选型 |
|---|---|
| 框架 | [Vue 2.7](https://v2.vuejs.org/) + [Vue Router 3](https://v3.router.vuejs.org/) + [Vuex 3](https://v3.vuex.vuejs.org/) |
| UI 库 | [Element UI 2.x](https://element.eleme.cn/) + [Avue](https://avuejs.com/)（中后台增强组件） |
| 网络 | [axios 0.27](https://axios-http.com/) + [vue-axios](https://github.com/imcvampire/vue-axios) + [sockjs-client](https://github.com/sockjs/sockjs-client) + [stompjs](https://github.com/jmesnil/stomp-websocket)（WebSocket / STOMP 推送） |
| 图表 | [ECharts 4](https://echarts.apache.org/) |
| 富文本 / 表单 | [wangEditor 3](https://www.wangeditor.com/)、[vue-json-editor](https://github.com/cloydlau/json-editor-vue)、[vue-json-tree-view](https://github.com/arvidkahl/vue-json-tree-view) |
| 文件 / 媒体 | [xlsx](https://github.com/SheetJS/sheetjs)、[file-saver](https://github.com/eligrey/FileSaver.js/)、[html2canvas](https://html2canvas.hertzen.com/)、[v-viewer / viewerjs](https://github.com/mirari/v-viewer)、[vue-pdf](https://github.com/FranckFreiburger/vue-pdf)、[lrz](https://github.com/think2011/localResizeIMG)（图片压缩）、[exif-js](https://github.com/exif-js/exif-js)、[vue-awesome-swiper](https://github.com/surmon-china/vue-awesome-swiper) |
| 工具 | [js-cookie](https://github.com/js-cookie/js-cookie)、[nprogress](https://ricostacruz.com/nprogress/)、[vue-clipboard2](https://github.com/Inndy/vue-clipboard2)、[vue-print-nb](https://github.com/Power-kxLee/vue-print-nb) |
| 加密 | CryptoJS（AES，见 `public/util/aes.js`） |
| 构建 | [Vue CLI 3](https://cli.vuejs.org/) + Babel + Sass |
| 运行容器 | Nginx（生产）/ webpack-dev-server（开发） |

> 部分基础库（vue、vue-router、vuex、axios、element-ui）通过 `vue.config.js → externals` 走 CDN（`public/cdn/*`），不打入主 bundle。

---

## 基础环境

| 工具 | 版本 |
|---|---|
| Node.js | **>= 22**（建议用 nvm/fnm 按 `package.json` 的 `engines` 锁定） |
| 包管理 | pnpm 11.x（以 `pnpm-lock.yaml` 为准） |
| 浏览器 | 现代浏览器，详见 `.browserslistrc`（`> 1%`、`last 2 versions`、`not ie <= 8`） |

---

## 快速开始

```bash
# 1. 进入管理端目录
cd smart-ui

# 2. 安装依赖
pnpm install

# 3. 配置环境变量（可选）
cp .env.example .env.local
# 编辑 .env.local，填入 VUE_APP_SECURITY_ENCODE_KEY

# 4. 启动开发服务器
pnpm dev
# 默认 http://localhost:8080，已在 vue.config.js 中配置后端代理
```

---

## 项目脚本

| 命令 | 作用 |
|---|---|
| `pnpm dev` | 启动本地开发服务器（带 HMR + 后端代理） |
| `pnpm build` | 生产构建，输出到 `dist/` |
| `pnpm lint` | ESLint 检查（`*.js` / `*.vue`） |
| `pnpm pre` | 使用 pnpm 安装依赖 |
| `pnpm build:print-runtime` | 使用 esbuild 独立预构建 pdfme ESM、Worker/WASM 与中文字体到 `public/print-designer/` |
| `pnpm build:print-compatibility` | 通过既有 Vue CLI 构建 Vue2 宿主库，生成只含合成数据的 `dist-print-compat/` 验证页 |

### 模板及组合管理

已增加 `/platform/print/templates` 和 `/platform/print/pairs` 登录路由。选择获准园区后可调用平台接口维护独立单面草稿、发布/回滚指针与两份已发布版本的组合；字段绑定独立于画布样式，预览使用合成数据及授权 PDF 下载。园区范围、功能权限、版本冲突及资源合法性仍由后端校验。

这两个页面需要本任务模板 schema、打印权限配置、可信渲染服务及受控文件适配器。当前尚无实际 Oracle 保存/重启验收，不能用离线测试证明持久化可用。静态图片经受控接口保存，模板图片与预览PDF共用打印私有对象表；真实Oracle建表、容量/备份及环境配置仍待验收。人员照片授权、职级适用规则及打印任务页尚未实现。旧访客打印入口保持原链路。下面的 18763 验证页仍是独立内存演示，不等同于这些正式接口。

### pdfme 兼容验证

当前实现是 `src/components/print/PdfmeHost.vue` 的生命周期封装及单面模板基础，`SingleTemplateWorkspace.vue` 提供合成模板的独立编辑、内存版本保存及正反面组合选择。正式模板与组合管理页面已新增，持久化和设备验收仍以[模板打印规格](../specs/009-print-template-designer/tasks.md)为准。

`@pdfme/ui`、`common`、`schemas` 固定为 `6.1.12`。直接让现有 Webpack 4 解析 pdfme 会在现代语法处失败，因此只为设计器使用 esbuild `0.27.7` 预构建，同源模块按需加载。运行时使用 React，Vue2 仅管理外层 DOM 和实例生命周期。业务侧读取 `getTemplate()` 时必须处理“属性修改尚未应用”的拒绝；上游属性面板有延迟提交，不能在编辑事件后立即默认为已保存。

验证页从同仓库 `smart-print-renderer/assets/fonts/` 复制固定 Noto Sans CJK SC 字体和 OFL 许可。需要完整仓库，使用 Node 24 / pnpm 11.3.0。每份模板只有一张画布；厂牌正面与背面各自保存，组合可引用不同版本号，保存新版不自动更新已关联组合。访客不显示背面或翻面选项。切换模板前读取稳定草稿，遇到待应用属性时阻止切换并提示。它只在页面内存保存模板和组合，刷新会重置，不调用后端或打印机：

```bash
pnpm install --frozen-lockfile --ignore-scripts
VUE_APP_PLATFORM_URL=http://127.0.0.1:1 \
VUE_APP_BASE_URL=http://127.0.0.1:1 pnpm build:print-compatibility
# 先确认 18763 未被其他任务占用，仅暴露生成的合成验证页。
python3 -m http.server 18763 --bind 127.0.0.1 --directory dist-print-compat
```

访问 `http://127.0.0.1:18763/` 可切换厂牌两面/访客单面、修改版式、内存保存重开和卸载重挂载。访客示例 `58 × 80 mm` 是软件夹具尺寸，实际纸张和裁切以 QL-800 介质档案及试打为准。

`pnpm dev` / `pnpm build` 会先预构建设计器。Docker 从仓库根目录执行 `docker build -f smart-ui/Dockerfile .`；`Dockerfile.dockerignore` 只允许管理端源码及固定字体进入上下文，排除环境文件、依赖、其他项目和生成资源。开发 Compose 已同步此上下文。生成资源和 `dist-print-compat/` 不入库，正式分发须保留字体许可、pdfme 法律注释和 PDFium 第三方声明。当前本地浏览器与构建结果见[验收记录](../specs/009-print-template-designer/quickstart.md)。

---

## 目录结构

```
smart-ui/
├── public/                    # 不经 webpack 处理、原样拷贝到 dist 的静态资源
│   ├── index.html             # HTML 模板（含 CDN 脚本与 config.js 引用）
│   ├── config.js              # 运行期配置覆盖（部署时可改，无需重新构建）
│   ├── favicon.ico
│   ├── cdn/                   # 第三方库 CDN 副本（vue、element-ui、avue 等）
│   ├── util/                  # 早期加载的工具脚本（CryptoJS / AES 等）
│   ├── img/  svg/  resource/  # 静态图片、SVG、其他资源
│
├── src/
│   ├── main.js                # 应用入口（注册全局组件、过滤器、插件）
│   ├── App.vue                # 根组件
│   ├── permission.js          # 路由守卫 / 鉴权
│   ├── error.js               # 全局错误捕获 / 日志上报
│   │
│   ├── api/                   # 后端接口封装，按业务域拆分
│   │   ├── login.js
│   │   ├── admin/             # 用户、角色、菜单、字典等管理接口
│   │   ├── gen/               # 代码生成相关接口
│   │   └── platform/          # 园区平台业务接口
│   │
│   ├── router/                # 路由与 axios 配置
│   │   ├── router.js          # Vue Router 主实例
│   │   ├── axios.js           # axios 实例 / 拦截器
│   │   ├── avue-router.js     # Avue 路由扩展
│   │   ├── page/              # 主框架页路由
│   │   ├── views/             # 业务页面路由
│   │   └── platform/          # 平台路由
│   │
│   ├── store/                 # Vuex
│   │   ├── index.js
│   │   ├── getters.js
│   │   └── modules/           # user / tagsView / dict 等模块
│   │
│   ├── views/                 # 业务页面
│   │   ├── admin/             # 系统管理：user/role/menu/dept/dict/log/token/...
│   │   ├── platform/          # 园区业务：alarm/appointment/area/basic/business/...
│   │   └── gen/               # 代码生成器
│   │
│   ├── page/                  # 框架级页面
│   │   ├── index/             # 主框架（侧边栏 + 顶栏 + 标签页）
│   │   ├── login/             # 登录
│   │   ├── lock/              # 锁屏
│   │   └── logs/              # 日志查看
│   │
│   ├── components/            # 通用组件
│   │   ├── basic-container/
│   │   ├── tce-search-bar/
│   │   ├── tce-img/
│   │   ├── tce-label-justify/
│   │   ├── iframe/
│   │   ├── error-page/
│   │   └── empty/
│   │
│   ├── config/                # 全局配置（env、菜单、常量）
│   ├── const/                 # 业务常量 / 枚举
│   ├── filters/               # Vue 过滤器
│   ├── mixins/                # Vue mixin
│   ├── styles/                # 全局样式（含 variables.scss，自动注入）
│   ├── util/                  # 工具函数
│   │   └── security.js        # 加密 key 读取
│   ├── vendor/                # 本地三方库（非包管理器安装）
│   └── docker/                # 备用 Dockerfile（与根目录 Dockerfile 区分使用）
│
├── .env.example               # 环境变量样例
├── .browserslistrc            # 目标浏览器
├── .eslintrc.js               # ESLint 规则
├── .prettierrc.js             # Prettier 规则
├── .editorconfig
├── babel.config.js
├── vue.config.js              # Vue CLI 配置（代理 / 别名 / externals / Terser）
├── Dockerfile                 # 生产镜像（Node 构建阶段 + nginx 运行阶段）
├── nginx.conf                 # 生产 nginx 配置（反向代理至 smart-gateway）
└── package.json
```

---

## 配置说明

### 环境变量（构建期）

定义在项目根目录的 `.env` / `.env.local` / `.env.production` 中（Vue CLI 约定）。

| 变量 | 作用 |
|---|---|
| `VUE_APP_SECURITY_ENCODE_KEY` | 登录密码加密 key，须与后端 `security.encode.key` 一致。详见 [安全说明](#安全说明)。 |

### 运行期配置（`public/config.js`）

部署后可直接修改，无需重新构建。文件在主 bundle 加载前同步引入，挂载在 `window.__SMART_CONFIG__`：

```js
window.__SMART_CONFIG__ = window.__SMART_CONFIG__ || {}
// window.__SMART_CONFIG__.securityEncodeKey = 'REPLACE_AT_DEPLOY_TIME'
```

读取顺序：**运行期 > 构建期**（见 [src/util/security.js](src/util/security.js)）。

### 后端服务地址

- **开发**：在 [vue.config.js](vue.config.js) 顶部 `platformUrl` / `url` 切换。
- **生产**：通过 [nginx.conf](nginx.conf) 反向代理到 `http://smart-gateway:9990`，前端只走相对路径。

---

## 构建与部署

### 本地构建

```bash
pnpm build
# 产物在 dist/
```

构建特性（见 `vue.config.js`）：
- 生产环境自动剥离 `console.*` 与 `debugger`（Terser）
- 不生成 `.map`（`productionSourceMap: false`）
- Vue / Vue Router / Vuex / axios / Element UI 走 CDN（`externals`），减小主包体积

### Docker 镜像

```bash
docker build -t smart-ui:latest .
docker run -d -p 80:80 --name smart-ui smart-ui:latest
```

`Dockerfile` 行为：
1. 在 Node 22 构建阶段执行 `pnpm install --frozen-lockfile` 和 `pnpm build`
2. 在 nginx 运行阶段把构建出的 `dist/` 拷贝到 `/data`
3. 用项目内 `nginx.conf` 覆盖默认配置

> 部署后若需调整加密 key、后端地址等，**改 `/data/config.js` 即可，无需重建镜像**。

---

## 反向代理

`nginx.conf` 把以下前缀代理到网关 `smart-gateway:9990`：

```
/code  /auth  /admin  /algorithm  /file  /push  /app  /platform  /schedule  /data  /gen
```

并对 `/config.js` 做了 method 白名单（仅 GET），防止运行期配置被外部写入。

开发环境的代理规则定义在 `vue.config.js → devServer.proxy`，与生产前缀**不完全一致**：

- dev 独有：`/daemon`、`/tx`、`/act`
- 生产独有：`/file`、`/push`、`/app`、`/schedule`

如新增前缀，需要同时维护两处。

---

## 代码规范

- **ESLint**：`plugin:vue/essential` + 生产环境禁止 `console` / `debugger`（开发期允许）。
- **Prettier**：见 `.prettierrc.js`。
- **EditorConfig**：缩进、换行、字符集统一，建议编辑器装对应插件。
- **lint-staged**：`package.json` 中声明了规则；提交前请手动跑 `pnpm lint`，或按需要接入 Git hook。

提交前本地自检：

```bash
pnpm lint
```

---

## 安全说明

### `securityEncodeKey` 是混淆密钥，不是机密

`VUE_APP_SECURITY_ENCODE_KEY`（构建期）与 `window.__SMART_CONFIG__.securityEncodeKey`（运行期，见 `public/config.js`）用于登录时对密码做对称加密后再传给后端，与后端 `security.encode.key` 一一对应。

由于本项目是 SPA，该值最终会出现在浏览器可读的 JS 代码或静态文件中，**任何访问到页面的用户都能拿到**。因此：

- 它的作用是**防止密码以明文出现在网络抓包/日志中**，配合 HTTPS 增加一层混淆。
- 它**不是**真正的密钥，不需要按机密管理（无需放入 Vault、不需要定期轮换、不需要按后端机密方式限制访问）。
- 前后端值更新时只需保证一致即可，无需走密钥管理流程。

真正的机密（数据库密码、第三方 API Key、JWT 签名密钥等）必须保留在后端，不得放入本前端项目。

### 其他

- 生产构建已剥离 `console` / `debugger` 与 source map，避免源代码与日志泄漏。
- `public/config.js` 在 nginx 层禁止非 GET 方法（见 [nginx.conf](nginx.conf)），防篡改。
- 严禁把后端机密、第三方 API Secret 写入 `.env*` 或 `public/`。

---

## 常见问题

**Q: `pnpm dev` 启动后接口都报跨域？**
A: 确认 `vue.config.js` 顶部的 `platformUrl` / `url` 指向了可达的后端环境。

**Q: 登录返回 "securityEncodeKey is not configured"？**
A: 没有配置加密 key。开发期把 `VUE_APP_SECURITY_ENCODE_KEY=xxx` 写到 `.env.local`；生产期把值写到 `dist/config.js`（或镜像里的 `/data/config.js`）的 `window.__SMART_CONFIG__.securityEncodeKey`。

**Q: 改了 `public/config.js` 还是没生效？**
A: 浏览器有强缓存。部署时给 `config.js` 加 `Cache-Control: no-cache` 或带 hash 参数；本地调试 Ctrl+F5 / 清缓存重试。

**Q: 构建产物太大？**
A: 大头基本是 `xlsx`、`echarts`、`wangeditor`。按需引入或按路由懒加载（`() => import('...')`) 可以显著瘦身。

**Q: Node 版本不一致导致 sass / node-gyp 编译失败？**
A: 按 `package.json` 的 `engines` 锁定 Node；必要时删除 `node_modules` 后执行 `pnpm install --frozen-lockfile`。
