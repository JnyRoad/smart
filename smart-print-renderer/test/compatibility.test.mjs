import assert from 'node:assert/strict';
import { existsSync, readFileSync } from 'node:fs';
import { fileURLToPath } from 'node:url';
import { test } from 'node:test';

import { PDFDocument } from '@pdfme/pdf-lib';
import { extractPdf } from 'clawpdf';

import { renderPrintTemplates, renderSinglePageTemplate } from '../src/render.mjs';

const CARD_SIZE_MM = Object.freeze({ width: 85.6, height: 53.98 });
const FONT_NAME = 'NotoSansSC';

/**
 * 为兼容性测试读取随模块固定的 CJK 字体；生产字体由同一版本资产提供。
 * 输出为字体字节；本函数只读固定测试资产，不产生副作用。
 */
function readCjkFontBytes() {
  const fontPath = fileURLToPath(new URL('../assets/fonts/NotoSansCJKsc-Regular.otf', import.meta.url));
  assert.ok(existsSync(fontPath), `缺少固定 CJK 测试字体：${fontPath}`);
  return new Uint8Array(readFileSync(fontPath));
}

/**
 * 构造固定尺寸的 pdfme 单面文本模板，供测试验证真实 PDF 生成结果。
 * 输入为 schema 名称和可选静态中文内容，输出为恰好一页的模板对象。
 */
function createTemplate(name, options = {}) {
  const {
    width = CARD_SIZE_MM.width,
    height = CARD_SIZE_MM.height,
    orientation = 'LANDSCAPE',
    staticContent,
  } = options;
  const template = {
    basePdf: {
      width,
      height,
      padding: [0, 0, 0, 0],
    },
    pageSpecJson: {
      widthMm: width,
      heightMm: height,
      orientation,
      maxPageCount: 1,
    },
    schemas: [[
      {
        name,
        type: 'text',
        position: { x: 8, y: 20 },
        width: 65,
        height: 12,
        fontName: FONT_NAME,
        fontSize: 14,
        alignment: 'center',
        verticalAlignment: 'middle',
      },
    ]],
  };

  if (staticContent !== undefined) {
    template.basePdf.staticSchema = [{
      name,
      type: 'text',
      content: staticContent,
      position: { x: 8, y: 6 },
      width: 65,
      height: 8,
      fontName: FONT_NAME,
      fontSize: 8,
    }];
  }

  return template;
}

/**
 * 构造错误的多页模板，验证每个新版本都必须只保留一页。
 * 输入为字段名数组，输出为 pdfme 模板对象；不读取业务数据。
 */
function createMultiPageTemplate(names, options = {}) {
  const template = createTemplate(names[0], options);
  template.schemas = names.map((name) => [{
    name,
    type: 'text',
    position: { x: 8, y: 20 },
    width: 65,
    height: 12,
    fontName: FONT_NAME,
    fontSize: 14,
    alignment: 'center',
    verticalAlignment: 'middle',
  }]);
  return template;
}

/**
 * 构造图片组件，供资源边界测试验证渲染器不会读取远程地址或本地路径。
 * 输入为字段名，输出为固定位置的 pdfme 图片 schema。
 */
function createImageSchema(name) {
  return {
    name,
    type: 'image',
    position: { x: 8, y: 4 },
    width: 20,
    height: 12,
  };
}

/**
 * 构造用于实际生成 PDF 的中文输入数据。
 * 输入为字段名和文本，输出为一条输入记录。
 */
function createInput(name, value) {
  return { [name]: value };
}

/**
 * 构造正面或背面的版本快照，版本号和模板数据在合并前必须保持不变。
 * 输入为版本标识、模板和输入值，输出为单面渲染快照。
 */
function createFace(templateVersionId, template, input) {
  return { templateVersionId, template, input };
}

/**
 * 读取 PDF 页面文字，使用 clawpdf 对真实生成结果做 CJK 断言。
 * 输入为 PDF 字节，输出为去掉布局空白后的文本；不使用 mock。
 */
async function extractText(pdfBytes) {
  const extracted = await extractPdf(pdfBytes, { mode: 'text' });
  return extracted.text.replace(/\s+/gu, '');
}

/**
 * 断言 PDF 页面尺寸接近模板尺寸，避免只检查页数而漏掉纸张规格漂移。
 * 输入为 pdf-lib 页面和毫米尺寸，输出无返回值；失败时保留尺寸上下文。
 */
function assertPageSize(page, expectedSizeMm) {
  const expectedWidthPt = (expectedSizeMm.width / 25.4) * 72;
  const expectedHeightPt = (expectedSizeMm.height / 25.4) * 72;
  assert.ok(Math.abs(page.getWidth() - expectedWidthPt) < 0.01);
  assert.ok(Math.abs(page.getHeight() - expectedHeightPt) < 0.01);
}

/**
 * 返回固定字体映射，确保每个真实渲染测试都使用随包字体而非环境字体。
 * 输出为字体名称到字节的映射；不读取外部路径。
 */
const fontBytes = () => ({ [FONT_NAME]: readCjkFontBytes() });

