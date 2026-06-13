export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 150,
  labelWidth: 100,
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
  column: [{
    label: '卡片号',
    prop: 'cardNo',
    type: 'input'
  },{
    label: '车主姓名',
    prop: 'name',
    type: 'input'
  },{
    label: '车牌号',
    prop: 'plate',
    type: 'input'
  },{
    label: '创建时间',
    prop: 'createTime',
  }
]
}