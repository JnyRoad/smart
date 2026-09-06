import { mount } from '@vue/test-utils'
import { afterEach, expect, it, vi } from 'vitest'
import Preview from './PrintPreview.vue'
import { verifyPreviewArtifact } from './preview-artifact'
vi.mock('@/api/platform/print/templates', () => ({ getPreview: vi.fn(), downloadPreviewArtifact: vi.fn() }))
vi.mock('./preview-artifact', () => ({ verifyPreviewArtifact: vi.fn(blob => Promise.resolve(blob)) }))
afterEach(() => vi.restoreAllMocks())
it('业务双面预览必须正反面完整且全部校验后才允许人工确认', async () => {
  URL.createObjectURL = vi.fn(() => 'blob:preview'); URL.revokeObjectURL = vi.fn()
  let back; const loader = vi.fn((id, artifact) => artifact === 'back' ? new Promise(resolve => { back = resolve }) : Promise.resolve(new Blob(['front'])))
  const wrapper = mount(Preview, { propsData: { business: true, expectedFaces: ['FRONT', 'BACK'], loadArtifact: loader, initial: { previewId: 'p', status: 'READY', artifacts: [{ artifactId: 'front', face: 'FRONT' }, { artifactId: 'back', face: 'BACK' }] } } })
  await vi.waitFor(() => expect(loader).toHaveBeenCalledTimes(2))
  expect(wrapper.emitted('verified')).toBeUndefined(); back(new Blob(['back']))
  await vi.waitFor(() => expect(wrapper.emitted('verified')).toEqual([['p']]))
  expect(verifyPreviewArtifact).toHaveBeenCalledTimes(2); expect(wrapper.text()).toContain('实际人员'); wrapper.destroy()
})
it('缺失或重复卡面不能确认，也不能通过合成预览接口替代业务预览', async () => {
  const loader = vi.fn()
  const wrapper = mount(Preview, { propsData: { business: true, expectedFaces: ['FRONT', 'BACK'], loadArtifact: loader, initial: { previewId: 'p', status: 'READY', artifacts: [{ artifactId: 'front', face: 'FRONT' }] } } })
  await vi.waitFor(() => expect(wrapper.text()).toContain('预览卡面不完整'))
  expect(loader).not.toHaveBeenCalled(); expect(wrapper.emitted('verified')).toBeUndefined(); wrapper.destroy()
})
