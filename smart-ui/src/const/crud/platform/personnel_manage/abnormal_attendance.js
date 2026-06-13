export const tableOption = {
  border: true,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  align: 'center',
  editBtn: false,
  delBtn: false,
  addBtn: false,
  column: [
    {
      label: '补卡日期',
      prop: 'startTime'
    },
    {
      label: '园区',
      prop: 'parkName'
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
      label: '补卡原因',
      prop: 'cause'
    },
    {
      label: '补卡人数',
      prop: 'statistics'
    }
  ]
}
