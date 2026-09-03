export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menu:false,
  menuAlign: 'center',
  menuWidth: 280,
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
      label: '主键',
      prop: 'id',
      type: 'input',
      hide:true
    },
    {
      label:'工号',
      prop:'badge'
    },
    {
      label:'姓名',
      prop:'personName'
    },
    {
      label:'员工状态',
      prop:'staffStatus',
      solt: true
    },
    {
      label: '创建时间',
      prop: 'createTime'
    },
    {
      label: '生效时间',
      prop: 'startTime'
    },
    {
      label: '失效时间',
      prop: 'endTime'
    }
  ]
}
