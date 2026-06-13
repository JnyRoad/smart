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
      prop: 'staffBadge',
      type: 'input'
    },
    {
      label: '姓名',
      prop: 'staffName',
      type: 'input'
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
      label: '岗位',
      prop: 'jobName',
    },
    {
      label: '加班时间',
      prop: 'extraworkDate'

    },
	  {
      label: '班级',
      prop: 'workClassCodeDesc'

    },
	  {
      label: '加班类型',
      prop: 'extraworkTypeName'
    },

    {
      label: '加班时长',
      prop: 'extraworkCount',
    },
    {
      label: '加班原因',
      prop: 'cause'
    },

	  {
      label: '流程编号',
      prop: 'processId',
    },

    {
      label: '创建时间',
      prop: 'recordDate',
      width: 165
    }

  ]
}