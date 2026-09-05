# 授权契约

- GET /open/admittance/photo/pending 与 /download/{photoId}：主 scope server；精确兼容 open:admittance:photo:read。
- 四个 /inner/energy/projection/* 入口：主 scope server；精确兼容 internal:energy:projection:run；保留 @Inner；ENFORCE 模式仍需既有内部调用标识 from=Y。
- 客户端 token 才能使用上述授权；空/错误 scope、用户 token、匿名继续拒绝。
- smart.openapi.allow-deprecated-compatibility-scopes 默认 true，仅控制旧细分兼容；false 时 server 仍可用。
- 客户端 scope 目录保持现有响应格式；正常可授予值只有 server，历史项只用于存量维护。
- 调度的 scope 与 energy-projection-run-scope 默认 server；保留原配置键及显式旧值兼容。

- 下载园区范围只来自 token 的 app_park_ids；客户端无需新增参数。授权园区内有效申请关联照片返回 200 image/png；空范围、跨园区、无有效申请关联、缺图统一 404；UUID 非法仍返回 400。有效申请与 pending 一致：审批通过、未过期、非车辆。
