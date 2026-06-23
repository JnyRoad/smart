import { baseTableOption } from './_base'

export const tableOption = {
  ...baseTableOption,
  column: [
    {
      label: 'ID',
      prop: 'id',
      hide: true
    },
    {
      label: 'parkId',
      prop: 'parkId',
      hide: true
    },
	  {
      label: '园区',
      prop: 'parkName'
    },
	  {
      label: '创建时间',
      prop: 'createTime'
    },
  ]
}
