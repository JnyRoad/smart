import { mount } from '@vue/test-utils'
import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import Page from './Workbench.vue'
import * as jobs from '@/api/platform/print/jobs'
import { listPrinterOptions } from '@/api/platform/print/printers'
import { listPairs } from '@/api/platform/print/pairs'
import { listTemplates, listVersions } from '@/api/platform/print/templates'
vi.mock('@/router/axios', () => ({ default: vi.fn() }))
vi.mock('@/api/platform/print/jobs', () => ({ searchSubjects: vi.fn(), loadSubjectSelection: vi.fn(), listJobs: vi.fn(), createJob: vi.fn(), createJobBatch: vi.fn(), previewJob: vi.fn(), getJobPreview: vi.fn(), downloadJobPreview: vi.fn() }))
vi.mock('@/api/platform/print/printers', () => ({ listPrinterOptions: vi.fn() }))
vi.mock('@/api/platform/print/pairs', () => ({ listPairs: vi.fn() }))
vi.mock('@/api/platform/print/templates', () => ({ listTemplates: vi.fn(), listVersions: vi.fn(), getPreview: vi.fn(), downloadPreviewArtifact: vi.fn() }))
let wrapper
const settle = () => new Promise(resolve => setTimeout(resolve, 0))
const Preview = { name: 'PrintPreview', props: ['initial'], template: '<div><button @click="$emit(\'verified\', initial.previewId)">测试制品通过</button></div>' }
beforeEach(async () => {
  vi.resetAllMocks()
  listPrinterOptions.mockResolvedValue({ records: [{ printerProfileId: 'p1', displayName: '制卡台', status: 'ENABLED', deviceType: 'CARD_PRINTER', allowedPrintModes: ['MANUAL_DUPLEX'], defaultPrintMode: 'MANUAL_DUPLEX' }], total: 1 })
  listPairs.mockResolvedValue({ records: [], total: 0 })
  listTemplates.mockResolvedValue({ records: ['FRONT', 'BACK'].map(face => ({ templateId: face, name: face, printItemType: 'STAFF_CARD', faceRole: face })), total: 2 })
  listVersions.mockImplementation(id => Promise.resolve({ records: [{ templateVersionId: id + '-v1', versionStatus: 'PUBLISHED', versionNo: 1 }] }))
  jobs.listJobs.mockResolvedValue({ records: [], total: 0 })
  jobs.searchSubjects.mockResolvedValue({ records: [{ subjectId: 's1', subjectType: 'STAFF', displayName: '测试员工', employeeGradeName: '员工级' }], total: 1 })
  jobs.previewJob.mockResolvedValue({ previewId: 'preview1', status: 'READY', resolution: { automaticResolution: { status: 'NOT_FOUND' } }, artifacts: [] })
  jobs.createJob.mockResolvedValue({ jobId: 'job1' })
  wrapper = mount(Page, { propsData: { parkId: '1' }, stubs: { PrintPreview: Preview, JobHistory: true } })
  await settle(); await wrapper.vm.search(); await wrapper.vm.toggleSubject(wrapper.vm.subjects[0], true)
  await wrapper.setData({ printerId: 'p1', printMode: 'MANUAL_DUPLEX', selectionKind: 'EXPLICIT', frontId: 'FRONT-v1', backId: 'BACK-v1' }); await settle()
})
afterEach(() => wrapper.destroy())
it('未关联模板可手选，但真实人员预览和人工确认之前不能创建任务', async () => {
  await wrapper.vm.submit(); expect(jobs.createJob).not.toHaveBeenCalled()
  await wrapper.vm.previewSubject(wrapper.vm.selected[0]); await settle()
  expect(jobs.previewJob).toHaveBeenCalledWith(expect.objectContaining({ subjectId: 's1', selection: { kind: 'EXPLICIT', frontTemplateVersionId: 'FRONT-v1', backTemplateVersionId: 'BACK-v1', manualSelectionConfirmed: false } }))
  await wrapper.vm.submit(); expect(jobs.createJob).not.toHaveBeenCalled()
  wrapper.findComponent({ name: 'PrintPreview' }).vm.$emit('verified', 'preview1'); await settle()
  await wrapper.find('[aria-label="确认 测试员工 的模板与资料"]').setChecked(true)
  await wrapper.vm.submit()
  expect(jobs.createJob).toHaveBeenCalledWith(expect.objectContaining({ subjectId: 's1', previewId: 'preview1', selection: expect.objectContaining({ kind: 'EXPLICIT', manualSelectionConfirmed: true }) }), expect.any(String))
  expect(jobs.createJob.mock.calls[0][0]).not.toHaveProperty('fields')
})
it('人员或模板变化会撤销预览确认，迟到预览结果不能确认新选择', async () => {
  let finish; jobs.previewJob.mockReturnValue(new Promise(resolve => { finish = resolve }))
  const pending = wrapper.vm.previewSubject(wrapper.vm.selected[0])
  await wrapper.setData({ frontId: 'another-v2' }); await settle()
  finish({ previewId: 'old', status: 'READY' }); await pending
  expect(wrapper.vm.activePreview).toBeNull()
  expect(wrapper.vm.selected[0].verified).toBe(false)
  await wrapper.vm.submit(); expect(jobs.createJob).not.toHaveBeenCalled()
})
it('访客强制单面，手选请求不发送背面或厂牌组合', async () => {
  await wrapper.setProps({ visitor: true }); await settle()
  await wrapper.setData({ selected: [{ subjectId: 'v1', subjectType: 'ADMITTANCE', displayName: '测试访客', verified: true, confirmed: true }], selectionKind: 'EXPLICIT', frontId: 'FRONT-v1', backId: 'BACK-v1', printerId: 'p1' })
  const request = wrapper.vm.requestFor(wrapper.vm.selected[0], true)
  expect(request.printMode).toBe('SINGLE'); expect(request.selection).not.toHaveProperty('backTemplateVersionId'); expect(request.selection).not.toHaveProperty('pairId')
})
it('提交连接失败保留人员和确认，同内容重试使用同一幂等键', async () => {
  await wrapper.setData({ selected: [{ subjectId: 's1', subjectType: 'STAFF', displayName: '测试员工', verified: true, confirmed: true, previewId: 'preview1' }] })
  jobs.createJob.mockRejectedValueOnce(new Error('连接断开')).mockResolvedValueOnce({ jobId: 'j1' })
  await wrapper.vm.submit(); expect(wrapper.vm.selected).toHaveLength(1)
  await wrapper.vm.submit(); expect(jobs.createJob.mock.calls[1]).toEqual(jobs.createJob.mock.calls[0])
})
it('打印操作员只读取公开设备候选，并排除已归档模板', async () => {
  listTemplates.mockResolvedValue({ records: [{ templateId: 'archived', lifecycleStatus: 'ARCHIVED' }], total: 1 })
  listVersions.mockClear(); await wrapper.vm.loadOptions()
  expect(listVersions).not.toHaveBeenCalled()
  expect(wrapper.vm.printers[0]).not.toHaveProperty('deviceIdentity')
})
it('从旧访客页交接的ID必须重新读取受权资料，不能沿用路由内人员内容', async () => {
  jobs.loadSubjectSelection.mockResolvedValue({ records: [{ subjectId: '21', subjectType: 'ADMITTANCE', displayName: '当前访客' }] })
  await wrapper.setProps({ visitor: true }); await settle()
  wrapper.vm.$route = { query: { parkId: '1', subjects: JSON.stringify([{ subjectId: '21', subjectType: 'ADMITTANCE' }]) } }
  await wrapper.vm.loadIncomingSelection()
  expect(jobs.loadSubjectSelection).toHaveBeenCalledWith({ parkId: '1', subjects: [{ subjectId: '21', subjectType: 'ADMITTANCE' }] })
  expect(wrapper.vm.selected[0].displayName).toBe('当前访客'); expect(wrapper.vm.selected[0].confirmed).toBe(false)
})
it('已核对的预览ID随提交逐人发送，批量不能复用第一人的确认', async () => {
  jobs.createJobBatch.mockResolvedValue({ jobs: [] })
  await wrapper.setData({ selected: [
    { subjectId: 's1', subjectType: 'STAFF', displayName: '甲', verified: true, confirmed: true, previewId: 'preview-a' },
    { subjectId: 's2', subjectType: 'STAFF', displayName: '乙', verified: true, confirmed: true, previewId: 'preview-b' }
  ] })
  await wrapper.vm.submit()
  expect(jobs.createJobBatch).toHaveBeenCalledWith(expect.objectContaining({ subjects: [
    { subjectId: 's1', subjectType: 'STAFF', previewId: 'preview-a' }, { subjectId: 's2', subjectType: 'STAFF', previewId: 'preview-b' }
  ] }), expect.any(String))
  expect(jobs.createJobBatch.mock.calls[0][0]).not.toHaveProperty('previewId')
})
it('服务端提示预览已变化时清空旧确认，要求重新核对实际卡面', async () => {
  await wrapper.setData({ selected: [{ subjectId: 's1', subjectType: 'STAFF', displayName: '甲', verified: true, confirmed: true, previewId: 'old' }] })
  jobs.createJob.mockRejectedValue(Object.assign(new Error('预览后内容已变化'), { code: 'PRINT_PREVIEW_STALE' }))
  await wrapper.vm.submit(); expect(wrapper.vm.selected[0].confirmed).toBe(false); expect(wrapper.vm.selected[0].verified).toBe(false)
  expect(wrapper.vm.canSubmit).toBe(false)
})
