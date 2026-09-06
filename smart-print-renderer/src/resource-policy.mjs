import { createHash } from 'node:crypto';
import { inflateSync } from 'node:zlib';
import { PNG } from 'pngjs';
import jpeg from 'jpeg-js';
const hash = bytes => 'sha256:' + createHash('sha256').update(bytes).digest('hex');
const fail = code => { const error = new Error(code); error.code = 'RENDER_VALIDATION_FAILED'; error.details = [{ code }]; throw error; };
const dimensions = (width, height) => {
  if (!Number.isInteger(width) || !Number.isInteger(height) || width <= 0 || height <= 0 || width > 4096 || height > 4096 || width * height > 16000000) fail('RESOURCE_IMAGE_INVALID');
};

/** 先核尺寸再解码像素，解码器始终受字节、像素及内存限制；不接收路径或网络来源。 */
function validateImage(bytes, mediaType) {
  try {
    if (mediaType === 'image/png') {
      if (bytes.length < 33 || !bytes.subarray(0, 8).equals(Buffer.from([137, 80, 78, 71, 13, 10, 26, 10])) || bytes.readUInt32BE(8) !== 13 || bytes.toString('ascii', 12, 16) !== 'IHDR') fail('RESOURCE_IMAGE_INVALID');
      const width = bytes.readUInt32BE(16), height = bytes.readUInt32BE(20);
      dimensions(width, height);
      // pngjs 的隔行路径没有 inflate 上限，先以最坏每像素8字节的预算核解压长度。
      const compressed = [];
      let headers = 0, ended = false;
      for (let offset = 8; offset < bytes.length;) {
        if (offset + 12 > bytes.length) fail('RESOURCE_IMAGE_INVALID');
        const length = bytes.readUInt32BE(offset), end = offset + 12 + length;
        if (end > bytes.length) fail('RESOURCE_IMAGE_INVALID');
        const type = bytes.toString('ascii', offset + 4, offset + 8);
        if (type === 'IHDR' && ++headers !== 1) fail('RESOURCE_IMAGE_INVALID');
        if (type === 'IDAT') compressed.push(bytes.subarray(offset + 8, end - 4));
        if (type === 'IEND') { if (end !== bytes.length || length !== 0) fail('RESOURCE_IMAGE_INVALID'); ended = true; }
        offset = end;
      }
      if (!ended || !compressed.length) fail('RESOURCE_IMAGE_INVALID');
      if (bytes[28] === 1) inflateSync(Buffer.concat(compressed), { maxOutputLength: width * height * 8 + height * 8 + 128 });
      const image = PNG.sync.read(bytes, { checkCRC: true });
      if (image.width !== width || image.height !== height || image.data.length !== width * height * 4) fail('RESOURCE_IMAGE_INVALID');
    } else {
      if (bytes.length < 4 || bytes[0] !== 255 || bytes[1] !== 216 || bytes.at(-2) !== 255 || bytes.at(-1) !== 217) fail('RESOURCE_IMAGE_INVALID');
      let found = false;
      for (let offset = 2; offset < bytes.length - 2;) {
        if (bytes[offset++] !== 255) fail('RESOURCE_IMAGE_INVALID');
        while (bytes[offset] === 255) offset++;
        const marker = bytes[offset++];
        if (marker === 0xda || marker === 0xd9) break;
        if (offset + 2 > bytes.length) fail('RESOURCE_IMAGE_INVALID');
        const length = bytes.readUInt16BE(offset);
        if (length < 2 || offset + length > bytes.length) fail('RESOURCE_IMAGE_INVALID');
        if ([0xc0, 0xc1, 0xc2].includes(marker)) {
          if (found || length < 8) fail('RESOURCE_IMAGE_INVALID');
          dimensions(bytes.readUInt16BE(offset + 5), bytes.readUInt16BE(offset + 3)); found = true;
        }
        offset += length;
      }
      if (!found) fail('RESOURCE_IMAGE_INVALID');
      const image = jpeg.decode(bytes, { useTArray: true, tolerantDecoding: false, maxResolutionInMP: 16, maxMemoryUsageInMB: 256 });
      dimensions(image.width, image.height);
      if (image.data.length !== image.width * image.height * 4) fail('RESOURCE_IMAGE_INVALID');
    }
  } catch (_) { fail('RESOURCE_IMAGE_INVALID'); }
}

