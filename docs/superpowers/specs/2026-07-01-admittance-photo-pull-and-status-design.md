# 入厂申请照片拉取与下发状态回写设计

- 日期：2026-07-01
- 状态：已定稿（三轮 Codex 独立评审，2026-07-01 双方一致；待业务方最终确认）
- 依赖：《开放 API 鉴权框架设计》（同日 spec，须先行合并）

## 1. 背景与问题

**问题一（线上误报，2026-07-02 日志证实）**：入厂申请审批通过后的 `SmtAdmittanceApplyServiceImpl.updateStatus()` 同步阻塞地把人脸照片 HTTP 推送到许昌打印机 Windows 机的 FileReceiver（`10.13.140.13:18080`，跨园区广域网）。推送超时即抛异常，整单被标为 `deviceStatus=下发失败(2)`——而 ISC 权限下发（`addDeviceTask` 创建、smart-schedule 异步执行）实际成功。管理后台与 H5 同时误报「下发失败」，误导运营与访客。

**问题二（架构缺陷）**：推送模型绑定单台目标机，对方程序未开 / 关机 / 网络不通即失败；多台打印电脑无法同时使用。

**问题三（状态不诚实）**：审批通过后立刻置「已下发(4)」，ISC 真实下发结果从不回写——ISC 真失败时页面仍显示已下发。

**照片消费方式（已确认）**：打印页面 `smart-ui #/qrCodeNew` 通过 Brother b-PAC 控件读取本地 `D:\visitor\{photoId}.png` 打印访客标签。落盘目录与命名是硬约定，不可变。

## 2. 目标与范围

1. 照片分发从「服务端推」改为「FileReceiver 定时拉」，多机可并行、断网自愈；
2. `deviceStatus` 只反映 ISC 权限下发的真实结果，由 smart-schedule 终态回写；
3. FileReceiver 增加过期照片清理（PII 合规）。

**范围限定**：只改入厂申请链路。老访客流程的 SMB 直推（`SmtVisitorServiceImpl.smbPutPhoto`）本期不动。管理后台 / H5 不改字段不改接口。

## 3. 设计

### 3.1 服务端照片拉取接口（smart-platform）

两个只读开放接口，标注 `@OpenApi("open:admittance:photo:read")`（鉴权见依赖 spec）：

| 接口 | 说明 |
|---|---|
| `GET /platform/admittance/photo/pending` | 返回「审批通过（Status_0）、未过期（endTime > now）、非车辆类型」申请单下全部随行人员的 photoId 列表 |
| `GET /platform/admittance/photo/download/{photoId}` | 返回照片二进制（复用 `smtImageService.getImageBinaryByCode`）；photoId 严格校验为 UUID 格式，防路径穿越/枚举 |

- **园区范围由服务端推导（Codex 评审阻断项）**：不接受调用方传 parkId——服务端按应用凭证绑定的 `allowedParkIds`（见鉴权 spec）确定查询范围，token 泄露也拉不到其他园区数据；
- 清单**只返回非空且图片实际存在的 photoId**（现有数据存在 photoId 为空/图片缺失的情况，直接下发会让客户端反复 404 空转）；缺图作为数据质量问题记 WARN 日志；
- 清单接口只返回 photoId（轻量，客户端按需 diff）；photoId 本身为随机 UUID，不含个人信息；
- download 接口对不存在的 photoId 返回 404，不回显入参；UUID 校验与现有 `saveImage` 默认生成规则一致（本接口只服务入厂申请照片，不承诺兼容外部传入的非 UUID 历史 imgCode）。

### 3.2 FileReceiver 改造（smart-module/FileReceiver）

新增定时拉取与清理，配置项（`application.yml`，部署时按机器配置）：

```yaml
file-receiver:
  pull:
    enabled: true
    server-url: http://<平台网关地址>      # 平台入口
    app-id: file-receiver-xc              # 应用凭证（园区范围绑定在服务端应用配置上，本地不配 parkId）
    app-secret: <部署时配置>
    interval-seconds: 30                  # 拉取频率，可配置
  photo-dir: D:/visitor                   # 落盘目录，与打印页面硬约定一致
  cleanup:
    retention-days: 7                     # 0 = 关闭清理
```

**拉取任务**（每 `interval-seconds` 一轮）：
1. token 管理：启动/过期前用 client_credentials 获取 token，401 时强制刷新一次并重试本轮；
2. 拉 pending 清单 → 与本地 `photo-dir` 比对 → 只下载缺失的 photoId；
3. 每张照片：下载到同目录临时文件（`{photoId}.png.tmp`）→ 校验非空 → **原子改名**为 `{photoId}.png`（防止 b-PAC 读到半张图）；
4. 单张失败记 ERROR 日志并继续其余照片，下轮自动重试（自愈）；本轮清单拉取失败整轮跳过、下轮重试；
5. 所有 HTTP 调用设置连接/读取超时（连接 5s、读取 30s）。

**清理任务**（每日一次）：删除同时满足以下两个条件的照片文件：
- 不在当前 pending 清单中（还在有效期内的长周期来访不会被删）；
- 文件修改时间早于 `retention-days` 天前。

