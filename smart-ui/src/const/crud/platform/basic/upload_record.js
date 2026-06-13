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
      prop: 'compId',
      type: 'select',
      hide:true
    },
	  {
      label: '部门',
      prop: 'compId',
      type: 'select',
      hide:true
    },
	  {
      label: '职层',
      prop: 'jcheId',
      type: 'select',
      hide:true
    },

    {
      label: 'id',
      prop: 'id',
      hide:true
    },
    {
      label: 'BU',
      prop: 'compName',
    },
	  {
      label: '部门',
      prop: 'depName',
    },
	  {
      label: '职层',
      prop: 'jcheName',
    },

    {
      label: '导入时间',
      prop: 'createTime',
      width: 165
    },
	  {
      label: '导入状态',
      prop: 'status',

      type: 'select',
      dicData:[{
        label: '失败',
        value: 0
      },
      {
        label: '成功',
        value: 1
      }]
    }
  ]
}
