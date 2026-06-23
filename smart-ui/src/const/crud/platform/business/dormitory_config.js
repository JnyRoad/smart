import { baseTableOption } from './_base'

export const tableOption = {
  ...baseTableOption,
  column: [
    {
      label: 'id',
      prop: 'id',
      hide: true
    },
    {
      label: '园区',
      prop: 'parkName'
    }
  ]
}
