import { mount } from '@vue/test-utils'
import { beforeEach, afterEach, expect, it, vi } from 'vitest'
import Page from './index.vue'
import * as api from '@/api/platform/print/printers'
vi.mock('@/router/axios', () => ({ default: vi.fn() }))
vi.mock('@/api/platform/print/printers', () => ({ listPrinters: vi.fn(), getPrinter: vi.fn(), savePrinter: vi.fn(), disablePrinter: vi.fn() }))
let wrapper
beforeEach(async () => { vi.resetAllMocks(); api.listPrinters.mockResolvedValue({ records: [], total: 0 }); wrapper = mount(Page, { propsData: { parkId: '1' } }); await wrapper.vm.begin() })
afterEach(() => wrapper.destroy())
it('未验收翻面模块不允许保存自动双面，手动模式仍可保存', async () => {
  await wrapper.setData({ form: { ...wrapper.vm.form, displayName: '制卡台', deviceIdentity: 'pc1', auto: true } })
  await wrapper.vm.save(); expect(api.savePrinter).not.toHaveBeenCalled()
  await wrapper.setData({ form: { ...wrapper.vm.form, auto: false } }); await wrapper.vm.save()
  expect(api.savePrinter).toHaveBeenCalledWith(null, expect.objectContaining({ allowedPrintModes: ['MANUAL_DUPLEX'], flipCapability: 'MANUAL_ONLY', capabilityStatus: 'UNVERIFIED' }), expect.any(String))
})
it('Brother 仅保存单面与58毫米打印宽度，不保留上次厂牌自动配置', async () => {
  await wrapper.setData({ form: { ...wrapper.vm.form, displayName: '访客台', deviceIdentity: 'pc1', model: 'QL-800', auto: true, flipVerified: true } }); wrapper.vm.modelChanged()
  await wrapper.vm.save()
  expect(api.savePrinter).toHaveBeenCalledWith(null, expect.objectContaining({ deviceType: 'LABEL_PRINTER', allowedPrintModes: ['SINGLE'], flipCapability: 'NONE', mediaSpec: expect.objectContaining({ maxPrintableWidthMm: 58 }) }), expect.any(String))
})
it('旧打印机档案缺少允许方式时仍可显示列表', async () => {
  await wrapper.setData({ records: [{ printerProfileId: 'legacy', displayName: '旧打印机', manufacturer: 'Brother', model: 'QL-800', deviceIdentity: 'legacy-pc', allowedPrintModes: null, status: 'ENABLED' }] })
  expect(wrapper.find('table').text()).toContain('旧打印机')
})
it('保存冲突或连接失败不覆盖表单，重复同一请求复用幂等键', async () => {
  await wrapper.setData({ form: { ...wrapper.vm.form, displayName: '制卡台', deviceIdentity: 'pc1' } })
  api.savePrinter.mockRejectedValueOnce(new Error('档案修订冲突')).mockResolvedValueOnce({})
  await wrapper.vm.save(); expect(wrapper.vm.form.displayName).toBe('制卡台')
  await wrapper.vm.save(); expect(api.savePrinter.mock.calls[1]).toEqual(api.savePrinter.mock.calls[0])
})

it('更换工作站标识撤销旧校准与翻面验收，不能带旧证据保存自动模式', async () => {
  await wrapper.setData({ form: { ...wrapper.vm.form, displayName: '制卡台', deviceIdentity: 'pc1', auto: true, flipVerified: true, calibrationVerified: true, driverVersion: 'test', verifiedBy: '合成验收人', verifiedAt: '2026-09-05T10:00', evidenceId: 'old-evidence' } })
  const identity = wrapper.findAll('input').wrappers.find(input => input.element.value === 'pc1')
  identity.element.value = 'pc2'; await identity.trigger('input')
  await wrapper.vm.save(); expect(api.savePrinter).not.toHaveBeenCalled()
  expect(wrapper.vm.form.calibrationVerified).toBe(false); expect(wrapper.vm.form.flipVerified).toBe(false)
  expect(wrapper.vm.form.verifiedBy).toBe(''); expect(wrapper.vm.form.evidenceId).not.toBe('old-evidence')
})
it('已配置取放卡说明随档案保存，重开继续显示', async () => {
  await wrapper.setData({ form: { ...wrapper.vm.form, displayName: '制卡台', deviceIdentity: 'pc1', frontFeedInstruction: '正面照片朝上', backFeedInstruction: '沿短边翻面' } })
  const data = wrapper.vm.payload()
  expect(data.calibration).toMatchObject({ frontFeedInstruction: '正面照片朝上', backFeedInstruction: '沿短边翻面' })
  api.getPrinter.mockResolvedValue({ ...data, printerProfileId: 'p1' }); await wrapper.vm.begin({ printerProfileId: 'p1' })
  expect(wrapper.vm.form.backFeedInstruction).toBe('沿短边翻面')
})