`retention-days=0` 或 pending 清单拉取失败时跳过本次清理（宁可不删，不误删）。

**兼容**：旧 `POST /file/upload` 接口保留、标记废弃（日志 WARN + README 注明），一个版本周期后删除。

**推拉并行的路径口径（Codex 评审指出的不兼容）**：平台推送现传绝对路径 `filePath=D:/visitor/{photoId}.png`，而仓库当前版 FileReceiver 拒绝绝对路径——若许昌机器部署的是新版 jar，「默认保留推送」会持续报错。统一口径：平台推送开关开启期间改传**相对文件名**（`{photoId}.png`），FileReceiver 配置 `upload-root=D:/visitor`（与拉取的 `photo-dir` 同目录）；**上线前必须核对许昌机器实际运行的 jar 版本**与其配置。

### 3.3 审批链路解耦（smart-platform）

`updateStatus()` 改动：
- **照片推送与审批链路解耦（不是立即删代码）**：`smbPutPhoto` 方法保留，`updateStatus` 中的调用改为受开关 `admittance.photo-push-enabled`（Nacos，默认 `true`）控制的**尽力而为**行为——开关开时执行推送，**失败只记 ERROR 日志，不抛异常、不影响 deviceStatus**；开关关时完全不调用。废弃周期结束后随开关一起删除推送代码（与旧 `/file/upload` 同步退役）；
- 状态流转保持现状：认领时 `下发中(3)` → `updateStatus` 完成（ISC 任务已创建提交）→ `已下发(4)`。`已下发(4)` 的语义明确为「任务已提交 ISC，等待结果确认」，是过渡态，终态由 3.4 的聚合回写决定（1/2）。字段与前端映射均不改；
- 车辆类型（不建 ISC 任务）：维持 `待下发(0)` 现状；
- 顺带修正误导性日志 tag：`smbPutPhoto` 内成功日志改用【入厂申请上传照片到远程电脑】前缀（现为复制粘贴的【…失败】）。

`markDeviceStatus(FAIL)` 的既有触发（`updateStatus` 真异常，如建任务失败）保留——那是真实失败。

### 3.4 ISC 真实结果回写（smart-schedule）

**批次模型（Codex 评审阻断项，替代原「任务存在性」判别）**——数据库变更（脚本放 `smart-module/database/manual/`）：
- `smt_isc_device_task` 新增 `apply_id`、`batch_id` 列（入厂申请来源的任务必填；其他来源任务为 NULL，不参与本聚合）；
- `smt_admittance_apply` 新增 `isc_submit_batch` 列（最近一次成功提交的批次号，NULL=从未完成提交）。

**提交协议**：`updateStatus()` 生成新批次号，在**同一事务**内插入该批次全部 ISC 任务并更新 `apply.isc_submit_batch=批次号`——事务保证「isc_submit_batch 已写 ⇔ 该批次任务集完整落库」，消除部分插入的模糊地带。

**补偿边界**：「审批通过后续处理补偿」只认领 `deviceStatus IN (下发失败, 下发中)` **且 `isc_submit_batch IS NULL`** 的单（updateStatus 从未完成过）；聚合产生的真失败单必有批次号，天然隔离，不会被自动重建任务，只走人工「重新下发」。

**重新下发（`repeat/auth`）**：现实现只补建缺失任务、不清旧任务——本次改为：将旧批次未终态任务置取消 → 生成新批次号重建任务集并更新 `isc_submit_batch`（同事务）→ 申请单回过渡态 `已下发(4)`。

**终态全集**：任务终态以 `DeviceTaskStatusEnum` 实际取值为准——除「成功」外的所有终态（失败/重试耗尽/取消/过期/设备离线等）在聚合中一律按「该任务失败」计；非终态（待下发/下发中）按「在途」计。旧批次任务因批次过滤天然不参与聚合。

**非 ISC 设备任务的边界（Codex 复审新增项，已确认代码事实）**：任务创建按设备 `isSync` 路由——ISC 设备入 `smt_isc_device_task`，非 ISC 设备入 `smt_device_task`（后者失败不改状态、无限等待重试，无可靠终态）。本设计的批次与聚合**只覆盖 ISC 任务表**，规则：
- 人员聚合只统计其 ISC 任务；批次内**没有任何 ISC 任务的人员**不参与聚合判定（其非 ISC 下发沿用现状机制）；
- 整单没有任何 ISC 任务时，不触发聚合，申请单停留 `已下发(4)` 过渡态——与现状展示语义一致，无回归；
- `repeat/auth` 的旧批次取消同样只作用于 ISC 任务表，非 ISC 任务沿用其现有重建逻辑；
- 本次线上问题（许昌园区）为纯 ISC 链路；非 ISC 设备的下发状态可观测性列为已知边界，后续按需扩展。

**聚合触发**：`ISCDeviceTaskServiceImpl` 中每当一个入厂申请来源任务到达终态时，按 `apply.isc_submit_batch` 对应批次聚合回写 `device_status`：

