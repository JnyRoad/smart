import { readFile, writeFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';

import { renderPrintTemplates } from '../src/render.mjs';

const SAMPLE_PATH = '/private/tmp/smart-print-renderer-sample.pdf';
const CARD_SIZE_MM = Object.freeze({ width: 85.6, height: 53.98 });
const FONT_NAME = 'NotoSansSC';

/**
 * 创建用于人工查看的单面厂牌模板。
 * 输入为该面的静态文案，输出为 85.6mm × 53.98mm 的一页 pdfme 模板。
 */
function createSampleTemplate(staticContent) {
  return {
    basePdf: {
      width: CARD_SIZE_MM.width,
      height: CARD_SIZE_MM.height,
      padding: [0, 0, 0, 0],
      staticSchema: [{
        name: 'name',
        type: 'text',
        content: staticContent,
        position: { x: 8, y: 6 },
        width: 65,
        height: 8,
        fontName: FONT_NAME,
        fontSize: 8,
      }],
    },
    pageSpecJson: {
      widthMm: CARD_SIZE_MM.width,
      heightMm: CARD_SIZE_MM.height,
      orientation: 'LANDSCAPE',
      maxPageCount: 1,
    },
    // 每个版本只保留一页；正面和背面由组合入口分别生成。
    schemas: [[{
      name: 'name',
      type: 'text',
      position: { x: 8, y: 20 },
      width: 65,
      height: 12,
      fontName: FONT_NAME,
      fontSize: 14,
      alignment: 'center',
      verticalAlignment: 'middle',
    }]],
  };
}

/**
 * 生成一份不含生产人员数据的双面中文 PDF 到临时目录。
 * 输出为临时文件路径；该脚本只用于本地验证，不是渲染服务接口。
 */
async function writeSamplePdf() {
  const fontPath = fileURLToPath(new URL('../assets/fonts/NotoSansCJKsc-Regular.otf', import.meta.url));
  const fontBytes = new Uint8Array(await readFile(fontPath));
  const pdfBytes = await renderPrintTemplates({
    printType: 'STAFF_CARD',
    printMode: 'AUTO_DUPLEX',
    front: {
      templateVersionId: 'sample-front-v1',
      template: createSampleTemplate('正面固定：员工厂牌'),
      input: { name: '正面姓名：张三' },
    },
    back: {
      templateVersionId: 'sample-back-v3',
      template: createSampleTemplate('背面固定：访客须知'),
      input: { name: '背面部门：研发部' },
    },
    fontBytes: { [FONT_NAME]: fontBytes },
  });
  await writeFile(SAMPLE_PATH, pdfBytes);
  console.log(`${SAMPLE_PATH} (${pdfBytes.byteLength} bytes)`);
}

await writeSamplePdf();
