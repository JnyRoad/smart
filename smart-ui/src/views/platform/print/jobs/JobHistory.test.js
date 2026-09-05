import { mount } from '@vue/test-utils'
import { expect, it, vi } from 'vitest'
import Page from './JobHistory.vue'
import * as api from '@/api/platform/print/jobs'
vi.mock('@/router/axios', () => ({ default: vi.fn() }))
vi.mock('@/api/platform/print/jobs', () => ({ listJobs: vi.fn(async () => ({ records: [], total: 0 })), getJob: vi.fn(), getJobEvents: vi.fn(async () => []), flipJob: vi.fn(async () => ({})), cancelJob: vi.fn(), checkJobOutput: vi.fn(), downloadJobArtifact: vi.fn() }))
it('手动翻面需要当前正面实物方向确认，重复点击只提交一项原命令', async () => {
  const wrapper = mount(Page, { propsData: { parkId: '1' } })
  const job = { jobId: 'j1', status: 'AWAITING_FLIP', currentAttemptId: 'a1', attempts: [{ attemptId: 'a1', face: 'FRONT' }] }
  await wrapper.setData({ detail: job })
  await wrapper.vm.flip(); expect(api.flipJob).not.toHaveBeenCalled()
  await wrapper.setData({ orientationConfirmed: true }); api.getJob.mockResolvedValue({ ...job, status: 'BACK_IN_PROGRESS' })
  await wrapper.vm.flip(); expect(api.flipJob).toHaveBeenCalledWith('j1', { attemptId: 'a1', orientationConfirmed: true }, expect.any(String)); wrapper.destroy()
})
it('结果不明不能默认出卡成功，必须明确选择实物结果并填写说明', async () => {
  const wrapper = mount(Page, { propsData: { parkId: '1' } })
  await wrapper.setData({ detail: { jobId: 'j1', status: 'RESULT_UNKNOWN', currentAttemptId: 'a1', attempts: [{ attemptId: 'a1', face: 'FRONT' }] } })
  await wrapper.vm.output(); expect(api.checkJobOutput).not.toHaveBeenCalled()
  await wrapper.setData({ decision: 'CONFIRMED_NOT_OUT', resolution: 'CANCEL', physicalState: 'NO_CARD_IN_DEVICE', note: '已核对原卡槽', reason: '停止本次打印' })
  api.checkJobOutput.mockResolvedValue({ operatorCheckId: 'check1' }); api.getJob.mockResolvedValue({ jobId: 'j1', status: 'CANCELLED', operatorCheckId: 'check1' })
  await wrapper.vm.output(); expect(api.checkJobOutput).toHaveBeenCalledWith('j1', expect.objectContaining({ attemptId: 'a1', face: 'FRONT', physicalCheck: { state: 'NO_CARD_IN_DEVICE', operatorNote: '已核对原卡槽', sameCardFaceVerified: false }, decision: 'CONFIRMED_NOT_OUT', resolution: 'CANCEL' }), expect.any(String)); wrapper.destroy()
})
it('已完成任务只能补录无卡检查，不能改变业务输出结果', async () => {
  api.checkJobOutput.mockClear()
  const wrapper = mount(Page, { propsData: { parkId: '1' } })
  const detail = { jobId: 'j2', status: 'COMPLETED', currentAttemptId: 'a2', attempts: [{ attemptId: 'a2', face: 'BACK' }] }
  await wrapper.setData({ detail, decision: 'CONFIRMED_NOT_OUT', physicalState: 'NO_CARD_IN_DEVICE', note: '检查原设备无卡' })
  await wrapper.vm.output(); expect(api.checkJobOutput).not.toHaveBeenCalled()
  await wrapper.setData({ decision: 'DEVICE_CLEARANCE', physicalState: 'CARD_IN_DEVICE' }); await wrapper.vm.output(); expect(api.checkJobOutput).not.toHaveBeenCalled()
  await wrapper.setData({ physicalState: 'NO_CARD_IN_DEVICE' }); api.getJob.mockResolvedValue({ ...detail, operatorCheckId: 'check2' })
  await wrapper.vm.output(); expect(api.checkJobOutput).toHaveBeenCalledWith('j2', expect.objectContaining({ decision: 'DEVICE_CLEARANCE', resolution: 'NONE', attemptId: 'a2', face: 'BACK' }), expect.any(String)); wrapper.destroy()
})
it('访客记录不展示厂牌翻面状态选项', async () => {
  const wrapper = mount(Page, { propsData: { parkId: '1', printItemType: 'VISITOR_SLIP' } })
  expect(wrapper.find('select').text()).not.toContain('待手动翻面'); expect(wrapper.find('select').text()).not.toContain('背面正在处理'); wrapper.destroy()
})

