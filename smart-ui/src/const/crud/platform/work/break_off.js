import { baseTableOption } from './_base'

export const tableOption = {
  ...baseTableOption,
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
      label: '出勤时间',
      prop: 'workDate'

    },
	  {
      label: '调休时间',
      prop: 'restDate'

    },
	  {
      label: '调休原因',
      prop: 'restDesc'
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
