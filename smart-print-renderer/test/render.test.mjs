import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import { test } from 'node:test';
import { extractPdf } from 'clawpdf';
import { renderSinglePageTemplate } from '../src/render.mjs';
const fontBytes = { NotoSansSC: new Uint8Array(readFileSync(new URL('../assets/fonts/NotoSansCJKsc-Regular.otf', import.meta.url))) };
const schema = { name: 'name', type: 'text', position: { x: 5, y: 5 }, width: 65, height: 12, fontName: 'NotoSansSC', fontSize: 12 };
const render = (value, overrides = {}) => renderSinglePageTemplate({ printType: 'STAFF_CARD', fontBytes, input: { name: value }, template: { basePdf: { width: 85.6, height: 53.98, padding: [0, 0, 0, 0] }, schemas: [[{ ...schema, ...overrides }]] } });
const code = expected => error => error.details?.some(item => item.code === expected);
test('缺少字形时拒绝且诊断不回显人员文字', async () => {
  await assert.rejects(render('张三\u{10ffff}'), error => code('GLYPH_MISSING')(error) && !error.message.includes('张三'));
});
test('超长中文按同一字体与换行算法测量，不能静默溢出文本框', async () => {
  await assert.rejects(render('长部门名称'.repeat(100)), code('TEXT_OVERFLOW'));
});
test('组件超出版面或旋转未校准时拒绝', async () => {
  await assert.rejects(render('姓名', { position: { x: 80, y: 4 } }), code('ELEMENT_OUT_OF_BOUNDS'));
  await assert.rejects(render('姓名', { rotate: 45 }), code('ROTATION_UNSUPPORTED'));
});
test('文字缺业务输入时使用固定内容并执行同样溢出校验', async () => {
  await assert.rejects(render(undefined, { readOnly: true, content: '厂牌'.repeat(100) }), code('TEXT_OVERFLOW'));
});
test('静态组件与动态字段同名时仍校验静态实际文字', async () => {
  await assert.rejects(renderSinglePageTemplate({ printType: 'STAFF_CARD', fontBytes, input: { name: '正常姓名' }, template: {
    basePdf: { width: 85.6, height: 53.98, padding: [0, 0, 0, 0], staticSchema: [{ ...schema, content: '\u{10ffff}', readOnly: false }] },
    schemas: [[{ ...schema, position: { x: 5, y: 25 } }]]
  } }), code('GLYPH_MISSING'));
});
test('固定文字占位符展开后按真实输出检查缺字和溢出', async () => {
  await assert.rejects(render('\u{10ffff}', { name: 'caption', readOnly: true, content: '{name}' }), code('GLYPH_MISSING'));
  await assert.rejects(render('部门'.repeat(300), { name: 'caption', readOnly: true, content: '{name}' }), code('TEXT_OVERFLOW'));
});
test('动态组件没有输入时不把画布示例内容当成实际打印文字', async () => {
  const bytes = await render(undefined, { content: '\u{10ffff}', readOnly: false });
  assert.equal((await extractPdf(bytes, { mode: 'text' })).text.trim(), '');
});
