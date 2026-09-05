import { mount } from '@vue/test-utils'
import { expect, it, vi } from 'vitest'
import Preview from './PrintPreview.vue'
import * as api from '@/api/platform/print/templates'
vi.mock('@/api/platform/print/templates', () => ({ getPreview: vi.fn(), downloadPreviewArtifact: vi.fn() }))
it('失败预览显示诊断，不创建文件下载或打印任务', async () => {
  const wrapper = mount(Preview, { propsData: { initial: { previewId: 'preview-1', status: 'RENDER_FAILED', error: { message: '姓名组件超出版面' } } } })
  await new Promise(resolve => setTimeout(resolve, 0))
  expect(wrapper.text()).toContain('姓名组件超出版面')
  expect(api.downloadPreviewArtifact).not.toHaveBeenCalled()
  wrapper.destroy()
})
it('READY仍必须校验文件hash，篡改文件不能打开预览', async () => {
  api.downloadPreviewArtifact.mockResolvedValue(new Blob(['%PDF-wrong'], { type: 'application/pdf' }))
  const wrapper = mount(Preview, { propsData: { initial: { previewId: 'preview-2', status: 'READY', artifacts: [{ artifactId: 'a', face: 'FRONT', bytes: 10, sha256: 'sha256:bad' }] } } })
  await new Promise(resolve => setTimeout(resolve, 0))
  expect(wrapper.findAll('iframe')).toHaveLength(0)
  await vi.waitFor(() => expect(wrapper.find('[role="alert"]').exists()).toBe(true))
  wrapper.destroy()
})
it('服务端结构化失败定位到正背面组件，并以中文解释缺字和溢出', async () => {
  const wrapper = mount(Preview, { propsData: { initial: { previewId: 'preview-3', status: 'RENDER_FAILED', errorCode: 'TEMPLATE_VALIDATION_FAILED', violations: [{ face: 'BACK', schemaName: '部门说明', code: 'TEXT_OVERFLOW' }, { face: 'FRONT', schemaName: '姓名', code: 'GLYPH_MISSING' }] } } })
  await new Promise(resolve => setTimeout(resolve, 0))
  expect(wrapper.text()).toContain('背面 · 部门说明')
  expect(wrapper.text()).toContain('正面 · 姓名')
  expect(wrapper.text()).toContain('文字超出组件范围')
  expect(wrapper.text()).toContain('字体缺少所需字形')
  wrapper.destroy()
})
