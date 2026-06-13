export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
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
  selection: true,
  column: [
    {
      label: '主键',
      prop: 'id',
      type: 'input',
      hide: true
    },
    {
      label: '所属园区',
      prop: 'parkName',
      type: 'select'
    },
	  {
      label: '车牌号',
      prop: 'vehiclePlate',
      type: 'input'
    },{
      label: '创建时间',
      prop: 'createTime',
      type: 'input'
    },{
      label: '原因',
      prop: 'remark',
      type: 'input'
    }
  ]
}