it('刷新恢复使用冻结姓名、两面制品和本任务设备取放卡说明', async () => {
  const wrapper = mount(Page, { propsData: { parkId: '1' }, stubs: { PrintPreview: { name: 'PrintPreview', props: ['initial', 'expectedFaces', 'loadArtifact'], template: '<div data-test="frozen-preview">冻结卡面</div>' } } })
  const artifacts = [{ artifactId: 'front1', face: 'FRONT' }, { artifactId: 'back1', face: 'BACK' }]
  api.getJob.mockResolvedValue({ jobId: 'j1', subjectId: '123', printMode: 'MANUAL_DUPLEX', status: 'AWAITING_FLIP', artifacts, subjectSummary: { displayName: '合成员工甲', staffNo: 'TEST001' }, printerSummary: { displayName: '测试制卡台', model: 'CS220', calibration: { frontFeedInstruction: '正面照片朝上，短边向内', backFeedInstruction: '沿短边翻面，背面朝上', frontRotation: 0, backRotation: 180 } } })
  await wrapper.vm.open('j1')
  expect(wrapper.text()).toContain('合成员工甲'); expect(wrapper.text()).toContain('TEST001'); expect(wrapper.text()).toContain('沿短边翻面，背面朝上')
  const preview = wrapper.findComponent({ name: 'PrintPreview' })
  expect(preview.exists()).toBe(true)
  expect(preview.props('initial')).toEqual({ previewId: 'j1', status: 'READY', artifacts })
  expect(preview.props('expectedFaces')).toEqual(['FRONT', 'BACK'])
  await preview.props('loadArtifact')('j1', 'back1'); expect(api.downloadJobArtifact).toHaveBeenCalledWith('j1', 'BACK')
  await expect(preview.props('loadArtifact')('j2', 'back1')).rejects.toThrow()
  wrapper.destroy()
})
it('没有冻结方向说明时明确提示未配置，不编造方向', async () => {
  const wrapper = mount(Page, { propsData: { parkId: '1' } })
  await wrapper.setData({ detail: { jobId: 'j1', printMode: 'MANUAL_DUPLEX', status: 'AWAITING_FLIP' } })
  expect(wrapper.text()).toContain('未配置取放卡说明'); expect(wrapper.text()).not.toContain('短边向内'); wrapper.destroy()
})
it('先打开任务的迟到响应不能覆盖最后选择的任务', async () => {
  const wrapper = mount(Page, { propsData: { parkId: '1' } })
  let first; api.getJob.mockImplementation(id => id === 'a' ? new Promise(resolve => { first = resolve }) : Promise.resolve({ jobId: 'b' }))
  const a = wrapper.vm.open('a'); await wrapper.vm.open('b'); first({ jobId: 'a' }); await a
  expect(wrapper.vm.detail.jobId).toBe('b'); wrapper.destroy()
})
it('记录筛选的迟到响应不能覆盖最新筛选', async () => {
  const wrapper = mount(Page, { propsData: { parkId: '1' } }); await Promise.resolve()
  let first; api.listJobs.mockImplementationOnce(() => new Promise(resolve => { first = resolve })).mockResolvedValueOnce({ records: [{ jobId: 'b' }], total: 1 })
  const a = wrapper.vm.refresh(); await wrapper.vm.refresh(); first({ records: [{ jobId: 'a' }], total: 1 }); await a
  expect(wrapper.vm.records[0].jobId).toBe('b'); wrapper.destroy()
})

it('同一任务刷新会重新挂载冻结预览，允许下载失败后重试', async () => {
  const mounted = vi.fn()
  const wrapper = mount(Page, { propsData: { parkId: '1' }, stubs: { PrintPreview: { name: 'PrintPreview', mounted, template: '<div />' } } })
  api.getJob.mockResolvedValue({ jobId: 'j1', printMode: 'SINGLE', artifacts: [{ artifactId: 'front', face: 'FRONT' }] })
  await wrapper.vm.open('j1'); await wrapper.vm.$nextTick()
  await wrapper.vm.open('j1'); await wrapper.vm.$nextTick()
  expect(mounted).toHaveBeenCalledTimes(2); wrapper.destroy()
})
