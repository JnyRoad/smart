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
      prop: 'name',
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
      label: '补贴开始时间',
      prop: 'startTime'

    },
	  {
      label: '补贴类型',
      prop: 'allowanceType'

    },
	  {
      label: '补贴金额',
      prop: 'amount'
    },
	  {
      label: '流程编号',
      prop: 'processId',
    },

    {
      label: '创建时间',
      prop: 'createTime',
      width: 165
    }

  ]
}
