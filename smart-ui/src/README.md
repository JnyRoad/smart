# smart-ui/src

`smart-ui/src/` 保存管理端前端生产源码。

## 目录结构

```text
smart-ui/src/
├── README.md
├── api/          # 后端接口封装，按 admin / gen / platform 等业务域拆分
├── components/   # 跨页面通用组件
├── config/       # 全局配置
├── const/        # CRUD 配置、枚举和常量
├── filters/      # Vue 过滤器
├── mixins/       # Vue mixin
├── page/         # 框架级页面：登录、主框架、锁屏、日志
├── router/       # 路由、axios 和 Avue 路由扩展
├── store/        # Vuex 状态
├── styles/       # 全局样式和业务样式
├── util/         # 工具函数
├── vendor/       # 本地第三方代码
└── views/        # 管理端业务页面
```

## 维护规则

- API 封装按业务域放在 `api/`，页面放在 `views/`。
- 跨页面组件放 `components/`，不要把业务页面塞进通用组件目录。
- 新增路由时同步检查 `router/`、菜单权限和后端路由配置。
