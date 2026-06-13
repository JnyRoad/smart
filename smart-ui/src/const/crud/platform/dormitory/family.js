export const tableOption = {
  border: false,
  index: false,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 160,
  labelWidth: 80,
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: true,
  showClomnuBtn: false,
  searchSize: 'mini',
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
      label: '姓名',
      prop: 'name'
    },{
      label: '工号',
      prop: 'badge'
    },{
      label: '身份证号',
      prop: 'certno',
      width: 200
    },{
      label: '手机号',
      prop: 'phone'
    },{
      label: '亲属关系',
      prop: 'relation',
      solt: true
    }
  ]
}