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
      label: '开始时间',
      prop: 'startDate',
      solt: true
    },
	  {
      label: '结束时间',
      prop: 'endDate',
      solt: true
    },
	  {
      label: '请假类型',
      prop: 'typeDesc'
    },

    {
      label: '请假时长',
      prop: 'vacateCount',
    },
    {
      label: '请假原因',
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
