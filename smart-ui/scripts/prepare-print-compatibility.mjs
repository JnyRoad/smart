// 组装完全隔离的本地兼容验证页，不接入管理端路由或后端服务。
import { cp, copyFile, mkdir } from 'node:fs/promises'
import { dirname, resolve } from 'node:path'
import { fileURLToPath } from 'node:url'
import { createRequire } from 'node:module'

const root = resolve(dirname(fileURLToPath(import.meta.url)), '..')
const require = createRequire(resolve(root, 'package.json'))
const output = resolve(root, 'dist-print-compat')
await mkdir(output, { recursive: true })
await copyFile(resolve(root, 'scripts/print-compatibility/index.html'), resolve(output, 'index.html'))
await copyFile(require.resolve('vue/dist/vue.min.js'), resolve(output, 'vue.min.js'))
await cp(resolve(root, 'public/print-designer'), resolve(output, 'print-designer'), { recursive: true })
console.info('本地兼容验证页已生成到 dist-print-compat；只包含合成数据。')
