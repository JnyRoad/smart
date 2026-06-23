import { describe, expect, it } from 'vitest'
import {
  emptySearchForm,
  trimValue,
  isStaffBadgeKeyword,
  isParkSyncEnabled,
  parkOptionLabel,
  validateCardNo,
  cardSyncStatusText,
  cardSyncStatusType,
  queueStatusText,
  queueStatusType,
  taskActionText,
  responseMessage,
  errorMessage
} from './flow-rules'

describe('isc card fast add flow rules', () => {
  it('creates a fresh empty search form', () => {
    const firstForm = emptySearchForm()
    const secondForm = emptySearchForm()

    expect(firstForm).toStrictEqual({ parkId: '', staffKeyword: '' })
    expect(secondForm).toStrictEqual({ parkId: '', staffKeyword: '' })
    expect(firstForm).not.toBe(secondForm)
  })

  it('normalizes empty and whitespace-only values to trimmed strings', () => {
    expect(trimValue(null)).toBe('')
    expect(trimValue(undefined)).toBe('')
    expect(trimValue('  YD8800010  ')).toBe('YD8800010')
    expect(trimValue(1217999)).toBe('1217999')
  })

  it('detects badge-like keywords without accepting names or blanks', () => {
    expect(isStaffBadgeKeyword('YD8800010')).toBe(true)
    expect(isStaffBadgeKeyword('1217999')).toBe(true)
    expect(isStaffBadgeKeyword('王金鸽')).toBe(false)
    expect(isStaffBadgeKeyword('')).toBe(false)
  })

  it('keeps existing ISC park enablement and label rules', () => {
    expect(isParkSyncEnabled({ cardSyncEnabled: 1, dispatcherParkId: 0 })).toBe(true)
    expect(isParkSyncEnabled({ cardSyncEnabled: 1, dispatcherParkId: null })).toBe(false)
    expect(isParkSyncEnabled({ cardSyncEnabled: 0, dispatcherParkId: 9001 })).toBe(false)
    expect(parkOptionLabel({ parkName: '裕同科技许昌园区', parkId: 5000021 })).toBe('裕同科技许昌园区')
    expect(parkOptionLabel({ parkId: 5000021 })).toBe(5000021)
    expect(parkOptionLabel(null)).toBe('')
  })

  it('validates ISC physical card numbers exactly as the page does today', () => {
    expect(validateCardNo('1024388812')).toStrictEqual({ valid: true, message: '' })
    expect(validateCardNo('')).toStrictEqual({ valid: false, message: 'ISC卡号不能为空' })
    expect(validateCardNo('abc12345')).toStrictEqual({ valid: false, message: 'ISC卡号必须为8-20位数字或大写字母' })
    expect(validateCardNo('1234567')).toStrictEqual({ valid: false, message: 'ISC卡号必须为8-20位数字或大写字母' })
    expect(validateCardNo('9994388812')).toStrictEqual({ valid: false, message: '999开头为ISC虚拟卡号，不允许维护' })
  })

  it('keeps card, queue, and task status text mappings stable', () => {
    expect(cardSyncStatusText({ syncStatusDesc: '平台返回文案', syncStatus: 2 })).toBe('平台返回文案')
    expect([0, 1, 2, 3, 9].map(syncStatus => cardSyncStatusText({ syncStatus }))).toStrictEqual(['待同步', '已同步', '同步失败', '本地取消', '未知'])
    expect([0, 1, 2, 3, 9].map(cardSyncStatusType)).toStrictEqual(['warning', 'success', 'danger', 'info', 'info'])
    expect(['ready', 'invalid', 'saving', 'success', 'failed', 'other'].map(queueStatusText)).toStrictEqual(['待提交', '校验失败', '保存中', '成功', '失败', '-'])
    expect(['ready', 'invalid', 'saving', 'success', 'failed', 'other'].map(queueStatusType)).toStrictEqual(['warning', 'danger', 'info', 'success', 'danger', 'info'])
    expect([1, 2, 9].map(action => taskActionText({ action }))).toStrictEqual(['新增卡片', '删除卡片', '-'])
    expect(taskActionText({ action: 2, actionDesc: '自定义动作' })).toBe('自定义动作')
  })

  it('keeps backend response and error message fallback behavior stable', () => {
    expect(responseMessage({ data: { msg: '业务失败' } }, '保存失败')).toBe('业务失败')
    expect(responseMessage({ data: { message: '详细失败' } }, '保存失败')).toBe('详细失败')
    expect(responseMessage({ data: {} }, '保存失败')).toBe('保存失败')
    expect(errorMessage({ response: { data: { msg: '接口失败' } } })).toBe('接口失败')
    expect(errorMessage({ message: '网络失败' })).toBe('网络失败')
    expect(errorMessage(null)).toBe('保存失败')
  })
})
