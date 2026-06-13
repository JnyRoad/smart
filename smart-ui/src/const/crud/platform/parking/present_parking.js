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
      label: '所属园区',
      prop: 'parkName',
      type: 'input'
    },
	  {
      label: '车牌号',
      prop: 'vehiclePlate',
      type: 'input'
    },
    {
      label: '车主',
      prop: 'driverName',
      type: 'input'
    },
    {
      label: '手机号',
      prop: 'driverPhone'
    },
	  {
      label: '驶入时间',
      prop: 'snapTime',
      type: 'date',
      format: "yyyy-MM-dd HH:mm"
    },
    {
      label: '查询标识',
      prop: 'AllFlag',
      hide: true
    }
  ]
}