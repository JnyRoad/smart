import { describe, expect, it } from 'vitest'
import {
  validateQueueCandidate,
  buildQueueRow,
  buildInvalidQueueRow,
  parsePasteText,
  queueSavingPatch,
  queueSuccessPatch,
  queueFailedPatch
} from './queue-flow'

const enabledPark = {
  parkId: 5000021,
  parkName: '裕同科技许昌园区',
  dispatcherParkId: 9001,
  dispatcherParkName: '许昌ISC',
  cardSyncEnabled: 1
}

const disabledPark = {
  parkId: 5000022,
  parkName: '未启用园区',
  dispatcherParkId: 9002,
  dispatcherParkName: '停用ISC',
  cardSyncEnabled: 0
}

const staff = {
  id: 1509092220633108482,
  badge: 'YD8800010',
  name: '王金鸽',
  status: 1
}

describe('isc card fast add queue flow', () => {
  it('keeps queue candidate validation messages and duplicate rule stable', () => {
    expect(validateQueueCandidate(null, '1024388812', null, [])).toStrictEqual({ valid: false, message: '请选择园区' })
    expect(validateQueueCandidate(staff, '1024388812', disabledPark, [])).toStrictEqual({ valid: false, message: '当前园区未启用ISC卡片同步' })
    expect(validateQueueCandidate(null, '1024388812', enabledPark, [])).toStrictEqual({ valid: false, message: '未找到有效员工' })
    expect(validateQueueCandidate({ ...staff, status: 0 }, '1024388812', enabledPark, [])).toStrictEqual({ valid: false, message: '员工已离职，不允许维护ISC卡片' })
    expect(validateQueueCandidate(staff, '9994388812', enabledPark, [])).toStrictEqual({ valid: false, message: '999开头为ISC虚拟卡号，不允许维护' })
    expect(validateQueueCandidate(staff, ' 1024388812 ', enabledPark, [
      { status: 'ready', parkId: enabledPark.parkId, cardNo: '1024388812' },
      { status: 'success', parkId: enabledPark.parkId, cardNo: '1024388812' }
    ])).toStrictEqual({ valid: false, message: '卡号已在待提交队列中' })
    expect(validateQueueCandidate(staff, '1024388812', enabledPark, [])).toStrictEqual({ valid: true, message: '' })
  })

  it('builds valid and invalid queue rows with the same field shape as the page', () => {
    expect(buildQueueRow({
      queueId: 7,
      staff,
      cardNo: ' 1024388812 ',
      park: enabledPark,
      queue: []
    })).toStrictEqual({
      queueId: 7,
      staffId: staff.id,
      badge: staff.badge,
      name: staff.name,
      staffStatus: staff.status,
      parkId: enabledPark.parkId,
      parkName: enabledPark.parkName,
      dispatcherParkId: enabledPark.dispatcherParkId,
      dispatcherParkName: enabledPark.dispatcherParkName,
      cardNo: '1024388812',
      status: 'ready',
      message: ''
    })

    expect(buildInvalidQueueRow({
      queueId: 8,
      badge: '10299',
      cardNo: ' 1024388899 ',
      message: '未找到该工号对应员工',
      park: enabledPark
    })).toStrictEqual({
      queueId: 8,
      staffId: null,
      badge: '10299',
      name: '-',
      staffStatus: null,
      parkId: enabledPark.parkId,
      parkName: enabledPark.parkName,
      dispatcherParkId: enabledPark.dispatcherParkId,
      dispatcherParkName: enabledPark.dispatcherParkName,
      cardNo: '1024388899',
      status: 'invalid',
      message: '未找到该工号对应员工'
    })
  })

  it('parses batch paste text exactly like the page parser', () => {
    expect(parsePasteText('10288 1024388812\n\n10290,1024388845\n10291，9994388812\n10292 1024388845 extra\n10293')).toStrictEqual([
      { line: 1, badge: '10288', cardNo: '1024388812', message: '' },
      { line: 3, badge: '10290', cardNo: '1024388845', message: '' },
      { line: 4, badge: '10291', cardNo: '9994388812', message: '999开头为ISC虚拟卡号，不允许维护' },
      { line: 5, badge: '10292', cardNo: '1024388845', message: '每行只能填写工号和卡号' },
      { line: 6, badge: '10293', cardNo: '', message: '缺少工号或卡号' }
    ])
  })

  it('keeps submit status patch text stable', () => {
    expect(queueSavingPatch()).toStrictEqual({
      status: 'saving',
      message: '正在保存并创建ISC同步任务...'
    })
    expect(queueSuccessPatch()).toStrictEqual({
      status: 'success',
      message: '保存成功，ISC同步任务已创建'
    })
    expect(queueFailedPatch('接口失败')).toStrictEqual({
      status: 'failed',
      message: '接口失败'
    })
  })
})
