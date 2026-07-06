# 设计：保密权限微信推送改造（正文压缩 + 重试治理 + 模板名参数化）

日期：2026-07-06
状态：已与用户对齐确认

## 背景与问题（2026-07-06 生产排查确认）

1. `smart-module/smart-tool/.../util/WeChatMsgUtil.java` 写死借用「访客出入园提醒」公众号模板
   （`TEMPLATE_NAME` 常量），全系统 18 个调用点（7 个业务文件，覆盖保密权限、宿舍报修、退宿、
   物品放行、入厂、访客抓拍、ISC 设备任务）共用这个壳，字段张冠李戴：
   - `thing18`（访客姓名）塞的是工号；
   - `thing14`（项目名称）塞完整正文，被微信 thing 字段 20 字上限截断，不同单子截断后无法区分；
   - `time4`（入出时间）是发送时刻。
2. `SmtSecurityAuthApplyServiceImpl.sendMessage()`（457-494 行）：发送失败（`WeChatMsgUtil`
   返回 false）时 `isMsg` 不置 1，定时任务每 20 分钟无上限、无退避地重发同一批。若中转服务
   （xchr.szyuto.com `insertTemplateMsg`，成功码 "1"）响应格式变化，会造成无限骚扰式推送。
3. 现存 bug（本次顺带修复）：`sendMessage()` 473-474 行 `getSimpleSttaffByBadge` 返回 null
   时直接 NPE，且这两行不在 try 块内——一个查不到员工的单会炸掉整轮循环，卡死其后所有单。

## 范围决策（已确认）

- 本轮**只改保密权限推送**（`sendMessage()`），其余 17 个调用点后续按业务逐个跟进。
- 允许给 `smt_security_auth_apply` **加 `msg_retry_count` 列**（手工 SQL 走 `database/manual/`）。
- 公众号可用模板清单需运维确认、目前未知：本轮**沿用「访客出入园提醒」模板 + 压缩正文**，
  不阻塞在外部依赖上；模板名参数化打好地基，拿到清单后换模板只改一处入参。
- 字段取舍：thing18 = 申请人姓名，thing14 = 20 字内结果摘要，time4 = 发送时刻（模板语义限制无法避免）。
- 参数化形态：**新增重载方法**（方案 A），旧签名保留委托，17 个旧调用点零改动。

## 1. 改动范围

| 位置 | 改动 |
|---|---|
| `smart-tool` `WeChatMsgUtil.java` | 新增 `sendTemplateMsg` 重载，旧 `sendMsg` 委托之 |
| `smart-platform-biz` `SmtSecurityAuthApplyServiceImpl.sendMessage()` | 正文压缩、显示姓名、重试上限、null 员工不中断循环 |
| `smart-platform-core` `SmtSecurityAuthApply.java` | 加 `msgRetryCount` 字段 |
| `smart-module/database/manual/` | 一条 DDL + 一条模板内容 UPDATE，注明 schema 专用账号执行 |

## 2. WeChatMsgUtil 参数化

```java
// 旧签名保留，行为完全不变（displayName = loginName，默认模板）
public static Boolean sendMsg(String loginName, String remark, String openId, String url) {
    return sendTemplateMsg(TEMPLATE_NAME, loginName, remark, loginName, openId, url);
}

// 新增：模板名与「显示名/路由工号」解耦
public static Boolean sendTemplateMsg(String templateName, String displayName,
                                      String body, String loginName, String openId, String url)
```

- `loginName` 既是中转服务查 openId 的路由键、又直接显示在 thing18——新方法把两者解耦：
  `displayName` 进 thing18，`loginName`/`openId` 只做路由。
- 字段 key（thing18/time4/thing14）本轮仍是常量：只有一个模板在用，等运维给出可用模板清单
  再扩字段映射（YAGNI）。
- `displayName` 为空时沿用"系统通知"兜底。

## 3. sendMessage() 重试治理与正文

