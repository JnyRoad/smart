// 为旧 Vue CLI 独立构建 pdfme；第三方运行时和字体只生成到被忽略的静态目录。
import { build } from 'esbuild'
import { createRequire } from 'node:module'
import { dirname, join, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { createHash } from 'node:crypto'
import { copyFile, mkdir, readdir, stat, readFile } from 'node:fs/promises'

const uiRoot = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire(join(uiRoot, 'package.json'))
const output = join(uiRoot, 'public/print-designer')

/** 为真实浏览器打包 ESM，保留 import.meta.url 以定位 PDF 预览 Worker/WASM。 */
async function buildRuntime() {
  const result = await build({
    absWorkingDir: uiRoot,
    entryPoints: ['src/components/print/runtime/entry.mjs'],
    outfile: join(output, 'runtime.js'),
    bundle: true,
    platform: 'browser',
    // clawpdf 的 Node 分支有运行环境判断；保留这些动态导入，浏览器不会执行它们。
    external: ['node:*'],
    format: 'esm',
    target: ['chrome110', 'safari16'],
    define: { 'process.env.NODE_ENV': '"production"' },
    minify: true,
    legalComments: 'external',
    metafile: true
  })
  return result.metafile
}

/** 复制上游按相对 URL 加载的 Worker/WASM，避免回退到公网 CDN。 */
async function copyPdfAssets(metadata) {
  const uiEntry = require.resolve('@pdfme/ui')
  const converterRoot = dirname(dirname(require.resolve('@pdfme/converter', { paths: [dirname(uiEntry)] })))
  // clawpdf 只导出 ESM，直接使用本次打包实际解析出的浏览器入口定位随包资源。
  const clawInput = Object.keys(metadata.inputs).find(path => path.endsWith('/clawpdf/dist/browser.js'))
  if (!clawInput) throw new Error('未找到本次打包使用的 clawpdf 浏览器入口')
  const clawRoot = dirname(dirname(resolve(uiRoot, clawInput)))
  await mkdir(join(output, 'assets'), { recursive: true })
  for (const name of await readdir(join(converterRoot, 'dist/assets'))) {
    if (name.endsWith('.js')) await copyFile(join(converterRoot, 'dist/assets', name), join(output, 'assets', name))
  }
  await mkdir(join(output, 'vendor'), { recursive: true })
  await copyFile(join(clawRoot, 'dist/vendor/pdfium.esm.wasm'), join(output, 'vendor/pdfium.esm.wasm'))
  await copyFile(join(clawRoot, 'LICENSE'), join(output, 'clawpdf-LICENSE.txt'))
  await copyFile(join(clawRoot, 'THIRD_PARTY_NOTICES.md'), join(output, 'PDFium-NOTICES.md'))
}

/** 使用与 Node 渲染器相同的固定版字体和许可，禁止构建时拉取未锁定外部字体。 */
async function copyFont() {
  const fontRoot = resolve(uiRoot, '../smart-print-renderer/assets/fonts')
  const bytes = await readFile(join(fontRoot, 'NotoSansCJKsc-Regular.otf'))
  if (createHash('sha256').update(bytes).digest('hex') !== '2c76254f6fc379fddfce0a7e84fb5385bb135d3e399294f6eeb6680d0365b74b') throw new Error('固定中文字体hash不匹配')
  await mkdir(join(output, 'fonts'), { recursive: true })
  await copyFile(join(fontRoot, 'NotoSansCJKsc-Regular.otf'), join(output, 'fonts/NotoSansCJKsc-Regular.otf'))
  await copyFile(join(fontRoot, 'LICENSE'), join(output, 'fonts/LICENSE'))
}

await mkdir(output, { recursive: true })
const buildMetadata = await buildRuntime()
await copyPdfAssets(buildMetadata)
await copyFont()
const runtimeBytes = (await stat(join(output, 'runtime.js'))).size
console.info(`pdfme 运行时构建完成：${runtimeBytes} bytes，${Object.keys(buildMetadata.inputs).length} 个输入模块；字体与 PDF Worker 按需加载。`)
