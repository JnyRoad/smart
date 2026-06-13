const mapText = (value, map, unknownLabel, emptyText = '-') => {
  if (value === null || value === undefined || value === '') {
    return emptyText
  }
  if (map[value]) {
    return map[value]
  }
  return /^[A-Z_]+$/.test(String(value)) ? `${unknownLabel}(${value})` : value
}

const isCodeText = value => /^[A-Z_]+$/.test(String(value || ''))

const modeText = (row, value) => {
  const map = {
    DRY_RUN: '预检',
    IMPORT: '初始化导入'
  }
  if (row && typeof row !== 'object') {
    return mapText(row, map, '未知模式')
  }
  if (row && row.modeDesc) {
    return row.modeDesc
  }
  return mapText(row && row.mode !== null && row.mode !== undefined ? row.mode : value, map, '未知模式')
}

const staffScopeText = (row, value) => {
  const map = {
    ALL: '全部人员',
    ACTIVE: '在职人员',
    RESIGNED: '离职人员'
  }
  if (row && typeof row !== 'object') {
    return mapText(row, map, '未知范围', '全部人员')
  }
  if (row && row.staffScopeDesc) {
    return row.staffScopeDesc
  }
  return mapText(row && row.staffScope !== null && row.staffScope !== undefined ? row.staffScope : value, map, '未知范围', '全部人员')
}

const statusText = (row, value) => {
  const map = {
    INIT: '初始化',
    RUNNING: '执行中',
    SUCCESS: '完成',
    FAIL: '失败'
  }
  if (row && typeof row !== 'object') {
    return mapText(row, map, '未知状态')
  }
  if (row && row.statusDesc) {
    return row.statusDesc
  }
  return mapText(row && row.status !== null && row.status !== undefined ? row.status : value, map, '未知状态')
}

const resultText = (row, value) => {
  const map = {
    READY_IMPORT: '可导入',
    IMPORTED: '已导入',
    REMOVED: '已清理',
    SKIP_SAME: '已一致',
    CONFLICT: '冲突',
    LOCAL_ONLY: '本地多出',
    ISC_EMPTY: 'ISC无卡',
    STAFF_NOT_FOUND: 'ISC无人员',
    INVALID: '无效卡',
    FAIL: '失败'
  }
  if (row && typeof row !== 'object') {
    return mapText(row, map, '未知结果')
  }
  if (row && row.resultDesc && !isCodeText(row.resultDesc)) {
    return row.resultDesc
  }
  const resultValue = row && row.resultCode !== null && row.resultCode !== undefined ? row.resultCode : (row && row.resultDesc ? row.resultDesc : value)
  return mapText(resultValue, map, '未知结果')
}

export const batchTableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menu: true,
  menuAlign: 'center',
  menuWidth: 90,
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
      label: '批次ID',
      prop: 'id',
      width: 170
    },
    {
      label: '园区',
      prop: 'parkName',
      width: 120
    },
    {
      label: '模式',
      prop: 'modeDesc',
      formatter: modeText,
      width: 90
    },
    {
      label: '人员范围',
      prop: 'staffScopeDesc',
      formatter: staffScopeText,
      width: 100
    },
    {
      label: '状态',
      prop: 'statusDesc',
      formatter: statusText,
      width: 90
    },
    {
      label: '总数',
      prop: 'totalCount',
      width: 80
    },
    {
      label: '成功',
      prop: 'successCount',
      width: 80
    },
    {
      label: '跳过',
      prop: 'skipCount',
      width: 80
    },
    {
      label: '冲突',
      prop: 'conflictCount',
      width: 80
    },
    {
      label: '失败',
      prop: 'failCount',
      width: 80
    },
    {
      label: '耗时(ms)',
      prop: 'consume',
      width: 90
    },
    {
      label: '开始时间',
      prop: 'startTime',
      width: 165
    },
    {
      label: '结束时间',
      prop: 'endTime',
      width: 165
    },
    {
      label: '操作人',
      prop: 'optUser',
      width: 110
    },
    {
      label: '备注',
      prop: 'remark',
      overHidden: true,
      minWidth: 180
    }
  ]
}

export const detailTableOption = {
  border: false,
  index: true,
  indexLabel: '序号',
  stripe: true,
  menu: false,
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
      label: '工号',
      prop: 'badge',
      width: 120
    },
    {
      label: '姓名',
      prop: 'name',
      width: 100
    },
    {
      label: 'ISC人员ID',
      prop: 'personId',
      width: 150
    },
    {
      label: 'ISC卡号',
      prop: 'iscCardNo',
      width: 140
    },
    {
      label: '本地卡号',
      prop: 'localCardNo',
      width: 140
    },
    {
      label: '结果',
      prop: 'resultDesc',
      formatter: resultText,
      width: 110
    },
    {
      label: '原因',
      prop: 'reason',
      overHidden: true,
      minWidth: 220
    },
    {
      label: '创建时间',
      prop: 'createTime',
      width: 165
    }
  ]
}
