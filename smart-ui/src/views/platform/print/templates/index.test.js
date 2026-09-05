// 管理页使用真实业务状态，只替换网络与画布边界，验证冲突不会覆盖草稿。
import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { webcrypto, createHash } from 'node:crypto'
import { Blob as NodeBlob } from 'node:buffer'
import Page from './index.vue'
import * as api from '@/api/platform/print/templates'
vi.mock('@/api/platform/print/templates', () => ({ listTemplates: vi.fn(), getTemplate: vi.fn(), createTemplate: vi.fn(), saveDraft: vi.fn(), publishTemplate: vi.fn(), rollbackTemplate: vi.fn(), previewTemplate: vi.fn(), getPreview: vi.fn(), uploadTemplateResource: vi.fn(), downloadTemplateResource: vi.fn() }))
vi.mock('@/router/axios', () => ({ default: vi.fn() }))
let wrapper
const version = { templateVersionId: 'draft-a', layoutJson: { schemaVersion: 1, schemas: [[]], basePdfRef: null }, pageSpecJson: { widthMm: 85.6, heightMm: 53.98 }, fieldSchemaJson: { fields: [] }, resourceManifest: [] }
const detail = { templateId: 'front-a', name: '员工正面', printItemType: 'STAFF_CARD', personType: 'EMPLOYEE', classificationCode: 'STAFF_DEFAULT', faceRole: 'FRONT', draftRevision: 2, currentDraftVersionId: 'draft-a', draft: version, versions: [] }
const canvas = { basePdf: { width: 85.6, height: 53.98 }, schemas: [[{ name: 'title', type: 'text', content: '新布局' }]] }
const Designer = { props: ['disabled'], template: '<div aria-label="单面画布" />', methods: { getTemplate: () => canvas } }
async function settle() { await new Promise(resolve => setTimeout(resolve, 0)) }
async function click(label) { const button = wrapper.findAll('button').wrappers.find(item => item.text() === label); expect(button).toBeDefined(); await button.trigger('click'); await settle() }
beforeEach(async () => {
  vi.resetAllMocks()
  vi.stubGlobal('crypto', webcrypto)
  api.listTemplates.mockResolvedValue({ records: [detail], total: 1 })
  api.getTemplate.mockResolvedValue(JSON.parse(JSON.stringify(detail)))
  wrapper = mount(Page, { propsData: { parkId: 'park-a' }, stubs: { PdfmeDesigner: Designer }, mocks: { $confirm: () => Promise.resolve(), $prompt: vi.fn().mockResolvedValue({ value: '修正错误版本' }) } })
  await settle()
})
afterEach(() => { wrapper.destroy(); canvas.schemas = [[{ name: 'title', type: 'text', content: '新布局' }]]; vi.unstubAllGlobals() })
it('从系统加载单面模板，保存携带当前修订并重新读取服务端结果', async () => {
  await click('编辑')
  expect(wrapper.findAll('[aria-label="单面画布"]')).toHaveLength(1)
  api.saveDraft.mockResolvedValue({ draftRevision: 3 })
  await click('保存草稿')
  expect(api.saveDraft).toHaveBeenCalledWith('front-a', expect.objectContaining({ parkId: 'park-a', draftRevision: 2, layoutJson: expect.objectContaining({ schemas: canvas.schemas }) }))
  expect(api.getTemplate).toHaveBeenCalledTimes(2)
})
it('草稿冲突保留当前画布，明确提示重新加载，不自动重试覆盖', async () => {
  await click('编辑')
  api.saveDraft.mockRejectedValue(Object.assign(new Error('草稿已被其他人修改，请重新加载'), { code: 'DRAFT_REVISION_CONFLICT' }))
  await click('保存草稿')
  expect(wrapper.find('[role="alert"]').text()).toContain('重新加载')
  expect(wrapper.findAll('[aria-label="单面画布"]')).toHaveLength(1)
  expect(api.saveDraft).toHaveBeenCalledTimes(1)
  expect(api.getTemplate).toHaveBeenCalledTimes(1)
})
it('重建页面会重新读取系统列表，不从浏览器缓存恢复模板', async () => {
  expect(api.listTemplates).toHaveBeenCalledWith(expect.objectContaining({ parkId: 'park-a' }))
  expect(wrapper.text()).toContain('员工正面')
})
it('发布请求响应丢失时同一修订重试复用幂等键', async () => {
  await click('编辑')
  api.publishTemplate.mockRejectedValueOnce(new Error('连接中断')).mockResolvedValueOnce({})
  await click('发布已保存草稿'); await click('发布已保存草稿')
  expect(api.publishTemplate.mock.calls[1][2]).toBe(api.publishTemplate.mock.calls[0][2])
})
it('新建保存成功但详情刷新失败时保留服务端ID，重试不能再次创建', async () => {
  await wrapper.setData({ current: {}, draft: { ...version, name: '新模板', printItemType: 'STAFF_CARD' }, canvas, dirty: true })
  api.createTemplate.mockResolvedValue({ templateId: 'new-template', draftRevision: 1 })
  api.getTemplate.mockRejectedValue(new Error('详情读取失败'))
  await click('保存草稿')
  expect(wrapper.vm.current.templateId).toBe('new-template')
  api.saveDraft.mockResolvedValue({ draftRevision: 2 })
  await click('保存草稿')
  expect(api.createTemplate).toHaveBeenCalledTimes(1)
})
it('保存和发布等待期间冻结整个设计器', async () => {
  await click('编辑')
  for (const [method, label] of [['saveDraft', '保存草稿'], ['publishTemplate', '发布已保存草稿']]) {
    let finish
    api[method].mockImplementationOnce(() => new Promise(resolve => { finish = resolve }))
    await click(label)
    expect(wrapper.findComponent(Designer).props('disabled')).toBe(true)
    finish({ draftRevision: 3 })
    await settle()
    expect(wrapper.findComponent(Designer).props('disabled')).toBe(false)
  }
})
it('回滚丢回执重试保留原请求和原因，成功后释放幂等状态', async () => {
  const target = { templateVersionId: 'published-old', versionStatus: 'PUBLISHED', versionNo: 1 }
  await wrapper.setData({ versionDetail: { ...detail, currentPublishedVersionId: 'published-new', versions: [target] } })
  api.rollbackTemplate.mockRejectedValueOnce(new Error('连接中断')).mockResolvedValueOnce({})
  await click('回滚到此版本'); await click('回滚到此版本')
  expect(api.rollbackTemplate.mock.calls[1]).toEqual(api.rollbackTemplate.mock.calls[0])
  expect(wrapper.vm.$prompt).toHaveBeenCalledTimes(1)
  expect(wrapper.vm.pendingRollback).toBeNull()
})
const png = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/mWQAAAAASUVORK5CYII='
const pngBytes = Buffer.from(png, 'base64')
const imageEntry = { objectId: 'asset-a', contentHash: 'sha256:' + createHash('sha256').update(pngBytes).digest('hex'), mediaType: 'image/png', sizeBytes: pngBytes.length, parkId: 'park-a', purpose: 'BACKGROUND', accessScope: 'TEMPLATE' }
it('保存图片先授权上传，提交仅含引用，重新打开按授权字节恢复原图', async () => {
  await click('编辑')
  canvas.schemas = [[{ name: '背景', type: 'image', content: `data:image/png;base64,${png}`, position: { x: 0, y: 0 }, width: 20, height: 20 }]]
  api.uploadTemplateResource.mockResolvedValue(imageEntry)
  api.downloadTemplateResource.mockResolvedValue(new NodeBlob([pngBytes], { type: 'image/png' }))
  api.saveDraft.mockImplementation(async (id, body) => {
    api.getTemplate.mockResolvedValue({ ...detail, draftRevision: 3, draft: { ...body } })
    return { draftRevision: 3 }
  })
  await click('保存草稿')
  await vi.waitFor(() => expect(api.saveDraft).toHaveBeenCalledTimes(1))
  const saved = api.saveDraft.mock.calls[0][1]
  expect(JSON.stringify(saved)).not.toContain('data:image')
  expect(saved.layoutJson.schemas[0][0].resourceRef).toEqual({ objectId: 'asset-a', contentHash: imageEntry.contentHash })
  expect(saved.resourceManifest).toEqual([imageEntry])
  expect(api.uploadTemplateResource).toHaveBeenCalledWith(expect.objectContaining({ parkId: 'park-a', mediaType: 'image/png', purpose: 'BACKGROUND', bytes: expect.any(Uint8Array) }))
  await vi.waitFor(() => expect(wrapper.vm.busy).toBe(false))
  expect(wrapper.vm.canvas.schemas[0][0].content).toBe(`data:image/png;base64,${png}`)
  expect(api.downloadTemplateResource).toHaveBeenCalledWith({ objectId: 'asset-a', parkId: 'park-a' })
})
it('保存失败保留临时画布与成功上传引用，重试不重复上传图片', async () => {
  await click('编辑')
  canvas.schemas = [[{ name: '背景', type: 'image', content: `data:image/png;base64,${png}` }]]
  api.uploadTemplateResource.mockResolvedValue(imageEntry)
  api.saveDraft.mockRejectedValue(new Error('保存连接中断'))
  await click('保存草稿'); await vi.waitFor(() => expect(wrapper.vm.busy).toBe(false))
  await click('保存草稿'); await vi.waitFor(() => expect(wrapper.vm.busy).toBe(false))
  expect(api.uploadTemplateResource).toHaveBeenCalledTimes(1)
  expect(api.saveDraft).toHaveBeenCalledTimes(2)
  expect(api.saveDraft.mock.calls[1][1].resourceManifest).toEqual([imageEntry])
  expect(canvas.schemas[0][0].content).toBe(`data:image/png;base64,${png}`)
})
