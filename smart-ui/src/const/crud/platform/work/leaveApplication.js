import { baseTableOption } from './_base'

export const tableOption = {
  ...baseTableOption,
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
      label: '离职日期',
      prop: 'leaveTime',
      type:'datetime',
      type: "datetime",
      format: "yyyy-MM-dd"

    },
	  {
      label: '离职原因',
      prop: 'leaveReasonDesc'
    },
    {
      label: '离职类型',
      prop: 'leaveTypeDesc',
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
