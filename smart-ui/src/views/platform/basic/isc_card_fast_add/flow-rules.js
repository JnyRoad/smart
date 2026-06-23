export const emptySearchForm = () => ({
  parkId: '',
  staffKeyword: ''
})

export const trimValue = value => (value === null || value === undefined ? '' : String(value).trim())

export const isStaffBadgeKeyword = value => /^[A-Za-z0-9]+$/.test(trimValue(value))

export function parkOptionLabel(item) {
  if (!item) {
    return ''
  }
  const parkName = item.parkName || item.parkId || '-'
  return parkName
}

export function isParkSyncEnabled(item) {
  return !!(item && item.cardSyncEnabled === 1 && item.dispatcherParkId !== null && item.dispatcherParkId !== undefined)
}

export function validateCardNo(cardNo) {
  const normalizedCardNo = trimValue(cardNo)
  if (!normalizedCardNo) {
    return { valid: false, message: 'ISC卡号不能为空' }
  }
  if (!/^[0-9A-Z]{8,20}$/.test(normalizedCardNo)) {
    return { valid: false, message: 'ISC卡号必须为8-20位数字或大写字母' }
  }
  if (normalizedCardNo.startsWith('999')) {
    return { valid: false, message: '999开头为ISC虚拟卡号，不允许维护' }
  }
  return { valid: true, message: '' }
}

export function cardSyncStatusText(row) {
  if (row && row.syncStatusDesc) {
    return row.syncStatusDesc
  }
  const syncStatus = Number(row && row.syncStatus)
  if (syncStatus === 0) {
    return '待同步'
  }
  if (syncStatus === 1) {
    return '已同步'
  }
  if (syncStatus === 2) {
    return '同步失败'
  }
  if (syncStatus === 3) {
    return '本地取消'
  }
  return '未知'
}

export function cardSyncStatusType(syncStatus) {
  const normalizedStatus = Number(syncStatus)
  if (normalizedStatus === 1) {
    return 'success'
  }
  if (normalizedStatus === 2) {
    return 'danger'
  }
  if (normalizedStatus === 3) {
    return 'info'
  }
  if (normalizedStatus === 0) {
    return 'warning'
  }
  return 'info'
}

export function queueStatusText(status) {
  const statusMap = {
    ready: '待提交',
    invalid: '校验失败',
    saving: '保存中',
    success: '成功',
    failed: '失败'
  }
  return statusMap[status] || '-'
}

export function queueStatusType(status) {
  const typeMap = {
    ready: 'warning',
    invalid: 'danger',
    saving: 'info',
    success: 'success',
    failed: 'danger'
  }
  return typeMap[status] || 'info'
}

export function taskActionText(row) {
  if (row && row.actionDesc) {
    return row.actionDesc
  }
  if (Number(row && row.action) === 1) {
    return '新增卡片'
  }
  if (Number(row && row.action) === 2) {
    return '删除卡片'
  }
  return '-'
}

export function responseMessage(response, fallback) {
  const responseData = response && response.data
  return (responseData && (responseData.msg || responseData.message)) || fallback
}

export function errorMessage(error) {
  if (error && error.response) {
    return responseMessage(error.response, '保存失败')
  }
  return (error && error.message) || '保存失败'
}
