export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
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
  column: [
    {
      label: '主键',
      prop: 'id',
      type: 'input',
      hide: true
    },
    {
      label: '反馈人',
      prop: 'staffName'

    },
    {
      label: '反馈人电话',
      prop: 'staffPhone'
    },{
      label: '反馈标签',
      prop: 'question'
    },
    {
      label: '处理状态',
      prop: 'status',
      width: 180,
      type: 'select',
      dicData:[{
        label:'未处理',
        value: 0
      },
      {
        label:'已处理',
        value: 1
      }]

    },
    {
      label: '反馈时间',
      prop: 'createTime'
    },
  ]
}