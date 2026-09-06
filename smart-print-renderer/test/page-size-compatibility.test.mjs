import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { createHash } from 'node:crypto';
import { test } from 'node:test';
import { PDFDocument } from '@pdfme/pdf-lib';
import { extractPdf } from 'clawpdf';
import { renderEnvelope } from '../src/envelope.mjs';
import { renderPrintTemplates, renderSinglePageTemplate } from '../src/render.mjs';

const fontBytes = { NotoSansSC: new Uint8Array(readFileSync(new URL('../assets/fonts/NotoSansCJKsc-Regular.otf', import.meta.url))) };
const frontSize = { widthMm: 85.6, heightMm: 53.98 };
const face = (role, size = frontSize) => ({
  face: role, templateVersionId: `version-${role.toLowerCase()}`, resourceManifest: [], resolvedInput: { fields: {} },
  input: {}, template: {
    schemaVersion: 1, sideCount: 1, faceRole: role,
    basePdf: { width: size.widthMm, height: size.heightMm, padding: [0, 0, 0, 0] },
    pageSpecJson: { ...size, orientation: 'LANDSCAPE', maxPageCount: 1 }, fieldSchemaJson: { fields: [] },
    schemas: [[{ name: 'caption', type: 'text', readOnly: true, content: role === 'FRONT' ? '正面样例' : '背面样例', position: { x: 5, y: 5 }, width: 65, height: 10, fontName: 'NotoSansSC', fontSize: 12 }]]
  }
});

const renderers = [
  ['直接组合入口', (front, back) => renderPrintTemplates({ printType: 'STAFF_CARD', printMode: 'MANUAL_DUPLEX', front, back, fontBytes })],
  ['预览协议入口', async (front, back) => {
    const result = await renderEnvelope({ requestId: 'size-request', previewId: 'size-preview', purpose: 'PREVIEW', printItemType: 'STAFF_CARD', printMode: 'MANUAL_DUPLEX', expectedFaceCount: 2, faceSources: [front, back] });
    assert.equal(result.status, 'READY');
    assert.deepEqual(result.artifacts.map(item => item.face), ['FRONT', 'BACK']);
    for (const artifact of [...result.artifacts, result.combinedArtifact]) {
      const bytes = Buffer.from(artifact.contentBase64, 'base64');
      assert.equal(bytes.length, artifact.bytes);
      assert.equal(artifact.sha256, 'sha256:' + createHash('sha256').update(bytes).digest('hex'));
      assert.equal((await PDFDocument.load(bytes)).getPageCount(), artifact.face ? 1 : 2);
    }
    return Buffer.from(result.combinedArtifact.contentBase64, 'base64');
  }]
];

for (const [name, renderPair] of renderers) {
  test(`${name}接受1e-6毫米内的差异并保持真实PDF两页内容、页序和尺寸`, async () => {
    const backSize = { widthMm: 85.6000005, heightMm: 53.9800005 };
    const bytes = await renderPair(face('FRONT'), face('BACK', backSize));
    const pdf = await PDFDocument.load(bytes);
    assert.equal(pdf.getPageCount(), 2);
    for (const [index, size] of [frontSize, backSize].entries()) {
      const page = pdf.getPage(index);
      // pdfme 6.1.12 的毫米转点系数为2.8346，实际PDF沿用原有0.01mm测量门限。
      assert.ok(Math.abs(page.getWidth() * 25.4 / 72 - size.widthMm) <= 0.01);
      assert.ok(Math.abs(page.getHeight() * 25.4 / 72 - size.heightMm) <= 0.01);
    }
    const extracted = (await extractPdf(bytes, { mode: 'text' })).text.replace(/\s+/gu, '');
    assert.match(extracted, /正面样例/u); assert.match(extracted, /背面样例/u);
    assert.ok(extracted.indexOf('正面样例') < extracted.indexOf('背面样例'));
  });
  for (const [dimension, size] of [['宽度', { widthMm: 85.600002, heightMm: 53.98 }], ['高度', { widthMm: 85.6, heightMm: 53.980002 }]]) {
    test(`${name}拒绝${dimension}差超过1e-6毫米的组合`, async () => {
      await assert.rejects(renderPair(face('FRONT'), face('BACK', size)), error => error.details?.some(item => item.code === 'PAGE_SIZE_MISMATCH'));
    });
  }
  test(`${name}不以尺寸容差放宽方向兼容性`, async () => {
    const back = face('BACK'); back.template.pageSpecJson.orientation = 'PORTRAIT';
    await assert.rejects(renderPair(face('FRONT'), back), error => error.details?.some(item => ['PAGE_SIZE_MISMATCH', 'PAGE_ORIENTATION_MISMATCH'].includes(item.code)));
  });
  test(`${name}按已声明widthMm比较组合，不能只比较basePdf而漏过超差声明`, async () => {
    const back = face('BACK'); back.template.pageSpecJson.widthMm = 85.600002;
    await assert.rejects(renderPair(face('FRONT'), back), error => error.details?.some(item => item.code === 'PAGE_SIZE_MISMATCH'));
  });
}

test('单面basePdf与pageSpec声明不一致仍拒绝渲染', async () => {
  const source = face('FRONT'); source.template.basePdf.width = 86;
  await assert.rejects(renderSinglePageTemplate({ printType: 'STAFF_CARD', template: source.template, input: {}, fontBytes }), error => error.details?.some(item => item.code === 'PAGE_SIZE_MISMATCH'));
});
