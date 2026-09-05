/**
 * 受控打印模板渲染入口：把平台已经校验过的单个人模板和字体字节交给 pdfme，
 * 生成固定一页或正反面两页的 PDF。此模块不查询业务数据、不访问文件服务，也不接触打印设备。
 */

import { checkTemplate } from '@pdfme/common';
import { generate } from '@pdfme/generator';
import { PDFDocument } from '@pdfme/pdf-lib';
import { barcodes, image, rectangle, line, ellipse, text } from '@pdfme/schemas';
import { validateContent } from './content-validation.mjs';

const SINGLE_PAGE_COUNT = 1;
const STAFF_PRINT_MODES = new Set(['MANUAL_DUPLEX', 'AUTO_DUPLEX']);
const PAGE_SIZE_TOLERANCE_MM = 0.01;
const TEMPLATE_PAIR_SIZE_TOLERANCE_MM = 1e-6;
const PRINT_PLUGINS = Object.freeze({
  Text: text,
  Image: image,
  Rectangle: rectangle,
  Line: line,
  Ellipse: ellipse,
  QRCode: barcodes.qrcode,
  Code128: barcodes.code128,
});
const SUPPORTED_SCHEMA_TYPES = new Set(
  Object.values(PRINT_PLUGINS).map((plugin) => plugin.propPanel.defaultSchema.type),
);

/**
 * 表示模板或渲染结果不满足固定打印合同的校验错误。
 * 输入为诊断消息和可选结构化细节，输出为可由上层映射成 422 的错误对象；不含人员数据。
 */
export class RenderValidationError extends Error {
  constructor(message, details = []) {
    super(message);
    this.name = 'RenderValidationError';
    this.code = 'RENDER_VALIDATION_FAILED';
    this.details = details;
  }
}

/**
 * 为一位人员生成一个固定单页 PDF。
 * 输入为打印物类型、单页 pdfme 模板、单条字段输入和已授权字体字节；输出为 PDF 字节。
 * 本函数只在内存中生成结果，拒绝外部 URL/路径字体和多页模板，不写文件或发请求。
 */
export async function renderSinglePageTemplate({ printType, template, input, fontBytes } = {}) {
  // 复制输入快照，避免 pdfme 的兼容处理改动调用方持有的对象。
  const renderTemplate = cloneJsonValue(template);
  const renderInput = cloneJsonValue(input);

  // 在调用第三方生成器前校验固定单页、页面规格和受控字体边界。
  const expectedPageCount = validateRequest({
    printType,
    template: renderTemplate,
    input: renderInput,
    fontBytes,
  });

  // 将内存字体字节转换成 pdfme 的字体配置，不允许生成器自行拉取资源。
  const fonts = createPdfmeFonts(fontBytes);
  await validateContent(renderTemplate, renderInput, fontBytes, RenderValidationError);

  let pdfBytes;
  try {
    // 使用 pdfme 内置白名单插件生成一个人的单页 PDF。
    pdfBytes = await generate({
      template: renderTemplate,
      inputs: [renderInput],
      options: { font: fonts },
      plugins: PRINT_PLUGINS,
    });
  } catch (error) {
    throw new RenderValidationError(
      `${printType} PDF 生成失败，请检查组件内容与布局`,
      [{ code: 'PDFME_GENERATION_FAILED' }],
    );
  }

  // 重新解析生成结果，确认页数和尺寸没有被动态布局或第三方库改变。
  await validateGeneratedPdf({
    printType,
    template: renderTemplate,
    pdfBytes,
    expectedPageCount,
  });
  return pdfBytes;
}

/**
 * 根据正反面独立版本快照生成打印 PDF。
 * 输入为打印类型、翻面模式、正反面版本快照和受控字体；输出按 FRONT/BACK 顺序合并的 PDF 字节。
 * 每面单独调用单页入口，因此同名字段不会在两面之间覆盖；快照只读且不写入外部资源。
 */
