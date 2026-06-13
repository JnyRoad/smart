export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
  stripe: true,
  menuAlign: 'center',
  menuWidth: 100,
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
      label: 'id',
      prop: 'id',
      hide:true
    },
    {
      label: '工号',
      prop: 'badge',
      type: 'input'
    },
    {
      label: '姓名',
      prop: 'name',
      type: 'input'
    },
    {
      label: 'BU',
      prop: 'compName',
    },
    {
      label: '所属园区',
      prop: 'parkName',
    },
	  {
      label: '部门',
      prop: 'depName',
    },
	  {
      label: '工资月份',
      prop: 'wageDate',
    },
    {
      label: '签收日期',
      prop: 'createTime',
    }
  ]
}
