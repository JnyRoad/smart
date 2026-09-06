import { afterEach, beforeEach, expect, it, vi } from 'vitest'
import { createHash, webcrypto } from 'node:crypto'
import { Blob as NodeBlob } from 'node:buffer'
import { newTemplateDraft, canvasFromVersion } from './template-model'
import { prepareTemplateResources, hydrateTemplateResources } from './template-resources'

const png = 'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR42mP8/x8AAwMCAO+/mWQAAAAASUVORK5CYII='
const dataUri = `data:image/png;base64,${png}`
const bytes = Buffer.from(png, 'base64')
const hash = value => 'sha256:' + createHash('sha256').update(value).digest('hex')
const entry = { objectId: 'asset-a', contentHash: hash(bytes), sizeBytes: bytes.length, mediaType: 'image/png', parkId: 'park-a', purpose: 'BACKGROUND', accessScope: 'TEMPLATE' }
const stored = () => {
  const draft = newTemplateDraft('STAFF_CARD', 'EMPLOYEE', 'FRONT')
  draft.resourceManifest = [entry]
  draft.layoutJson.schemas = [[{ name: '背景', type: 'image', resourceRef: { objectId: entry.objectId, contentHash: entry.contentHash } }]]
  return draft
}
beforeEach(() => vi.stubGlobal('crypto', webcrypto))
afterEach(() => vi.unstubAllGlobals())
it('人员照片保存仅保留必填绑定，重开使用合成占位而不读取或上传具体照片', async () => {
  const draft = stored()
  draft.fieldSchemaJson.fields = [{ key: 'personPhoto', schemaName: '背景', required: true }]
  const canvas = canvasFromVersion(draft)
  canvas.schemas[0][0].content = dataUri
  const uploadResource = vi.fn(); const downloadResource = vi.fn()
  const saved = await prepareTemplateResources(draft, canvas, { parkId: 'park-a', uploadResource })
  expect(saved.resourceManifest).toEqual([])
  expect(saved.layoutJson.schemas[0][0].resourceRef).toBeUndefined()
  expect(saved.layoutJson.schemas[0][0].content).toBeUndefined()
  expect(saved.fieldSchemaJson).toEqual(draft.fieldSchemaJson)
  const opened = await hydrateTemplateResources(saved, { parkId: 'park-a', downloadResource })
  expect(opened.schemas[0][0].content).toMatch(/^data:image\/png;base64,/)
  expect(opened.schemas[0][0].content).not.toBe(dataUri)
  expect(opened.schemas[0][0].readOnly).toBe(true)
  expect(downloadResource).not.toHaveBeenCalled(); expect(uploadResource).not.toHaveBeenCalled()
  saved.fieldSchemaJson.fields = []
  await expect(prepareTemplateResources(saved, opened, { parkId: 'park-a', uploadResource })).rejects.toThrow('重新选择')
})
it('照片不允许绑定文字，照片绑定不允许可选', () => {
  const draft = stored(); draft.layoutJson.schemas[0][0].type = 'text'
  draft.fieldSchemaJson.fields = [{ key: 'personPhoto', schemaName: '背景', required: true }]
  expect(() => canvasFromVersion(draft)).toThrow('照片')
  draft.layoutJson.schemas[0][0].type = 'image'; draft.fieldSchemaJson.fields[0].required = false
  expect(() => canvasFromVersion(draft)).toThrow('必填')
})
it('未修改图片复用原引用，删除组件只去除清单引用', async () => {
  const draft = stored()
  const canvas = canvasFromVersion(draft)
  canvas.schemas[0][0].content = dataUri
  const uploadResource = vi.fn()
  const saved = await prepareTemplateResources(draft, canvas, { parkId: 'park-a', uploadResource, cache: new Map() })
  expect(saved.resourceManifest).toEqual([entry])
  expect(saved.layoutJson.schemas[0][0].content).toBeUndefined()
  expect(uploadResource).not.toHaveBeenCalled()
  const removed = await prepareTemplateResources(draft, { ...canvas, schemas: [[]] }, { parkId: 'park-a', uploadResource, cache: new Map() })
  expect(removed.resourceManifest).toEqual([])
})
it('替换图片必须按新字节hash上传，不沿用旧引用', async () => {
  const draft = stored()
  const canvas = canvasFromVersion(draft)
  const jpeg = Buffer.from([255, 216, 255, 217])
  canvas.schemas[0][0].content = 'data:image/jpeg;base64,' + jpeg.toString('base64')
  const next = { ...entry, objectId: 'asset-b', contentHash: hash(jpeg), sizeBytes: jpeg.length, mediaType: 'image/jpeg' }
  const uploadResource = vi.fn().mockResolvedValue(next)
  const saved = await prepareTemplateResources(draft, canvas, { parkId: 'park-a', uploadResource, cache: new Map() })
  expect(saved.layoutJson.schemas[0][0].resourceRef).toEqual({ objectId: 'asset-b', contentHash: next.contentHash })
  expect(saved.resourceManifest).toEqual([next])
  expect(uploadResource).toHaveBeenCalledTimes(1)
})
it('图片回读hash、园区或人员照片用途不符合模板授权时阻断画布打开', async () => {
  const downloadResource = vi.fn().mockResolvedValue(new NodeBlob([Buffer.from([137, 80, 78, 71, 13, 10, 26, 10])], { type: 'image/png' }))
  await expect(hydrateTemplateResources(stored(), { parkId: 'park-a', downloadResource })).rejects.toThrow('校验')
  downloadResource.mockClear()
  await expect(hydrateTemplateResources(stored(), { parkId: 'park-other', downloadResource })).rejects.toThrow('园区')
  const photo = stored(); photo.resourceManifest = [{ ...entry, purpose: 'PHOTO' }]
  await expect(hydrateTemplateResources(photo, { parkId: 'park-a', downloadResource })).rejects.toThrow('用途')
  expect(downloadResource).not.toHaveBeenCalled()
})
it('每面累计图片超过32MiB时在读取或上传前拒绝', async () => {
  const draft = stored()
  draft.resourceManifest = ['asset-a', 'asset-b'].map(objectId => ({ ...entry, objectId, sizeBytes: 17 * 1024 * 1024 }))
  draft.layoutJson.schemas = [[...draft.resourceManifest.map(item => ({ name: item.objectId, type: 'image', resourceRef: { objectId: item.objectId, contentHash: item.contentHash } }))]]
  const downloadResource = vi.fn(); const uploadResource = vi.fn()
  await expect(hydrateTemplateResources(draft, { parkId: 'park-a', downloadResource })).rejects.toThrow('32 MiB')
  await expect(prepareTemplateResources(draft, canvasFromVersion(draft), { parkId: 'park-a', uploadResource, cache: new Map() })).rejects.toThrow('32 MiB')
  expect(downloadResource).not.toHaveBeenCalled(); expect(uploadResource).not.toHaveBeenCalled()
})
it('拒绝远程图片和上传响应中的错误hash，保留原画布供修复', async () => {
  const draft = stored(); const canvas = canvasFromVersion(draft)
  canvas.schemas[0][0].content = 'https://untrusted.invalid/image.png'
  await expect(prepareTemplateResources(draft, canvas, { parkId: 'park-a', uploadResource: vi.fn() })).rejects.toThrow('PNG/JPEG')
  canvas.schemas[0][0].content = dataUri; delete canvas.schemas[0][0].resourceRef; draft.resourceManifest = []
  await expect(prepareTemplateResources(draft, canvas, { parkId: 'park-a', uploadResource: vi.fn().mockResolvedValue({ ...entry, contentHash: 'sha256:' + '0'.repeat(64) }) })).rejects.toThrow('校验')
  expect(canvas.schemas[0][0].content).toBe(dataUri)
})
