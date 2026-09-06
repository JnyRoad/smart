import { printRequest } from './client'
/** 正反面关联由平台持久化，页面只保留本次表单。 */
export const listPairs = params => printRequest({ url: '/template-pairs', method: 'get', params })
export const getPair = (id, parkId) => printRequest({ url: `/template-pairs/${encodeURIComponent(id)}`, method: 'get', params: { parkId } })
export const savePair = (id, data, key) => printRequest({ url: `/template-pairs${id ? '/' + encodeURIComponent(id) : ''}`, method: id ? 'patch' : 'post', params: { parkId: data.parkId }, data, headers: { 'Idempotency-Key': key } })
export const archivePair = (id, data, key) => printRequest({ url: `/template-pairs/${encodeURIComponent(id)}/archive`, method: 'post', params: { parkId: data.parkId }, data, headers: { 'Idempotency-Key': key } })
export const previewPair = (id, data) => printRequest({ url: `/template-pairs/${encodeURIComponent(id)}/preview`, method: 'post', params: { parkId: data.parkId }, data })
