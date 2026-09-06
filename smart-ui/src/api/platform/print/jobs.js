import { printRequest } from './client'
import request from '@/router/axios'
const idPath = value => encodeURIComponent(value)
export const searchSubjects = params => printRequest({ url: '/print-subjects', method: 'get', params })
export const listJobs = params => printRequest({ url: '/print-jobs', method: 'get', params })
export const getJob = id => printRequest({ url: `/print-jobs/${idPath(id)}`, method: 'get' })
export const getJobEvents = id => printRequest({ url: `/print-jobs/${idPath(id)}/events`, method: 'get' })
export const previewJob = data => printRequest({ url: '/print-jobs/preview', method: 'post', data })
export const getJobPreview = id => printRequest({ url: `/print-jobs/previews/${idPath(id)}`, method: 'get' })
export const createJob = (data, key) => printRequest({ url: '/print-jobs', method: 'post', data, headers: { 'Idempotency-Key': key } })
export const createJobBatch = (data, key) => printRequest({ url: '/print-jobs/batch', method: 'post', data, headers: { 'Idempotency-Key': key } })
export const flipJob = (id, data, key) => printRequest({ url: `/print-jobs/${idPath(id)}/flip-confirmation`, method: 'post', data, headers: { 'Idempotency-Key': key } })
export const checkJobOutput = (id, data, key) => printRequest({ url: `/print-jobs/${idPath(id)}/output-check`, method: 'post', data, headers: { 'Idempotency-Key': key } })
export const cancelJob = (id, data, key) => printRequest({ url: `/print-jobs/${idPath(id)}/cancel`, method: 'post', data, headers: { 'Idempotency-Key': key } })
/** 下载只使用已知接口和授权制品ID，不跟随响应中的外部URL。 */
export async function downloadJobPreview(previewId, artifactId) {
  const response = await request({ url: `/platform/print/v1/print-jobs/previews/${idPath(previewId)}/artifacts/${idPath(artifactId)}`, method: 'get', responseType: 'blob', printDomain: true })
  if (response.status !== 200 || (response.headers['content-type'] || '').split(';')[0] !== 'application/pdf') throw new Error('实际人员预览读取失败')
  return response.data
}

export const loadSubjectSelection = data => printRequest({ url: '/print-subjects/selection', method: 'post', data })

/** 任务恢复只下载冻结卡面，不重新生成或读取外部下载地址。 */
export async function downloadJobArtifact(jobId, face) {
  if (!['FRONT', 'BACK'].includes(face)) throw new Error('任务卡面无效')
  const response = await request({ url: `/platform/print/v1/print-jobs/${idPath(jobId)}/artifacts/${face}/download`, method: 'get', responseType: 'blob', printDomain: true })
  if (response.status !== 200 || (response.headers['content-type'] || '').split(';')[0] !== 'application/pdf') throw new Error('冻结卡面读取失败')
  return response.data
}
