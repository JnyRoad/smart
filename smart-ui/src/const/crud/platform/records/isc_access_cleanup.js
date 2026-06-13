const textOrEmpty = value => {
  if (value === null || value === undefined || value === '') {
    return '-'
  }
  return value
}

const statusText = (row, value) => {
  if (row && typeof row !== 'object') {
    return textOrEmpty(row)
  }
  return textOrEmpty(row && row.cleanupStatusDesc ? row.cleanupStatusDesc : value)
}

const taskStatusText = (row, value) => {
  if (row && typeof row !== 'object') {
    return textOrEmpty(row)
  }
  return textOrEmpty(row && row.deleteTaskStatusDesc ? row.deleteTaskStatusDesc : value)
}

export const tableOption = {
  border: false,
  index: true,
  selection: true,
  indexLabel: '序号',
  stripe: true,
  menu: true,
  menuAlign: 'center',
  menuWidth: 110,
  labelWidth: 100,
  align: 'center',
  refreshBtn: false,
  columnBtn: false,
  searchBtn: false,
  showClomnuBtn: false,
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  column: [
    {
      label: '记录ID',
      prop: 'downRecordId',
      width: 170,
      hide: true
    },
    {
      label: '园区',
      prop: 'parkName',
      width: 120
    },
    {
      label: '人员类型',
      prop: 'personTypeDesc',
      width: 90
    },
    {
      label: '姓名',
      prop: 'personName',
      width: 110
    },
    {
      label: '证件/工号',
      prop: 'badge',
      width: 150
    },
    {
      label: '本地人员ID',
      prop: 'cardNo',
      width: 140
    },
    {
      label: 'ISC人员ID',
      prop: 'personId',
      width: 150
    },
    {
      label: '设备',
      prop: 'deviceName',
      minWidth: 150,
      overHidden: true
    },
    {
      label: '设备编码',
      prop: 'deviceCode',
      width: 140
    },
    {
      label: '权限来源',
      prop: 'serviceTypeDesc',
      width: 100
    },
    {
      label: '开始时间',
      prop: 'startTime',
      width: 165
    },
    {
      label: '结束时间',
      prop: 'overTime',
      width: 165
    },
    {
      label: '处理状态',
      prop: 'cleanupStatusDesc',
      formatter: statusText,
      width: 90
    },
    {
      label: '删除任务',
      prop: 'deleteTaskStatusDesc',
      formatter: taskStatusText,
      width: 90
    },
    {
      label: '原因',
      prop: 'reason',
      minWidth: 220,
      overHidden: true
    }
  ]
}
