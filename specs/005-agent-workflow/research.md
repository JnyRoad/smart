# 决策依据

- 决策：复用已提交的 `.specify/` 和 `specs/`。依据：main 已含 34 个运行时文件和 32 个规格文件，无需重新初始化。
- 决策：去除 `.superpowers/`、`**/docs/superpowers/` 的整体忽略。依据：21 份门锁资料因此仅存在本地。
- 决策：保持 `.specify/feature.json` 本机化。依据：它是每个 checkout 的当前功能指针；续作使用 `SPECIFY_FEATURE_DIRECTORY` 绑定原规格。
- 决策：根规则保留写入门禁，三个独立文档只承载细节。依据：任何 Agent 都必须先看到不可漏读约束，同时避免重复加载长清单。
- 决策：隔离发生在规格写入之前。依据：Spec Kit 分支 hook 可选，规格目录与 Git 分支独立，不能承担强制 worktree 隔离。
- 决策：工作流在分析任务后交给 superpowers。依据：项目 YAML 仍自动 implement，与当前使用方式冲突。