/**
 * 判断渲染校验错误是否包含指定诊断码，保持对外错误类别与内部原因码的区分。
 * 输入为异常对象和诊断码，输出为布尔值；不修改异常对象。
 */
function hasDetail(error, code) {
  return error?.details?.some((detail) => detail.code === code) === true;
}

test('员工厂牌独立单页模板使用真实 pdfme 生成一页并保留中文字体', async () => {
  const template = createTemplate('name', { staticContent: '正面固定：厂牌' });
  const input = createInput('name', '正面姓名：张三');
  const templateSnapshot = structuredClone(template);
  const inputSnapshot = structuredClone(input);
  const pdfBytes = await renderSinglePageTemplate({
    printType: 'STAFF_CARD',
    template,
    input,
    fontBytes: fontBytes(),
  });

  assert.ok(pdfBytes instanceof Uint8Array);
  assert.ok(pdfBytes.byteLength > 1000);
  const pdf = await PDFDocument.load(pdfBytes);
  assert.equal(pdf.getPageCount(), 1);
  assertPageSize(pdf.getPage(0), CARD_SIZE_MM);

  const pdfText = new TextDecoder('latin1').decode(pdfBytes);
  assert.match(pdfText, /\/CIDFontType0C/);
  const extracted = await extractText(pdfBytes);
  assert.match(extracted, /正面固定：厂牌/u);
  assert.match(extracted, /正面姓名：张三/u);
  assert.deepEqual(template, templateSnapshot);
  assert.deepEqual(input, inputSnapshot);
});

test('访客凭条独立单页模板使用真实 pdfme 生成一页', async () => {
  const pdfBytes = await renderSinglePageTemplate({
    printType: 'VISITOR_SLIP',
    template: createTemplate('visitorName'),
    input: createInput('visitorName', '访客：李四'),
    fontBytes: fontBytes(),
  });

  const pdf = await PDFDocument.load(pdfBytes);
  assert.equal(pdf.getPageCount(), 1);
  assertPageSize(pdf.getPage(0), CARD_SIZE_MM);
  assert.match(await extractText(pdfBytes), /访客：李四/u);
});

test('任何打印物的独立模板包含多页时拒绝生成', async () => {
  for (const printType of ['STAFF_CARD', 'VISITOR_SLIP']) {
    await assert.rejects(
      renderSinglePageTemplate({
        printType,
        template: createMultiPageTemplate(['front', 'back']),
        input: { front: '正面', back: '背面' },
        fontBytes: fontBytes(),
      }),
      (error) => hasDetail(error, 'TEMPLATE_PAGE_COUNT_MISMATCH') && /固定 1 page/u.test(error.message),
    );
  }
});

for (const printMode of ['MANUAL_DUPLEX', 'AUTO_DUPLEX']) {
  test(`员工厂牌${printMode}独立渲染正反面并按 FRONT/BACK 合并`, async () => {
    const frontTemplate = createTemplate('name', { staticContent: '正面固定：员工' });
    const backTemplate = createTemplate('name', { staticContent: '背面固定：须知' });
    const front = createFace('front-version-7', frontTemplate, createInput('name', '正面姓名：张三'));
    const back = createFace('back-version-12', backTemplate, createInput('name', '背面部门：研发部'));
    const frontSnapshot = structuredClone(front);
    const backSnapshot = structuredClone(back);
    const pdfBytes = await renderPrintTemplates({
      printType: 'STAFF_CARD',
      printMode,
      front,
      back,
      fontBytes: fontBytes(),
    });

    const pdf = await PDFDocument.load(pdfBytes);
    assert.equal(pdf.getPageCount(), 2);
    for (const page of pdf.getPages()) assertPageSize(page, CARD_SIZE_MM);

    // 两面都使用 name，但独立生成后合并，正面和背面的静态/输入值均须保留。
    const extracted = await extractText(pdfBytes);
    assert.match(extracted, /正面固定：员工/u);
    assert.match(extracted, /正面姓名：张三/u);
    assert.match(extracted, /背面固定：须知/u);
    assert.match(extracted, /背面部门：研发部/u);
    assert.ok(extracted.indexOf('正面固定：员工') < extracted.indexOf('背面固定：须知'));
    assert.deepEqual(front, frontSnapshot);
    assert.deepEqual(back, backSnapshot);
  });
}

test('访客凭条 SINGLE 模式只合成正面且不接受背面', async () => {
  const pdfBytes = await renderPrintTemplates({
    printType: 'VISITOR_SLIP',
    printMode: 'SINGLE',
    front: createFace('visitor-version-3', createTemplate('name'), createInput('name', '访客：王五')),
    fontBytes: fontBytes(),
  });

  const pdf = await PDFDocument.load(pdfBytes);
  assert.equal(pdf.getPageCount(), 1);
  assertPageSize(pdf.getPage(0), CARD_SIZE_MM);
  assert.match(await extractText(pdfBytes), /访客：王五/u);

  await assert.rejects(
    renderPrintTemplates({
      printType: 'VISITOR_SLIP',
      printMode: 'SINGLE',
      front: createFace('visitor-version-3', createTemplate('name'), createInput('name', '访客：王五')),
      back: createFace('visitor-back-version-1', createTemplate('name'), createInput('name', '不应有背面')),
    }),
    (error) => hasDetail(error, 'BACK_NOT_ALLOWED'),
  );
});

