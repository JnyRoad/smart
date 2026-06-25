import {
  trimValue,
  isParkSyncEnabled,
  validateCardNo
} from './flow-rules'

export function validateQueueCandidate(staff, cardNo, park, queue = []) {
  if (!park) {
    return { valid: false, message: '请选择园区' }
  }
  if (!isParkSyncEnabled(park)) {
    return { valid: false, message: '当前园区未启用ISC卡片同步' }
  }
  if (!staff || !staff.id) {
    return { valid: false, message: '未找到有效员工' }
  }
  if (Number(staff.status) === 0) {
    return { valid: false, message: '员工已离职，不允许维护ISC卡片' }
  }
  const cardValidation = validateCardNo(cardNo)
  if (!cardValidation.valid) {
    return cardValidation
  }
  const duplicate = queue.find(item => item.status !== 'success' && String(item.parkId) === String(park.parkId) && item.cardNo === trimValue(cardNo))
  if (duplicate) {
    return { valid: false, message: '卡号已在待提交队列中' }
  }
  return { valid: true, message: '' }
}

export function buildQueueRow({
  queueId,
  staff,
  cardNo,
  park,
  queue = []
}) {
  const validation = validateQueueCandidate(staff, cardNo, park, queue)
  return {
    queueId,
    staffId: staff && staff.id,
    badge: staff && staff.badge,
    name: staff && staff.name,
    staffStatus: staff && staff.status,
    parkId: park && park.parkId,
    parkName: park && park.parkName,
    dispatcherParkId: park && park.dispatcherParkId,
    dispatcherParkName: park && park.dispatcherParkName,
    cardNo: trimValue(cardNo),
    status: validation.valid ? 'ready' : 'invalid',
    message: validation.message
  }
}

export function buildInvalidQueueRow({
  queueId,
  badge,
  cardNo,
  message,
  park
}) {
  return {
    queueId,
    staffId: null,
    badge,
    name: '-',
    staffStatus: null,
    parkId: park && park.parkId,
    parkName: park && park.parkName,
    dispatcherParkId: park && park.dispatcherParkId,
    dispatcherParkName: park && park.dispatcherParkName,
    cardNo: trimValue(cardNo),
    status: 'invalid',
    message
  }
}

export function parsePasteText(text) {
  return (text || '').split(/\n/).map((line, index) => {
    const normalizedLine = trimValue(line)
    if (!normalizedLine) {
      return null
    }
    const parts = normalizedLine.split(/[\s,，]+/)
    if (parts.length !== 2) {
      return {
        line: index + 1,
        badge: parts[0] || '',
        cardNo: parts[1] || '',
        message: parts.length < 2 ? '缺少工号或卡号' : '每行只能填写工号和卡号'
      }
    }
    const badge = parts[0]
    const cardNo = parts[1]
    const cardValidation = validateCardNo(cardNo)
    return {
      line: index + 1,
      badge,
      cardNo,
      message: cardValidation.valid ? '' : cardValidation.message
    }
  }).filter(Boolean)
}

export const queueSavingPatch = () => ({
  status: 'saving',
  message: '正在保存并创建ISC同步任务...'
})

export const queueSuccessPatch = () => ({
  status: 'success',
  message: '保存成功，ISC同步任务已创建'
})

export const queueFailedPatch = message => ({
  status: 'failed',
  message
})
