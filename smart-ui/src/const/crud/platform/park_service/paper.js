export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 260,
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
      label: '所在园区',
      prop: 'parkName'
    },
    {
      label: '调查表名称',
      prop: 'title',
      width: 100
    },
    {
      label: '开始日期',
      prop: 'startTime',
      width: 170
    },{
      label: '截止日期',
      prop: 'endTime',
      width: 170
    },
    {
      label: '状态',
      prop: 'status',
      solt: true
    },
    {
      label: '发布范围',
      prop: 'compNames'
    },
    {
      label: '发布者',
      prop: 'createUser'
    },
    {
      label: '发布时间',
      prop: 'createTime',
      width: 170
    },
  ]
}