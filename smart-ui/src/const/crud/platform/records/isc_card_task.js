const actionText = (row, value) => {
  if (row && typeof row !== 'object') {
    return row
  }
  if (row && row.actionDesc) {
    return row.actionDesc
  }
  const actionValue = row && row.action !== null && row.action !== undefined && row.action !== '' ? row.action : value
  if (actionValue === null || actionValue === undefined || actionValue === '') {
    return '-'
  }
  const action = Number(actionValue)
  if (action === 1) {
    return '新增卡片'
  }
  if (action === 2) {
    return '删除卡片'
  }
  return `未知动作(${actionValue})`
}

const resultText = (row, value) => {
  if (row && typeof row !== 'object') {
    return row || '-'
  }
  return row && row.remark ? row.remark : (value || '-')
}

export const tableOption = {
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
  searchSize: 'mini',
  addBtn: false,
  editBtn: false,
  delBtn: false,
  viewBtn: false,
  column: [
    {
      label: '任务ID',
      prop: 'id',
      width: 170,
      hide: true
    },
    {
      label: '园区',
      prop: 'parkName',
      width: 120
    },
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
      label: '卡 ID',
      prop: 'cardNo',
      width: 140
    },
    {
      label: '任务动作',
      prop: 'actionDesc',
      formatter: actionText,
      width: 110
    },
    {
      label: '执行结果',
      prop: 'remark',
      formatter: resultText,
      overHidden: true,
      minWidth: 220
    },
    {
      label: '创建时间',
      prop: 'createTime',
      width: 165
    },
    {
      label: '更新时间',
      prop: 'updateTime',
      width: 165
    },
    {
      label: '操作人',
      prop: 'optUser',
      width: 110
    }
  ]
}
