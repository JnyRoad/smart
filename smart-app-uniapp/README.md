# smart-app-uniapp

「裕慧家园」移动 App（manifest 描述为"智慧工厂"，appid `__UNI__0973011`，当前 versionName 1.0.41），面向园区员工的原生 App 客户端。

与 `smart-h5`（微信公众号 H5）**并行使用、互不替代**：部分场景走 App（如推送、人脸识别登录），部分场景走公众号 H5。两者各自维护。

后端对接本仓库网关：认证走 `/auth/oauth/token` 等 `smart-auth` 端点，业务走 `/app/**` 端点（`smart-module/smart-app` 后端模块）。注意 `smart-app-uniapp`（本目录，移动客户端）与 `smart-module/smart-app`（后端业务模块）是两个不同的东西，不要混淆。

## 技术栈

- uni-app（HBuilderX 可视化工程，App-plus 打包目标，Android / iOS）
- Vue 2 + Vuex
- UniPush 推送、摄像头人脸识别、定位
- npm 依赖：`crypto-js`、`image-tools`、`uni-request`、`vue-pdf`

## 目录结构

```text
smart-app-uniapp/
├── api/              # 按业务拆分的接口封装（考勤、薪资、离职、访客、招聘、请假等）
├── common/           # 公共样式（css/）与公共脚本（js/）
├── components/       # 通用组件
├── config/           # 端点常量（apis.js）、权限（authority.js）、字典、弹窗/系统封装
├── hybrid/           # web-view 使用的本地 HTML 资源
├── mixins/           # 全局 mixin（网络恢复重启、隐藏输入框等）
├── pages/page/       # 业务页面（home / logins / mine / service / tabbar 等）
├── static/           # 运行时静态资源（会原样打进应用包，首启解压，注意控制体积）
├── tools/            # 请求封装（request.js）、本地存储（storage.js）等工具
├── unpackage/res/    # HBuilderX 生成的 App 图标与启动图，被 manifest.json 引用，必须入库
├── App.vue           # 应用入口配置与全局样式
├── main.js           # Vue 初始化入口
├── manifest.json     # 打包配置：应用名、appid、版本、权限、图标/启动图路径
├── pages.json        # 页面路由与导航配置
├── store.js          # Vuex 全局状态
└── uni.scss          # uni-app 内置样式变量
```

## 开发与打包

本工程是 HBuilderX 可视化工程（非 CLI 工程），没有命令行构建脚本：

1. 用 HBuilderX 打开本目录；
2. 首次需 `npm install` 安装依赖；
3. 通过 HBuilderX 菜单"运行"到真机/模拟器调试，"发行"做云打包生成 APK / IPA。

## 约定与注意事项

- `unpackage/` 下只有 `res/` 入库（图标与启动图，manifest.json 引用了 33 处，且是 HBuilderX"自动生成图标"功能的固定落盘目录）；`dist/`、`debug/`、`release/`、`cache/` 均为编译产物，已在本目录 `.gitignore` 中忽略。不要把 `unpackage/` 整体加入忽略，会丢启动图。
- App 签名文件（`*.keystore` / `*.jks`）严禁入库。
- 仓库根 `.gitignore` 全局忽略 `package-lock.json`，因此本工程依赖不锁版本；依赖仅 4 个且处于维护态，可接受。
- 接口端点集中在 `config/apis.js`，环境地址等配置在 `tools/request.js`，无独立 env 文件。
