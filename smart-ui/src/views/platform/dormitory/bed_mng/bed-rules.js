// bed_mng 床位管理页的纯展示/转换规则：从 index.vue 抽出，不依赖组件 this，便于单测与复用。
// 行为与原方法逐一对应：registerRowClassName←tableRowClassName、formatCheckInDate←dateFormat、toExportRows←formatJson。
import { validatenull } from '@/util/validate'
import { dateFormat2 } from '@/util/date'

// 登记状态(status === -1)的行返回高亮类名 isRegister，其余不加类（返回 undefined）。
export function registerRowClassName (row) {
  if (row.status === -1) {
    return 'isRegister'
  }
}

// 入住时间展示：空值（validatenull 判定）不显示，非空格式化为 yyyy-MM-dd。
export function formatCheckInDate (val) {
  if (!validatenull(val)) {
    return dateFormat2(new Date(val))
  }
}

// 导出用：按列字段顺序把对象数组映射成二维数组。
export function toExportRows (columns, rows) {
  return rows.map((row) => columns.map((field) => row[field]))
}
