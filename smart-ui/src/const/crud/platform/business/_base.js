// Avue 表格（tableOption）的 business 业务域公共基础配置。
// 本域 9 个 crud 配置的顶层键取值完全一致（仅 isc_park_config 额外多一个 menuWidth），抽出统一维护、消除重复。
// 用法：各 crud 文件 `export const tableOption = { ...baseTableOption, <本表特有键>, column: [...] }`。
// 注意：这是 business 域风格（border:true / stripe:false），与 admin 的 crud/_base.js（border:false / stripe:true）相反，
// 刻意分开维护；menuWidth 只有个别表需要，不放进 base，由各文件自行声明。
export const baseTableOption = {
  border: true,
  index: true,
  indexLabel: '序号',
  stripe: false,
  menuAlign: 'center',
  align: 'center',
  editBtn: false,
  delBtn: false,
  addBtn: false
}
