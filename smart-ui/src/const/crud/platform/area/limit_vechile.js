export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
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
      hide: true
    },
    {
      label:'车牌号',
      prop:'vehiclePlate'
    },
    {
      label:'车主',
      prop:'personName'
    },
    {
      label: '创建时间',
      prop: 'createTime'
    }
  ]
}