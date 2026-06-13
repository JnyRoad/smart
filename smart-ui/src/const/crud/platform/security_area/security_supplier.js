export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 220,
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
    label: '园区',
    prop: 'parkName'
  },{
    label: '单位名称',
    prop: 'companyName'
  },
  {
    label: '状态',
    prop: 'status',
    solt: true
  },{
    label: '生效开始时间',
    prop: 'beginEffectTime'
  },{
    label: '生效结束时间',
    prop: 'endEffectTime'
  },
  {
    label: '备注',
    prop: 'remark'
  }]

}