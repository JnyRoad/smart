# smart-h5/src

`smart-h5/src/` 保存微信 H5 生产源码。

## 目录结构

```text
smart-h5/src/
├── README.md
├── app/          # Next.js App Router 路由和页面壳
├── components/   # 跨业务模块复用的 React 组件
├── features/     # 业务域：API、流程规则、状态和局部组件
└── lib/          # API 客户端、认证、配置、加密、格式化、微信 OAuth 等基础设施
```

## 维护规则

- 页面路由放 `app/`，业务规则优先放 `features/<domain>/`。
- 跨模块基础设施放 `lib/`，跨页面 UI 放 `components/`。
- 影响业务行为的纯规则优先补 Vitest 单测。
