// 开放 API 鉴权框架：可授权的 scope 常量登记表。
// App（OAuth2 客户端）的 scope 表单在此选择，值即写入后端 client_details.scope 字段（逗号分隔）。
// 后续新增开放接口权限，在此数组追加一项即可，无需改动页面逻辑。
export const OPEN_SCOPES = [
  {
    value: 'open:admittance:photo:read',
    label: '入厂申请照片-读取'
  }
]
