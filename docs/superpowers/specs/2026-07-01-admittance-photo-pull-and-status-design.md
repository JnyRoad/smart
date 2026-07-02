# 入厂申请照片拉取与下发状态回写设计

- 日期：2026-07-01
- 状态：待评审（需 Codex 独立评审、双方一致后定稿）
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
| `GET /platform/admittance/photo/pending?parkId=` | 返回该园区「审批通过（Status_0）、未过期（endTime > now）、非车辆类型」申请单下全部随行人员的 photoId 列表 |
| `GET /platform/admittance/photo/download/{photoId}` | 返回照片二进制（复用 `smtImageService.getImageBinaryByCode`）；photoId 严格校验为 UUID 格式，防路径穿越/枚举 |

- 清单接口只返回 photoId（轻量，客户端按需 diff）；photoId 本身为随机 UUID，不含个人信息；
- download 接口对不存在的 photoId 返回 404，不回显入参。

### 3.2 FileReceiver 改造（smart-module/FileReceiver）

新增定时拉取与清理，配置项（`application.yml`，部署时按机器配置）：

```yaml
file-receiver:
  pull:
    enabled: true
    server-url: http://<平台网关地址>      # 平台入口
    client-id: file-receiver-xc           # OAuth2 客户端凭证
    client-secret: <部署时配置>
    park-id: <本机所属园区ID>
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

### 3.3 审批链路解耦（smart-platform）

`updateStatus()` 改动：
- **删除** `smbPutPhoto` 调用及其抛异常逻辑（照片分发与审批链路彻底解耦）；
- 状态流转保持现状：认领时 `下发中(3)` → `updateStatus` 完成（ISC 任务已创建提交）→ `已下发(4)`。`已下发(4)` 的语义明确为「任务已提交 ISC，等待结果确认」，是过渡态，终态由 3.4 的聚合回写决定（1/2）。字段与前端映射均不改；
- 车辆类型（不建 ISC 任务）：维持 `待下发(0)` 现状；
- 过渡开关 `admittance.photo-push-enabled`（Nacos，默认 `true`）：为 true 时仍执行照片推送，但**推送失败只记 ERROR 日志，不再抛异常、不再影响 deviceStatus**（上线顺序见 3.5）；
- 顺带修正误导性日志 tag：`smbPutPhoto` 内成功日志改用【入厂申请上传照片到远程电脑】前缀（现为复制粘贴的【…失败】）。

`markDeviceStatus(FAIL)` 的既有触发（`updateStatus` 真异常，如建任务失败）保留——那是真实失败。

### 3.4 ISC 真实结果回写（smart-schedule）

在 `ISCDeviceTaskServiceImpl` 中，每当一个入厂申请相关 ISC 任务到达**终态**（下发成功 / 重试耗尽失败 / 标记最大重试）时，按申请单聚合回写 `smt_admittance_apply.device_status`：

**聚合规则**（已与业务确认）：
- **人员维度**：该人员在**任一设备**任务成功 → 该人员成功（门岗多台设备，坏一台不影响通行）；
- **申请单维度**（聚合**只回写终态**，在途不回写）：
  - 单下**所有人员**都至少一台设备成功 → `下发成功(1)`；
  - **任一人员**在其所有设备任务上均为终态失败 → `下发失败(2)`（该人员到门口过不去，必须暴露）；
  - 其余（仍有任务在途）→ 不回写，保持 `已下发(4)` 过渡态。

**与补偿任务的隔离（关键约束）**：现有「审批通过后续处理补偿」靠 `deviceStatus IN (下发失败, 下发中)` 识别「updateStatus 没做完的单」并重跑全流程（会重建 ISC 任务）。聚合回写引入的 `下发失败(2)` 是「ISC 重试耗尽的真失败」，**不得进入该自动补偿**，否则会对真失败人员无限重建任务。隔离方式：补偿查询追加「该单不存在 ISC 任务记录」条件——updateStatus 没做完的单无任务，聚合失败的单必有任务，天然可区分。真失败的单只走人工「重新下发」。实施时需确认 `addDeviceTask` 的事务性，保证「任务存在 ⇔ 提交完成」不出现部分插入。

**实现要点**：
- 任务与申请单的关联沿用现有链路：task.cardNo → `SmtAdmittanceFellow.id` → `fellow.visitorId` → applyId；非入厂申请来源的任务（cardNo 解析不到 fellow）不触发回写；
- 回写在任务终态处理的同一事务外做（先落任务状态，再聚合回写；回写失败立即重试 2 次，仍失败记 ERROR，同单后续任务终态会再次触发聚合）；
- 聚合查询按 applyId 汇总该单全部 fellow 的全部任务状态，一次 SQL 完成，避免逐条查询；
- 「重新下发」（`repeat/auth`）逻辑沿用：重发时清理旧任务、重建任务并回到过渡态，新任务终态再次驱动聚合。

**兼容**：历史数据中的 `已下发(4)` 保留原义不迁移；前端两端的枚举映射已覆盖 0-4 全部取值，无需改动。

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
| 清单接口 parkId 无数据 | 返回空列表（正常，非错误） |
| 推送开关开启期间推送失败 | 仅 ERROR 日志，不影响 deviceStatus |

## 5. 测试

- **unit**：
  - 聚合规则矩阵：单人/多人 × 单设备/多设备 × 成功/终态失败/在途 的组合断言；
  - pending 清单查询过滤条件（状态/过期/车辆类型）；
  - photoId 格式校验（非法输入 400）；
  - FileReceiver 清理双条件判定、原子写（tmp→rename）；
- **integration**：
  - FileReceiver 拉取器对 mock 服务端：全量下载、增量跳过、单张失败隔离、401 刷新 token；
  - 照片接口带/不带 scope 的 200/403（复用鉴权框架的测试设施）；
- **人工验收（许昌现场）**：提测试申请 → 审批通过 → 30s 内照片落盘 `D:\visitor` → 打印正常 → 页面状态随 ISC 结果变化正确；拔网线 5 分钟恢复后自动补齐。

## 6. 风险

- FileReceiver 需现场重新部署（Windows 机、人工操作），部署失败回退手段：保持 `photo-push-enabled=true` 即维持旧行为；
- 聚合回写增加 schedule 对 platform 库的写入频度（每任务终态一次聚合 SQL），量级为访客申请数 × 设备数，许昌规模下可忽略；如后续园区扩容出现热点，再考虑批量合并回写；
- 照片（PII）经内网 HTTP 明文传输，与现状一致，随基础设施规划处理；
- 「任一设备成功即人员成功」规则下，部分设备长期故障会被状态掩盖——设备健康监控是独立能力（已有 `DeviceStatusTask`），不在本 spec 范围。
