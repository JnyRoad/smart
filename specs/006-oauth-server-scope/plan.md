# Implementation Plan: 内部开放接口恢复 server

**Branch**: `fix/restore-oauth-server-scope` | **Date**: 2026-09-05 | **Spec**: [spec.md](spec.md)

## Summary
将 server 恢复为目录唯一正常授权项；两个细分 scope 改为历史兼容。六个入口主 scope 改为 server，并精确声明各自历史 scope。沿用既有拦截器和兼容开关，server 主授权始终不受开关影响。管理端仅显示正常选项和当前应用已持有的历史项，新建沿用现有非废弃选项筛选，仅提供 server。

## Technical Context
Java 8 / Spring Boot 2.1 / Spring Security OAuth2；Vue 2 / Avue；JUnit4 / Mockito / MockMvc / Vitest。
不增加依赖、不改变存储。OAuth 客户端仍由 UPMS 校验后写入；本次不连接真实数据库、设备或生产服务。兼容旧常量与配置键以支持滚动部署。

## Constitution Check
设计前后均通过：独立 linked worktree 和任务分支已建立；UI 不承担最终鉴权；无 DDL；下载查询复用现有实体和 MyBatis-Plus 参数化条件；中文注释；先行为测试后实现；规格与历史材料保留。主 checkout 未提交内容不触碰。

## Project Structure
- `smart/smart-common/smart-common-security/`：目录、拦截器注释及鉴权回归。
- `smart/smart-upms/smart-upms-biz/`：客户端授权和撤销回归、过时注释修订。
- `smart-ui/src/const/crud/admin/client.js` 和管理页：正常授权选项及历史展示。
- `smart-module/smart-platform/smart-platform-biz/`：照片两个入口和能耗四个入口、真实注解结合拦截器的 MockMvc 回归。
- `smart-module/smart-schedule/`：默认申请 server、保持缓存与旧显式配置兼容。
- `docker/nacos/config/dev/`、`docker/.env.production.example`：示例默认值和说明。
- `docs/yuhui-prototype/yuhui-blueprint.html`：历史与本次未合并状态。

## Implementation and validation
先运行原有相关测试基线。US1 先增加 server 正常授予、细分保留与 UI 行为失败测试，再改目录和表单。
US2/US3 先增加真实控制器入口的 server 正向、空/错误/用户负向及细分兼容测试，再改注解和调度默认。
使用 smart 基础模块 reactor 生成的最新 common-security 依赖执行 smart-module 测试，避免旧本机 JAR 假通过；仅运行相关测试，不启动应用。
保留 scope 变更、secret 重置和删除后的 token 撤销验证。最终自审和独立审查后回填结果。

## US4 下载园区校验续作
控制器将 token 园区范围传入照片服务，移除无园区参数的下载接口。先按目标 fellowPhotoId 查关联 visitorId，再分 1000 批检查申请 ID、授权园区、status=0、endTime>now、applyType!=CAR；命中才读取图片，拒绝及缺图均返回 404。无园区或无关联时提前返回，不扫描全部 pending 照片，不修改通用图片服务。

先增加真实控制器、OAuth 适配器、拦截器和照片服务组合的失败测试，再改接口签名及查询；原 service 单测增加精确参数绑定、授权成功、无关联拒绝、空授权不查库、超过 1000 个申请分批的回归。数据库服务使用 Mock，不把 SQL 条件测试称为真实 Oracle 验收。
本地没有本任务专属 Oracle 实例，不启动或改动共享服务；沿用已存在的表和字段，不做性能承诺。发布前在目标 schema 只读复核两表字段、约束、相关索引/统计和实际参数执行计划，步骤记录在 quickstart.md；真实 Oracle 执行与性能保持未验证。
