import { beforeEach, expect, it, vi } from 'vitest'
import { dispatchVisitorPrint, parseVisitorSelection } from './cutover'
import { printRequest } from './client'
vi.mock('./client', () => ({ printRequest: vi.fn() }))
beforeEach(() => vi.resetAllMocks())
const visitor = { parkId: 1, id: 21, visitorName: '合成访客', smsCode: '保密码', fellowVisitorList: [{ id: 31, visitorName: '合成随行', cardNo: '保密身份' }] }
it('默认旧通道只调用旧入口，状态不可读或暂停不能回退旧通道', async () => {
  const legacy = vi.fn(); const navigate = vi.fn()
  printRequest.mockResolvedValue({ visitorMode: 'LEGACY', legacyVisitorAllowed: true })
  await dispatchVisitorPrint(visitor, 'ADMITTANCE', legacy, navigate); expect(legacy).toHaveBeenCalledTimes(1); expect(navigate).not.toHaveBeenCalled()
  for (const status of [{ visitorMode: 'PAUSED' }, { visitorMode: 'LEGACY', legacyVisitorAllowed: false }, {}]) {
    printRequest.mockResolvedValue(status); await expect(dispatchVisitorPrint(visitor, 'ADMITTANCE', legacy, navigate)).rejects.toThrow(); expect(legacy).toHaveBeenCalledTimes(1)
  }
  printRequest.mockRejectedValue(new Error('连接失败')); await expect(dispatchVisitorPrint(visitor, 'ADMITTANCE', legacy, navigate)).rejects.toThrow(); expect(legacy).toHaveBeenCalledTimes(1)
})
it('新入口仅携带申请与随行ID，绝不携带预约码、照片和姓名，也不直接打印', async () => {
  printRequest.mockResolvedValue({ visitorMode: 'TEMPLATE', newJobCreationEnabled: true })
  const legacy = vi.fn(); const navigate = vi.fn()
  await dispatchVisitorPrint(visitor, 'ADMITTANCE', legacy, navigate)
  expect(legacy).not.toHaveBeenCalled()
  expect(navigate).toHaveBeenCalledWith({ path: '/platform/print/jobs/visitor', query: { parkId: '1', subjects: JSON.stringify([{ subjectType: 'ADMITTANCE', subjectId: '21' }, { subjectType: 'ADMITTANCE_COMPANION', subjectId: '31' }]) } })
  expect(JSON.stringify(navigate.mock.calls)).not.toMatch(/保密|合成/)
})
it('路由输入须显式访客命名空间、非空唯一ID，拒绝伪造字段和超量', () => {
  expect(parseVisitorSelection(JSON.stringify([{ subjectType: 'VISITOR', subjectId: '2' }]))).toHaveLength(1)
  for (const value of ['bad', '{}', '[]', JSON.stringify([{ subjectType: 'STAFF', subjectId: '2' }]), JSON.stringify([{ subjectType: 'VISITOR', subjectId: '2', fields: {} }]), JSON.stringify(Array(101).fill({ subjectType: 'VISITOR', subjectId: '2' }))]) expect(() => parseVisitorSelection(value)).toThrow()
})
it('入厂申请同行列表中的主访客不会再次作为随行打印', async () => {
  printRequest.mockResolvedValue({ visitorMode: 'TEMPLATE', newJobCreationEnabled: true })
  const navigate = vi.fn()
  await dispatchVisitorPrint({ ...visitor, fellowVisitorList: [{ id: 30, isMain: 1 }, { id: 31, isMain: 0 }] }, 'ADMITTANCE', vi.fn(), navigate)
  expect(JSON.parse(navigate.mock.calls[0][0].query.subjects)).toHaveLength(2)
})
it('浏览器已丢失精度的数字ID不能交给新打印通道', async () => {
  printRequest.mockResolvedValue({ visitorMode: 'TEMPLATE', newJobCreationEnabled: true })
  await expect(dispatchVisitorPrint({ ...visitor, id: Number.MAX_SAFE_INTEGER + 1 }, 'ADMITTANCE', vi.fn(), vi.fn())).rejects.toThrow()
})
