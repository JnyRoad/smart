import { printRequest } from './client'
import request from '@/router/axios'
const idPath = id => encodeURIComponent(id)
/** 模板服务只提交用户编辑意图；正式校验、版本及园区授权由后端负责。 */
export const listTemplates = params => printRequest({ url: '/templates', method: 'get', params })
export const getTemplate = (id, parkId) => printRequest({ url: `/templates/${idPath(id)}`, method: 'get', params: { parkId } })
export const createTemplate = data => printRequest({ url: '/templates', method: 'post', params: { parkId: data.parkId }, data })
export const saveDraft = (id, data) => printRequest({ url: `/templates/${idPath(id)}`, method: 'patch', params: { parkId: data.parkId }, data })
export const listVersions = (id, parkId) => printRequest({ url: `/templates/${idPath(id)}/versions`, method: 'get', params: { parkId } })
export const publishTemplate = (id, data, key) => printRequest({ url: `/templates/${idPath(id)}/publish`, method: 'post', params: { parkId: data.parkId }, data, headers: { 'Idempotency-Key': key } })
export const rollbackTemplate = (id, data, key) => printRequest({ url: `/templates/${idPath(id)}/rollback`, method: 'post', params: { parkId: data.parkId }, data, headers: { 'Idempotency-Key': key } })
export const previewTemplate = (id, data) => printRequest({ url: `/templates/${idPath(id)}/preview`, method: 'post', params: { parkId: data.parkId }, data })
export const getPreview = id => printRequest({ url: `/previews/${idPath(id)}`, method: 'get' })

/** 模板图片通过原始字节上传，服务端生成不可覆盖对象并核对园区及用途。 */
export const uploadTemplateResource = ({ bytes, mediaType, purpose, parkId }) => printRequest({ url: '/resources', method: 'post', params: { parkId }, data: bytes, headers: { 'Content-Type': mediaType, 'X-Print-Resource-Purpose': purpose } })

/** 每次打开模板重新认证读取对象，不跟随外部地址，字节hash由资源模型校验。 */
export async function downloadTemplateResource({ objectId, parkId }) {
  const response = await request({ url: `/platform/print/v1/resources/${idPath(objectId)}`, method: 'get', params: { parkId }, responseType: 'blob', printDomain: true })
  const mediaType = (response.headers['content-type'] || '').split(';')[0]
  if (response.status !== 200 || !['image/png', 'image/jpeg'].includes(mediaType)) throw new Error('模板图片读取失败，请检查权限或重新上传')
  return response.data
}

/** 下载路径由已授权预览ID构造，不跟随服务端传回的任意URL。 */
export async function downloadPreviewArtifact(previewId, artifactId) {
  const response = await request({ url: `/platform/print/v1/previews/${idPath(previewId)}/artifacts/${idPath(artifactId)}`, method: 'get', responseType: 'blob', printDomain: true })
  if (response.status !== 200 || !(response.headers['content-type'] || '').startsWith('application/pdf')) throw new Error('预览文件无法读取，请检查权限或重新生成')
  return response.data
}