- **重试上限**：常量 `MAX_MSG_RETRY = 3`。定时任务本身 20 分钟一轮
  （`PlatformTimerTask.securitySendMessage`，Nacos 开关 `supplierAuthMsg`），
  天然就是重试间隔，不再叠加退避（KISS）。
- **isMsg 三态**：`0` 未发送、`1` 已发送、`2` 失败放弃（新增常量 + 中文注释；
  扫描条件仍 `isMsg = 0`，所以 `2` 天然不再入扫）。
- **失败路径**：发送失败 → `msgRetryCount + 1`；达到 3 次 → `isMsg = 2` 并 `log.warn`
  告警（带 processId、applyBadge、失败次数），留人工排查线索。
- **null 员工**：`getSimpleSttaffByBadge` 返回 null → `log.warn` + 按一次失败计数，
  不中断循环（修复现存 NPE 卡死整轮的 bug）。
- **正文**：统一为「保密权限下发完成 成功{成功数量}/共{总数量}」
  （成功数 = totalNum − failNum，三位数内 ≤19 字）。全成功也用同一格式（"成功5/共5"自明），
  避免文案一半在 DB 模板一半在代码。`smt_msg_template` 的 `WECHAT_SECURITY_11101`
  内容随 manual SQL 更新。
- **thing18** = 申请人姓名（`staffName.getName()`），路由仍用 `applyBadge`。

## 4. 数据脚本（database/manual/）

两条语句执行窗口不同（见「部署注意」），拆成两个脚本文件，均注明 schema 专用账号执行：

```sql
-- 脚本 1：加列，可提前执行（DEFAULT 0，旧代码不感知新列）
ALTER TABLE smt_security_auth_apply
  ADD COLUMN msg_retry_count INT NOT NULL DEFAULT 0 COMMENT '微信推送失败次数，达上限后 is_msg 置 2 放弃';

-- 脚本 2：更新模板内容，必须与新代码同窗口生效（占位符集合随代码一起变）
-- 注意 temp_code 的数据库值是 '11101'（SmsTemplateEnum.WECHAT_SECURITY_11101.getCode()），不是枚举常量名
UPDATE smt_msg_template
  SET temp_content = '保密权限下发完成 成功{成功数量}/共{总数量}'
  WHERE temp_code = '11101';
```

**存量数据行为**：线上现存 `isMsg = 0` 的历史失败单，上线后带着 `retry_count = 0`
最多再试 3 次然后终态化——骚扰一次性收敛，符合预期。

## 5. 错误处理、兼容与测试

- 其余 17 个调用点走旧签名委托，行为逐字节不变；生产 Nacos 开关 `supplierAuthMsg` 不动。
- 单测（Mockito）：
  1. 重试状态机：失败 1/2 次仍留 `isMsg = 0`，第 3 次失败置 `isMsg = 2`；
  2. 员工为 null：不中断循环、计一次失败；
  3. 正文渲染：成功数计算、占位符替换；
  4. `sendMsg` 旧签名委托后参数不变形（模板名、displayName 落位正确）。
- 验证命令：`mvn -pl smart-module/smart-platform/smart-platform-biz -am test` 及
  smart-tool 模块对应测试。
- 风险：中转服务响应格式若变化，`parseResponse` 返回 false → 最多重试 3 次即止，
  无限骚扰的根因被封死。剩余风险：模板仍是「访客出入园提醒」壳，标题观感问题待运维
  给模板清单后二期解决。

## 部署注意

- 先执行 manual SQL（加列 + 更新模板内容），再发布 smart-platform 与 smart-tool 依赖方；
  加列有 `DEFAULT 0`，旧代码不感知新列，先执行 DDL 无兼容风险。
- 模板内容 UPDATE 与新代码一起生效才有意义（旧代码占位符 {申请人}{OA单标题} 替换不到会原样
  留在正文里）；两者需在同一窗口完成：建议顺序 = DDL → 停 `supplierAuthMsg` 开关 →
  发布 → UPDATE 模板 → 开开关。
