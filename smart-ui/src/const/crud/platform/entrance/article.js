export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuAlign: 'center',
  menuWidth: 200,
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
      label: '主键',
      prop: 'id',
      hide: true
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
      prop: 'deptName',
      width: 100
    },
    {
      label: '申请人',
      prop: 'name',
      width: 100
    },
    {
      label: '状态',
      prop: 'statusName',
      width: 100
    },
    {
      label: '携带人',
      prop: 'carrier',
      width: 100
    },
    {
      label: '车牌号码',
      prop: 'licensePlate',
      width: 100
    },
    {
      label: '物品类型',
      prop: 'articlesTypeName',
      width: 160
    },
    {
      label: '计划出发时间',
      prop: 'plannedDepartureTime',
      width: 160
    },
    {
      label: '安保确认时间',
      prop: 'departureTime',
      width: 160
    }
  ]
}
