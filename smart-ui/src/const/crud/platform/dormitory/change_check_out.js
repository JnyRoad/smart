export const tableOption = {
  border: false,
  index: false,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 160,
  labelWidth: 100,
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: true,
  showClomnuBtn: false,
  searchSize: 'mini',
  dialogWidth: '600px',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  column: [
    {
      label: 'id',
      prop: 'id',
      hide: true
    },{
      label: '床号',
      prop: 'bedNumber',
      width: 60
    },{
      label: '状态',
      prop: 'status',
      solt: true
    },{
      label: '姓名',
      prop: 'staffName',
      solt: true
    },{
      label: '入住时间',
      prop: 'inDate',
      width: 170,
      solt: true
    }
  ]
}