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
