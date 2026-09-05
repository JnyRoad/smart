// 旧 axios 拦截器可能 resolve 错误响应，打印页面必须再次识别失败，不能显示保存成功。
import { beforeEach, expect, it, vi } from 'vitest'
import request from '@/router/axios'
import { printRequest, newIdempotencyKey } from './client'
import * as templates from './templates'
vi.mock('@/router/axios', () => ({ default: vi.fn() }))
beforeEach(() => vi.resetAllMocks())
it('读取 201 成功响应并返回业务数据', async () => {
  request.mockResolvedValue({ status: 201, data: { code: 0, data: { templateId: 'new' } } })
  await expect(printRequest({ url: '/templates', method: 'post' })).resolves.toEqual({ templateId: 'new' })
})
it('识别服务端冲突并保留错误码和修订信息', async () => {
  request.mockResolvedValue({ status: 409, data: { error: { code: 'DRAFT_REVISION_CONFLICT', message: '草稿已变更', details: { currentDraftRevision: 4 } } } })
  await expect(printRequest({ url: '/templates/x' })).rejects.toMatchObject({ code: 'DRAFT_REVISION_CONFLICT', details: { currentDraftRevision: 4 } })
})
it('HTTP 200 的业务失败同样拒绝', async () => {
  request.mockResolvedValue({ status: 200, data: { code: 1, msg: '无操作权限' } })
  await expect(printRequest({ url: '/templates' })).rejects.toThrow('无操作权限')
})
it('每次新操作使用独立幂等键，重试由调用方复用原键', () => {
  const key = newIdempotencyKey()
  expect(key.length).toBeLessThanOrEqual(128)
  expect(newIdempotencyKey()).not.toBe(key)
})
it('模板图片上传传原始字节和用途，回读只构造当前园区授权对象路径', async () => {
  const bytes = new Uint8Array([137, 80, 78, 71])
  request.mockResolvedValueOnce({ status: 201, data: { code: 0, data: { objectId: 'asset-a' } } })
  await expect(templates.uploadTemplateResource({ bytes, mediaType: 'image/png', purpose: 'BACKGROUND', parkId: 'park-a' })).resolves.toEqual({ objectId: 'asset-a' })
  expect(request).toHaveBeenCalledWith(expect.objectContaining({ url: '/platform/print/v1/resources', method: 'post', params: { parkId: 'park-a' }, data: bytes, headers: { 'Content-Type': 'image/png', 'X-Print-Resource-Purpose': 'BACKGROUND' } }))
  const blob = new Blob([bytes], { type: 'image/png' })
  request.mockResolvedValueOnce({ status: 200, headers: { 'content-type': 'image/png' }, data: blob })
  await expect(templates.downloadTemplateResource({ objectId: 'asset/a', parkId: 'park-a' })).resolves.toBe(blob)
  expect(request).toHaveBeenLastCalledWith(expect.objectContaining({ url: '/platform/print/v1/resources/asset%2Fa', params: { parkId: 'park-a' }, responseType: 'blob', printDomain: true }))
})