export async function renderPrintTemplates({
  printType,
  printMode,
  front,
  back,
  fontBytes,
} = {}) {
  const combination = validatePrintCombination({ printType, printMode, front, back });

  // 访客只允许独立生成正面，不能借组合接口附带背面。
  if (printType === 'VISITOR_SLIP') {
    return renderSinglePageTemplate({
      printType,
      template: combination.front.template,
      input: combination.front.input,
      fontBytes,
    });
  }

  // 先校验两面规格，确保错误尺寸在实际生成任何页面前被拒绝。
  for (const face of [combination.front, combination.back]) validateRequest({ printType, template: face.template, input: face.input, fontBytes });
  validateTemplatePairSpec(combination.front.template, combination.back.template);

  // 先分别渲染两个单页快照，再合并 PDF，避免把两个同名字段塞进同一个输入对象。
  const frontPdfBytes = await renderSinglePageTemplate({
    printType,
    template: combination.front.template,
    input: combination.front.input,
    fontBytes,
  });
  const backPdfBytes = await renderSinglePageTemplate({
    printType,
    template: combination.back.template,
    input: combination.back.input,
    fontBytes,
  });

  return mergePrintPages({
    printType,
    front: combination.front,
    back: combination.back,
    frontPdfBytes,
    backPdfBytes,
  });
}

/**
 * 兼容旧原型调用方的别名；旧入口现在遵循单页模板合同，不再接受同一模板的双页 schemas。
 * 输入和输出与 renderSinglePageTemplate 相同；业务代码应迁移到明确命名的新入口。
 */
export async function renderFixedPageTemplate(args = {}) {
  return renderSinglePageTemplate(args);
}

/**
 * 检查渲染请求的打印物类型、模板页结构、输入和受控字体。
 * 输入为单页渲染请求对象，输出为固定页数 1；只做内存校验，不产生副作用。
 */
function validateRequest({ printType, template, input, fontBytes }) {
  if (printType !== 'STAFF_CARD' && printType !== 'VISITOR_SLIP') {
    throw new RenderValidationError(`不支持的打印物类型：${String(printType)}`, [
      { code: 'PRINT_TYPE_UNSUPPORTED' },
    ]);
  }

  if (!isRecord(template)) {
    throw new RenderValidationError(`${printType} 模板必须是对象`, [{ code: 'TEMPLATE_INVALID' }]);
  }
  if (!Array.isArray(template.schemas) || template.schemas.length !== SINGLE_PAGE_COUNT) {
    const actualPageCount = Array.isArray(template.schemas) ? template.schemas.length : 0;
    throw new RenderValidationError(
      `${printType} 模板必须固定 ${SINGLE_PAGE_COUNT} page，收到 ${actualPageCount}`,
      [{ code: 'TEMPLATE_PAGE_COUNT_MISMATCH' }],
    );
  }

  validateFixedBasePdf(printType, template.basePdf);
  validateDeclaredPageSpec(printType, template);
  const staticSchemas = Array.isArray(template.basePdf.staticSchema)
    ? template.basePdf.staticSchema
    : [];
  validateSchemaTypes(printType, [...template.schemas, staticSchemas]);
  validateNoExpandableSchema(printType, [...template.schemas, staticSchemas]);

  try {
    checkTemplate(template);
  } catch (error) {
    throw new RenderValidationError(
      `${printType} 模板不符合 pdfme 模板结构：${getErrorMessage(error)}`,
      [{ code: 'TEMPLATE_SCHEMA_INVALID' }],
    );
  }

  if (!isRecord(input)) {
    throw new RenderValidationError(`${printType} 必须提供一条输入数据`, [{ code: 'INPUT_INVALID' }]);
  }

  validateFontBytes(fontBytes);
  validateSchemaFonts([...template.schemas, staticSchemas], fontBytes);
  validateImageInputs(printType, template.schemas, staticSchemas, input);
  return SINGLE_PAGE_COUNT;
}

/**
 * 检查打印模式和正反面版本快照，并复制成不可变的本次渲染输入。
 * 输入为组合请求，输出为已复制的正面和可选背面；不会修改调用方对象。
 */
