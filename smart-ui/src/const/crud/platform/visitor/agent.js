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
      label: '被访人工号',
      prop: 'interVieweeBadge',
      width: 100
    },
    {
      label: '被访人姓名',
      prop: 'interVieweeName',
      width: 100
    },
    {
      label: 'BU',
      prop: 'interVieweeCompName'
    },
	  {
      label: '部门',
      prop: 'interVieweeDepName'
    },
    {
      label: '代理审批人工号',
      prop: 'proxyBadge',
      width: 120
    },
    {
      label: '代理审批人姓名',
      prop: 'proxyName',
      width: 120
    },
    {
      label: 'BU',
      prop: 'proxyCompName'
    },
	  {
      label: '部门',
      prop: 'proxyDepName'
    },
    {
      label: '创建时间',
      prop: 'createTime'
    }
  ]
}