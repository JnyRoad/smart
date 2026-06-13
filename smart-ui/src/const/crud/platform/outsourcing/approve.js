export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 200,
  labelWidth: 150,
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: false,
  showClomnuBtn: false,
  searchSize: 'mini',
  dialogWidth : '600px',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  column: [
    {
      label: '申请单号',
      prop: 'applyId'
    },
    {
      label: '单位名称',
      prop: 'compName',
    },
    {
      label: '申请人数',
      prop: 'applyNum'
    },
    {
      label: '申请时间',
      prop: 'applyTime'
    },
    {
      label: '状态',
      prop: 'statusDesc'
    }]

}