function validatePrintCombination({ printType, printMode, front, back }) {
  if (printType !== 'STAFF_CARD' && printType !== 'VISITOR_SLIP') {
    throw new RenderValidationError(`不支持的打印物类型：${String(printType)}`, [
      { code: 'PRINT_TYPE_UNSUPPORTED' },
    ]);
  }

  if (printType === 'STAFF_CARD') {
    if (!STAFF_PRINT_MODES.has(printMode)) {
      throw new RenderValidationError(`STAFF_CARD 不支持打印模式：${String(printMode)}`, [
        { code: 'PRINT_MODE_UNSUPPORTED' },
      ]);
    }
    if (!isRecord(front)) {
      throw new RenderValidationError('STAFF_CARD 必须提供正面模板版本', [
        { code: 'FRONT_REQUIRED' },
      ]);
    }
    if (!isRecord(back)) {
      throw new RenderValidationError('STAFF_CARD 必须提供背面模板版本', [
        { code: 'BACK_REQUIRED' },
      ]);
    }
    return {
      front: cloneFaceSnapshot(front, 'FRONT'),
      back: cloneFaceSnapshot(back, 'BACK'),
    };
  }

  if (printMode !== 'SINGLE') {
    throw new RenderValidationError(`VISITOR_SLIP 只支持 SINGLE 打印模式：${String(printMode)}`, [
      { code: 'PRINT_MODE_UNSUPPORTED' },
    ]);
  }
  if (!isRecord(front)) {
    throw new RenderValidationError('VISITOR_SLIP 必须提供正面模板版本', [
      { code: 'FRONT_REQUIRED' },
    ]);
  }
  if (back !== undefined && back !== null) {
    throw new RenderValidationError('VISITOR_SLIP 不允许提供背面模板版本', [
      { code: 'BACK_NOT_ALLOWED' },
    ]);
  }

  return { front: cloneFaceSnapshot(front, 'FRONT') };
}

/**
 * 复制并检查单面模板版本快照。
 * 输入为正面或背面快照和面角色，输出为独立对象；版本标识为空或非字符串时拒绝。
 */
function cloneFaceSnapshot(face, side) {
  if (typeof face.templateVersionId !== 'string' || face.templateVersionId.trim() === '') {
    throw new RenderValidationError(`${side} 模板版本标识不能为空`, [
      { code: 'TEMPLATE_VERSION_ID_INVALID', side },
    ]);
  }

  return {
    templateVersionId: face.templateVersionId,
    template: cloneJsonValue(face.template),
    input: cloneJsonValue(face.input),
  };
}

/**
 * 限制正式渲染只使用当前业务设计器允许的文字、图片、矩形、二维码和 Code128 组件。
 * 输入为打印物类型与按面组织的 schema，输出无返回值；不执行任何插件代码。
 */
function validateSchemaTypes(printType, schemaPages) {
  for (const pageSchemas of schemaPages) {
    if (!Array.isArray(pageSchemas)) {
      throw new RenderValidationError(`${printType} 每一面必须是 schema 数组`, [
        { code: 'SCHEMA_PAGE_INVALID' },
      ]);
    }
    for (const schema of pageSchemas) {
      if (!isRecord(schema) || !SUPPORTED_SCHEMA_TYPES.has(schema.type)) {
        throw new RenderValidationError(
          `${printType} 不支持的组件类型：${isRecord(schema) ? String(schema.type) : '<invalid>'}`,
          [{ code: 'SCHEMA_TYPE_UNSUPPORTED' }],
        );
      }
    }
  }
}

/**
 * 限制模板只能使用内联空白页面定义，避免 pdfme 通过 URL、路径或外部 PDF 引入未受控内容。
 * 输入为打印物类型和 basePdf，输出无返回值；不访问 basePdf 指向的任何外部资源。
 */
function validateFixedBasePdf(printType, basePdf) {
  if (!isRecord(basePdf) || Array.isArray(basePdf)) {
    throw new RenderValidationError(`${printType} 只允许使用内联固定页面尺寸`, [
      { code: 'BASE_PDF_NOT_INLINE' },
    ]);
  }

  if (
    !Number.isFinite(basePdf.width) ||
    !Number.isFinite(basePdf.height) ||
    basePdf.width <= 0 ||
    basePdf.height <= 0
  ) {
    throw new RenderValidationError(`${printType} 页面尺寸必须为正数`, [{ code: 'PAGE_SIZE_INVALID' }]);
  }

  if (
    !Array.isArray(basePdf.padding) ||
    basePdf.padding.length !== 4 ||
    basePdf.padding.some((padding) => !Number.isFinite(padding) || padding < 0)
  ) {
    throw new RenderValidationError(`${printType} 页面边距定义无效`, [{ code: 'PAGE_PADDING_INVALID' }]);
  }
}

/**
 * 校验单页模板声明的 pageSpecJson、pageSizes 或方向与 basePdf 一致。
 * 输入为打印物类型和单页模板，输出无返回值；该声明属于平台快照，不触发文件或网络访问。
 */
