import assert from 'node:assert/strict';
import { test } from 'node:test';
import { createHash } from 'node:crypto';
import { spawn } from 'node:child_process';
import { once } from 'node:events';
import { PDFDocument } from '@pdfme/pdf-lib';
import { createRenderServer } from '../src/server.mjs';
const token = 'test-only-renderer-service-token-32';
function request() { return { requestId: 'render-test-1', purpose: 'PREVIEW', previewId: 'preview-1', printItemType: 'STAFF_CARD', printMode: 'MANUAL_DUPLEX', expectedFaceCount: 1, faceSources: [{ face: 'BACK', templateId: 'template-1', draftRevision: 1, template: { schemaVersion: 1, faceRole: 'BACK', sideCount: 1, basePdf: { width: 85.6, height: 53.98, padding: [0, 0, 0, 0] }, pageSpecJson: { widthMm: 85.6, heightMm: 53.98, orientation: 'LANDSCAPE', maxPageCount: 1 }, schemas: [[{ name: 'name', type: 'text', position: { x: 5, y: 5 }, width: 65, height: 10, fontSize: 12, fontName: 'NotoSansSC' }]], fieldSchemaJson: { fields: [{ key: 'staffName', schemaName: 'name', required: true }] } }, resolvedInput: { fields: { staffName: '合成员工' } }, resourceManifest: [] }] }; }
async function withServer(run, options = {}) { const server = createRenderServer({ token, ...options }); await new Promise(resolve => server.listen(0, '127.0.0.1', resolve)); try { await run(`http://127.0.0.1:${server.address().port}/internal/print-renderer/v1/render`); } finally { await new Promise(resolve => server.close(resolve)); } }
const post = (url, body, auth = token) => fetch(url, { method: 'POST', headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${auth}` }, body: JSON.stringify(body) });
test('私有渲染必须有服务身份，拒绝浏览器匿名请求', () => withServer(async url => { const response = await post(url, request(), 'invalid'); assert.equal(response.status, 401); }));
test('独立背面草稿预览返回真实可解析PDF及准确hash', () => withServer(async url => {
  const response = await post(url, request()); assert.equal(response.status, 200); const result = await response.json(); const artifact = result.artifacts[0];
  assert.equal(result.previewId, 'preview-1'); assert.equal(result.renderRequestId, 'render-test-1'); assert.equal(artifact.face, 'BACK');
  const bytes = Buffer.from(artifact.contentBase64, 'base64'); assert.equal(bytes.length, artifact.bytes); assert.equal(artifact.sha256, 'sha256:' + createHash('sha256').update(bytes).digest('hex'));
  assert.equal((await PDFDocument.load(bytes)).getPageCount(), 1); assert.equal(result.combinedArtifact, undefined);
}));
test('正式打印不能使用草稿，访客不能有背面，来源不能歧义', () => withServer(async url => {
  for (const change of [body => { body.purpose = 'PRINT'; body.jobId = 'job-1'; delete body.previewId; }, body => { body.printItemType = 'VISITOR_SLIP'; body.printMode = 'SINGLE'; }, body => { body.faceSources[0].templateVersionId = 'version-1'; }]) { const body = request(); change(body); assert.equal((await post(url, body)).status, 422); }
}));
test('HTTP请求大小在解析前受限', () => withServer(async url => { assert.equal((await post(url, request())).status, 413); }, { maxBodyBytes: 128 }));
test('组合预览文字失败返回准确面与组件，不能丢失背面定位', () => withServer(async url => {
  const body = request(); const front = structuredClone(body.faceSources[0]); front.face = 'FRONT'; front.template.faceRole = 'FRONT';
  body.faceSources[0].resolvedInput.fields.staffName = '部门'.repeat(200);
  body.faceSources.unshift(front); body.expectedFaceCount = 2;
  const response = await post(url, body);
  assert.equal(response.status, 422);
  assert.ok((await response.json()).error.details.some(item => item.code === 'TEXT_OVERFLOW' && item.schemaName === 'name' && item.face === 'BACK'));
}));
test('从模块内联入口启动时文件Worker仍生成真实PDF', async () => {
  const script = `import { createRenderServer } from './src/server.mjs'; const server = createRenderServer({ token: ${JSON.stringify(token)} }); server.listen(0, '127.0.0.1', () => process.send(server.address().port));`;
  const child = spawn(process.execPath, ['--input-type=module', '-e', script], { cwd: new URL('..', import.meta.url), stdio: ['ignore', 'ignore', 'ignore', 'ipc'] });
  try {
    const [port] = await once(child, 'message', { signal: AbortSignal.timeout(5000) });
    const response = await post(`http://127.0.0.1:${port}/internal/print-renderer/v1/render`, request());
    assert.equal(response.status, 200);
    const result = await response.json();
    assert.equal((await PDFDocument.load(Buffer.from(result.artifacts[0].contentBase64, 'base64'))).getPageCount(), 1);
  } finally { const exited = once(child, 'exit'); child.kill(); await exited; }
});
