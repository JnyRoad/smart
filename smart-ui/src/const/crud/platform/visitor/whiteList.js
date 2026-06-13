export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
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
  selection: true,
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label: '所属园区',
      prop: 'parkName'
    },
    {
      label: '工号',
      prop: 'staffBadge'
    },
    {
      label: '姓名',
      prop: 'staffName'
    },
    {
      label: 'BU',
      prop: 'compId',
      type: 'select',
      hide:true
    },
	  {
      label: '部门',
      prop: 'compId',
      type: 'select',
      hide:true
    },
    {
      label: '岗位',
      prop: 'jobId',
      type: 'select',
      hide:true
    },
    {
      label: 'id',
      prop: 'id',
      hide:true
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
      label: '岗位',
      prop: 'jobName',
    },
    {
      label: '创建时间',
      prop: 'createTime',
      width: 170
    }
  ]
}