**聚合规则**（已与业务确认）：
- **人员维度**：该人员在**任一设备**任务成功 → 该人员成功（门岗多台设备，坏一台不影响通行）；
- **申请单维度**（聚合**只回写终态**，在途不回写）：
  - 单下**所有人员**都至少一台设备成功 → `下发成功(1)`；
  - **任一人员**在其所有设备任务上均为终态失败 → `下发失败(2)`（该人员到门口过不去，必须暴露）；
  - 其余（仍有任务在途）→ 不回写，保持 `已下发(4)` 过渡态。

**与补偿任务的隔离**：见上方「补偿边界」——以 `isc_submit_batch` 显式标记提交完成，不再依赖任务存在性推断（Codex 评审确认后者在无事务部分插入场景下不可靠，已废弃该方案）。

**实现要点**：
- 任务与申请单的关联使用新增 `apply_id` 列（创建任务时写入），不再运行时经 cardNo → fellow → visitorId 反查；`apply_id IS NULL` 的任务（非入厂申请来源）不触发回写；
- 回写在任务终态处理的同一事务外做（先落任务状态，再聚合回写；回写失败立即重试 2 次，仍失败记 ERROR，同单后续任务终态会再次触发聚合）；
- 聚合查询按 `apply_id + batch_id` 一次 SQL 汇总当前批次全部任务状态，避免逐条查询。

**兼容与展示映射（Codex 评审重要项）**：
- 历史数据中的 `已下发(4)` 保留原义不迁移；
- H5 访客自查接口目前把 `已下发(4)` 与 `下发成功(1)` 都映射为 `SUCCESS`——语义调整后 4 是过渡态，**后端映射层（`VisitorSelfQueryServiceImpl`）改为 4 → `ISSUING`（正在下发）**，H5 前端无需改动；
- 管理后台 `已下发` 文案维持（过渡态短暂存在，终态会被 1/2 覆盖），文案统一为可选后续优化。

### 3.5 上线顺序（防照片断供窗口）

1. **先合并鉴权框架**（依赖 spec），注册 `file-receiver-xc` 客户端；
2. 服务端上线：照片接口生效、状态回写生效、`photo-push-enabled=true`（推送保留但失败不再影响状态）；
3. FileReceiver 新版本部署到许昌 Windows 机，拉取生效（短期推拉并存，写同一目录同名文件，幂等无害）;
4. 观察 1-2 天（看拉取日志与打印正常）后，Nacos 置 `photo-push-enabled=false`，推送退役。

## 4. 错误处理汇总

| 场景 | 行为 |
|---|---|
| FileReceiver 拉清单失败 | 本轮跳过，下轮重试，ERROR 日志 |
| 单张照片下载失败/空内容 | 跳过该张继续其余，下轮重试 |
| token 过期/401 | 刷新 token 重试一次，仍失败则下轮 |
| ISC 任务终态但聚合回写失败 | 立即重试 2 次，仍失败记 ERROR；若该单还有在途任务，后续终态会再次触发聚合。残余风险：全部任务已终态且回写始终失败时停留过渡态，人工「重新下发」可恢复 |
| 应用绑定园区下无待拉取照片 | 返回空列表（正常，非错误） |
| 推送开关开启期间推送失败 | 仅 ERROR 日志，不影响 deviceStatus |

## 5. 测试

- **unit**：
  - 聚合规则矩阵：单人/多人 × 单设备/多设备 × 成功/各类终态失败（含取消/过期/离线）/在途 的组合断言；
  - 批次边界：旧批次终态不污染新批次聚合；重发后旧批次未终态任务被取消；
  - 补偿边界：`isc_submit_batch IS NULL` 才进自动补偿，聚合真失败单不进；
  - pending 清单查询过滤条件（状态/过期/车辆类型）；
  - photoId 格式校验（非法输入 400）；
  - FileReceiver 清理双条件判定、原子写（tmp→rename）；
- **integration**：
  - FileReceiver 拉取器对 mock 服务端：全量下载、增量跳过、单张失败隔离、401 刷新 token；
  - 照片接口带/不带 scope 的 200/403（复用鉴权框架的测试设施）；
- **人工验收（许昌现场）**：提测试申请 → 审批通过 → 30s 内照片落盘 `D:\visitor` → 打印正常 → 页面状态随 ISC 结果变化正确；拔网线 5 分钟恢复后自动补齐。

## 6. 风险

- FileReceiver 需现场重新部署（Windows 机、人工操作），部署失败回退手段：保持 `photo-push-enabled=true` 即维持旧行为；
- 涉及两张表加列的 DDL（`smt_isc_device_task`、`smt_admittance_apply`），需在业务低峰执行并提供回滚脚本；
- 聚合回写增加 schedule 对 platform 库的写入频度（每任务终态一次聚合 SQL），量级为访客申请数 × 设备数，许昌规模下可忽略；如后续园区扩容出现热点，再考虑批量合并回写；
- 照片（PII）经内网 HTTP 明文传输，与现状一致，随基础设施规划处理；
- 「任一设备成功即人员成功」规则下，部分设备长期故障会被状态掩盖——设备健康监控是独立能力（已有 `DeviceStatusTask`），不在本 spec 范围。
