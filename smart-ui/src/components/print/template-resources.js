import { canvasFromVersion, draftFromCanvas } from './template-model'
import { PERSON_PHOTO_PLACEHOLDER, applyPersonPhotoBindings } from './person-photo'

const MAX_IMAGE_BYTES = 20 * 1024 * 1024
const MAX_FACE_BYTES = 32 * 1024 * 1024
const imageSchemas = canvas => canvas.schemas.flat().filter(schema => schema.type === 'image')
const cacheKey = (parkId, hash, purpose) => `${String(parkId)}:${purpose}:${hash}`

/** 只保留服务端模板资源字段；客户端缓存不能扩大园区或人员照片权限。 */
function checkedEntry(entry, parkId) {
  if (!entry || !/^[\w-]{1,128}$/.test(entry.objectId || '') || !/^sha256:[a-f0-9]{64}$/.test(entry.contentHash || '')) throw new Error('图片引用校验失败，请重新上传')
  if (String(entry.parkId) !== String(parkId)) throw new Error('图片不属于当前园区，请重新上传')
  if (!['BACKGROUND', 'LOGO'].includes(entry.purpose) || entry.accessScope !== 'TEMPLATE') throw new Error('图片用途不允许用于模板，人员照片需独立授权')
  if (!['image/png', 'image/jpeg'].includes(entry.mediaType) || !Number.isSafeInteger(entry.sizeBytes) || entry.sizeBytes <= 0 || entry.sizeBytes > MAX_IMAGE_BYTES) throw new Error('图片校验失败，仅支持不超过20 MiB的PNG/JPEG')
  const { objectId, contentHash, mediaType, sizeBytes, purpose, accessScope } = entry
  return { objectId, contentHash, mediaType, sizeBytes, parkId: entry.parkId, purpose, accessScope }
}

function referencedEntry(schema, manifest, parkId) {
  const ref = schema.resourceRef
  if (!ref || typeof ref !== 'object') throw new Error(`图片“${schema.name}”缺少已授权引用，请重新上传`)
  const matches = manifest.filter(item => item.objectId === ref.objectId)
  if (matches.length !== 1) throw new Error(`图片“${schema.name}”的资源清单校验失败`)
  const entry = checkedEntry(matches[0], parkId)
  if (ref.contentHash !== entry.contentHash) throw new Error(`图片“${schema.name}”的引用hash校验失败`)
  return entry
}

function assertFaceBudget(resources) {
  if (resources.size > 32 || [...resources.values()].reduce((total, item) => total + item.sizeBytes, 0) > MAX_FACE_BYTES) throw new Error('每面图片累计不能超过32 MiB，且最多32个资源')
}

function assertImageBytes(bytes, mediaType) {
  const png = [137, 80, 78, 71, 13, 10, 26, 10].every((byte, index) => bytes[index] === byte)
  const jpeg = bytes[0] === 255 && bytes[1] === 216 && bytes[2] === 255
  if (!bytes.length || bytes.length > MAX_IMAGE_BYTES || (mediaType === 'image/png' ? !png : !jpeg)) throw new Error('图片字节校验失败，仅支持不超过20 MiB的PNG/JPEG')
}

async function contentHash(bytes) {
  if (!window.crypto.subtle) throw new Error('当前页面无法校验图片，请使用 HTTPS 或本机地址打开系统')
  const digest = await window.crypto.subtle.digest('SHA-256', bytes)
  return 'sha256:' + Array.from(new Uint8Array(digest), byte => byte.toString(16).padStart(2, '0')).join('')
}

function decodeImage(content) {
  const match = typeof content === 'string' && /^data:(image\/(?:png|jpeg));base64,([A-Za-z0-9+/]+={0,2})$/.exec(content)
  if (!match || match[2].length > Math.ceil(MAX_IMAGE_BYTES / 3) * 4) throw new Error('仅支持不超过20 MiB的PNG/JPEG图片，请在画布中重新选择图片')
  let binary
  try { binary = atob(match[2]) } catch (_) { throw new Error('PNG/JPEG图片编码校验失败，请重新选择图片') }
  if (btoa(binary) !== match[2]) throw new Error('PNG/JPEG图片编码校验失败，请重新选择图片')
  const bytes = Uint8Array.from(binary, character => character.charCodeAt(0))
  assertImageBytes(bytes, match[1])
  return { bytes, mediaType: match[1] }
}

function asDataUri(bytes, mediaType) {
  const chunks = []
  for (let offset = 0; offset < bytes.length; offset += 32768) chunks.push(String.fromCharCode(...bytes.subarray(offset, offset + 32768)))
  return `data:${mediaType};base64,${btoa(chunks.join(''))}`
}

