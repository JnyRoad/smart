# PR #179 审查判定

审查输入为 [PR #179](https://github.com/JnyRoad/smart/pull/179) 的
`ed0b7cc20a8e2810152bb42ee4a487e17e12d872`，共 9 条未解决、未过时的 CodeRabbit 意见。
按当前源码、仓库规则和已登记证据核实，8 条有需要修正的缺陷或歧义，1 条为可选改进。
这些判定不代表已验证生产缺陷，也不将机器人给出的严重度直接视为实际影响。

| 审查意见 | 判定与处理 |
| --- | --- |
| [宪法首次写入范围](https://github.com/JnyRoad/smart/pull/179#discussion_r3938950559) | 有效的规则歧义。宪法漏列配置、文档和测试，但根 AGENTS 已禁止这些文件在共享目录开发，不能据此认定已允许 main 写入。宪法补全全部开发文件范围，版本升为 1.1.1。 |
| [H5 当前接口契约](https://github.com/JnyRoad/smart/pull/179#discussion_r3938950565) | 有效的事实错误。`smart-h5/src/features/dorm/api.ts` 实际使用 `/get/pwd`、`/update/lock/pwd`、`/update/pwd`，并传 `badge`、`newPwd`、`facePic`。文档按真实路径、方法和负载修正；未来服务端解析本人身份的要求继续保留，未实现新 API。 |
| [原型不可用状态](https://github.com/JnyRoad/smart/pull/179#discussion_r3938950568) | 有效的原型行为缺陷。未入住确认后仍可修改动态码、触发刷新演示。禁用按钮并保护编辑、提交和刷新入口，恢复已入住时恢复可用；通过运行真实内联脚本的回归验证，不改变演示导航为真实页面跳转。 |
| [本机绝对路径](https://github.com/JnyRoad/smart/pull/179#discussion_r3938950571) | 有效的可移植性问题，不属于凭据泄露。来源改为文档登记的离线证据包及构建产物文件名，保留无源码/source map、bundle 还原和 UNVERIFIED 限定。没有将离线 bundle 错标为 FCT-016 截图证据。 |
| [README 增加结构和八条优化建议](https://github.com/JnyRoad/smart/pull/179#discussion_r3938950574) | 未采纳，属于可选改进。此 README 已提供页面索引、逐页规格、实施顺序和验收入口，职责是导航与边界；仓库中未找到要求每份嵌套 README 必须重复八组优化建议的规则。机器人私有路径配置未验证，不能据此认定仓库规则违规。 |
| [未确认的高风险操作](https://github.com/JnyRoad/smart/pull/179#discussion_r3938950575) | 有效的证据与规划表述冲突。既有运行态记录未确认设备开关、解绑、启停、管理员密码或批量改密及网关删除。索引与页面规格标为待核验/受控不可用；版本、角色、权限、API 和设备状态全部确认后才开放，保留设备编辑与 OPEN-008。 |
| [网关端口语义](https://github.com/JnyRoad/smart/pull/179#discussion_r3938950579) | 有效的字段语义错误。既有证据明确该值为 TCP 源端口，页面规格却称服务端口。修正列名，保留仅运维角色可见 IP/端口的限制。 |
| [凭据列展示语义](https://github.com/JnyRoad/smart/pull/179#discussion_r3938950582) | 有效的规格歧义。列名本身不能证明会展示明文，但未说明展示范围会误导实现。明确只显示状态或脱敏摘要，不展示密码内容、完整卡号或指纹模板，与总规则及验收一致。 |
| [提交、推送与 PR 授权](https://github.com/JnyRoad/smart/pull/179#discussion_r3938950586) | 有效的流程歧义，不是已发生越权的证据。FR-007、plan 与 T010 改为分别按各动作已有授权交付；已覆盖授权不重复询问。原交付记录保留真实已执行结果，不自动合并。 |

## 验证与边界

- 本轮复用 `specs/005-agent-workflow`，新增审查任务并同步原始导入与后续修订的验收口径；未重生成规格。
- 首次导入的 21 份资料仍可从 `e43d826a0eb769e61e1d71ed890e4936d0200eec` 取得，已再次核对原始摘要。后续修订通过 Git 差异追溯。
- 验证命令与实际结果见 [quickstart.md](quickstart.md)。原型回归只验证文档内 HTML 脚本，不等于真实浏览器、微信、业务接口、数据库或设备验收。
- 未修改业务源码、主目录原有删除、部署或数据库；未自动合并 PR，未将审查线程手动标记为已解决。

## 后续审查修订

- [验证命令覆盖范围](https://github.com/JnyRoad/smart/pull/179#discussion_r3939029110)：有效。`git diff --check` 只比较工作区与暂存区，遗漏已暂存的空白错误；验证指南改为 `git diff HEAD --check`，以 HEAD 为基线同时覆盖暂存与未暂存改动。首次导入资料的既存 EOF 空行例外保持不变。
