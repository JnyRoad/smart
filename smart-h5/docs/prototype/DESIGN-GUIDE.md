# yuto-park-h5 原型设计规范（所有页面原型必须遵守）

基准：`docs/mockups/2026-06-11-login-mockup.html`（已评审 4.4/5）。本文件是其设计语言的提炼，所有模块原型共用，保证 69 页视觉一致。

## 1. 设计令牌（每个 HTML 原型的 `:root` 原样使用）

```css
:root {
  --brand-orange: #EC6C00;
  --accent-strong: #D95F00;
  --accent-soft: #FFF1E3;
  --accent-ink: #5A2600;
  --white: #FFFFFF;
  --surface: #F7F7F7;
  --yuto-black: #595757;
  --mid-gray: #8C8A8A;
  --weak-gray: #AAA7A7;
  --light-gray: #E5E3E3;
  --border-soft: #EFEEEE;
  --success: #16A673;
  --warning: #C98416;
  --danger: #D83B36;
  --info: #2376D9;
  --shadow-large: 0 24px 70px rgba(89, 87, 87, 0.14);
  --shadow-panel: 0 16px 40px rgba(89, 87, 87, 0.10);
  --font-main: "Helvetica Neue", Helvetica, "PingFang SC", "Hiragino Sans GB", "Microsoft YaHei", Arial, sans-serif;
}
```

## 2. 页面骨架

- 单文件自包含 HTML（无外链资源），`lang="zh-CN"`，viewport `width=device-width, initial-scale=1, viewport-fit=cover`。
- 手机框：`.app { width: min(100%, 430px); margin: 0 auto; min-height: 100dvh; background: var(--white); box-shadow: var(--shadow-large); }`，页面底色 `var(--surface)`，顶部 220px 高的 `--accent-soft` 渐变铺底。
- 安全区：顶部 `max(14px, env(safe-area-inset-top))`、底部 `max(24px, env(safe-area-inset-bottom))`。
- 二级页顶栏：左侧返回箭头（44px 触控区）+ 居中页面标题（16px/750）+ 右侧占位；一级页顶栏用品牌区（裕同智慧园区 wordmark）。

## 3. 组件约定

- **主按钮**：高 48px、圆角 14px、背景 `--brand-orange`、文字白色 650；按下态 `--accent-strong`。次按钮：白底 + `--border-soft` 边框。危险操作 `--danger`。
- **卡片**：白底、圆角 16px、`--shadow-panel`、内边距 16–18px、卡片间距 12px。
- **表单**：标签 13px `--mid-gray`；输入框高 48px、圆角 12px、边框 `--border-soft`，聚焦 3px `rgba(236,108,0,.22)` 外圈；必填项标签后红色 `*`。
- **状态徽章**：圆角 999px、12px/650，待审批 `--warning`、已通过/已放行 `--success`、已拒绝/已驳回 `--danger`、进行中 `--info`，统一用 12% 透明度同色背景。
- **列表项**：卡片式，左侧主信息（标题 15px/700 + 副信息 13px `--mid-gray`），右侧状态徽章/箭头。
- **空态**：居中插画位（用简洁 SVG 圆形底 + 图标）+ 14px `--weak-gray` 说明文字 + 可选引导按钮。
- **触控**：所有可点元素 ≥44px 命中区；字号最小 12px。

## 4. 内容与功能要求

- 文案全中文，mock 数据要真实可信（姓名、车牌、宿舍号、时间用合理值），禁止 lorem/占位英文。
- **功能 100% 对齐旧页**：旧页上的每个字段、按钮、入口、状态在原型里都必须出现；UI 重新设计但功能不增不减。
- 一页内有多个关键状态（如审批中/通过/拒绝）时，原型内用顶部演示切换条（标注「原型演示」）或纵向分段展示全部状态。
- 每个 HTML 头部加注释块：旧路由、旧组件路径、页面调用的接口列表、跳转关系。

## 5. 净室边界（法律红线）

旧仓库 `~/source/YUTO/yuto-smart/smart-h5` 只读参考：只提取功能事实（字段名、文案、流程、接口契约），**禁止复制其任何 HTML 结构、CSS、JS 代码**。原型全部从本规范出发新写。