function validateDeclaredPageSpec(printType, template) {
  const basePdf = template.basePdf;
  const orientationValues = [];

  if (template.pageSizes !== undefined) {
    if (!Array.isArray(template.pageSizes) || template.pageSizes.length !== SINGLE_PAGE_COUNT) {
      throw new RenderValidationError(`${printType} 页面规格必须只声明一页`, [
        { code: 'PAGE_SPEC_COUNT_MISMATCH' },
      ]);
    }
    const pageSize = template.pageSizes[0];
    if (!isRecord(pageSize) || !isValidPageSize(pageSize)) {
      throw new RenderValidationError(`${printType} 页面规格无效`, [{ code: 'PAGE_SPEC_INVALID' }]);
    }
    if (
      !isApproximatelyEqual(pageSize.width, basePdf.width) ||
      !isApproximatelyEqual(pageSize.height, basePdf.height)
    ) {
      throw new RenderValidationError(`${printType} 页面规格与 basePdf 尺寸不一致`, [
        { code: 'PAGE_SIZE_MISMATCH' },
      ]);
    }
    if (pageSize.orientation !== undefined) {
      orientationValues.push(normalizeOrientation(printType, pageSize.orientation));
    }
  }

  const pageSpec = template.pageSpecJson ?? template.pageSpec;
  if (pageSpec !== undefined) {
    if (!isRecord(pageSpec)) {
      throw new RenderValidationError(`${printType} 页面规格必须是对象`, [{ code: 'PAGE_SPEC_INVALID' }]);
    }
    const width = pageSpec.widthMm ?? pageSpec.width;
    const height = pageSpec.heightMm ?? pageSpec.height;
    if (!Number.isFinite(width) || !Number.isFinite(height) || width <= 0 || height <= 0) {
      throw new RenderValidationError(`${printType} 页面规格无效`, [{ code: 'PAGE_SPEC_INVALID' }]);
    }
    if (!isApproximatelyEqual(width, basePdf.width) || !isApproximatelyEqual(height, basePdf.height)) {
      throw new RenderValidationError(`${printType} 页面规格与 basePdf 尺寸不一致`, [
        { code: 'PAGE_SIZE_MISMATCH' },
      ]);
    }
    if (pageSpec.maxPageCount !== undefined && pageSpec.maxPageCount !== SINGLE_PAGE_COUNT) {
      throw new RenderValidationError(`${printType} 页面规格最多只能包含一页`, [
        { code: 'PAGE_SPEC_COUNT_MISMATCH' },
      ]);
    }
    if (pageSpec.orientation !== undefined) {
      orientationValues.push(normalizeOrientation(printType, pageSpec.orientation));
    }
  }

  if (template.orientation !== undefined) {
    orientationValues.push(normalizeOrientation(printType, template.orientation));
  }
  if (orientationValues.some((orientation) => orientation !== orientationValues[0])) {
    throw new RenderValidationError(`${printType} 页面方向声明不一致`, [
      { code: 'PAGE_ORIENTATION_MISMATCH' },
    ]);
  }
}

/**
 * 规范化页面方向声明，只接受平台合同中的 PORTRAIT/LANDSCAPE。
 * 输入为打印物类型和方向值，输出为大写方向；不会根据页面内容推断方向。
 */
function normalizeOrientation(printType, orientation) {
  if (typeof orientation !== 'string') {
    throw new RenderValidationError(`${printType} 页面方向无效`, [{ code: 'PAGE_ORIENTATION_INVALID' }]);
  }
  const normalized = orientation.trim().toUpperCase();
  if (normalized !== 'PORTRAIT' && normalized !== 'LANDSCAPE') {
    throw new RenderValidationError(`${printType} 页面方向无效：${orientation}`, [
      { code: 'PAGE_ORIENTATION_INVALID' },
    ]);
  }
  return normalized;
}

/**
 * 读取单页模板的有效尺寸和方向声明，供正反面合并前比较。
 * 输入为已经通过基础校验的模板，输出为毫米尺寸和可选方向；不修改模板。
 */
