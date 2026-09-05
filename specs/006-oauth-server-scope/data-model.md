# 数据模型

复用 SysOauthClientDetails.scope 逗号字符串，不新增表或字段，不改真实记录。
server 从历史权限变为可正常授予；open:admittance:photo:read 与 internal:energy:projection:run 变为历史兼容。存量未知值继续仅可原样保留。
删除/重置密钥/实际变更 scope 后的旧 token 撤销语义保持不变。

照片授权复用 SmtAdmittanceFellow.fellowPhotoId → visitorId → SmtAdmittanceApply.id 的关联，以申请单 parkId、status、endTime、applyType 判定可见范围；无新增表、字段或迁移。图片读取仍经 SmtImageService，拒绝前不访问图片字节。
