export const tableOption = {
  border: true,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 100,
  align: 'center',
  editBtn: false,
  delBtn: false,
  addBtn: false,
  selection: true,
  column: [
	  {
      label: 'id',
      prop: 'id',
      hide: true
    },
    {
      label: '所属园区',
      prop: 'parks',
      hide: true
    },
    {
      label: '所属园区',
      prop: 'parkNames'
    },
	  {
      label: '工号',
      prop: 'badge'
    },
    {
      label: '姓名',
      prop: 'name'
    },
    {
      label: 'BU',
      prop: 'compName'
    },
    {
      label: '部门',
      prop: 'depName'
    },
    {
      label: '福利层次',
      prop: 'welfareLevel'
    },
	  {
      label: '入职日期',
      prop: 'createTime'
    },
    {
      label: '餐补标准',
      prop: 'standard'
    },
    {
      label: '考勤月份',
      prop: 'checkMonth'
    },
	  {
      label: '应出勤',
      prop: 'shouldOn'
    },
	  {
      label: '实出勤',
      prop: 'actualOn'
    },
    {
      label: '应发餐补',
      prop: 'account',
      solt: true
    },
    {
      label: '备注',
      prop: 'blank'
    }
  ]
}
