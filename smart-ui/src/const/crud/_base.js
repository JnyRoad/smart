// Avue 表格（tableOption）的公共基础配置。
// 这些键在 admin 等多个 crud 配置里取值完全一致，抽出统一维护、消除重复。
// 用法：各 crud 文件 `export const tableOption = { ...baseTableOption, <本表特有键>, column: [...] }`。
// 注意：indexLabel / viewBtn / menuWidth 等并非所有表都一致，刻意不放进 base，由各文件自行声明。
export const baseTableOption = {
  border: false,
  index: true,
  stripe: true,
  menuAlign: 'center',
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: false,
  editBtn: false,
  delBtn: false,
  addBtn: false
}
