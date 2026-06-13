const cardSyncEnabledText = (row, value) => {
  const syncValue = row && typeof row === 'object' ? row.cardSyncEnabled : value
  if (syncValue === null || syncValue === undefined || syncValue === '') {
    return '-'
  }
  const syncEnabled = Number(syncValue)
  if (syncEnabled === 1) {
    return '启用卡片同步'
  }
  if (syncEnabled === 0) {
    return '停用卡片同步'
  }
  return `未知状态(${syncValue})`
}

export const tableOption = {
  border: true,
  index: true,
  indexLabel: '序号',
  stripe: false,
  menuAlign: 'center',
  menuWidth: 320,
  align: 'center',
  editBtn: false,
  delBtn: false,
  addBtn: false,
  column: [
    {
      label: 'ID',
      prop: 'id',
      hide: true
    },
    {
      label: '业务园区',
      prop: 'parkName'
    },
    {
      label: 'ISC调度园区',
      prop: 'dispatcherParkName'
    },
    {
      label: '调度园区ID',
      prop: 'dispatcherParkId'
    },
    {
      label: '卡片同步',
      prop: 'cardSyncEnabled',
      formatter: cardSyncEnabledText
    },
    {
      label: '备注',
      prop: 'remark',
      overHidden: true
    },
    {
      label: '更新时间',
      prop: 'updateTime'
    }
  ]
}