/** 平台完成业务授权后传入资源字节；按模板绑定、人员归属和hash复核，仅消费当前冻结资源。 */
export function authorizeFaceResources(source, { allowSynthetic = false } = {}) {
  const manifest = source.resourceManifest;
  if (!Array.isArray(manifest) || manifest.length > 32) fail('RESOURCE_MANIFEST_INVALID');
  const template = structuredClone(source.template);
  if (typeof template?.basePdf !== 'object' || template.basePdf === null || template.basePdfRef) fail('RESOURCE_REFERENCE_INVALID');
  const schemas = [...(template.schemas || []).flat(), ...(template.basePdf.staticSchema || [])];
  const mappings = template.fieldSchemaJson?.fields || [];
  if (!Array.isArray(mappings)) fail('FIELD_SCHEMA_INVALID');
  const photoNames = new Set();
  const boundNames = new Set();
  for (const mapping of mappings) {
    if (!mapping || boundNames.has(mapping.schemaName)) fail('FIELD_SCHEMA_INVALID');
    boundNames.add(mapping.schemaName);
    const component = (template.schemas?.[0] || []).find(schema => schema.name === mapping.schemaName);
    if (mapping.key === 'personPhoto') {
      if (!component || component.type !== 'image' || mapping.required !== true || component.resourceRef !== undefined || (component.content !== undefined && component.content !== '')) fail('RESOURCE_BINDING_INVALID');
      photoNames.add(mapping.schemaName);
    } else if (component?.type === 'image') fail('RESOURCE_BINDING_INVALID');
  }
  let total = 0, photo;
  const resources = new Map();
  for (const item of manifest) {
    if (!item || typeof item.objectId !== 'string' || !/^[\w-]{1,128}$/u.test(item.objectId) || ['url', 'path', 'downloadUrl'].some(key => key in item)) fail('RESOURCE_REFERENCE_INVALID');
    if (!['image/png', 'image/jpeg'].includes(item.mediaType) || typeof item.contentBase64 !== 'string') fail('RESOURCE_MEDIA_UNSUPPORTED');
    if (item.contentBase64.length > Math.ceil(20 * 1024 * 1024 / 3) * 4) fail('RESOURCE_BYTES_INVALID');
    const bytes = Buffer.from(item.contentBase64, 'base64');
    if (bytes.toString('base64') !== item.contentBase64 || !bytes.length || bytes.length > 20 * 1024 * 1024) fail('RESOURCE_BYTES_INVALID');
    total += bytes.length;
    if (total > 32 * 1024 * 1024) fail('RESOURCE_LIMIT_EXCEEDED');
    if (hash(bytes) !== item.contentHash) fail('RESOURCE_HASH_MISMATCH');
    if (resources.has(item.objectId)) fail('RESOURCE_REFERENCE_INVALID');
    validateImage(bytes, item.mediaType);
    const resource = { ...item, dataUri: `data:${item.mediaType};base64,${item.contentBase64}` };
    if (item.bindingKey !== undefined) {
      const owner = source.resolvedInput;
      if (item.bindingKey !== 'personPhoto' || photo || !photoNames.size || !owner || typeof owner.subjectId !== 'string' || !owner.subjectId.trim() || typeof owner.subjectType !== 'string' || !owner.subjectType.trim() || item.subjectId !== owner.subjectId || item.subjectType !== owner.subjectType) fail('RESOURCE_OWNERSHIP_INVALID');
      if (item.synthetic === true || owner.synthetic === true || item.subjectType === 'SYNTHETIC') {
        if (!allowSynthetic || item.synthetic !== true || owner.synthetic !== true || item.subjectType !== 'SYNTHETIC' || item.subjectId !== 'synthetic-preview') fail('RESOURCE_OWNERSHIP_INVALID');
      }
      photo = resource;
    } else if (['subjectId', 'subjectType', 'synthetic'].some(key => item[key] !== undefined && item[key] !== null) || item.purpose === 'PHOTO') fail('RESOURCE_OWNERSHIP_INVALID');
    resources.set(item.objectId, resource);
  }
  if (photoNames.size && !photo) fail('RESOURCE_PHOTO_REQUIRED');
  for (const schema of schemas) {
    if (schema.type !== 'image') continue;
    if (photoNames.has(schema.name)) schema.content = photo.dataUri;
    else {
      if (schema.resourceRef) {
        const resource = resources.get(typeof schema.resourceRef === 'string' ? schema.resourceRef : schema.resourceRef.objectId);
        if (!resource || resource.bindingKey) fail('RESOURCE_NOT_AUTHORIZED');
        if (typeof schema.resourceRef === 'object' && schema.resourceRef.contentHash !== resource.contentHash) fail('RESOURCE_HASH_MISMATCH');
        schema.content = resource.dataUri;
      }
      if (!schema.content || ![...resources.values()].some(item => !item.bindingKey && item.dataUri === schema.content)) fail('RESOURCE_NOT_AUTHORIZED');
    }
    // 图片只能来自核准资源，业务字段不能覆盖其内容。
    schema.readOnly = true;
  }
  return template;
}
