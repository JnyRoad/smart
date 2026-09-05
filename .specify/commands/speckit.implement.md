---
description: 本项目禁用 Spec Kit 直接实现入口，改由 superpowers 执行已分析的任务清单
---

# 实现入口已禁用

本项目不执行 `/speckit.implement`，本命令不会写入业务代码、测试、配置或任务文件。

请按以下顺序交接：

1. 确认当前任务位于非 `main` 分支的 linked worktree；主目录或共享 checkout 禁止写入。
2. 缺少本机 `.specify/feature.json` 时，显式设置
   `SPECIFY_FEATURE_DIRECTORY=specs/<已有目录>`，不要重新初始化或重新生成已有规格。
3. 运行 `/speckit.analyze` 并处理其一致性结论；分析通过后，使用
   `superpowers:test-driven-development` 和适用的执行技能，按 `tasks.md` 逐项实现。
4. 业务行为测试必须先于实现任务，并先确认测试按预期失败；纯文档或低风险配置使用链接、
   结构、解析或差异检查。

工作流在 `speckit.tasks` 后执行 `speckit.analyze`，通过审查门后再交给 superpowers；不要从
其他路径直接调用本命令执行代码。
