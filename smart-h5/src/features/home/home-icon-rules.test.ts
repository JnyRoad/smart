import { describe, expect, test } from 'vitest'
import { resolveApprovalIconName, resolveServiceIconName } from './home-icon-rules'

describe('home icon rules', () => {
  test('固定审批入口使用稳定的本地图标', () => {
    expect(resolveApprovalIconName('good-release-live')).toBe('release')
    expect(resolveApprovalIconName('dorm-repairs')).toBe('repair')
    expect(resolveApprovalIconName('dorm-exit')).toBe('exit')
  })

  test('后端园区服务按 moduleUrl 映射本地图标', () => {
    expect(resolveServiceIconName('/approve', '待我审批')).toBe('approval')
    expect(resolveServiceIconName('/articlesrelease', '办公物品放行')).toBe('workRelease')
    expect(resolveServiceIconName('/releaseGoods', '宿舍物品放行')).toBe('release')
    expect(resolveServiceIconName('/xuchang/checkIn', '入住申请')).toBe('checkIn')
    expect(resolveServiceIconName('/xuchang/dormExit', '退宿申请')).toBe('exit')
    expect(resolveServiceIconName('/dormRepairs', '园区报修')).toBe('repair')
    expect(resolveServiceIconName('/dorm', '我的宿舍')).toBe('dorm')
    expect(resolveServiceIconName('/returnFactory', '返厂确认')).toBe('returnFactory')
  })

  test('扫码入口和未知服务有明确兜底', () => {
    expect(resolveServiceIconName('', '扫码放行')).toBe('scan')
    expect(resolveServiceIconName('/unknown', '自定义服务')).toBe('fallback')
  })
})