test('厂牌组合缺少正反面或版本标识时拒绝生成', async () => {
  const front = createFace('front-version-1', createTemplate('name'), createInput('name', '正面'));
  const back = createFace('back-version-1', createTemplate('name'), createInput('name', '背面'));

  await assert.rejects(
    renderPrintTemplates({
      printType: 'STAFF_CARD',
      printMode: 'MANUAL_DUPLEX',
      front,
      fontBytes: fontBytes(),
    }),
    (error) => hasDetail(error, 'BACK_REQUIRED'),
  );

  await assert.rejects(
    renderPrintTemplates({
      printType: 'STAFF_CARD',
      printMode: 'AUTO_DUPLEX',
      front: { ...front, templateVersionId: '' },
      back,
      fontBytes: fontBytes(),
    }),
    (error) => hasDetail(error, 'TEMPLATE_VERSION_ID_INVALID'),
  );

  await assert.rejects(
    renderPrintTemplates({
      printType: 'STAFF_CARD',
      printMode: 'SINGLE',
      front,
      back,
      fontBytes: fontBytes(),
    }),
    (error) => hasDetail(error, 'PRINT_MODE_UNSUPPORTED'),
  );
});

test('访客凭条只允许 SINGLE，厂牌组合必须使用双面模式', async () => {
  await assert.rejects(
    renderPrintTemplates({
      printType: 'VISITOR_SLIP',
      printMode: 'MANUAL_DUPLEX',
      front: createFace('visitor-version-1', createTemplate('name'), createInput('name', '访客')),
      fontBytes: fontBytes(),
    }),
    (error) => hasDetail(error, 'PRINT_MODE_UNSUPPORTED'),
  );

  await assert.rejects(
    renderPrintTemplates({
      printType: 'STAFF_CARD',
      printMode: 'AUTO_DUPLEX',
      front: createFace('front-version-1', createTemplate('name'), createInput('name', '正面')),
      back: createFace('back-version-1', createTemplate('name'), createInput('name', '背面')),
    }),
    (error) => hasDetail(error, 'FONT_BYTES_MISSING'),
  );
});

test('厂牌正反面尺寸或方向不一致时拒绝合并', async () => {
  const front = createFace(
    'front-version-1',
    createTemplate('name', { orientation: 'LANDSCAPE' }),
    createInput('name', '正面'),
  );
  const backWithWrongSize = createFace(
    'back-version-1',
    createTemplate('name', { width: 54, height: 85.6, orientation: 'PORTRAIT' }),
    createInput('name', '背面'),
  );

  await assert.rejects(
    renderPrintTemplates({
      printType: 'STAFF_CARD',
      printMode: 'MANUAL_DUPLEX',
      front,
      back: backWithWrongSize,
      fontBytes: fontBytes(),
    }),
    (error) => hasDetail(error, 'PAGE_SIZE_MISMATCH') || hasDetail(error, 'PAGE_ORIENTATION_MISMATCH'),
  );

  const backWithWrongDirection = createFace(
    'back-version-2',
    createTemplate('name', { orientation: 'PORTRAIT' }),
    createInput('name', '背面'),
  );
  await assert.rejects(
    renderPrintTemplates({
      printType: 'STAFF_CARD',
      printMode: 'AUTO_DUPLEX',
      front,
      back: backWithWrongDirection,
      fontBytes: fontBytes(),
    }),
    (error) => hasDetail(error, 'PAGE_ORIENTATION_MISMATCH'),
  );
});

test('图片组件使用远程 URL 时拒绝生成', async () => {
  const template = createTemplate('name');
  template.schemas[0].push(createImageSchema('avatar'));
  const input = createInput('name', '访客');
  input.avatar = 'https://example.invalid/avatar.png';

  await assert.rejects(
    renderSinglePageTemplate({
      printType: 'VISITOR_SLIP',
      template,
      input,
      fontBytes: fontBytes(),
    }),
    /data URI|图片.*PNG.*JPEG/u,
  );

  const templateWithRemoteDefault = createTemplate('name');
  templateWithRemoteDefault.schemas[0].push({
    ...createImageSchema('avatar'),
    content: 'https://example.invalid/template-avatar.png',
  });
  await assert.rejects(
    renderSinglePageTemplate({
      printType: 'VISITOR_SLIP',
      template: templateWithRemoteDefault,
      input: createInput('name', '访客'),
      fontBytes: fontBytes(),
    }),
    /data URI|图片.*PNG.*JPEG/u,
  );
});

test('未纳入当前设计器白名单的组件类型拒绝生成', async () => {
  const template = createTemplate('name');
  template.schemas[0][0].type = 'svg';

  await assert.rejects(
    renderSinglePageTemplate({
      printType: 'STAFF_CARD',
      template,
      input: createInput('name', '员工'),
      fontBytes: fontBytes(),
    }),
    /不支持的组件类型/u,
  );
});
