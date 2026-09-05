# 决策依据

- 已验证 PR #161 在 2026-09-02 合入 main，merge 5574af7352b7f7f85c4240795fa2fd226b90c973；当前起点 c7e8532f。
- 目录将 server 标 deprecated，UPMS save/update 拒绝新增 deprecated，UI 禁用该选项，照片仅接受照片细分权限。
- 选择恢复 server 为主授权并保留原细分为精确兼容：符合内部管理目标，不破坏已运行客户端。
- 不整体 revert PR：原 PR 中 token 撤销、缓存、表单处理等改进仍有用，完整撤回会夹带回归。
- 不仅启用兼容开关：无法解决新建 server 应用受阻和照片不接受 server。
- 图谱 Tier2 generation 2026-09-05T02:08:14Z；相关 Java/JS 目录文件无记录缺口，方法调用链以源码校核，图谱存在 trim/hasText 同名误连，不依此断言运行调用。

- 下载修复已获用户追加授权；按目标 photoId 定位随行人员，再检查关联有效申请，避免每次下载生成全量 pending 清单。
- FileReceiver 的 HutoolPhotoServerClient 将 404 映射为 NOT_FOUND，PhotoPullTask 计为缺图并跳过，不刷新 token；现有客户端兼容拒绝响应。
- 本轮 Tier2 图谱 generation 2026-09-05T03:06:15Z 指向共享 main；生产候选文件覆盖无记录缺口，新增 worktree 测试不在该索引，直接读取源码。图谱未识别下载控制器 caller，已用源码调用检索兜底。
