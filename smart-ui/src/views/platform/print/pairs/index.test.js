// 组合保存必须请求平台并固定两个版本；刷新以后以平台记录为准。
import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import Page from './index.vue'
import * as api from '@/api/platform/print/pairs'
import * as templates from '@/api/platform/print/templates'
vi.mock('@/router/axios', () => ({ default: vi.fn() }))
vi.mock('@/api/platform/print/pairs', () => ({ listPairs: vi.fn(), savePair: vi.fn(), getPair: vi.fn(), archivePair: vi.fn() }))
vi.mock('@/api/platform/print/templates', () => ({ listTemplates: vi.fn(), getTemplate: vi.fn(), getPreview: vi.fn(), downloadPreviewArtifact: vi.fn() }))
let wrapper
const rows = ['FRONT', 'BACK'].map((faceRole, index) => ({ templateId: faceRole, name: faceRole === 'FRONT' ? '员工级正面' : '通用背面', faceRole, printItemType: 'STAFF_CARD', personType: 'EMPLOYEE', classificationCode: 'STAFF_DEFAULT', lifecycleStatus: 'ACTIVE', versions: [{ templateVersionId: `version-${index}`, versionStatus: 'PUBLISHED', versionNo: index + 1, pageSpecJson: { widthMm: 85.6, heightMm: 53.98, orientation: 'LANDSCAPE' } }] }))
async function settle() { await new Promise(resolve => setTimeout(resolve, 0)) }
async function click(label) { const button = wrapper.findAll('button').wrappers.find(item => item.text() === label); expect(button).toBeDefined(); if (button.attributes('type') === 'submit') await wrapper.find('form').trigger('submit'); else await button.trigger('click'); await settle() }
beforeEach(async () => { vi.resetAllMocks(); api.listPairs.mockResolvedValue({ records: [], total: 0 }); templates.listTemplates.mockResolvedValue({ records: rows, total: 2 }); templates.getTemplate.mockImplementation(id => Promise.resolve(rows.find(row => row.templateId === id))); wrapper = mount(Page, { propsData: { parkId: 'park-a' }, mocks: { $confirm: vi.fn().mockResolvedValue() } }); await settle() })
afterEach(() => wrapper.destroy())
it('新模板没有适用绑定也能建立正反面组合，并向系统保存两个具体版本', async () => {
  await click('新建组合')
  await wrapper.find('[aria-label="组合名称"]').setValue('员工级组合')
  await wrapper.find('[aria-label="正面版本"]').setValue('version-0')
  await wrapper.find('[aria-label="背面版本"]').setValue('version-1')
  api.savePair.mockResolvedValue({ pairId: 'pair-a', revision: 0 })
  await click('保存组合')
  expect(api.savePair).toHaveBeenCalledWith(null, expect.objectContaining({ parkId: 'park-a', frontTemplateVersionId: 'version-0', backTemplateVersionId: 'version-1' }), expect.any(String))
  expect(api.listPairs).toHaveBeenCalledTimes(2)
})
it('未选齐两面不能保存，不调用接口', async () => {
  await click('新建组合')
  await wrapper.find('[aria-label="组合名称"]').setValue('缺背面')
  await click('保存组合')
  expect(api.savePair).not.toHaveBeenCalled()
  expect(wrapper.find('[role="alert"]').text()).toContain('正面和背面')
})
it('归档响应丢失后以同一修订和幂等键重试，成功后释放状态', async () => {
  await wrapper.setData({ records: [{ pairId: 'pair-a', revision: 3, name: '员工组合', status: 'ACTIVE' }] })
  api.archivePair.mockRejectedValueOnce(new Error('连接中断')).mockResolvedValueOnce({})
  await click('归档'); await click('归档')
  expect(api.archivePair.mock.calls[1]).toEqual(api.archivePair.mock.calls[0])
  expect(wrapper.vm.pendingArchive).toBeNull()
})