/** 保存先准备独立快照。成功上传保存在调用方会话缓存，后续图片或草稿失败不丢引用。 */
export async function prepareTemplateResources(draft, canvas, { parkId, uploadResource, cache = new Map() }) {
  const body = draftFromCanvas(draft, canvas)
  const manifest = Array.isArray(draft.resourceManifest) ? draft.resourceManifest : []
  const prepared = []
  const resources = new Map()
  const photos = applyPersonPhotoBindings(body.layoutJson, body.fieldSchemaJson, false)
  for (const schema of imageSchemas({ schemas: body.layoutJson.schemas })) {
    if (photos.has(schema.name)) continue
    if (schema.content === PERSON_PHOTO_PLACEHOLDER) throw new Error(`图片“${schema.name}”已解除人员照片绑定，请重新选择固定图片`)
    let entry = schema.resourceRef ? referencedEntry(schema, manifest, parkId) : null
    let content
    if (schema.content !== undefined) {
      content = decodeImage(schema.content)
      content.hash = await contentHash(content.bytes)
      if (entry && (entry.contentHash !== content.hash || entry.mediaType !== content.mediaType || entry.sizeBytes !== content.bytes.length)) {
        content.purpose = entry.purpose
        entry = null
      }
      // 复制组件可能不保留插件扩展字段，按相同园区、hash、媒体类型复用已授权清单。
      if (!entry) {
        const existing = manifest.find(item => item.contentHash === content.hash && item.mediaType === content.mediaType && item.sizeBytes === content.bytes.length && (!content.purpose || item.purpose === content.purpose))
        if (existing) entry = checkedEntry(existing, parkId)
      }
    }
    if (!entry && !content) throw new Error(`图片“${schema.name}”没有可上传内容`)
    const purpose = entry ? entry.purpose : content.purpose || 'BACKGROUND'
    const key = cacheKey(parkId, entry ? entry.contentHash : content.hash, purpose)
    if (!entry && cache.has(key)) {
      entry = checkedEntry(cache.get(key), parkId)
      if (entry.contentHash !== content.hash || entry.mediaType !== content.mediaType || entry.sizeBytes !== content.bytes.length) throw new Error('图片缓存校验失败，请重新打开模板')
    }
    resources.set(entry ? entry.objectId : key, { sizeBytes: entry ? entry.sizeBytes : content.bytes.length })
    prepared.push({ schema, entry, content, purpose, key })
  }
  assertFaceBudget(resources)
  const savedResources = new Map()
  for (const item of prepared) {
    let entry = item.entry || cache.get(item.key)
    if (!entry) {
      entry = checkedEntry(await uploadResource({ parkId, bytes: item.content.bytes, mediaType: item.content.mediaType, purpose: item.purpose }), parkId)
      if (entry.contentHash !== item.content.hash || entry.mediaType !== item.content.mediaType || entry.sizeBytes !== item.content.bytes.length || entry.purpose !== item.purpose) throw new Error('上传图片校验失败，请重试')
    }
    cache.set(item.key, entry)
    savedResources.set(entry.objectId, entry)
    item.schema.resourceRef = { objectId: entry.objectId, contentHash: entry.contentHash }
    item.schema.readOnly = true
    delete item.schema.content
  }
  // 删除组件只更新引用，不删除存储对象，也不携带未引用的人员照片或旧图片。
  body.resourceManifest = [...savedResources.values()]
  return body
}

/** 每次打开都重新通过受控接口授权读图，全部校验后才将临时字节交给画布。 */
export async function hydrateTemplateResources(version, { parkId, downloadResource, cache = new Map() }) {
  const canvas = canvasFromVersion(version)
  const manifest = Array.isArray(version.resourceManifest) ? version.resourceManifest : []
  const resources = new Map()
  const photos = applyPersonPhotoBindings(canvas, version.fieldSchemaJson || { fields: [] })
  const images = imageSchemas(canvas).filter(schema => !photos.has(schema.name))
  for (const schema of images) {
    const entry = referencedEntry(schema, manifest, parkId)
    resources.set(entry.objectId, entry)
  }
  assertFaceBudget(resources)
  const decoded = new Map()
  for (const entry of resources.values()) {
    const blob = await downloadResource({ objectId: entry.objectId, parkId })
    if (blob.type !== entry.mediaType || blob.size !== entry.sizeBytes) throw new Error('回读图片校验失败，请重新上传')
    const bytes = new Uint8Array(await blob.arrayBuffer())
    assertImageBytes(bytes, entry.mediaType)
    if (await contentHash(bytes) !== entry.contentHash) throw new Error('回读图片hash校验失败，请重新上传')
    decoded.set(entry.objectId, asDataUri(bytes, entry.mediaType))
    cache.set(cacheKey(parkId, entry.contentHash, entry.purpose), entry)
  }
  for (const schema of images) { schema.content = decoded.get(schema.resourceRef.objectId); schema.readOnly = true }
  return canvas
}
