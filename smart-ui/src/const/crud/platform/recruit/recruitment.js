export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 300,
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
      label: '岗位名称',
      prop: 'jobId',
      type: 'select',
      hide:true
    },
	  {
      label: 'BU',
      prop: 'compId',
      type: 'select',
      hide:true
    },
	  {
      label: '部门名称',
      prop: 'depId',
      type: 'select',
      hide:true
    },
	  {
      label: '职层',
      prop: 'jcheId',
      type: 'select',
      hide:true
    },
    {
      label: 'id',
      prop: 'id',
      hide:true
    },
    {
      label: '岗位名称',
      prop: 'jobName'
    },
    {
      label: '所属园区',
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
      label: '职层',
      prop: 'jcheName'
    },
    {
      label: '招聘人数',
      prop: 'recruitNum'
    },
	  {
      label: '状态',
      prop: 'status',
      type: 'select',
      solt: true
    }
  ]
}