function getTemplatePageSpec(template) {
  const pageSpec = template.pageSpecJson ?? template.pageSpec;
  const pageSize = Array.isArray(template.pageSizes) ? template.pageSizes[0] : undefined;
  const orientationCandidate = pageSpec?.orientation ?? pageSize?.orientation ?? template.orientation;
  return {
    // 与平台组合和预览入口一致，优先比较已声明规格；无声明的原型模板才回退到basePdf。
    width: pageSpec?.widthMm ?? pageSpec?.width ?? pageSize?.width ?? template.basePdf.width,
    height: pageSpec?.heightMm ?? pageSpec?.height ?? pageSize?.height ?? template.basePdf.height,
    orientation: orientationCandidate === undefined
      ? undefined
      : normalizeOrientation('STAFF_CARD', orientationCandidate),
  };
}

/**
 * 校验正反面声明的尺寸和方向完全兼容。
 * 输入为两面模板，输出无返回值；发现不兼容时在生成合并 PDF 前失败。
 */
function validateTemplatePairSpec(frontTemplate, backTemplate) {
  const frontSpec = getTemplatePageSpec(frontTemplate);
  const backSpec = getTemplatePageSpec(backTemplate);
  if (
    !isTemplateDimensionCompatible(frontSpec.width, backSpec.width) ||
    !isTemplateDimensionCompatible(frontSpec.height, backSpec.height)
  ) {
    throw new RenderValidationError('STAFF_CARD 正反面页面尺寸不一致', [{ code: 'PAGE_SIZE_MISMATCH' }]);
  }
  if (frontSpec.orientation !== backSpec.orientation) {
    throw new RenderValidationError('STAFF_CARD 正反面页面方向不一致', [
      { code: 'PAGE_ORIENTATION_MISMATCH' },
    ]);
  }
}

/**
 * 拒绝 pdfme 的 expand 扩页设置；此处仅约束分页，不验证文字能否装入文本框。
 * 输入为打印物类型和按面组织的 schema，输出无返回值；不改变模板对象。
 */
function validateNoExpandableSchema(printType, schemas) {
  for (const pageSchemas of schemas) {
    if (!Array.isArray(pageSchemas)) {
      throw new RenderValidationError(`${printType} 每一面必须是 schema 数组`, [
        { code: 'SCHEMA_PAGE_INVALID' },
      ]);
    }
    for (const schema of pageSchemas) {
      if (isRecord(schema) && schema.overflow === 'expand') {
        throw new RenderValidationError(`${printType} 不允许通过内容扩展增加 PDF 页面`, [
          { code: 'DYNAMIC_PAGE_EXPANSION_FORBIDDEN' },
        ]);
      }
    }
  }
}

/**
 * 校验图片组件只能使用内联 PNG/JPEG data URI，阻断 pdfme 图片插件的远程或路径加载。
 * 输入为页面 schema、静态 schema 和单条业务输入，输出无返回值；不会读取 URI 或发起网络请求。
 */
function validateImageInputs(printType, schemaPages, staticSchemas, input) {
  const dynamicImageSchemas = schemaPages.flat().filter((schema) => schema.type === 'image');
  const allImageSchemas = [...dynamicImageSchemas, ...staticSchemas.filter((schema) => schema.type === 'image')];

  for (const schema of allImageSchemas) {
    const values = [schema.content];
    if (Object.hasOwn(input, schema.name)) values.push(input[schema.name]);
    for (const value of values) {
      if (value === undefined || value === null || value === '') continue;
      if (typeof value !== 'string' || !isInlineImageDataUri(value)) {
        throw new RenderValidationError(`${printType} 图片必须是 PNG/JPEG data URI`, [
          { code: 'IMAGE_URI_NOT_INLINE', schemaName: schema.name },
        ]);
      }
    }
  }
}

/**
 * 判断字符串是否为可交给 pdfme 图片插件的内联 PNG/JPEG Base64 数据。
 * 输入为图片值，输出为布尔值；不解析图片、不访问外部资源。
 */
function isInlineImageDataUri(value) {
  return /^data:image\/(?:png|jpeg);base64,[A-Za-z0-9+/]+={0,2}$/u.test(value);
}

/**
 * 校验受控字体只由内存字节提供，不接受 URL、绝对路径或其它隐式加载形式。
 * 输入为字体名称到字节的映射，输出无返回值；不会读取字体文件。
 */
