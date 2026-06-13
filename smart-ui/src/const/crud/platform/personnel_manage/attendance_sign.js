export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 100,
  menuAlign: 'center',
  align: 'center',
  editBtn: false,
  delBtn: false,
  addBtn: false,
  column: [
	  {
      label: 'id',
      prop: 'id',
      hide: true
    },
	  {
      label: '工号',
      prop: 'badge'
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
      label: '部门',
      prop: 'depName',
    },
    {
      label: '所属园区',
      prop: 'parkName',
    },
    {
      label: '考勤月份',
      prop: 'checkDate',
    },
    {
      label: '确认状态',
      prop: 'signStatusDesc'
    }
  ]
}
