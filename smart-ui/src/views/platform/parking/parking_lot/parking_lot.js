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
  column: [
    {
      label: '主键',
      prop: 'id',
      type: 'input',
      hide: true
    },
    {
      label: '主键',
      prop: 'parkId',
      type: 'input',
      hide: true
    },
    {
      label: '所属园区',
      prop: 'parkName',
      type: 'select'
    },
	  {
      label: '停车场名称',
      prop: 'name',
      type: 'input'
    },{
      label: '总车位',
      prop: 'totalCount',
      type: 'input'
    }
  ]
}