function validateFontBytes(fontBytes) {
  if (!isRecord(fontBytes) || Object.keys(fontBytes).length === 0) {
    throw new RenderValidationError('必须提供至少一种受控字体字节', [{ code: 'FONT_BYTES_MISSING' }]);
  }
  for (const [fontName, bytes] of Object.entries(fontBytes)) {
    if (!fontName || !isByteArray(bytes) || bytes.byteLength === 0) {
      throw new RenderValidationError(`字体 ${fontName || '<empty>'} 的字节无效`, [
        { code: 'FONT_BYTES_INVALID' },
      ]);
    }
  }
}

/**
 * 确保模板引用的每个显式字体都存在于受控字体映射中。
 * 输入为模板页面和字体字节映射，输出无返回值；只读取模板元数据。
 */
function validateSchemaFonts(schemas, fontBytes) {
  for (const pageSchemas of schemas) {
    for (const schema of pageSchemas) {
      if (!isRecord(schema) || schema.fontName === undefined) continue;
      if (!Object.hasOwn(fontBytes, schema.fontName)) {
        throw new RenderValidationError(`模板引用了未登记字体：${schema.fontName}`, [
          { code: 'FONT_NOT_REGISTERED', fontName: schema.fontName },
        ]);
      }
    }
  }
}

/**
 * 把受控字体字节转换成 pdfme generator 所需的字体对象。
 * 输入为字体字节映射，输出为每个字体都启用子集的内存对象；不写入磁盘。
 */
function createPdfmeFonts(fontBytes) {
  const fallbackFontName = Object.keys(fontBytes)[0];
  return Object.fromEntries(
    Object.entries(fontBytes).map(([fontName, bytes]) => [
      fontName,
      {
        data: new Uint8Array(bytes),
        fallback: fontName === fallbackFontName,
        subset: true,
      },
    ]),
  );
}

/**
 * 加载生成结果并验证页数和每页尺寸，确保输出没有被动态布局扩页或改纸张规格。
 * 输入为打印物类型、模板、PDF 字节和预期页数，输出无返回值；只在内存中解析 PDF。
 */
async function validateGeneratedPdf({ printType, template, pdfBytes, expectedPageCount }) {
  if (!isByteArray(pdfBytes) || pdfBytes.byteLength === 0) {
    throw new RenderValidationError(`${printType} 未生成有效 PDF`, [{ code: 'PDF_BYTES_INVALID' }]);
  }

  let pdf;
  try {
    // 先确认字节确实是可解析的 PDF，避免后续检查建立在伪造结果上。
    pdf = await PDFDocument.load(pdfBytes);
  } catch (error) {
    throw new RenderValidationError(
      `${printType} 生成结果无法解析：${getErrorMessage(error)}`,
      [{ code: 'PDF_INVALID' }],
    );
  }

  if (pdf.getPageCount() !== expectedPageCount) {
    throw new RenderValidationError(
      `${printType} 生成后必须固定 ${expectedPageCount} page，收到 ${pdf.getPageCount()}`,
      [{ code: 'GENERATED_PAGE_COUNT_MISMATCH' }],
    );
  }

  // 逐页核对模板尺寸，保证打印端收到的页面规格没有漂移。
  const { width, height } = template.basePdf;
  for (const page of pdf.getPages()) {
    const pageWidthMm = (page.getWidth() / 72) * 25.4;
    const pageHeightMm = (page.getHeight() / 72) * 25.4;
    if (!isApproximatelyEqual(pageWidthMm, width) || !isApproximatelyEqual(pageHeightMm, height)) {
      throw new RenderValidationError(`${printType} 生成页面尺寸与模板不一致`, [
        { code: 'GENERATED_PAGE_SIZE_MISMATCH' },
      ]);
    }
  }
}

/**
 * 把两份已经验证的一页 PDF 复制到新文档，固定 FRONT/BACK 页序并再次检查尺寸。
 * 输入为两个面快照和对应 PDF 字节，输出为两页合并字节；不会把输入字段重新合并。
 */
