import { create } from 'fontkit';
import { getDynamicLayoutForText } from '@pdfme/schemas/texts';
import { replacePlaceholders } from '@pdfme/common';
const parsedFonts = new WeakMap();
/** 使用与 pdfme 相同的公开测量入口检查固定框，诊断只包含组件名和原因码。 */
export async function validateContent(template, input, fontBytes, ErrorType) {
  const fail = (code, name) => { throw new ErrorType('模板内容无法安全放入固定版面，请检查对应组件', [{ code, schemaName: name }]); };
  const fonts = Object.fromEntries(Object.entries(fontBytes).map(([name, data], i) => [name, { data, fallback: i === 0 }]));
  const cache = new Map();
  const variables = { ...input, totalPages: 1, currentPage: 1 };
  const components = [...template.schemas[0].map(component => ({ component, isStatic: false })), ...(template.basePdf.staticSchema || []).map(component => ({ component, isStatic: true }))];
  for (const { component, isStatic } of components) {
    const { name, position, width, height } = component;
    if (!position || ![position.x, position.y, width, height].every(Number.isFinite) || position.x < 0 || position.y < 0 || width <= 0 || height <= 0 || position.x + width > template.basePdf.width + 0.001 || position.y + height > template.basePdf.height + 0.001) fail('ELEMENT_OUT_OF_BOUNDS', name);
    if (component.rotate) fail('ROTATION_UNSUPPORTED', name);
    if (component.type !== 'text') continue;
    // 当前固定文本能力只接受普通字号，避免未校验的富文本或自动缩字改变最终样式。
    if (component.dynamicFontSize || (component.textFormat && component.textFormat !== 'plain') || component.__splitRange) fail('TEXT_MODE_UNSUPPORTED', name);
    // 与固定单页 generator 一致：静态组件不读取同名输入，只读组件先展开占位符。
    const value = String(component.readOnly
      ? replacePlaceholders({ content: component.content || '', variables, schemas: template.schemas })
      : isStatic ? component.content || '' : input[name] || '');
    if (value.length > 10000) fail('TEXT_OVERFLOW', name);
    const fontName = component.fontName || Object.keys(fontBytes)[0];
    const bytes = fontBytes[fontName];
    let font = parsedFonts.get(bytes);
    if (!font) { font = create(Buffer.from(bytes)); parsedFonts.set(bytes, font); }
    for (const character of value.replace(/[\r\n\t]/gu, '')) {
      if (!font.hasGlyphForCodePoint(character.codePointAt(0))) fail('GLYPH_MISSING', name);
    }
    const fontSize = component.fontSize ?? 13;
    const lineHeight = component.lineHeight ?? 1;
    const spacing = component.characterSpacing ?? 0;
    if (![fontSize, lineHeight, spacing].every(Number.isFinite) || fontSize <= 0 || lineHeight < 1 || spacing < 0) fail('TEXT_METRICS_INVALID', name);
    // 测量副本可以展开以得到完整高度；真正生成的模板仍保持固定版面。
    const result = await getDynamicLayoutForText(value, { schema: { ...component, fontSize, lineHeight, characterSpacing: spacing, overflow: 'expand' }, options: { font: fonts }, _cache: cache });
    if (result.heights.reduce((sum, part) => sum + part, 0) > height + 0.01) fail('TEXT_OVERFLOW', name);
    // 单个不可再拆分的字形也必须放得下，避免窄框横向溢出。
    for (const character of value.replace(/[\r\n\t]/gu, '')) {
      const advance = font.layout(character).glyphs.reduce((sum, glyph) => sum + glyph.advanceWidth, 0) / font.unitsPerEm * fontSize;
      if (advance > width * 72 / 25.4 + 0.01) fail('TEXT_OVERFLOW', name);
    }
  }
}
