# 版本控制规约

## 分支模型（建议形式化）

仓库当前分支策略不明确（首次进入时仅有 `main` 与一个 worktree 分支 `dazzling-jones-960dc1`，且只有 2 个 commit）。建议采用：

| 分支 | 用途 | 保护 |
|------|------|------|
| `main` | 生产线主干，永远可发布 | ✅ 禁止直推，仅经 PR 合并 |
| `release/yuto-3.0` | 当前生产版本维护 | ✅ |
| `feat/<short-kebab>` | 功能开发 | - |
| `fix/<short-kebab>` | Bug 修复 | - |
| `hotfix/<short-kebab>` | 生产紧急修复（从 release 分支拉，回合 main） | - |
| `refactor/<short-kebab>` | 重构 |
| `docs/<short-kebab>` | 仅文档 |
| `chore/<short-kebab>` | 杂务（依赖、构建脚本） |

## Commit 规约

遵循 **Conventional Commits**（与全局 CLAUDE.md 一致）：

```
<type>(scope): <summary>

<optional body in English>
```

- type：`feat` / `fix` / `docs` / `refactor` / `test` / `chore` / `perf` / `build` / `ci` / `style` / `revert`
- summary：英文祈使句，不以句号结尾
- body：用于说明 what/why/migration/testing 影响

示例：
```
feat(upms): support park-aware user paging
fix(auth): refresh token rotation race condition
chore(deps): bump xstream to 1.4.21
```

## PR 规约

- 标题同 Commit 规约（英文）
- 描述按 Summary / Changes / Testing / Risks 四段
- 默认非 draft；合并前需通过 Code Review + 自动化测试（建议引入 SonarQube）
- 禁止 `--force` 推送 `main` / `release/*`

## Tag 与版本号

Maven 版本号当前为 `yuto-3.0-RELEASE`（非 SemVer）。建议：

- 历史版本继续沿用裕同惯例（`yuto-3.0`、`yuto-3.1`…）；
- 在 Git 上每次生产发布打 tag：`v3.0.0`、`v3.0.1`…；
- tag 注释中链接对应 release notes。

## 仓库访问

- 集团内部 GitLab（具体地址见运维），按角色分配 Maintainer / Developer / Reporter。
- 私服 Nexus 用户名密码由项目负责人统一发放。
