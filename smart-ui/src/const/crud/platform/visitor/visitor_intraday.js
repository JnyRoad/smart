export const personOption = {
  border: false,
  stripe: true,
  index: true,
  indexLabel: '序号',
  menuAlign: 'center',
  menu: false,
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
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label: '访客姓名',
      prop: 'visitorName'
    },
    {
      label: '来访园区',
      prop: 'parkName'
    },
	  {
      label: '所属单位',
      prop: 'company'
    },
	  {
      label: '被访部门',
      prop: 'receptionistDept'
    },
	  {
      label: '预约来访时间',
      prop: 'startTime'
    },
	  {
      label: '预约离开时间',
      prop: 'endTime'
    }
  ]
}
