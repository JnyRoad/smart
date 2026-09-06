// 适用规则由系统保存；职级必须来自已确认字典，不能以页面示例值替代。
import { mount } from '@vue/test-utils'
import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import Page from './index.vue'
import * as api from '@/api/platform/print/bindings'
import { listPairs } from '@/api/platform/print/pairs'
import { listTemplates } from '@/api/platform/print/templates'
vi.mock('@/router/axios', () => ({ default: vi.fn() }))
vi.mock('@/api/platform/print/bindings', () => ({ listBindings: vi.fn(), saveBinding: vi.fn(), disableBinding: vi.fn(), employeeGrades: vi.fn() }))
vi.mock('@/api/platform/print/pairs', () => ({ listPairs: vi.fn() }))
vi.mock('@/api/platform/print/templates', () => ({ listTemplates: vi.fn() }))
let wrapper
const settle = () => new Promise(resolve => setTimeout(resolve, 0))
async function click(text) { const button = wrapper.findAll('button').wrappers.find(item => item.text() === text); expect(button).toBeDefined(); await button.trigger('click'); await settle() }
beforeEach(async () => {
  vi.resetAllMocks()
  api.listBindings.mockResolvedValue({ records: [], total: 0 })
  api.employeeGrades.mockResolvedValue({ confirmed: true, records: [{ code: '7', name: '职员' }, { code: '9', name: '员工' }] })
  listPairs.mockResolvedValue({ records: [{ pairId: 'pair-a', name: '已发布员工组合', personType: 'EMPLOYEE', classificationCode: 'STAFF_DEFAULT', status: 'ACTIVE' }, { pairId: 'pair-b', name: '外包组合', personType: 'OUTSOURCED', classificationCode: 'OUTSOURCE_DEFAULT', status: 'ACTIVE' }], total: 2 })
  listTemplates.mockResolvedValue({ records: [], total: 0 })
  wrapper = mount(Page, { propsData: { parkId: '1' } })
  await settle()
})
afterEach(() => wrapper.destroy())
it('只显示服务端职级选项，允许多个职级保存到同一系统组合', async () => {
  await click('新增适用规则')
  expect(wrapper.findAll('[aria-label="适用职级"] option').wrappers.map(item => item.text())).toEqual(['职员', '员工'])
  await wrapper.find('[aria-label="适用职级"]').setValue(['7', '9'])
  await wrapper.find('[aria-label="模板组合"]').setValue('pair-a')
  await wrapper.find('form').trigger('submit'); await settle()
  expect(api.saveBinding).toHaveBeenCalledWith(null, expect.objectContaining({ parkId: '1', pairId: 'pair-a', employeeGradeCodes: ['7', '9'], scopeType: 'EXPLICIT_DEFAULT', classificationCode: 'STAFF_DEFAULT' }), expect.any(String))
})
it('职级映射未确认时禁止员工规则保存，但仍可维护外包规则', async () => {
  api.employeeGrades.mockRejectedValue(new Error('DHR职层映射尚未确认'))
  await click('新增适用规则')
  await wrapper.find('[aria-label="模板组合"]').setValue('pair-a')
  await wrapper.find('form').trigger('submit'); await settle()
  expect(api.saveBinding).not.toHaveBeenCalled()
  expect(wrapper.text()).toContain('尚未确认')
  await wrapper.find('[aria-label="人员类型"]').setValue('OUTSOURCED'); await settle()
  await wrapper.find('[aria-label="模板组合"]').setValue('pair-b')
  await wrapper.find('form').trigger('submit'); await settle()
  expect(api.saveBinding).toHaveBeenCalledWith(null, expect.objectContaining({ personType: 'OUTSOURCED', employeeGradeCodes: null }), expect.any(String))
})
it('保存冲突保留表单，连接错误重试复用同一请求键', async () => {
  await click('新增适用规则')
  await wrapper.find('[aria-label="适用职级"]').setValue(['9'])
  await wrapper.find('[aria-label="模板组合"]').setValue('pair-a')
  api.saveBinding.mockRejectedValueOnce(new Error('关联修订冲突')).mockResolvedValueOnce({})
  await wrapper.find('form').trigger('submit'); await settle()
  expect(wrapper.find('[aria-label="模板组合"]').element.value).toBe('pair-a')
  expect(wrapper.text()).toContain('关联修订冲突')
  await wrapper.find('form').trigger('submit'); await settle()
  expect(api.saveBinding.mock.calls[1]).toEqual(api.saveBinding.mock.calls[0])
})
it('重新打开页面从平台读取保存规则，不以设计器内存恢复关联', async () => {
  wrapper.destroy()
  api.listBindings.mockResolvedValue({ records: [{ bindingRuleId: 'b1', personType: 'EMPLOYEE', classificationCode: 'STAFF_DEFAULT', scopeType: 'EXPLICIT_DEFAULT', employeeGradeCodes: ['9'], pairId: 'pair-a', revision: 2, status: 'ACTIVE' }], total: 1 })
  wrapper = mount(Page, { propsData: { parkId: '1' } }); await settle()
  expect(wrapper.text()).toContain('STAFF_DEFAULT')
  expect(api.listBindings).toHaveBeenLastCalledWith({ parkId: '1', current: 1, size: 20 })
})

it('编辑非员工规则也遵守后端职级字段必须为空的契约', async () => {
  await wrapper.vm.beginEdit({ bindingRuleId: 'b-out', revision: 2, personType: 'OUTSOURCED', pairId: 'pair-b', classificationCode: 'OUTSOURCE_DEFAULT', scopeType: 'EXPLICIT_DEFAULT', validFrom: '2026-01-01T00:00:00Z', validTo: null, priority: 100 })
  await wrapper.find('form').trigger('submit'); await settle()
  expect(api.saveBinding).toHaveBeenCalledWith('b-out', expect.objectContaining({ revision: 2, pairId: 'pair-b', employeeGradeCodes: null }), expect.any(String))
})