async function mergePrintPages({ printType, front, back, frontPdfBytes, backPdfBytes }) {
  validateTemplatePairSpec(front.template, back.template);

  let frontPdf;
  let backPdf;
  try {
    frontPdf = await PDFDocument.load(frontPdfBytes);
    backPdf = await PDFDocument.load(backPdfBytes);
  } catch (error) {
    throw new RenderValidationError(`${printType} 正反面 PDF 无法合并：${getErrorMessage(error)}`, [
      { code: 'PDF_MERGE_SOURCE_INVALID' },
    ]);
  }

  if (frontPdf.getPageCount() !== SINGLE_PAGE_COUNT || backPdf.getPageCount() !== SINGLE_PAGE_COUNT) {
    throw new RenderValidationError(`${printType} 正反面每份都必须只有一页`, [
      { code: 'GENERATED_PAGE_COUNT_MISMATCH' },
    ]);
  }

  const frontPage = frontPdf.getPage(0);
  const backPage = backPdf.getPage(0);
  if (
    !isApproximatelyEqual((frontPage.getWidth() / 72) * 25.4, (backPage.getWidth() / 72) * 25.4) ||
    !isApproximatelyEqual((frontPage.getHeight() / 72) * 25.4, (backPage.getHeight() / 72) * 25.4)
  ) {
    throw new RenderValidationError(`${printType} 正反面生成页面尺寸不一致`, [
      { code: 'PAGE_SIZE_MISMATCH' },
    ]);
  }

  const mergedPdf = await PDFDocument.create();
  try {
    // copyPages 会保留每面独立生成的字体和资源，不会重写同名 schema 的输入值。
    const [copiedFrontPage] = await mergedPdf.copyPages(frontPdf, [0]);
    mergedPdf.addPage(copiedFrontPage);
    const [copiedBackPage] = await mergedPdf.copyPages(backPdf, [0]);
    mergedPdf.addPage(copiedBackPage);
  } catch (error) {
    throw new RenderValidationError(`${printType} 正反面 PDF 合并失败：${getErrorMessage(error)}`, [
      { code: 'PDF_MERGE_FAILED' },
    ]);
  }

  const mergedBytes = await mergedPdf.save();
  await validateGeneratedPdf({
    printType,
    template: front.template,
    pdfBytes: mergedBytes,
    expectedPageCount: 2,
  });
  return mergedBytes;
}

/**
 * 判断值是否为普通对象，拒绝 null、数组和带有不透明原型的输入。
 * 输入为任意值，输出为布尔值；不产生副作用。
 */
function isRecord(value) {
  if (value === null || typeof value !== 'object' || Array.isArray(value)) return false;
  const prototype = Object.getPrototypeOf(value);
  return prototype === Object.prototype || prototype === null;
}

/**
 * 复制平台传入的 JSON 模板和单条输入，避免 pdfme 的校验/布局过程修改调用方对象。
 * 输入为只含受控 JSON 值的对象，输出为独立对象；遇到不可复制值时直接抛错。
 */
function cloneJsonValue(value) {
  try {
    return structuredClone(value);
  } catch (error) {
    throw new RenderValidationError(`渲染输入无法复制：${getErrorMessage(error)}`, [
      { code: 'RENDER_INPUT_NOT_CLONEABLE' },
    ]);
  }
}

/**
 * 判断值是否为可交给 pdfme 的字节数组。
 * 输入为任意值，输出为布尔值；不产生副作用。
 */
function isByteArray(value) {
  return value instanceof Uint8Array || value instanceof ArrayBuffer;
}

/**
 * 判断页面规格是否包含有限的正宽高。
 * 输入为页面规格对象，输出为布尔值；不产生副作用。
 */
function isValidPageSize(pageSize) {
  return Number.isFinite(pageSize.width) &&
    pageSize.width > 0 &&
    Number.isFinite(pageSize.height) &&
    pageSize.height > 0;
}

/**
 * 使用页面毫米单位的固定容差比较两个数值。
 * 输入为两个数值，输出为布尔值；不产生副作用。
 */
function isApproximatelyEqual(left, right) {
  return Math.abs(left - right) <= PAGE_SIZE_TOLERANCE_MM;
}

/** 模板组合采用平台保存时的毫米容差；不与生成PDF的物理尺寸测量容差混用。 */
export function isTemplateDimensionCompatible(left, right) {
  return Number.isFinite(left) && Number.isFinite(right) && Math.abs(left - right) <= TEMPLATE_PAIR_SIZE_TOLERANCE_MM;
}

/**
 * 保留外部库异常的首行诊断；此处未做业务脱敏，对外接口须另行映射安全错误码。
 * 输入为任意异常值，输出为单行字符串；不产生副作用。
 */
function getErrorMessage(error) {
  if (error instanceof Error) return error.message.split('\n')[0];
  return String(error).split('\n')[0];
}
