export const tableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  indexWidth: 100,
  indexFixed: true,
  stripe: true,
  menuAlign: 'center',
  menuWidth: 100,
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
      label: '短信模板',
      prop: 'tempName',
      type: 'input'
    },
    {
      label: '发送手机号码',
      prop: 'msgObject',
      type: 'input'
    },
    {
      label: '发送时间',
      prop: 'createTime',
    },
	  {
      label: '发送内容',
      prop: 'msgContent',
    },
    {
      label: '发送状态id',
      prop: 'msgState',
      hide: true
    },
	  {
      label: '发送状态',
      prop: 'msgStateName',
      solt: true
    }
  ]
}