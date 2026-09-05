# smart-bridge-lock 统一接入设计入口

本目录仅为门锁协议桥的设计入口，当前尚无本规格对应的可运行服务或真机验收结果。

- [架构与实施计划](../../../../../specs/011-doorlock-oracle/plan.md)
- [生命周期与设备契约](../../../../../specs/011-doorlock-oracle/contracts/lifecycle-and-device.md)
- [验证矩阵](../../../../../specs/011-doorlock-oracle/test-matrix.md)
- [唯一开发任务清单](../../../../../specs/011-doorlock-oracle/tasks.md)
- [旧设计修订记录](../../../../../specs/011-doorlock-oracle/design-revisions.md)

职责：协议编解码、设备认证、单活连接、发送前校验及回执关联。不得维护第二套住宿事实、独立管理员登录或 MySQL 在线任务库。开发和影子状态默认禁止真实发送；正式切换按完整通信域执行，不能按单锁或单网关灰度。
