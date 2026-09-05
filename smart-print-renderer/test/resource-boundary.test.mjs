import assert from 'node:assert/strict';
import { test } from 'node:test';
import { PNG } from 'pngjs';
import jpeg from 'jpeg-js';
import { renderEnvelope } from '../src/envelope.mjs';
import { createHash } from 'node:crypto';
import { authorizeFaceResources } from '../src/resource-policy.mjs';
const png = PNG.sync.write({ width: 2, height: 2, data: Buffer.from([255, 0, 0, 255, 0, 0, 255, 255, 255, 0, 0, 255, 0, 0, 255, 255]) }).toString('base64');
const hash = 'sha256:' + createHash('sha256').update(Buffer.from(png, 'base64')).digest('hex');
const face = () => ({ template: { basePdf: { width: 58, height: 80, padding: [0, 0, 0, 0] }, schemas: [[{ name: 'photo', type: 'image', content: `data:image/png;base64,${png}` }]] }, resourceManifest: [] });
const personFace = () => ({ ...face(), template: { ...face().template, schemas: [[{ name: 'photo', type: 'image' }]], fieldSchemaJson: { fields: [{ key: 'personPhoto', schemaName: 'photo', required: true }] } }, resolvedInput: { subjectType: 'STAFF', subjectId: 'person-a', fields: {} }, resourceManifest: [{ objectId: 'photo-a', bindingKey: 'personPhoto', subjectType: 'STAFF', subjectId: 'person-a', mediaType: 'image/png', contentHash: hash, contentBase64: png }] });
test('动态照片按资源绑定填充，保持原快照且不能由普通字段覆盖', () => {
  const source = personFace(); const before = structuredClone(source)
  const result = authorizeFaceResources(source)
  assert.equal(result.schemas[0][0].content, `data:image/png;base64,${png}`)
  assert.equal(result.schemas[0][0].readOnly, true)
  assert.deepEqual(source, before)
})
test('照片缺失、重复、跨人员或来源、未知绑定及矛盾静态内容全部拒绝', () => {
  for (const mutate of [s => { s.resourceManifest = [] }, s => s.resourceManifest.push({ ...s.resourceManifest[0], objectId: 'other' }), s => { s.resourceManifest[0].subjectId = 'other' }, s => { s.resourceManifest[0].subjectType = 'VISITOR' }, s => { s.resourceManifest[0].bindingKey = 'unknown' }, s => { s.template.schemas[0][0].content = 'https://invalid.example/photo' }, s => { s.template.schemas[0][0].resourceRef = 'photo-a' }, s => { s.template.schemas[0][0].type = 'text' }, s => { s.template.fieldSchemaJson.fields[0].required = false }]) {
    const source = personFace(); mutate(source); assert.throws(() => authorizeFaceResources(source))
  }
})
test('仅有图片magic而不能解码的资源必须拒绝', () => {
  for (const [mediaType, bytes] of [['image/png', Buffer.from([137, 80, 78, 71, 13, 10, 26, 10])], ['image/jpeg', Buffer.from([255, 216, 255, 217])]]) {
    const source = face(); source.resourceManifest = [{ objectId: 'bad', mediaType, contentHash: 'sha256:' + createHash('sha256').update(bytes).digest('hex'), contentBase64: bytes.toString('base64') }]; source.template.schemas[0][0] = { name: 'photo', type: 'image', resourceRef: 'bad' };
    assert.throws(() => authorizeFaceResources(source), /RESOURCE_(BYTES|IMAGE)_INVALID/)
  }
})
test('即使是内联图片，没有授权清单仍拒绝', () => assert.throws(() => authorizeFaceResources(face()), /RESOURCE_NOT_AUTHORIZED/));
test('资源hash被篡改时拒绝，不尝试下载外部URL', () => {
  const source = face(); source.resourceManifest = [{ objectId: 'image-1', mediaType: 'image/png', contentHash: hash.replace(/.$/, '0'), contentBase64: png }];
  assert.throws(() => authorizeFaceResources(source), /RESOURCE_HASH_MISMATCH/);
  source.resourceManifest[0].url = 'http://127.0.0.1/secret';
  assert.throws(() => authorizeFaceResources(source), /RESOURCE_REFERENCE_INVALID/);
});
test('真实JPEG可以冻结，但错误媒体类型、图片尺寸炸弹和损坏像素被拒绝', () => {
  const bytes = jpeg.encode({ width: 2, height: 2, data: Buffer.from([255, 0, 0, 255, 0, 0, 255, 255, 255, 0, 0, 255, 0, 0, 255, 255]) }, 90).data;
  const source = personFace(); Object.assign(source.resourceManifest[0], { mediaType: 'image/jpeg', contentBase64: bytes.toString('base64'), contentHash: 'sha256:' + createHash('sha256').update(bytes).digest('hex') });
  assert.match(authorizeFaceResources(source).schemas[0][0].content, /^data:image\/jpeg;base64,/);
  source.resourceManifest[0].mediaType = 'image/png'; assert.throws(() => authorizeFaceResources(source), /RESOURCE_IMAGE_INVALID/);
  const oversized = Buffer.from(png, 'base64'); oversized.writeUInt32BE(100000, 16);
  const bomb = personFace(); Object.assign(bomb.resourceManifest[0], { contentBase64: oversized.toString('base64'), contentHash: 'sha256:' + createHash('sha256').update(oversized).digest('hex') });
  assert.throws(() => authorizeFaceResources(bomb), /RESOURCE_IMAGE_INVALID/);
  const corrupt = Buffer.from(png, 'base64'); corrupt[45] ^= 1;
  Object.assign(bomb.resourceManifest[0], { contentBase64: corrupt.toString('base64'), contentHash: 'sha256:' + createHash('sha256').update(corrupt).digest('hex') });
  assert.throws(() => authorizeFaceResources(bomb), /RESOURCE_IMAGE_INVALID/);
});
test('合成照片仅准许明确的合成预览，业务预览保留当前人员冻结照片', () => {
  const synthetic = personFace(); Object.assign(synthetic.resourceManifest[0], { synthetic: true, subjectId: 'synthetic-preview', subjectType: 'SYNTHETIC' }); Object.assign(synthetic.resolvedInput, { synthetic: true, subjectId: 'synthetic-preview', subjectType: 'SYNTHETIC' });
  assert.throws(() => authorizeFaceResources(synthetic), /RESOURCE_OWNERSHIP_INVALID/);
  assert.match(authorizeFaceResources(synthetic, { allowSynthetic: true }).schemas[0][0].content, /^data:image\/png/);
  const business = personFace(); assert.equal(authorizeFaceResources(business, { allowSynthetic: true }).schemas[0][0].content, `data:image/png;base64,${png}`);
});
test('外部人员字段不能覆盖图片组件或注入人员照片URL', async () => {
  for (const fields of [{ personPhoto: 'https://invalid.example/photo' }, { photo: `data:image/png;base64,${png}` }]) {
    const source = personFace(); source.resolvedInput.fields = fields;
    Object.assign(source, { face: 'FRONT', templateVersionId: 'version-a' });
    Object.assign(source.template, { schemaVersion: 1, sideCount: 1, faceRole: 'FRONT', pageSpecJson: { widthMm: 58, heightMm: 80, maxPageCount: 1, orientation: 'PORTRAIT' } });
    await assert.rejects(renderEnvelope({ requestId: 'r1', previewId: 'p1', purpose: 'PREVIEW', printItemType: 'VISITOR_SLIP', printMode: 'SINGLE', expectedFaceCount: 1, faceSources: [source] }), error => error.details.some(item => item.code === 'FIELD_VALUE_INVALID'));
  }
});
test('校验通过的资源只交付原有PNG字节，不改调用方快照', () => {
  const source = face(); source.resourceManifest = [{ objectId: 'image-1', mediaType: 'image/png', contentHash: hash, contentBase64: png }];
  const before = structuredClone(source);
  const result = authorizeFaceResources(source);
  assert.deepEqual(source, before);
  assert.equal(result.schemas[0][0].content, before.template.schemas[0][0].content);
  assert.equal(result.schemas[0][0].readOnly, true);
});
test('布局图片引用的hash必须与已授权资源一致', () => {
  const source = face(); source.resourceManifest = [{ objectId: 'image-1', mediaType: 'image/png', contentHash: hash, contentBase64: png }];
  source.template.schemas[0][0] = { type: 'image', name: '背景', resourceRef: { objectId: 'image-1', contentHash: 'sha256:' + '0'.repeat(64) } };
  assert.throws(() => authorizeFaceResources(source), /RESOURCE_HASH_MISMATCH/);
});
test('固定图片的历史资源元数据允许空人员归属，但禁止真实人员归属', () => {
  const source = face(); source.resourceManifest = [{ objectId: 'static-a', mediaType: 'image/png', contentHash: hash, contentBase64: png, subjectId: null, sourceRevision: null, purpose: 'BACKGROUND', accessScope: 'TEMPLATE' }];
  assert.equal(authorizeFaceResources(source).schemas[0][0].readOnly, true);
  source.resourceManifest[0].subjectId = 'someone';
  assert.throws(() => authorizeFaceResources(source), /RESOURCE_OWNERSHIP_INVALID/);
});
