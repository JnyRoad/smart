export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menuWidth: 150,
  align: 'center',
  menuAlign: 'left',
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
      label:'id',
      prop:'id',
      hide: true
    },
    {
      label:'是否可删除',
      prop:'isFix',
      hide: true
    },
    {
      label:'创建时间',
      prop:'createTime',
      hide: true
    },
    {
      label:'所属园区',
      prop:'parkName',
    },
    {
      label:'权限名称',
      prop:'authName'
    },
    {
      label: '备注',
      prop: 'authDesc'
    }
  ]
}
