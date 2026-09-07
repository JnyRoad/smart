# 研究结论
- 决策：用户确认 uni-app x Vapor，HBuilderX创建的原始manifest保留AppID与字节码配置。新工程smart-app位于仓库根目录。
- 工作台：稳定四页签；常用最多八个，全部应用按业务分类搜索。多权限求并集，但每个具体操作独立校验。
- 扫码：PDA键盘输入无需主动调用厂商SDK；以结束符或明确确认接收完整结果。相机独立适配；Web原生uni.scanCode不支持，显示明确原因。
- 安全：扫码不等于NFC身份核验；真实状态变化由后端决定。前端权限只用于交互体验。
- 已淘汰：Tauri作为全端主框架无法承担小程序编译；保留桌面容器的后续方向。暂不全系统改写后端。
- 工具链：本机HBuilderX 5.24，直接CLI启动遇到Qt neon限制，正在验证内置编译器路径；无真机不声称原生验收。
- 官方资料：https://doc.dcloud.net.cn/uni-app-x/app-vapor.html 、https://doc.dcloud.net.cn/uni-app-x/api/scan-code.html。

## 实际工具链验收补充
HBuilderX 5.24 内置 `plugins/node/node` 为Node22，实际完整工程Android Vapor、微信和支付宝源码编译均成功；Node18在Vapor加载jsc时触发cachedDataRejected，脚本改用内置Node22。Android产物包含12页及16个Kotlin渲染源文件，尚非签名APK或真机运行验收。Web/小程序输出VDOM符合该编译器行为。小程序AppID未配置，产物使用游客ID。


## Phase 10 调查中的已确认事实
- 改造前 `state/session.uts` 的 availablePosts 同时供申请表和执行岗位使用；本阶段将申请候选与执行岗位拆开，普通申请人不再借用执行授权。
- 改造前 loadExecutionApplications 调用不带岗位的 api.list 后在客户端过滤；本阶段改为带岗位的查询契约，工作台/待办也使用该查询；服务端终审接口仍待实现。
- 本 worktree 与共享 checkout 根目录的 .env.local 均不存在；测试环境位置已向用户询问，未读取或输出凭据。
- Java 8 与离线 Maven reactor 编译依赖可用；仅依赖预检成功不计为业务测试通过。

- 决策：保密物品新域放 smart-platform-core，client适配留smart-app。理由：旧 ArticlesRelease 是 OA 出厂/生活/办公放行，SecurityAreaOrder 是供应商预约；字段和状态语义都不足。排除把旧OA状态直接扩展为A/B执行的方案。
- 决策：先实现纯领域校验而不启动伪造后端。理由：测试目标、真实岗位与卡证来源尚缺，独立不可变状态机可先由JUnit验证；它不能替代Oracle并发事务、幂等记录或真实放行。
- 身份核对修正：用户确认供应商人员不登录App，供应商台账不是外包工作人员身份源。已通过既有platform/outsourcing用工模块及SmtOutSrcApply/StaffExt核对工作人员工号与认证，不能基于供应商台账新增账号体系。
- 厂牌核对：已核预约二维码smsCode并不足以证明实际供应商厂牌编码。保持不透明凭证契约，等待脱敏样例和打印来源，不编造二维码解析器。

- 外包用工链路最终核对：导入提供工号，OutSrcApply审批后由StaffExt.saveBatchTemporaryStaff写smt_staff且status=4；旧SysUserService.simpleLogin已有临时人员认证/首次用户初始化。无需因供应商台账缺凭据而新增账号方案。compType与DHR PSNTYPE分别映射，默认App/门禁权限不得当安检岗位。

## Phase11 核实与决策
- UPMS `SysUserService.simpleLogin(username,password)` 本已显式传参；实际需解耦的是common-security读取OAuth请求的UserDetails入口和强密码工具。新入口使用POST JSON，旧GET/OAuth兼容保留；新认证不能命中旧缓存即视作本次密码通过。
- 两个checkout的根与docker下都没有 `.env.local`；样例规定实际文件放仓库根。主checkout的生产env没有读取。
- 已运行Oracle `smart-auth-012-oracle` 带 `codex.task=012-reliable-auth-batch`，属于另一个任务；无容器标签指向本worktree，也没有运行中的smart-gateway。
- 用户明确选择新建本任务独立本机测试环境。采用专用Compose项目、回环端口、数据卷和应用schema，不直接启动固定容器名的通用backend profile。新本地Oracle属于项目自有数据库测试，不模拟DHR等第三方数据库。
