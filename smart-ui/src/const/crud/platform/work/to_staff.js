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
  selection: false,
  props: {
    label: 'label',
    value: 'value'
  },
  column: [
    {
      label: '工号',
      prop: 'badge',
      type: 'input'
    },
    {
      label: '姓名',
      prop: 'name',
      type: 'input'
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
      label: '岗位名称',
      prop: 'jobName'

    },
    {
      label: '关联ID',
      prop: 'seqId',
    },

    {
      label: '创建时间',
      prop: 'createTime',
      width: 165
    }

  ]
}
