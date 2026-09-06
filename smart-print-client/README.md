# Windows 打印工作站

本模块使用独立设备凭据从平台领取冻结的厂牌/访客任务，并通过 HiTi 官方 Windows 驱动或 Brother b-PAC 提交。一人一卡；手动翻面每个命令一页，自动翻面一个命令按正面→背面提交两页；访客始终单面。不会登记卡号、写IC卡、编码芯片或更改门禁权限。

设计与验收以 [功能规格](../specs/009-print-template-designer/spec.md)、[打印契约](../specs/009-print-template-designer/contracts/print-api.md) 为准。当前离线测试已覆盖真实 PDF 解析、页序、制品校验、驱动接口组合、持久日志和 HTTP 协议；Windows COM/驱动、打印机和现场介质尚未验收。默认执行开关关闭，示例档案未验收。

## 构建与验证

构建使用 .NET 10 SDK（本轮10.0.300），NuGet版本由锁文件固定。在仓库根目录运行：

```sh
dotnet restore smart-print-client/tests/Smart.PrintClient.Tests.csproj --locked-mode
dotnet test smart-print-client/tests/Smart.PrintClient.Tests.csproj --no-restore
dotnet publish smart-print-client/src/Smart.PrintClient.csproj -c Release -r win-x64 --self-contained false -o smart-print-client/.runtime/publish
```

工作站需 Windows、.NET 10 x64 Runtime，以及与进程位数匹配的厂商驱动/b-PAC。发布目录包含原生 PDFium/Skia 库及许可文件，不能只复制 EXE。第三方组件允许商业使用的授权及保留声明要求见 [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md)。厂商安装包不随本项目分发。

## 工作站配置

将 `appsettings.example.json` 复制为本机 `appsettings.local.json`，填入 HTTPS 平台地址、已授权设备身份、实际打印机档案和固定快照 hash。不要把本机配置或凭据提交仓库。设备令牌只从 `SMART_PRINT_DEVICE_TOKEN`（或配置的环境变量名）读取，不写入JSON或打印日志。

- `printerProfileId`、`printerSnapshotHash` 从平台档案详情取得。平台修改档案后需重新校准本机配置；活动任务继续使用原冻结档案，不随当前设置变化。
- 精确填写 Windows 队列名称、驱动名称及端口。工作站会核对这三项，且同一队列/端口不能同时注册两份可领取档案。
- HiTi 介质 `paperRawKind/paperName` 和分辨率须来自已安装驱动；`landscape`、正背面旋转（0/180）、毫米偏移由实卡校准确定。手动只设 `MANUAL_DUPLEX`；自动还需真实翻面模块、`autoDuplexVerified=true` 和已验证的 `duplexEdge=LONG/SHORT`。
- Brother 只允许 `SINGLE`。`mediaWidthMm` 最大58；`lbxPath/lbxHash/lbxObjectName/bpacMediaId/tempDirectory` 必须配置，页面尺寸与固定LBX图像区域一致。`MONO` 与 `BLACK_RED` 使用不同介质/模板，黑红需 `blackRedVerified=true`。QL-800照片和二维码最终效果必须实机验收。
- `journalDirectory` 默认在系统 ProgramData 下，按设备身份隔离；Windows ACL仅授予运行用户、SYSTEM和管理员。命令与回执强制刷盘、原子替换，目录锁避免多个进程同时使用；不要手工删除或修改日志以解除占用。

示例 LBX 的物理纸张为62×80mm，图像区域58×76mm，只有 `PageImage` 一个对象。`resources/brother/build-template.py` 可重建黑白与黑红版本；实际启用前应在 P-touch Editor 打开、确认介质/颜色/裁切并完成实机校准，将最终LBX的hash登记到本机档案。模板内容在提交前核验并复制到私有临时目录，不把任意网页或文件当作图像来源。

```powershell
.\Smart.PrintClient.exe validate --config .\appsettings.local.json
.\Smart.PrintClient.exe run --config .\appsettings.local.json
```

`validate` 只检查配置格式，不证明设备能力。服务端执行开关与本机 `executionEnabled` 都要由部署配置显式开启。实际运行需要平台专用设备 API 路由，地址的路径前缀应与网关一致；不使用管理员会话。

## 断线与现场恢复

客户端先记录命令意图及提交标记，再交驱动。记录为已开始提交但没有结果的命令，在重启后只上报 `OUTPUT_UNKNOWN`；不会自动再印一次。回执重传沿用原 `eventId`。驱动队列受理只表示 `DEVICE_ACCEPTED`，需要平台人工核对出卡；设备占用不会因租约超时自动释放。

手动模式需要在平台核对同一张卡的当前面。若原提交结果不明，先停止该工作站程序，在Windows驱动/设备面板中终止原任务，现场确认同一卡面尚未印、原队列为空，再运行：

```powershell
.\Smart.PrintClient.exe retire --config .\appsettings.local.json --command-id <原命令ID> --queue-cleared --same-card-face-verified
.\Smart.PrintClient.exe run --config .\appsettings.local.json
```

客户端会再次读取原队列，非空则拒绝；持久退休证据回到平台后，仍须操作员明确决定继续，服务端才创建新的attempt/command。自动双面不支持通过此命令重试结果不明的任务。

任务经平台核对成为终态后，现场取出卡/凭条并确认设备内无卡；停下工作站运行进程后提交清空证据：

```powershell
.\Smart.PrintClient.exe clear-device --config .\appsettings.local.json --printer-profile-id <档案ID> --job-id <任务ID> --operator-check-id <平台核对记录ID> --no-card
```

服务端只有在终态、有效核对证据且无待打印背面时释放占用。核对前不要删除日志、换设备身份或重新建相同任务来绕过结果不明。
