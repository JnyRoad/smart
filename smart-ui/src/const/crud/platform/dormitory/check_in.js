export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 150,
  labelWidth: 100,
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: false,
  showClomnuBtn: false,
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  column: [
    {
      label: 'id',
      prop: 'id',
      hide:true
    },
    {
      label: '所属园区',
      prop: 'parkName'
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
      label: '岗位',
      prop: 'jobName'
    },
    {
      label: '职层',
      prop: 'jcheName'
    },
    {
      label: '福利层次',
      prop: 'welfareLevel'
    }
  ]
}
