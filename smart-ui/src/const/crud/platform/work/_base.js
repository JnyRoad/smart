// Avue 表格（tableOption）的 work 业务域公共基础配置。
// 本域 8 个审批/流程列表的顶层键取值完全一致，抽出统一维护、消除重复。
// 用法：各 crud 文件 `export const tableOption = { ...baseTableOption, column: [...] }`。
// 注意：column 内的列、slot、formatter 等业务细节由各文件自行维护，本基础配置不包含 column。
export const baseTableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
  stripe: true,
  menuAlign: 'center',
  menuWidth: 150,
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: false,
  showClomnuBtn: false,
  searchSize: 'mini',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  selection: false,
  props: {
    label: 'label',
    value: 'value'
  }
}
