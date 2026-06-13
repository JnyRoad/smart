export const tableOption = {
  border: false,
  index: false,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 230,
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
      label: '床位信息',
      prop: 'dormitoryName',
      solt: true
    },
    {
      label: 'id',
      prop: 'id',
      hide: true
    },{
      label: '姓名',
      prop: 'name',
      solt: true
    },{
      label: '性别',
      prop: 'sex',
      solt: true
    }
  ]
}