/** 使用已安装的HBuilderX编译器构建，避免混装不同版本的DCloud依赖。 */
import { spawnSync } from 'node:child_process'
import { existsSync } from 'node:fs'
import { fileURLToPath } from 'node:url'
import path from 'node:path'
import { hasCompilerErrors } from './compiler-diagnostics.mjs'
const project = fileURLToPath(new URL('..', import.meta.url))
const target = process.argv[2] || 'web'
const platform = target === 'web' ? 'h5' : (target === 'android' ? 'app' : target)
if (!['h5', 'mp-weixin', 'mp-alipay', 'app'].includes(platform)) throw new Error('支持web、mp-weixin、mp-alipay与android源码编译；安装包请通过HBuilderX打包')
const hx = process.env.HBUILDERX_ROOT || '/Applications/HBuilderX.app/Contents/HBuilderX'
const node = process.env.HBUILDERX_NODE || path.join(hx, 'plugins/node/node')
const entry = path.join(hx, 'plugins/uniapp-cli-vite/node_modules/@dcloudio/vite-plugin-uni/bin/uni.js')
if (!existsSync(node) || !existsSync(entry)) throw new Error('未找到HBuilderX编译器，请设置HBUILDERX_ROOT与HBUILDERX_NODE')
const output = path.join(project, 'unpackage/dist/build', platform)
const result = spawnSync(node, [entry, 'build', '--platform', platform, '--outDir', output, '--clearScreen', 'false'], {
  cwd: project,
  env: { ...process.env, HX_APP_ROOT: hx, HX_Version: process.env.HBUILDERX_VERSION || '5.24.2026081301', NODE_ENV: 'production', UNI_PLATFORM: platform, UNI_INPUT_DIR: project, UNI_OUTPUT_DIR: output, ...(platform === 'app' ? { UNI_UTS_PLATFORM: 'app-android', UNI_APP_X: 'true', UNI_APP_X_DOM2: 'true', UNI_APP_X_UVUE_SCRIPT_ENGINE: 'js' } : {}) },
  stdio: ['inherit', 'pipe', 'pipe'], encoding: 'utf8', maxBuffer: 32 * 1024 * 1024, timeout: 180000
})
// 保留完整编译诊断，再同时核对进程状态和原生样式错误。
if (result.stdout) process.stdout.write(result.stdout)
if (result.stderr) process.stderr.write(result.stderr)
if (result.error) throw result.error
if (hasCompilerErrors((result.stdout || '') + '\n' + (result.stderr || ''))) {
  console.error('构建包含原生样式错误，请修复后重新验证；不能仅以 Build complete 判断成功。')
  process.exit(1)
}
process.exit(result.status ?? 1)
