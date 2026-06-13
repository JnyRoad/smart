# 术语表

| 术语 | 英文/缩写 | 释义 |
|------|---------|------|
| 智慧家园 | Smart Park | 许昌裕同园区内部对智慧园区平台的统称 |
| UPMS | User Permission Management System | 统一用户权限管理 |
| 园区 | Park | 集团下属的物理园区，每个园区有独立运营，但共享同一身份中台。当前生产实例：许昌园区 |
| TCE | - | 内部代号（包名 com.tce.smart）；包/邮箱中沿用 |
| YHT | 友互通 | 集团内部协同/IM 平台，作为社交登录提供方之一 |
| OCR 登录 | Optical Character Recognition Login | 此处指 **人脸识别登录**（非字面 OCR） |
| Inner 调用 | @Inner | 服务间内部调用注解，被 SmartSecurityInnerAspect 拦截后跳过外部鉴权 |
| 强密码 | Strong Password | sys_user.is_strong_pwd 标识是否满足强密码策略，未满足时强制刷新 |
| Token Enhancer | - | 自定义 OAuth Token 增强器，注入 user_id、parkList、license 等业务字段 |
| 动态路由 | Dynamic Route | 网关路由不写死在配置文件，从 DB+Redis 加载，通过 Nacos 事件刷新 |
| 数据迁移任务 | Move Data Task | 用于辅助旧 UPMS（1.0/2.0）到 3.0 的切换 |
| SysLog | @SysLog | 操作日志注解，触发异步 SysLogEvent → 入 sys_log 表 |
| Jasypt | - | 配置文件敏感字段对称加密库，秘钥通过环境变量传入 |
| Nacos | - | 阿里开源服务注册中心 + 配置中心，本系统单实例承担两职 |
| BOM | Bill of Materials | 依赖版本清单（smart-common-bom） |
