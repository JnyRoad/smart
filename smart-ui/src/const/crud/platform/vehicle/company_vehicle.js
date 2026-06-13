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
      label: '车辆类型',
      prop: 'vehicleTypeName',
      type: 'select'
    },
    {
      label: '所属园区',
      prop: 'parkName',
      type: 'select'
    },
	  {
      label: '所属部门',
      prop: 'depName',
      type: 'select'
    },
    {
      label: '车辆状态',
      prop: 'isDelete',
      type: 'select',
      dicData:[{
        label: '已添加',
        value: 0
      },
      {
        label: '已删除',
        value: 1
      }]
    },
	  {
      label: '手机号',
      prop: 'phone'
    }
  ]
}