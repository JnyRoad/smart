import { readFileSync } from 'node:fs';
import { createHash, randomUUID } from 'node:crypto';
import { PDFDocument } from '@pdfme/pdf-lib';
import { renderSinglePageTemplate, RenderValidationError, isTemplateDimensionCompatible } from './render.mjs';
import { authorizeFaceResources } from './resource-policy.mjs';
const hash = bytes => 'sha256:' + createHash('sha256').update(bytes).digest('hex');
const font = new Uint8Array(readFileSync(new URL('../assets/fonts/NotoSansCJKsc-Regular.otf', import.meta.url)));
if (hash(font) !== 'sha256:2c76254f6fc379fddfce0a7e84fb5385bb135d3e399294f6eeb6680d0365b74b') throw new Error('固定中文字体校验失败');
const fontBytes = { NotoSansSC: font };
const fields = new Set(['staffName', 'staffNo', 'departmentName', 'companyName', 'cardNo', 'employeeGradeName', 'visitorName', 'visitorCredentialPayload', 'parkName', 'validFrom', 'validTo']);
const id = value => typeof value === 'string' && /^[\w-]{1,128}$/u.test(value);
const fail = code => { throw new RenderValidationError('渲染请求不符合已冻结的模板合同', [{ code }]); };
const byteSize = value => Buffer.byteLength(JSON.stringify(value));
/** 单次请求严格绑定一份预览或打印快照；两面从各自字段映射分别生成。 */
export async function renderEnvelope(body) {
  if (!body || !id(body.requestId) || !['PREVIEW', 'PRINT'].includes(body.purpose)) fail('REQUEST_INVALID');
  const preview = body.purpose === 'PREVIEW';
  if (!id(preview ? body.previewId : body.jobId) || (preview ? body.jobId !== undefined : body.previewId !== undefined)) fail('REQUEST_PURPOSE_INVALID');
  const staff = body.printItemType === 'STAFF_CARD';
  if (!staff && body.printItemType !== 'VISITOR_SLIP') fail('PRINT_TYPE_UNSUPPORTED');
  if (staff ? !['MANUAL_DUPLEX', 'AUTO_DUPLEX'].includes(body.printMode) : body.printMode !== 'SINGLE') fail('PRINT_MODE_UNSUPPORTED');
  const sources = body.faceSources;
  if (!Array.isArray(sources) || ![1, 2].includes(sources.length) || body.expectedFaceCount !== sources.length || (!staff && sources.length !== 1) || (staff && !preview && sources.length !== 2) || (body.pairId && sources.length !== 2)) fail('FACE_COUNT_INVALID');
  if (new Set(sources.map(source => source?.face)).size !== sources.length) fail('FACE_DUPLICATED');
  const prepared = sources.map(source => {
    if (!source || !['FRONT', 'BACK'].includes(source.face) || (!staff && source.face !== 'FRONT')) fail('FACE_ROLE_INVALID');
    const version = id(source.templateVersionId);
    const draft = id(source.templateId) && Number.isSafeInteger(source.draftRevision) && source.draftRevision >= 0;
    if (preview ? !(version !== draft) : !version) fail('VERSION_SOURCE_INVALID');
    if (version && (source.templateId !== undefined || source.draftRevision !== undefined) || draft && source.templateVersionId !== undefined || (body.pairId && !version)) fail('VERSION_SOURCE_INVALID');
    const template = authorizeFaceResources(source, { allowSynthetic: preview });
    if (byteSize(source.template) > 2 * 1024 * 1024 || byteSize(source.resolvedInput || {}) > 1024 * 1024) fail('PAYLOAD_LIMIT_EXCEEDED');
    if (template.schemaVersion !== 1 || template.sideCount !== 1 || template.faceRole !== source.face || template.pageSpecJson?.maxPageCount !== 1) fail('TEMPLATE_FACE_INVALID');
    const names = new Map((template.schemas?.[0] || []).map(schema => [schema.name, schema]));
    if (names.size !== template.schemas?.[0]?.length) fail('SCHEMA_NAME_DUPLICATED');
    const mappings = template.fieldSchemaJson?.fields || [];
    if (!Array.isArray(mappings) || mappings.length > 256) fail('FIELD_SCHEMA_INVALID');
    const input = Object.create(null);
    const values = source.resolvedInput?.fields || {};
    if (!values || Array.isArray(values) || typeof values !== 'object' || Object.keys(values).some(key => !fields.has(key))) fail('FIELD_VALUE_INVALID');
    const bound = new Set();
    for (const mapping of mappings) {
      if (!mapping || !names.has(mapping.schemaName) || typeof mapping.required !== 'boolean' || bound.has(mapping.schemaName)) fail('FIELD_SCHEMA_INVALID');
      bound.add(mapping.schemaName);
      if (mapping.key === 'personPhoto') {
        if (names.get(mapping.schemaName).type !== 'image' || mapping.required !== true) fail('FIELD_SCHEMA_INVALID');
        continue;
      }
      if (!fields.has(mapping.key) || names.get(mapping.schemaName).type === 'image') fail('FIELD_SCHEMA_INVALID');
      const value = values[mapping.key];
      if (mapping.required && (value === undefined || value === null || String(value).trim() === '')) fail('FIELD_REQUIRED');
      if (value !== undefined && value !== null && typeof value !== 'string' && typeof value !== 'number') fail('FIELD_VALUE_INVALID');
      input[mapping.schemaName] = value === undefined || value === null ? '' : String(value);
    }
    return { source, template, input };
  }).sort((a, b) => a.source.face === 'FRONT' ? -1 : b.source.face === 'FRONT' ? 1 : 0);
  if (prepared.length === 2) {
    const frontSpec = prepared[0].template.pageSpecJson;
    const backSpec = prepared[1].template.pageSpecJson;
    if (['widthMm', 'heightMm'].some(key => !isTemplateDimensionCompatible(frontSpec[key], backSpec[key])) || frontSpec.orientation !== backSpec.orientation) fail('PAGE_SIZE_MISMATCH');
  }
  const artifacts = [];
  const pdfs = [];
  for (const face of prepared) {
    let bytes;
    try {
      bytes = await renderSinglePageTemplate({ printType: body.printItemType, template: face.template, input: face.input, fontBytes });
    } catch (error) {
      // 两面允许同名组件，诊断必须携带当前面；只补定位，不暴露字段取值。
      if (Array.isArray(error.details)) error.details = error.details.map(detail => ({ ...detail, face: face.source.face }));
      throw error;
    }
    pdfs.push(bytes);
    artifacts.push(makeArtifact(bytes, face.template.pageSpecJson, 1, face.source.face));
  }
  let combinedArtifact;
  if (pdfs.length === 2) {
    const combined = await PDFDocument.create();
    for (const bytes of pdfs) { const pdf = await PDFDocument.load(bytes); const [page] = await combined.copyPages(pdf, [0]); combined.addPage(page); }
    combinedArtifact = makeArtifact(await combined.save(), prepared[0].template.pageSpecJson, 2);
  }
  return { renderRequestId: body.requestId, [preview ? 'previewId' : 'jobId']: preview ? body.previewId : body.jobId, status: 'READY', manifestHash: hash(Buffer.from(JSON.stringify(artifacts.map(({ face, sha256, bytes }) => ({ face, sha256, bytes }))))), artifacts, ...(combinedArtifact ? { combinedArtifact } : {}) };
}
/** 制品只通过内存字节返回平台，大小/hash由平台再次校验。 */
function makeArtifact(bytes, page, pageCount, face) {
  if (bytes.length > 32 * 1024 * 1024) fail('ARTIFACT_LIMIT_EXCEEDED');
  return { artifactId: randomUUID(), ...(face ? { face } : {}), mediaType: 'application/pdf', sha256: hash(bytes), bytes: bytes.length, contentBase64: Buffer.from(bytes).toString('base64'), pageCount, widthMm: page.widthMm, heightMm: page.heightMm };
}
