export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 200,
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
  },
  column: [
    {
      label: '主键',
      prop: 'id',
      type: 'input',
      hide: true
    },
    {
      label: '对口人员',
      prop: 'name',
      type: 'input'
    },
	  {
      label: '车牌号',
      prop: 'vehiclePlate',
      type: 'input'
    },
    {
      label: '电话',
      prop: 'phone'
    },
    {
      label: '所属园区',
      prop: 'parkName',
      type: 'select'
    },
    {
      label: '备注',
      prop: 'remark',
      solt: true,
    }
  ]
}