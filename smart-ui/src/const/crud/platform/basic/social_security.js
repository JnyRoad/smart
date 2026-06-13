export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
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
  selection: false,
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label: 'id',
      prop: 'id',
      hide:true
    },
    {
      label: '标题名称',
      prop: 'title'

    },
    {
      label: '标题链接',
      prop: 'url'
    },
    {
      label: '创建时间',
      prop: 'createTime',
    }
  ]
}